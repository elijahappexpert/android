package com.versaggi.android.disasteralerts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** ********************** BEGIN LEGAL STUFF ****************************
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*     http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
********************** END LEGAL STUFF ******************************
*/


/** HSIncidentProvider:
 * 
 * This class handles all of the data interactions dealing with the HSIncident 
 * segment of this application. It has full knowledge of the underlying Database,
 * which MAYBE different than the structure of the HSIncident objects themselves.
 *
 */
public class HSIncidentProvider {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = HSIncidentProvider.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	// Initialize Class Wide objects ...
	   Context context;
	   HSIDBHelper hsidbHelper; 
	   SQLiteDatabase db;
		
	// DB Constants ...
	public static final String DATABASE_NAME = "hsincident.db";
	public static final int DATABASE_VERSION = 1;
	public static final String HSINCIDENT_TABLE = "hsincident";
 
	// DATABASE Column Names (4)
	public static final String KEY_ID = "_id";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_LINK = "link";

	// DATABASE indexes (4)
	public static final int ID_COLUMN = 1;
	public static final int DETAILS_COLUMN = 2;
	public static final int DESCRIPTION_COLUMN = 3;
	public static final int LINK_COLUMN = 4;
	  
	// Database Create string Constant ...
    private static final String DATABASE_CREATE =
      "create table " + HSINCIDENT_TABLE  + " (" 
      + KEY_ID + " INTEGER Primary KEY, "
      + KEY_DETAILS + " TEXT, "
      + KEY_DESCRIPTION + " TEXT, "
      + KEY_LINK + " TEXT);";
	
    
 // Constructor ....
	public HSIncidentProvider(Context context) {
		
		this.context = context;     			// Get the context ...	
		hsidbHelper = new HSIDBHelper();		// Get DBHelper object, 
												// inexpensive, lives long  ...
	}
	
	
	////DATABASE Methods ************************************************************
	
	
	
	/** QUERY: 
	 * 
	 * 	This class queries the database and stores the result in a cursor  for later processing.  
	 * 	@returns Cursor 
	 */
	public Cursor query() {
		
		
		db = hsidbHelper.getReadableDatabase();		// Get the database ...	
		
		if (debug) { Log.d(TAG, "in query : Got *DB* " ); }
		
		// QUERY THE DATABASE:
		// SQL:  SELECT * FROM statuses where id=47 HAVING .... GROUP BY ... ORDER BY .... 
		// PRECompiled: db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
		// db.query returns a CURSOR (a pointer to a particular record or a large data set)
		
		// NORMAL QUERY: return db.query(HSIncidentProvider.HSINCIDENT_TABLE, null, null, null, null, null, null);
		//
		// Randomizing the Query results: In this particular data set date isn't relevant so to keep the data
		// 'fresh' each time it's refreshed we randomize the ordering of the return set of data from the query.
		// This necessitates the use of the 'db.rawQuery' clause instead of the 'normal' query. 
		return db.rawQuery("select * from hsincident ORDER BY RANDOM()", null);
		
		// CLOSE Database  ....  we can't, because the moment we do, the GARBAGE collector comes 
		// out and deletes our data from the returning Cursor. (Odd idiosyncrasy). We can't leave it 
		// open either because of memory leaks. So we close it (and cursors) in the onStop and onDestroy
		// Life Cycle Overrides of the Activity Class ...
		
		//db.close();

	}// END Query ...

	
	/** INSERT Method #1:  [ContentValues pairs] -> CS_DB
	 * 	@param: (ContentValues nv_pairs) 
	 * 	Takes the Name/Values pairs data contained in the ContentValues object 
	 * 	that is passed in an input and inserts that into what ever storage medium is 
	 * 	happens to be using. In this case it's a SQLite Database, but it could be ANYTHING.   
	 */
	public long insert(ContentValues nv_pairs) {

		// Get writable Database ...
		// getWritableDatabase will actually opens up a writable database handle and 
		// consumes valued system resources in the process so we should close the handle as
		// soon as we are able to.
		
		db = hsidbHelper.getWritableDatabase();
		
		// Alternate:  db.insertWithOnConflict(DBHelper.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		// Performs the INSERT of the data into our database ...
		
		long ret;
		try {
			ret = db.insertOrThrow(HSIncidentProvider.HSINCIDENT_TABLE, null, nv_pairs);
		} catch (SQLException e) {
			ret = -1;		// So we REALLY know if we hit the end of the list ...
		} finally {
			db.close(); 	// Close Database
		}
		// Return the result of the insert statement which we will
		// use to indicate if the record was really inserted ...
		return ret;
		
	}// END insert ... (ContentValues)
	
	
	
	
	/**INSERT Method #2: (HSIncident hsincident) -> INSERT Method #1
	 * 
	 * 	@param [HSIncident hsincident] :=> (HSIncident data as provided by Online XML Feed)
	 * 	Takes the data from our XML data feed and creates a new ContentValues
	 * 	(name/value pairs) object, inserts the HSIncident data into that object and 
	 * 	then calls the 'other' INSERT method, which knows how to insert that type of data
	 * 	into the underlying database. 
	 */		
	public long insert(HSIncident hsincident) {
		
		// Create blank KEY/VALUE pairs object 
	    ContentValues nv_pairs = new ContentValues();
	    
	    // Set the key/value pairs to the object
	    nv_pairs.put(HSIncidentProvider.KEY_ID, hsincident.getId());
	    nv_pairs.put(HSIncidentProvider.KEY_DETAILS, hsincident.getDetails());
	    nv_pairs.put(HSIncidentProvider.KEY_DESCRIPTION, hsincident.getDescription());
	    nv_pairs.put(HSIncidentProvider.KEY_LINK, hsincident.getLink());

	    return this.insert(nv_pairs); // now call the 'other' insert method that takes ContentValues as inputs...	    
	}// End insert ... (HSIncident) 
	


	/** close:
	 * 	Closes the Earthquakes Database via 'eqdbHelper.close()'
	 */
	public void close() {
		hsidbHelper.close();
	}
	
	
	
	/** delete:
	 * 	Delete ALL the records in our database ...
	 */
	public void delete () {
		db = hsidbHelper.getWritableDatabase();	// getWritableDatabase
		// Delete the actual data ...
		db.delete(HSIncidentProvider.HSINCIDENT_TABLE, null, null);
		db.close(); // Close Database 
	}
	
	

	//// HELPER Methods ************************************************************
	
	
	/** CSDBHelper:
	 * 
	 * 	Inner Class to help open/create/upgrade database ....
	 * 	Provides connection to the database "server", but the actual getWritableDatabase
	 * 	is what actually gives you the exclusive connection. EqDBHelper can live for a LONG time.
	 * 	It doesn't hold a valuable resource whereas getWritableDatabase actually opens up a database 
	 * 	which is a valued system resource and must be closed ASAP.
	 */
	private class HSIDBHelper extends SQLiteOpenHelper {
			
	    // Constructor ...	
		public HSIDBHelper() {
	      super(context, HSIncidentProvider.DATABASE_NAME, null, HSIncidentProvider.DATABASE_VERSION);
	    }

	    
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      db.execSQL(HSIncidentProvider.DATABASE_CREATE);           
	    }


		@Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	if (debug) { Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data"); }
	         
	    	db.execSQL("DROP TABLE IF EXISTS " + HSIncidentProvider.HSINCIDENT_TABLE);
	    	onCreate(db);
	    }
		
	  }// END Private Class EqDBHelper ...
	
	
	
	
	
}// END Class HSIncidentProvider




