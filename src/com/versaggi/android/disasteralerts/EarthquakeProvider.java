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

/** EarthquakeProvider:
 * 
 * This class handles all of the data interactions dealing with the Earthquake 
 * segment of this application. It has full knowledge of the underlying Database,
 * which IS different than the structure of the QUAKE objects themselves.
 *
 */
public class EarthquakeProvider  {
	
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = EarthquakeProvider.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	
	// Initialize Class Wide objects ...
	   Context context;
	   EqDBHelper eqdbHelper; 
	   SQLiteDatabase db;
		
	// DB Constants ...
	public static final String DATABASE_NAME = "earthquakes.db";
	public static final int DATABASE_VERSION = 1;
	public static final String EARTHQUAKE_TABLE = "earthquakes";
 
	// DATABASE Column Names (7)
	public static final String KEY_ID = "_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_LOCATION_LAT = "latitude";
	public static final String KEY_LOCATION_LNG = "longitude";
	public static final String KEY_MAGNITUDE = "magnitude";
	public static final String KEY_CDATA = "cdata";
	public static final String KEY_LINK = "link";

	// DATABASE indexes (7)
	public static final int ID_COLUMN = 1;
	public static final int DATE_COLUMN = 2;
	public static final int DETAILS_COLUMN = 3;
	public static final int LONGITUDE_COLUMN = 4;
	public static final int LATITUDE_COLUMN = 5;
	public static final int MAGNITUDE_COLUMN = 6;
	public static final int MAGNITUDE_CDATA = 7;
	public static final int LINK_COLUMN = 8;
	  
	// Database Create string Constant ...
    private static final String DATABASE_CREATE =
      "create table " + EARTHQUAKE_TABLE + " (" 
      + KEY_ID + " INTEGER Primary KEY, "
      + KEY_DATE + " INTEGER, "
      + KEY_DETAILS + " TEXT, "
      + KEY_LOCATION_LAT + " FLOAT, "
      + KEY_LOCATION_LNG + " FLOAT, "
      + KEY_MAGNITUDE + " FLOAT, "
      + KEY_CDATA + " TEXT, "
      + KEY_LINK + " TEXT);";	
	
    
	
	// Constructor ....
	public EarthquakeProvider(Context context) {
		
		this.context = context;     		// Get the context ...	
		eqdbHelper = new EqDBHelper();		// Get DBHelper object, 
											// inexpensive, lives long  ...
	}
		
	
	
	//// DATABASE Methods ************************************************************
	
	
	/** QUERY: 
	 * 
	 * 	This class queries the database and stores the result in a cursor  for later processing.  
	 * 	@returns Cursor 
	 */
	public Cursor query() {
		db = eqdbHelper.getReadableDatabase();		// Get the database ...	
		
		// QUERY THE DATABASE:
		// SQL:  SELECT * FROM statuses where id=47 HAVING .... GROUP BY ... ORDER BY .... 
		// PRECompiled: db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
		// db.query returns a CURSOR (a pointer to a particular record or a large data set)
		// NOTE: We use [KEY_MAGNITUDE + " DESC"] in the "ORDER BY" slot to have the output ordered 
		
		return db.query(EarthquakeProvider.EARTHQUAKE_TABLE, null, null, null, null, null, KEY_MAGNITUDE + " DESC");		   		
		
		// CLOSE Database  ....  we can't, because the moment we do, the GARBAGE collector comes 
		// out and deletes our data from the returning Cursor. (Odd idiosyncrasy). We can't leave it 
		// open either because of memory leaks. So we close it (and cursors) in the onStop and onDestroy
		// Life Cycle Overrides of the EarthquakeActivity Class ...
		
		//db.close();

	}// END Query ...
	
	
	
	/** INSERT Method #1:  [ContentValues pairs] -> EQ_DB
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
		
		db = eqdbHelper.getWritableDatabase();
		
		// Alternate:  db.insertWithOnConflict(DBHelper.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		// Performs the INSERT of the data into our database ...
		
		long ret;
		try {
			ret = db.insertOrThrow(EarthquakeProvider.EARTHQUAKE_TABLE, null, nv_pairs);
		} catch (SQLException e) {
			ret = -1;		// So we REALLY know if we hit the end of the list ...
		} finally {
			db.close(); 	// Close Database
		}
		// Return the result of the insert statement which we will
		// use to indicate if the record was really inserted ...
		return ret;
		
	}// END insert ... (ContentValues)
	
	
	
	
	/**INSERT Method #2: (QUAKE Object) -> INSERT Method #1
	 * 
	 * 	@param [Quake quake] :=> (Quake data as provided by Online XML Feed)
	 * 	Takes the Quake data from our XML data feed and creates a new ContentValues
	 * 	(name/value pairs) object, inserts the Quake data into that object and 
	 * 	then calls the 'other' INSERT method, which knows how to insert that type of data
	 * 	into the underlying database. 
	 */		
	public long insert(Quake quake) {
		
		// Create blank KEY/VALUE pairs object 
	    ContentValues nv_pairs = new ContentValues();
	    
	    // Set the key/value pairs to the object
	    nv_pairs.put(EarthquakeProvider.KEY_ID, quake.getId());
	    nv_pairs.put(EarthquakeProvider.KEY_DATE, quake.getDate().getTime());
	    nv_pairs.put(EarthquakeProvider.KEY_DETAILS, quake.getDetails());
	    nv_pairs.put(EarthquakeProvider.KEY_LOCATION_LAT, quake.getLocation().getLatitude());
	    nv_pairs.put(EarthquakeProvider.KEY_LOCATION_LNG, quake.getLocation().getLongitude());
	    nv_pairs.put(EarthquakeProvider.KEY_MAGNITUDE, quake.getMagnitude());
	    nv_pairs.put(EarthquakeProvider.KEY_CDATA, quake.getCdata());
	    nv_pairs.put(EarthquakeProvider.KEY_LINK, quake.getLink());
	    
	    return this.insert(nv_pairs); // now call the 'other' insert method that takes ContentValues as inputs...	    
	}// End insert ... (Status) 
	

	/** close:
	 * 	Closes the Earthquakes Database via 'eqdbHelper.close()'
	 */
	public void close() {
		eqdbHelper.close();
	}
	
	
	
	/** delete:
	 * 	Delete ALL the records in our database ...
	 */
	public void delete () {
		db = eqdbHelper.getWritableDatabase();	// getWritableDatabase
		// Delete the actual data ...
		db.delete(EarthquakeProvider.EARTHQUAKE_TABLE, null, null);
		db.close(); // Close Database 
	}
	
	
	//// HELPER Methods ************************************************************
	
	
	/** EqDBHelper:
	 * 
	 * 	Inner Class to help open/create/upgrade database ....
	 * 	Provides connection to the database "server", but the actual getWritableDatabase
	 * 	is what actually gives you the exclusive connection. EqDBHelper can live for a LONG time.
	 * 	It doesn't hold a valuable resource whereas getWritableDatabase actually opens up a database 
	 * 	which is a valued system resource and must be closed ASAP.
	 */
	private class EqDBHelper extends SQLiteOpenHelper {
			
	    // Constructor ...	
		public EqDBHelper() {
	      super(context, EarthquakeProvider.DATABASE_NAME, null, EarthquakeProvider.DATABASE_VERSION);
	    }

	    
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      db.execSQL(EarthquakeProvider.DATABASE_CREATE);           
	    }


		@Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	if (debug) { Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data"); }
	         
	    	db.execSQL("DROP TABLE IF EXISTS " + EarthquakeProvider.EARTHQUAKE_TABLE);
	    	onCreate(db);
	    }
		
	  }// END Private Class EqDBHelper ...
	
		
	
	
}// END CLASS EarthquakeProvider ...









