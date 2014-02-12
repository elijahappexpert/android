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

/** VolcanoProvider:
 * 
 * This class handles all of the data interactions dealing with the Volcanos
 * segment of this application. It has full knowledge of the underlying Database, 
 * which IS different than the structure of the EBHIncident objects themselves.
 */
public class VolcanoProvider {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = VolcanoProvider.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	
	// Initialize Class Wide objects ...
	   Context context;
	   VDBHelper vdbHelper; 
	   SQLiteDatabase db;
	
		// DB Constants ...
		public static final String DATABASE_NAME = "volcano.db";
		public static final int DATABASE_VERSION = 1;
		public static final String VOLCANO_TABLE = "volcano";
	 
		// DATABASE Column Names (7)
		public static final String KEY_ID = "_id";
		public static final String KEY_DATE = "date";
		public static final String KEY_DETAILS = "details";
		public static final String KEY_DESCRIPTION = "description";
		public static final String KEY_GEOPOINT_LAT = "geopoint_lat";
		public static final String KEY_GEOPOINT_LNG = "geopoint_lng";
		public static final String KEY_LINK = "link";

		// DATABASE indexes (7)
		public static final int ID_COLUMN = 1;
		public static final int DATE_COLUMN = 2;
		public static final int DETAILS_COLUMN = 3;
		public static final int DESCRIPTION_COLUMN = 3;
		public static final int GEOPOINT_LAT_COLUMN = 4;
		public static final int GEOPOINT_LNG_COLUMN = 5;
		public static final int LINK_COLUMN = 6;
		  
		// Database Create string Constant ...
	    private static final String DATABASE_CREATE =
	      "create table " + VOLCANO_TABLE + " (" 
	      + KEY_ID + " INTEGER Primary KEY, "
	      + KEY_DATE + " INTEGER, "
	      + KEY_DETAILS + " TEXT, "
	      + KEY_DESCRIPTION + " TEXT, "
	      + KEY_GEOPOINT_LAT + " FLOAT, "
	      + KEY_GEOPOINT_LNG + " FLOAT, "
	      + KEY_LINK + " TEXT);";	
	    
	    
	    
		// Constructor ....
		public VolcanoProvider(Context context) {
			
			this.context = context;     		// Get the context ...	
			vdbHelper = new VDBHelper();	// Get DBHelper object, 
												// inexpensive, lives long  ...
		}
		
		//// DATABASE Methods ************************************************************
		
		
		/** QUERY: 
		 * 
		 * 	This class queries the database and stores the result in a cursor  for later processing.  
		 * 	@returns Cursor 
		 */
		public Cursor query() {
			db = vdbHelper.getReadableDatabase();		// Get the database ...	
			
			// QUERY THE DATABASE:
			// SQL:  SELECT * FROM statuses where id=47 HAVING .... GROUP BY ... ORDER BY .... 
			// PRECompiled: db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
			// db.query returns a CURSOR (a pointer to a particular record or a large data set)
			// NOTE: We use [KEY_MAGNITUDE + " DESC"] in the "ORDER BY" slot to have the output ordered 
			
			// return db.query(VolcanoProvider.VOLCANO_TABLE, null, null, null, null, null, KEY_DATE + " DESC");
			//
			// Randomizing the Query results: In this particular data set date isn't relevant so to keep the data
			// 'fresh' each time it's refreshed we randomize the ordering of the return set of data from the query.
			// This necessitates the use of the 'db.rawQuery' clause instead of the 'normal' query. 
			return db.rawQuery("select * from volcano ORDER BY RANDOM()", null);
			
			// CLOSE Database  ....  we can't, because the moment we do, the GARBAGE collector comes 
			// out and deletes our data from the returning Cursor. (Odd idiosyncrasy). We can't leave it 
			// open either because of memory leaks. So we close it (and cursors) in the onStop and onDestroy
			// Life Cycle Overrides of the EBHIncidentActivity Class ...
			
			//db.close();

		}// END Query ...
		
		
		/** INSERT Method #1:  [ContentValues pairs] -> V_DB
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
			
			db = vdbHelper.getWritableDatabase();
			
			// Alternate:  db.insertWithOnConflict(DBHelper.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			// Performs the INSERT of the data into our database ...
			
			long ret;
			try {
				ret = db.insertOrThrow(VolcanoProvider.VOLCANO_TABLE, null, nv_pairs);
			} catch (SQLException e) {
				ret = -1;		// So we REALLY know if we hit the end of the list ...
			} finally {
				db.close(); 	// Close Database
			}
			// Return the result of the insert statement which we will
			// use to indicate if the record was really inserted ...
			return ret;
			
		}// END insert ... (ContentValues)
		
		
		
		/**INSERT Method #2: (EBHIncident Object) -> INSERT Method #1
		 * 
		 * 	@param [Volcano volcano] :=> (Volcano data as provided by Online XML Feed)
		 * 	Takes the Volcano data from our XML data feed and creates a new ContentValues
		 * 	(name/value pairs) object, inserts the Volcano data into that object and 
		 * 	then calls the 'other' INSERT method, which knows how to insert that type of data
		 * 	into the underlying database. 
		 */		
		public long insert(Volcano volcano) {
			
		    ContentValues nv_pairs = new ContentValues();	// Create blank KEY/VALUE pairs object 
		    
		    // Set the key/value pairs to the new object's values 
		    nv_pairs.put(VolcanoProvider.KEY_ID, volcano.getId());
		    nv_pairs.put(VolcanoProvider.KEY_DATE, volcano.getDate().getTime());
		    nv_pairs.put(VolcanoProvider.KEY_DETAILS, volcano.getDetails());
		    nv_pairs.put(VolcanoProvider.KEY_DESCRIPTION, volcano.getDescription());
		    nv_pairs.put(VolcanoProvider.KEY_GEOPOINT_LAT, volcano.getGeoPointLat());
		    nv_pairs.put(VolcanoProvider.KEY_GEOPOINT_LNG, volcano.getGeoPointLng());
		    nv_pairs.put(VolcanoProvider.KEY_LINK, volcano.getLink());
		    
		    return this.insert(nv_pairs); // now call the 'other' insert method that takes ContentValues as inputs...	    
		}// End insert #2 ...
		
		
		
		/** close:
		 * 	Closes the EBHIncident Database via 'ebhdbHelper.close()'
		 */
		public void close() {
			vdbHelper.close();
		}
		
		
		
		/** delete:
		 * 	Delete ALL the records in our database ...
		 */
		public void delete () {
			db = vdbHelper.getWritableDatabase();	// getWritableDatabase
			// Delete the actual data ...
			db.delete(VolcanoProvider.VOLCANO_TABLE, null, null);
			db.close(); // Close Database 
		}
		
		
		//// HELPER Methods ************************************************************
		
		
		/** VDBHelper:
		 * 
		 * 	Inner Class to help open/create/upgrade database ....
		 * 	Provides connection to the database "server", but the actual getWritableDatabase
		 * 	is what actually gives you the exclusive connection. EbhDBHelper can live for a LONG time.
		 * 	It doesn't hold a valuable resource whereas getWritableDatabase actually opens up a database 
		 * 	which is a valued system resource and must be closed ASAP.
		 */
		private class VDBHelper extends SQLiteOpenHelper {
				
		    // Constructor ...	
			public VDBHelper() {
		      super(context, VolcanoProvider.DATABASE_NAME, null, VolcanoProvider.DATABASE_VERSION);
		    }

		    
		    @Override
		    public void onCreate(SQLiteDatabase db) {
		      db.execSQL(VolcanoProvider.DATABASE_CREATE);           
		    }


			@Override
		    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    	if (debug) { Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data"); }
		         
		    	db.execSQL("DROP TABLE IF EXISTS " + VolcanoProvider.VOLCANO_TABLE);
		    	onCreate(db);
		    }
			
		  }// END Private Class EqDBHelper ...
		
		
}// END Class VolcanoProvider
