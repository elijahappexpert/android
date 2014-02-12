package com.versaggi.android.disasteralerts;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

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

/**EarthquakeActivity:
 * 
 * This class is the Earthquake Application itself. It sets up the UI, fetches 
 * the online XML data, calls EarthquakeProvider methods to insert it into a
 * SQLite DB, populates the Activity screen, constructs the dialog for selected 
 * quakes and calls the Map activity to map the quake.
 *   
 */
public class EarthquakeActivity extends BaseActivity {
 
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = EarthquakeActivity.class.getSimpleName();
		
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   	
	////  QUAKE Stuff
	ListView quake_listView;				 // Blank ListView Object
	Quake quake;					 		 // Blank Quake object 
	int selectedQuakeId;					 // Quake object id
	MatrixCursor quakeCursor;				 // Blank MatrixCursor Object (for debug)
	Cursor quakeDBCursor;					 // Blank Cursor Object 
	SimpleCursorAdapter quake_cursorAdapter; // Blank SimpleCursorAdapter Object 
	GetEarthquakes getEqTask;				 // Blank GetEarthquakes [AsyncTask] Object   
	String[] quake_columns;					 // Blank String Array {'quake_columns'} (for debug)
	String[] quakeDB_Columns;				 // Blank String Array {selected 'quakeDB_columns'}
	int[] db_to;							 // Blank Integer Array {R.TextView's from 'row_earthquakes_multi.xml'}
	String quakeFeed;						 // Blank 'R.string.quake_feed' Resource variable, holds XML URL
	
	//// DIALOG Stuff
	View quakeDetailsView;					 // Blank View Object
	
	static final int GET_EQS_DIALOG_ID = 47;	  	 // Dialog ID for the GetEarthquakes Dialog (arbitrary)
	static final int INFO_QUAKE_DIALOG_ID = 92; 	 // Dialog ID for the Info Earthquakes Dialog (arbitrary)
	static final int QUICKVIEW_QUAKE_DIALOG_ID = 93; // Dialog ID for the Quickview Earthquakes Dialog (arbitrary)
	

	//// LIFE CYCLE Overrides  *****************************************************
	
	// OnCreate
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
																			// Set up UI 
		setContentView(R.layout.earthquakes);    							// Inflate View
		quake_listView = (ListView)this.findViewById(R.id.eq_listView);   	// Get ListView Handle
		DA_App.searchParameters = "recent earthquakes";		// Initialize Global Search Parameters
		displayEarthQuakeScreen();	  						// Displays Earthquakes Activity Screen 
	}// END onCreate ... 
	
	
	// onResume:
	// Calls 'displayEarthQuakeScreen' to refresh the screen any time it becomes active again
	  @Override
	protected void onResume() {
		super.onResume();
		DA_App.searchParameters = "recent earthquakes";		// Initialize Global Search Parameters
		displayEarthQuakeScreen();	  						// Displays Earthquakes Activity Screen 
	}

	  
	// onPause:
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	// onDestroy:
	// Close the Database and stop managing the Cursor 
	@Override
	protected void onDestroy() {
		super.onDestroy();
		quakeDBCursor.close();
		stopManagingCursor(quakeDBCursor);
		DA_App.earthquakeProvider.close();
		DA_App.searchParameters = null;
	}


	// onStop:
	// Close the Database and stop managing the Cursor 
	@Override
	protected void onStop() {
		super.onStop();
		quakeDBCursor.close();
		stopManagingCursor(quakeDBCursor);
		DA_App.earthquakeProvider.close();
		DA_App.searchParameters = null;
	}

	
	
	//// UTILITY METHODS  *****************************************************
	
	
	// onClickRefreshQuakes:
	// Called from Dashboard Title Bar GET [DATAELEMENT]
	//
	public void onClickRefreshQuakes (View v) {
		getOnlineData();
	}
	
	
	
	/** displayEarthQuakeScreen:
	 * This method prepares the String Array ('quakeDB_Columns')and the Integer Array ('db_to') 
	 * as input for the construction of the SimpleCursorAdapter  ('quake_cursorAdapter'). It also
	 * queries the DB for ALL of the Quake records and returns them in a Cursor ('quakeDBCursor').
	 * Once the Adapter is constructed the ListView's ('quake_listView') Adapter is set populating
	 * the screen w/the Quake information. 
	 */
	private void displayEarthQuakeScreen() {
		
		//// ********** QUICKACTION PATTERN: ActionItem Setup 
		
        ActionItem websiteAction = new ActionItem();
        websiteAction.setTitle("WebSite");
        websiteAction.setIcon(getResources().getDrawable(R.drawable.qa_website));

		ActionItem mapAction = new ActionItem();
		mapAction.setTitle("Map");
		mapAction.setIcon(getResources().getDrawable(R.drawable.qa_map));
		
		ActionItem quickviewAction = new ActionItem();
		quickviewAction.setTitle("Quick View");
		quickviewAction.setIcon(getResources().getDrawable(R.drawable.qa_quickview));
		
		ActionItem infoAction = new ActionItem();
		infoAction.setTitle("Details");
		infoAction.setIcon(getResources().getDrawable(R.drawable.qa_info));
		
		ActionItem searchAction = new ActionItem();
		searchAction.setTitle("Google It");
		searchAction.setIcon(getResources().getDrawable(R.drawable.qa_google_search));
		
		ActionItem shareAction = new ActionItem();
		shareAction.setTitle("Share");
		shareAction.setIcon(getResources().getDrawable(R.drawable.qa_share));
		
		/// ********** QUICKACTION PATTERN: QuickAction Setup
		
		final QuickAction mQuickAction 	= new QuickAction(this);
		 
		mQuickAction.addActionItem(websiteAction);
		mQuickAction.addActionItem(mapAction);
		mQuickAction.addActionItem(quickviewAction);
		mQuickAction.addActionItem(infoAction);
		mQuickAction.addActionItem(searchAction);
		mQuickAction.addActionItem(shareAction);
		
		//// ********** QUICKACTION PATTERN: QuickAction Listener Setup
		
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@Override
			public void onItemClick(int pos) {
				
				if (pos == 0) { 												// **** Web Site BTN ****
					quakeDBCursor.moveToPosition(selectedQuakeId);
					final String link = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("link"));
					Uri uri = Uri.parse(link);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
					startActivity(intent);
					
				} else if (pos == 1) { 											// **** Map It BTN ****
					quakeDBCursor.moveToPosition(selectedQuakeId);
					final float lng = quakeDBCursor.getFloat(quakeDBCursor.getColumnIndex("longitude"));
					final float lat = quakeDBCursor.getFloat(quakeDBCursor.getColumnIndex("latitude"));
					Intent intent = new Intent(Intent.ACTION_DEFAULT, null, DA_App, DisasterMapActivity.class);
					intent.putExtra("longitude", lng);		// Add LNG to Extra Data Passed w/Intent
					intent.putExtra("latitude", lat);		// Add LAT to Extra Data Passed w/Intent
					startActivity(intent);
					
				} else if (pos == 2) { 											// **** QuickView Dialog BTN ****
					showDialog(QUICKVIEW_QUAKE_DIALOG_ID);
					
				} else if (pos == 3) { 											// **** Info Dialog BTN ****
					showDialog(INFO_QUAKE_DIALOG_ID);
				}
				else if (pos == 4) { 											// **** Search Dialog BTN ****
					  quakeDBCursor.moveToPosition(selectedQuakeId);
					  final String details = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("details"));
					  DA_App.searchParameters = "earthquake " + details ; 
			    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
			    	  startActivity(search);
				}
				else if (pos == 5) { 											// **** Share Dialog BTN ****
					// share text with 'any' APPS on your Android device that accept text Intents
					quakeDBCursor.moveToPosition(selectedQuakeId);
					final String details = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("details"));
					final String link = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("link"));
					Intent shareIntent = new Intent(Intent.ACTION_SEND, null, DA_App, DisasterMapActivity.class);
					shareIntent.setType("text/plain");
					shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Look what I just found ...");
					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, details + ": \n" + link);
					startActivity(Intent.createChooser(shareIntent, "From Diaster Events: "));
				}	
			} 
		}); 
		
		
		// ********** QUICKACTION PATTERN: Setup dismiss listener, set the icon back to normal
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {			
			@Override
			public void onDismiss() {
				if (debug) { Log.d(TAG, "in setOnDismissListener -> **Dismiss'd** " ); }
			}
		});
		
		
		// Attach a Listener to the ListView to respond to clicks on an item 
		quake_listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
				selectedQuakeId = _index;		// Get ID of Quake Cursor Object for later retrieval using
												// quakeDBCursor.moveToPosition(selectedQuakeId) in 
												// 'prepareSelectedEQDialog' Dialog method
				
				//// ******** Set Activity Search Parameters:
		        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
		        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
		        // extra parameter to a Google web search when someone clicks on the looking glass 
		        // search icon on this activities Dashboard title bar. In this case it'll be the details  
				// of the 'selected' earthquake. 
				
				quakeDBCursor.moveToPosition(selectedQuakeId);
				final String details = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("details"));
				DA_App.searchParameters = "earthquake " + details ; 
				
				mQuickAction.show(_v);		// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
    		}
    	});
		
		// 'quakeDB_Columns': A String Array of the COLUMN (Field) names from our Quake Database 
		String[] quakeDB_Columns = {EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_DATE, EarthquakeProvider.KEY_DETAILS, 
									EarthquakeProvider.KEY_MAGNITUDE, EarthquakeProvider.KEY_LINK };
	
		// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_earthquakes_multi.xml' 
		int[] db_to = new int[] {R.id.textViewEqID, R.id.textViewEqDate, R.id.textViewEqDetails,  R.id.textViewEqMagnitude, R.id.textViewEqLink };
		
		// Query *DB*, capture the result in Standard Cursor Object
		quakeDBCursor = DA_App.earthquakeProvider.query();	
		
		// Manage the Life Cycle of the Standard Cursor
		startManagingCursor(quakeDBCursor);   
		
		// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
		quake_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_earthquakes_multi, quakeDBCursor, quakeDB_Columns, db_to);
		quake_cursorAdapter.setViewBinder(VIEW_BINDER);	// Use VIEW_BINDER to convert System Data/Time to Relative 
		quake_listView.setAdapter(quake_cursorAdapter);		// Display the view ...

	}// END displayEarthquakeScreen ...
	
	
	
	/** VIEW_BINDER:  
 	   * Anonymous Inner Class (No Name...) 
	   * Our Custom Binder to bind the DATE column to its view and change data from
	   * 'system' time stamp stored in a database to 'relative' time.
	   */
	  static final ViewBinder VIEW_BINDER = new ViewBinder() {

		   
		/** setViewValue: (InnerClass) 
		 * Binds the Cursor column defined by the specified index to the specified view  
		 * This gets called for EACH column [ID, Created_At, User, Text] in our cursor, 
		 *  so we look for the column relating to DATE and then take action when we find it. 
		 */		  
	    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	    	  	// We are not processing anything other than the DATE column here
	      if (cursor.getColumnIndex("date") != columnIndex) {
	    	  	// We return false so that the system continues it's binding phase using some other method.
	        return false;
	      } else {
	    	  	// Get the system time in 'time stamp'
	        long timestamp = cursor.getLong(columnIndex); 
	        	// Get the RELATIVE time from conversion methods 
	        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);
	        
	        	// Set the relativeTime to the view ,  CASTE to avoid any runtime exceptions
	        ((TextView)view).setText(relativeTime);
	        
	        	// Return true so we can stop the processing of the SimpleCursorAdapter's binding process ...
	        return true;
	      }
	    }// END setViewValue inner class

	  }; // END ViewBinder Anonymous Inner Class
	
	

	/** getOnlineData: 
	 *	 Utility function used to start the AsyncTask that fetches the XML data from 
	 * 	our online XML feed and store it into the database.
	 */
	private void getOnlineData() {	
		
		getEqTask = new GetEarthquakes(this);    		// Instantiate an AsynchTask GetEarthquakes object
		getEqTask.execute();							// Execute AsynchTask(s), URL read in by resource ID 
														// embedded in 'load_online_data' load_online_data ...
	}// END getOnlineData ...
	
	
	
	/** GetEarthquakes: 
	 * 	[doInBackground -> load_online_data] => (insert EQ_DB)
	 * 	This method fetches the online earthquake data using AsyncTask as a vehicle
	 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
	 * 	and a SAX parser to interrogate the XML DOM object.
	 */	
	private class GetEarthquakes extends AsyncTask<Void, Void, MatrixCursor> {
		
		private Context mContext;
		
		GetEarthquakes(Context context) {        // Constructor 
			Context mContext = context;
		}
	
		
		// onPreExecute
		@Override
		protected void onPreExecute() {
			//super.onPreExecute();
			
			// Delete the Current DB of Earthquakes due to our TYPE of insert statement.
			DA_App.earthquakeProvider.delete();
			
			// START Process Dialog widget ....
			showDialog(GET_EQS_DIALOG_ID);
		}
	
		
		// onPostExecute
	    @Override
	    protected void onPostExecute(MatrixCursor result) {
			super.onPostExecute(result);        
			
			// STOP Process Dialog widget ....
			dismissDialog(GET_EQS_DIALOG_ID);
			
			// Displays (refresh) Earthquakes Activity Screen
			displayEarthQuakeScreen();
	    }

	    
	    // doInBackground
		@Override
	    protected  MatrixCursor doInBackground(Void... params) {
			return load_online_data();   	// Do the actual AsyncTask work ....
		}


		/** load_online_data:
		 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
		 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
		 * 	object and then puts that new object in the MatrixCursor object for later display (if needed).
		 * 	It also inserts that Data object into our database for later retrieval. 
		 * 	@param VOID
		 * 	@return MatrixCUrsor
		 */
	    private MatrixCursor load_online_data(Void... params) {
	    	
	      URL url;
	      try { 
	    	  
	    	// HTTP Connection Setup ********************************
	    	//
	        String quakeFeed = EarthquakeActivity.this.getString(R.string.quake_feed);  // <= Change this to change feed URL
	        url = new URL(quakeFeed);        
	        URLConnection connection;
	        connection = url.openConnection();      
	        HttpURLConnection httpConnection = (HttpURLConnection)connection; 
	        int responseCode = httpConnection.getResponseCode(); 
	        
	        if (responseCode == HttpURLConnection.HTTP_OK) { 
	            InputStream in = httpConnection.getInputStream();    					// initialize HTTP input stream  
	          
	            // DOM STUFF ********************************
	            
	            // Create a Blank 'dbf' Document Builder Factory Object which is a parser that produces DOM object trees 
	            // from XML documents
	            
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder(); 							// Get the Document Builder 
	          																			// object from the 'dbf' object.
	            Document dom = db.parse(in);      						// Parse in the HTTP input stream into the DOM object
	            Element docEle = dom.getDocumentElement();   			// Get the ROOT element ....
	          
	            // Get a list of EACH XML 'entry' using it's XML NODE TAG NAME.
	            // NOTE: At this stage you REALLY have to KNOW what your XML data looks like  ...
	            // The EarthQuake XML Data Source looks like this:
	            
	            // EARTHQUAKE XML DATA ********************************
	            //
	            //    		   <entry>
	            //	          		<id> *ID* </id> 
	            //	          		<title> *TITLE* </title> 
	            //	          		<updated> *DATE* </updated> 
	            //	          		<link rel="alternate" type="text/html" href=" *URL* " /> 
	            //	        		- <summary type="html">
	            //				          - <![CDATA[ *CDATA* ]]> 
	            //	        		  </summary>
	            //	          		<georss:point> *GEORSS_POINT*</georss:point> 
	            //	          		<georss:elev> -NOT_USED- </georss:elev> 
	            //	          		<category label="Age" term="Past hour" /> 
	            //	          	</entry>
	            //
	            // This application only uses: ['entry', 'id', 'title', 'updated', 'link', georss:point']]
	          
	            NodeList nl = docEle.getElementsByTagName("entry");
	          
	            if (nl != null && nl.getLength() > 0) {					// Cycle through the ALL the Nodes
	            	for (int i = 0 ; i < nl.getLength(); i++) {			// Interrogate EACH node 
	            	
	            		Element entry 		= (Element)nl.item(i);			// The node at the indexth position 
	            															// in the NodeList
	            		
	            		// Element Level Parsing  ********************************
	            		//
	            		Element id_e 		= (Element)entry.getElementsByTagName("id").item(0);			
	            		Element title_e 	= (Element)entry.getElementsByTagName("title").item(0);		
	            		Element georsspt_e	= (Element)entry.getElementsByTagName("georss:point").item(0);	
	            		Element updated_e	= (Element)entry.getElementsByTagName("updated").item(0);	
	            		Element summary_e	= (Element)entry.getElementsByTagName("summary").item(0);
	            		Element link_e 		= (Element)entry.getElementsByTagName("link").item(0);		 
	            		//  Element CDATA_e = (Element)entry.getElementsByTagName("summary").item(0);	// Summary <=> CDATA
	            		
	            		// String Level Parsing  ********************************
	            		//
	            		String title_str	= title_e.getFirstChild().getNodeValue();					// Details <=> Title
	            		String id_str 		= id_e.getFirstChild().getNodeValue();
	            		String link_str 	= link_e.getAttribute("href");
	            		String georsspt_str	= georsspt_e.getFirstChild().getNodeValue();
	            		String updated_str	= updated_e.getFirstChild().getNodeValue();
	            		String summary_str	= summary_e.getFirstChild().getNodeValue();
	            		// String CDATA_str = CDATA_e.getFirstChild().getNodeValue();
	              
	            		///// Supplemental Parsing 
	            		
	            		// DATE Processing ********************************
	            		// XML Date Format => <updated>2011-08-22T17:39:38Z</updated>
	            		// Format: ("yyyy-MM-dd'T'hh:mm:ss'Z'")
	            		
	            		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	            		Date date = new GregorianCalendar(0,0,0).getTime();								// Blank Date Object
	            		try {  														// Populate Date Object w/Parsed Date String
	            			date = sdf.parse(updated_str);
	            		} catch (ParseException e) {
	            			e.printStackTrace();
	            		}
	            		//if (debug) { Log.d(TAG, "AsyncTask: date ->: " + date); }				// Best Date Form
	              
	            		// GEORSS_POINT Parsing ********************************
	            		//
	            		String[] loc_lat_lng = georsspt_str.split(" ");			// GeoRSS points form: [5.6563 151.0320] 
	            		Location android_location = new Location("dummyGPS");
	            		
	            		// Latitude and Longitude Parsing  ********************************
	            		android_location.setLatitude(Double.parseDouble(loc_lat_lng[0]));	// LAT: [5.6563]
	            		android_location.setLongitude(Double.parseDouble(loc_lat_lng[1]));	// LNG: [151.0320]
	            		
	            		// Capture final split values (if necessary)
	            		// float lat = (float)android_location.getLatitude();
			            // float lng = (float)android_location.getLongitude();
			              
	            		
	            		// Magnitude Parsing ********************************
	            		//
	            		String magnitudeString = title_str.split(" ")[1];		// TITLE: [M 5.8, New Britain region, Papua New Guinea]
	            		int end =  magnitudeString.length()-1;
	            		double magnitude = Double.parseDouble(magnitudeString.substring(0, end));
	            		
	            		// Title Parsing and Formatting
	            		String temp_title1 = title_str.split(",")[1].trim();	// Starts at Zero, so take the place 1 data, get title.
	            																// [0=M 5.8] [1=New Britain region] [2=Papua New Guinea]
	            		String temp_title2 ="";									// **OR** [0=M 5.8] [1=New Britain region] and no [2=XXX]
						try {
							temp_title2 = ", " + title_str.split(",")[2].trim();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            		
	            		title_str = temp_title1 + temp_title2;					// Put together final string 		
	            																
	    	             														
	            		// Create a new Quake Object:
	            		// (6 columns, Location splits into LAT, LNG later in INSERT #2 from 'android_location'))
	            		// Quake stores the TITLE in it's DETAILS field so we pass 'title_str' here to be received in the
	            		// 'details' field of the Quake Object. 
	            		quake = new Quake(i, date, title_str, android_location, magnitude, summary_str, link_str);
	              
	            		// DATABASE INSERT ********************************
	            		DA_App.earthquakeProvider.insert(quake);
	              	              
	              
	            	   }// END_FOR 	(int i = 0 ; i < nl.getLength(); i++)
	            	  }// END_IF 	(nl != null && nl.getLength() > 0)
	        		}// END_IF 		(responseCode)
	        
	      		  	} catch (MalformedURLException e) {
	      		  		e.printStackTrace();
	      		   } catch (IOException e) {
	      			  e.printStackTrace();
	      		  } catch (ParserConfigurationException e) {
	      			  e.printStackTrace();
	      		 } catch (SAXException e) {
	      			  e.printStackTrace();
	      		}
	      		finally {
	      		}
	      	return quakeCursor;  	// Return the quakeCursor (as options)
	      
	    } // END load_online_data ...
	    
	}// END Class GetEarthquakes ...



	//// MANAGED Dialog Infrastructure **************************************************************
	
		
	// onCreateDialog:
	// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
	@Override
	protected Dialog onCreateDialog(int id) {
		 switch (id) {
         	case GET_EQS_DIALOG_ID: {     	 
         		// Progression Dialog used when downloading XML data from online source.
        	 	return createGetEqDialog();
         		}
         	case INFO_QUAKE_DIALOG_ID: {
        	 	// Selection Dialog displayed when an individual quake object has been selected 
        	 	return createSelectedEQDialog();
         		}
         	case QUICKVIEW_QUAKE_DIALOG_ID: {
        	 	// Selection Dialog displayed when an individual quake object has been selected 
        	 	return createSelectedEQDialog();
         		}
		 	}
		 return null;
	}// END onCreateDialog 
	
	
	
	/** createGetEqDialog:
	 * 
	 * 	Creates the "Please Wait while downloading dialog object, canceled in 
   	 * 	'onPostExecute' method of 'GetEarthquakes' class.
	 */
	private Dialog createGetEqDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(this.getString(R.string.progressDialogue));  // Set the parameters of the dialog:
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        return dialog; 
	}
	
	
	
	/** createSelectedEQDialog:
	 * 
	 * Creates and populates the Dialog screen responsible for displaying individual Earthquake 
	 * details and a live link to it's web page. 
	 */
	private Dialog createSelectedEQDialog() {
		
		// Set up UI
		LayoutInflater layinflt = LayoutInflater.from(this);
		View quakeDetailsView = layinflt.inflate(R.layout.quake_details, null);
		
		// Dialog Builder Stuff
		AlertDialog.Builder quakeDialog = new AlertDialog.Builder(this);
		quakeDialog.setView(quakeDetailsView);
		
		return quakeDialog.create();			// Creates and Invokes onPrepareDialog before display ...
	}
	
	
	// onPrepareDialog:
	// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
    	
    	String CONTENT = null;	 // Determine if the content for the WebView is a URL Scrape or the CDATA 
    	
    	switch(id) {
			case (GET_EQS_DIALOG_ID) :
				 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_EQS_DIALOG_ID'd " ); }
				break;
    		case (INFO_QUAKE_DIALOG_ID) :
    			CONTENT = "SCRAPED";
    			prepareSelectedEQDialog(dialog, CONTENT);	// Prepare the dialog to display selected quakes
    			break;
    		case (QUICKVIEW_QUAKE_DIALOG_ID) :
    			CONTENT = "CDATA";
    			prepareSelectedEQDialog(dialog, CONTENT);		// Prepare the dialog to display selected quakes
    			break;
    		}
    	}// END onPrepareDialog 
	
    
    
    /** prepareSelectedEQDialog:
     * 	Prepares the dialog for displaying the Selected Quake information 
     */
    private void prepareSelectedEQDialog(Dialog dialog, String content) {
      	
    	AlertDialog selectedQuakeDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
    	
		// 'selectedQuakeId' is instantiated in the 'setOnItemClickListener' method is 
		// an index to the cursor, corresponding to the list item just selected.
    	
		quakeDBCursor.moveToPosition(selectedQuakeId);
		
		// Grab QUAKE field values from 'quakeDBCursor' object created from DB Query.
		
		// STRINGS ...
		final String magnitude = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("magnitude"));
		final String details = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("details"));
		 	  String cdata = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("cdata"));
		final String date = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("date"));
		final String link = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("link"));
		final String _id = quakeDBCursor.getString(quakeDBCursor.getColumnIndex("_id"));
		
		// FLOATS ...
		final float lng = quakeDBCursor.getFloat(quakeDBCursor.getColumnIndex("longitude"));
		final float lat = quakeDBCursor.getFloat(quakeDBCursor.getColumnIndex("latitude"));
		
		// DATE Parsing (Relative time) *********************************
    	long timestamp = quakeDBCursor.getLong(quakeDBCursor.getColumnIndex("date"));
    	CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);   	
    	
    	// JSOUP HTML Parsing  *********************************
    	org.jsoup.nodes.Document doc = null;						// Set the 'proper' Jsoup Doc type 
    	
		try {
			doc = Jsoup.connect(link).get();			// Connect and fetch web page using 'link' URL
		} catch (IOException e) {
			Log.d(TAG, " WebSite Scrape Error .... " );
			e.printStackTrace();
		}

		if (debug) { Log.d(TAG, "JSoup'd Link: " + link ); }	
		if (debug) { Log.d(TAG, "JSoup'd CDATA: " + cdata); }	
		
		Elements tables = doc.select("table#parameters");			// Get the HTML Table (#parameter) from page URL
		
		
    	// WebView Processing **********************************
		WebView myWebView = (WebView) dialog.findViewById(R.id.DialogEqWebView);
		
		WebSettings webSettings = myWebView.getSettings();					// Get WebView Settings, set font size
		webSettings.setDefaultFontSize(11);

		if (content == "SCRAPED") {
			//myWebView.loadData( tables.toString() , "text/html", "UTF-8");	// Load Scrapped HTML Table, make string first
			myWebView.loadDataWithBaseURL(null, tables.toString() , "text/html", "UTF-8", null);
		}
		else if (content == "CDATA") {			
			myWebView.loadDataWithBaseURL(null, cdata, "text/html", "UTF-8", null);  // Load CData into WebView instead 
		}
		
    	
    } // END prepareSelectedEQDialog
    
    
    
}// END CLASS EarthquakeActivity





