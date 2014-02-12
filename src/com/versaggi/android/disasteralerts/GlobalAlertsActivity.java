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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

/**GlobalAlertsActivity:
 * 
 * This class is the Global Alerts Application itself. 
 * It sets up the UI, fetches the online XML data, calls GlobalAlertsProvider methods 
 * to insert it into a SQLite DB, populates the Activity screen, constructs the dialog 
 * for selected GlobalAlerts and calls the Map activity to map the GlobalAlerts.
 *   
 */
public class GlobalAlertsActivity extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = GlobalAlertsActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   	
	////  GlobalAlerts Stuff
	ListView globalalerts_listView;			 			// Blank ListView Object
	GlobalAlerts globalalerts;				 			// Blank GlobalAlerts object 
	int selectedeGlobalAlertstId;				 		// GlobalAlerts object id
	MatrixCursor globalalertsCursor;			 		// Blank MatrixCursor Object (for debug)
	Cursor globalalertsDBCursor;				 		// Blank Cursor Object 
	SimpleCursorAdapter globalalerts_cursorAdapter; 	// Blank SimpleCursorAdapter Object 
	GetGlobalAlerts getGaTask;				 			// Blank GetGlobalAlerts [AsyncTask] Object   
	String[] globalalerts_columns;						// Blank String Array {'globalalerts_columns'} (for debug)
	String[] globalalertsDB_Columns;				 	// Blank String Array {selected 'globalalertsDB_columns'}
	int[] db_to;							 			// Blank Integer Array {R.TextView's from 'row_globalalerts_multi.xml'}
	String globalalertsFeed;						 	// Blank 'R.string.globalalerts_feed' Resource variable, holds XML URL
	
	//// DIALOG Stuff
	View globalalertsDetailsView;					 	// Blank View Object
	
	static final int GET_GA_DIALOG_ID = 47;	  			 	// Dialog ID for the GetGlobalAlerts Dialog (arbitrary)
	static final int INFO_GLOBALALERTS_DIALOG_ID = 92; 	 	// Dialog ID for the SelectGlobalAlerts Dialog (arbitrary)
	
	
	//// LIFE CYCLE Overrides  *****************************************************
	
	
	// OnCreate
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
																				// Set up UI 
		setContentView(R.layout.globalalerts);    								// Inflate View
		globalalerts_listView = (ListView)this.findViewById(R.id.ga_listView);	// Get ListView Handle
		DA_App.searchParameters = "global disaster alerts ";			// Initialize Global Search Parameters
		displayGlobalAlertsScreen();	  								// Displays GlobalAlerts Activity Screen 
	}// END onCreate ... 
	
	
	// onResume:
	// Calls 'displayGlobalAlertsScreen' to refresh the screen any time it becomes active again
	  @Override
	protected void onResume() {
		super.onResume();
		DA_App.searchParameters = "global disaster alerts ";
		displayGlobalAlertsScreen();	  		// Displays GlobalAlerts Activity Screen 
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
		globalalertsDBCursor.close();
		stopManagingCursor(globalalertsDBCursor);
		DA_App.globalAlertsProvider.close();
		DA_App.searchParameters = null;
	}


	// onStop:
	// Close the Database and stop managing the Cursor 
	@Override
	protected void onStop() {
		super.onStop();
		globalalertsDBCursor.close();
		stopManagingCursor(globalalertsDBCursor);
		DA_App.globalAlertsProvider.close();
		DA_App.searchParameters = null;
	}

	
	
	//// UTILITY METHODS  *****************************************************
	

	// onClickRefreshGlobalAlerts: 
	// Called from Dashboard Title Bar GET [DATAELEMENT]
	//
	public void onClickRefreshGlobalAlerts (View v)	{
		getOnlineData();
	}
	
	
	
	/** displayGlobalAlertsScreen:
	 * This method prepares the String Array ('globalalertsDB_Columns')and the Integer Array ('db_to') 
	 * as input for the construction of the SimpleCursorAdapter  ('globalalerts_cursorAdapter'). It also
	 * queries the DB for ALL of the GlobalAlerts records and returns them in a Cursor ('globalalertsDBCursor').
	 * Once the Adapter is constructed the ListView's ('globalalerts_listView') Adapter is set populating
	 * the screen w/the GlobalAlerts information. 
	 */
	private void displayGlobalAlertsScreen() {
		
		
		//// ********** QUICKACTION PATTERN: ActionItem Setup 
		
        ActionItem websiteAction = new ActionItem();
        websiteAction.setTitle("WebSite");
        websiteAction.setIcon(getResources().getDrawable(R.drawable.qa_website));

		ActionItem mapAction = new ActionItem();
		mapAction.setTitle("Map");
		mapAction.setIcon(getResources().getDrawable(R.drawable.qa_map));
		
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
		mQuickAction.addActionItem(infoAction);
		mQuickAction.addActionItem(searchAction);
		mQuickAction.addActionItem(shareAction);
		
		//// ********** QUICKACTION PATTERN: QuickAction Listener Setup
		
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@Override
			public void onItemClick(int pos) {
				
				if (pos == 0) { 												// **** Web Site BTN ****
					globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);
					final String link = globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("link"));
					Uri uri = Uri.parse(link);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
					startActivity(intent);
				} 
				else if (pos == 1) { 											// **** Map It BTN ****
					globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);					
					final float lat = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lat"));
					final float lng = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lng"));
					Intent intent = new Intent(Intent.ACTION_DEFAULT, null, DA_App, DisasterMapActivity.class);
					intent.putExtra("longitude", lng);		// Add LNG to Extra Data Passed w/Intent
					intent.putExtra("latitude", lat);		// Add LAT to Extra Data Passed w/Intent
					startActivity(intent);
				} 
				else if (pos == 2) { 											// **** Info Dialog BTN ****
					showDialog(INFO_GLOBALALERTS_DIALOG_ID);
				}
				else if (pos == 3) { 											// **** Search Dialog BTN ****
					globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);
					  final String details = globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("details"));
					  DA_App.searchParameters = details ; 
			    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
			    	  startActivity(search);
				}
				else if (pos == 4) { 											// **** Share Dialog BTN ****
					// **** share text with 'any' APPS on your Android device that accept text Intents ****
					globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);
					final String details = globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("details"));
					final String link = globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("link"));
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
		globalalerts_listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
				selectedeGlobalAlertstId = _index;	// Get ID of GlobalAlerts Cursor Object for later retrieval using
													// globalalertsDBCursor.moveToPosition(selectedGaId) in 
													// 'prepareSelectedGaDialog' Dialog method 
				
				//// ******** Set Activity Search Parameters:
		        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
		        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
		        // extra parameter to a Google web search when someone clicks on the looking glass 
		        // search icon on this activities Dashboard title bar. In this case it'll be the details  
				// of the 'selected' earthquake. 
				
				globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);
				final String details = globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("details"));
				DA_App.searchParameters = details ; 
				
				mQuickAction.show(_v);			// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
				
    		}
    	});
		
		// 'globalalertsDB_Columns': A String Array of the COLUMN (Field) names from our GlobalAlerts Database 
		String[] globalalertsDB_Columns = {GlobalAlertsProvider.KEY_ID, GlobalAlertsProvider.KEY_DATE, GlobalAlertsProvider.KEY_DETAILS, 
										   GlobalAlertsProvider.KEY_LINK };
	
		// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_globalalerts_multi.xml' 
		int[] db_to = new int[] {R.id.textViewGaID, R.id.textViewGaDate, R.id.textViewGaDetails, R.id.textViewGaLink };
		
		// Query *DB*, capture the result in Standard Cursor Object
		globalalertsDBCursor = DA_App.globalAlertsProvider.query();	
		
		// Manage the Life Cycle of the Standard Cursor
		startManagingCursor(globalalertsDBCursor);   
		
		// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
		globalalerts_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_globalalerts_multi, globalalertsDBCursor, globalalertsDB_Columns, db_to);
		globalalerts_cursorAdapter.setViewBinder(VIEW_BINDER);				// Use VIEW_BINDER to convert System Data/Time to Relative Time
		globalalerts_listView.setAdapter(globalalerts_cursorAdapter);		// Display the view ...

	}// END displayGlobalAlertsScreen ...
	
	
	
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
			
			getGaTask = new GetGlobalAlerts(this);    		// Instantiate an AsynchTask GetGlobalAlerts object
			getGaTask.execute();							// Execute AsynchTask(s), URL read in by resource ID 
															// embedded in 'load_online_data' load_online_data ...
		}// END getOnlineData ...
		
		
		/** GetGlobalAlerts: 
		 * 	[doInBackground -> load_online_data] => (insert GA_DB)
		 * 	This method fetches the online GlobalAlerts data using AsyncTask as a vehicle
		 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
		 * 	and a SAX parser to interrogate the XML DOM object.
		 */	
		private class GetGlobalAlerts extends AsyncTask<Void, Void, MatrixCursor> {
			
			private Context mContext;
			
			GetGlobalAlerts(Context context) {        // Constructor 
				Context mContext = context;
			}
		
			
			// onPreExecute
			@Override
			protected void onPreExecute() {
				//super.onPreExecute();
				
				// Delete the Current DB of GlobalAlerts due to our TYPE of insert statement.
				DA_App.globalAlertsProvider.delete();
				
				// START Process Dialog widget ....
				showDialog(GET_GA_DIALOG_ID);
			}
		
			
			// onPostExecute
		    @Override
		    protected void onPostExecute(MatrixCursor result) {
				super.onPostExecute(result);        
				
				// STOP Process Dialog widget ....
				dismissDialog(GET_GA_DIALOG_ID);
				
				// Displays (refresh) GlobalAlerts Activity Screen
				displayGlobalAlertsScreen();
		    }

		    
		    // doInBackground
			@Override
		    protected  MatrixCursor doInBackground(Void... params) {
				return load_online_data();   	// Do the actual AsyncTask work ....
			}


			/** load_online_data:
			 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
			 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
			 * 	object (GlobalAlerts) and then puts that new object in the MatrixCursor object for later display.
			 * 	It also inserts that GlobalAlerts object into our database for later retrieval. 
			 * 	@param VOID
			 * 	@return MatrixCUrsor
			 */
		    private MatrixCursor load_online_data(Void... params) {
		    	
		      // Get the XML
		      URL url;
		      try { 
		    	  
		    	  // HTTP Connection Setup ********************************
		    	  //
		    	  String globalalertsFeed = GlobalAlertsActivity.this.getString(R.string.globalalerts_feed);  // <= Change this to change feed URL
		    	  url = new URL(globalalertsFeed);        
		    	  URLConnection connection;
		    	  connection = url.openConnection();      
		          HttpURLConnection httpConnection = (HttpURLConnection)connection; 
		          int responseCode = httpConnection.getResponseCode(); 
		        
		        	if (responseCode == HttpURLConnection.HTTP_OK) { 
		        		InputStream in = httpConnection.getInputStream();      
		          
			    		  
			    		// DOM STUFF ********************************
				            
			    		// Create a Blank 'dbf' Document Builder Factory Object which is a parser that produces DOM object trees 
			    		// from XML documents
		        		
		        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        		DocumentBuilder db = dbf.newDocumentBuilder();							// Get the Document Builder
		        																				// object from the 'dbf' object.		
		        		Document dom = db.parse(in);      					// Parse in the HTTP input stream into the DOM object.
		        		Element docEle = dom.getDocumentElement();    		// Get the ROOT element ....
		          
				          
				        // Get a list of EACH XML 'entry' using it's XML NODE TAG NAME.
				        // NOTE: At this stage you REALLY have to KNOW what your XML data looks like  ..
			    		  
		        		// The GlobalAlert XML Data Source looks like this:
		        		// ************************************************
//		        		<item>
//		    				<title>*TITLE*title>
//		    				<description>*DESCRIPTION* <description>
//		    				<gdas:image>*IMAGE*</gdas:image>
//		    				<enclosure *ENCLOSURE* ></enclosure>
//		    				<link>*LINK*</link>
//		    				<pubDate>Wed, 24 Aug 2011 09:00:00 +0100</pubDate>
//		    				<guid isPermaLink="false">IRENE-11</guid>
//		    				<glide:number></glide:number>
//		    				<gdas:eventType>**</gdas:eventType>
//		    				<asgard:name>**</asgard:name>
//		    				<asgard:basin>**</asgard:basin>
//		    				<asgard:alertLevel>**</asgard:alertLevel>
//		    				<asgard:ID>**</asgard:ID>
//		    				<dc:subject>**</dc:subject>
//		    				<gdas:alertLevel>**</gdas:alertLevel>
//		    				<gdas:country>**</gdas:country>
//		    				<gdas:iso3>**</gdas:iso3>
//		    				<geo:Point>
//		    					<geo:lat>21.6000000002186</geo:lat>
//		    					<geo:long>-72.8999999997814</geo:long>
//		    				</geo:Point>
//		    				<georss:point>21.6000000002186 -72.8999999997814</georss:point>
//		    		</item>
		        		
		        		// This application only uses: ['item', 'title', 'description', 'georss:point', 'pubDate', 'link']]
		        		
		        		// NOTE: The LINK element contains "&amp;", which are going to be a problem to parse since the DOM will 
		        		// cease parsing aftet it hits the first "&amp;" it finds. The reality is that there is a 'tree' of 
		        		// nodes after that which can be access and concatenated together to form a proper URL which takes this: 
		        		// http://www.gdacs.org/reports.asp?eventType=TC&amp;ID=26299&amp;system=asgard&amp;alertlevel=Green&amp;glide_no=&amp;location=&amp;country=&amp;new=true
		        		// and transforms it into  this:
		        		// http://www.gdacs.org/reports.asp?eventType=TC&ID=26299&system=asgard&alertlevel=Green&glide_no=&location=&country=&new=true

		        				
		        		NodeList nl = docEle.getElementsByTagName("item");
		        		
		        		if (nl != null && nl.getLength() > 0) {					// Cycle through the ALL the Nodes
		        			for (int i = 0 ; i < nl.getLength(); i++) {			// Interrogate EACH node
		        				
			            		Element item 		= (Element)nl.item(i);			// The node at the indexth position 
																					// in the NodeList
			            		
				            	// Element Level Parsing  ********************************
				            	//	
		        				Element title_e			= (Element)item.getElementsByTagName("title").item(0);						
		        				Element description_e 	= (Element)item.getElementsByTagName("description").item(0);				
		        				Element georsspt_e		= (Element)item.getElementsByTagName("georss:point").item(0);
		        				Element date_e 			= (Element)item.getElementsByTagName("pubDate").item(0);		
		        				Element link_e 			= (Element)item.getElementsByTagName("link").item(0);	
		             
				            	// String Level Parsing  ********************************
				            	//
		        				String title_str 			= title_e.getFirstChild().getNodeValue();	
		        				String description_str 		= description_e.getFirstChild().getNodeValue();
		        				String georsspt_str 		= georsspt_e.getFirstChild().getNodeValue();
		        				String date_str 			= date_e.getFirstChild().getNodeValue();
		        				
		        				// ***** WorkAround for malformed XML (Contains  '&amp;' instead of '&') *******
		        				// Set the FirstChild of the element obtained by the TagName 'LINK' to 'node'
		        				// Traverse the list of nodes, using the getNextSibling() method to access the 
		        				// next node in the list while concatinating the results into a proper URL.
		        				//
		        				String link_str = "";
		        				Node node = link_e.getFirstChild();
		        				
		        				while (node != null) {
		        					link_str = link_str + node.getNodeValue();
		        					node = node.getNextSibling();
		        				}

			            		///// Supplemental Parsing 
			            		
			            		// DATE Processing ********************************
			            		// XML Date Format => <pubDate>Wed, 24 Aug 2011 09:00:00 +0100</pubDate>
		        				// 					  <pubDate>Mon, 22 Aug 2011 20:12:00 +0100</pubDate>
		        				// FORMAT: ("EEE, dd MMM yyyy hh:mm:ss ZZZZZ")
		        				
		        				//SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm Z");
		        				Date date = new GregorianCalendar(0,0,0).getTime();	       // Blank Date Object
		        				
		        				// Parse Date & Populate Date Object w/ 2 Parsed Date String formats
		        				//
		        				try {  							
		        					SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm Z");
		        					date = sdf.parse(date_str);		 				
		        					} catch (ParseException e) {
		        						e.printStackTrace();
		        				
		        						try {  							
				        					SimpleDateFormat sdf1 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
				        					date = sdf1.parse(date_str);		 				
				        					} catch (ParseException e1) {
				        						e.printStackTrace();
				        					}
		        					}
		        				
			            		// GEORSS_POINT Parsing ********************************
			            		//
		        				String[] loc_lat_lng = georsspt_str.split(" ");				// GeoRSS points form: [5.6563 151.0320]					
		        				Location android_location = new Location("dummyGPS");
		        				
		        				// Latitude and Longitude Parsing  ********************************
			            		android_location.setLatitude(Double.parseDouble(loc_lat_lng[0]));	// LAT: [5.6563]
			            		android_location.setLongitude(Double.parseDouble(loc_lat_lng[1]));	// LNG: [151.0320]
		        				
			            		// Capture final split values (if necessary)
			            		float lat = (float)android_location.getLatitude();
					            float lng = (float)android_location.getLongitude();
			            		
		        				// Create a new GlobalAlerts Object:
		        				// GlobalAlerts(_ebhid, _date, _details, _descr, _geopt_lat, _geopt_lng, _cdata, _link)
		        				globalalerts = new GlobalAlerts(i, date, title_str,  description_str, lat, lng, link_str);
		              
		        				// INSERT GlobalAlerts Object into the Database
		        				DA_App.globalAlertsProvider.insert(globalalerts);
		              	              
		        				
	          				}// END_FOR (int i = 0 ; i < nl.getLength(); i++)
        			  	  }// END_IF (nl != null && nl.getLength() > 0)
  	  					}// END_IF (responseCode)
  	  
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
		      return globalalertsCursor;  	// Return the globalalertsCursor
		      
		    } // END doInBackground ...
		    
		}// END Class GetGlobalAlerts ...



		//// MANAGED Dialog Infrastructure **************************************************************
		
		
		// onCreateDialog:
		// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
		@Override
		protected Dialog onCreateDialog(int id) {
			 switch (id) {
	         	case GET_GA_DIALOG_ID: {     	 
	         		// Progression Dialog used when downloading XML data from online source.
	        	 	return createGetGaDialog();
	         		}
	         	case INFO_GLOBALALERTS_DIALOG_ID: {
	        	 	// Selection Dialog displayed when an individual quake object has been selected 
	        	 	return createSelectedGaDialog();
	         		}
			 	}
			 return null;
		}// END onCreateDialog 
		
		
		
		/** createGetEbhiDialog:
		 * 
		 * 	Creates the "Please Wait while downloading dialog object, canceled in 
	   	 * 	'onPostExecute' method of 'GetENHIncidents' class.
		 */
		private Dialog createGetGaDialog() {
	        ProgressDialog dialog = new ProgressDialog(this);
	        // Set the parameters of the dialog:
	        dialog.setMessage(this.getString(R.string.progressDialogue));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog; 
		}
		
		/** createSelectedGaDialog:
		 * 
		 * Creates and populates the Dialog screen responsible for displaying individual Earthquake 
		 * details and a live link to it's web page. 
		 */
		private Dialog createSelectedGaDialog() {

			// Set up UI
			LayoutInflater layinflt = LayoutInflater.from(GlobalAlertsActivity.this);
			View globalalertsDetailsView = layinflt.inflate(R.layout.globalalerts_details, null);

			// Dialog Builder Stuff
			AlertDialog.Builder globalalertsDialog = new AlertDialog.Builder(GlobalAlertsActivity.this);
			globalalertsDialog.setView(globalalertsDetailsView);
			
			return globalalertsDialog.create();			// Invokes onPrepareDialog before display ...
		}
		
		
		// onPrepareDialog:
		// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
	    @Override
	    public void onPrepareDialog(int id, Dialog dialog) {
	    	switch(id) {
				case (GET_GA_DIALOG_ID) :
					 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_EQS_DIALOG_ID'd " ); }
					break;
	    		case (INFO_GLOBALALERTS_DIALOG_ID) :
	    			prepareSelectedGADialog(dialog);		// Prepare the dialog to display selected quakes
	    			break;
	    		}
	    	}// END onPrepareDialog 
	    
	    
	    
	    /** prepareSelectedGADialog:
	     * 	Prepares the dialog for displaying the Selected GlobalAlert information 
	     */
	    private void prepareSelectedGADialog(Dialog dialog) {
	    	
	    	AlertDialog selectedGlobalAlertsDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
	    	
	    	// Infrastructure NOTES:
			// 'selectedeGlobalAlertsId' is instantiated in the 'setOnItemClickListener' method is 
			// an index to the cursor, corresponding to the list item just selected. 'globalalertsDBCursor' is
	    	// populated via the DB query call made to initially populate the screen. 
	    	
	    	globalalertsDBCursor.moveToPosition(selectedeGlobalAlertstId);
			
			// Grab key  field values from 'ebhincidentDBCursor' object created from DB Query.
	    	
			// STRINGS **********************************
			final String link 			= globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("link"));
			final String _id 			= globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("_id"));
				  String description 	= globalalertsDBCursor.getString(globalalertsDBCursor.getColumnIndex("description"));
			description = description + " ... [Read More Online]";			// To counter funky formatting in the XML
			
			// FLOATS **********************************
			final float lat = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lat"));
			final float lng = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lng"));

	    	// DATE Parsing (Relative time) *********************************
	    	long timestamp = globalalertsDBCursor.getLong(globalalertsDBCursor.getColumnIndex("date"));
	    	CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);
	    	
	    	
	    	// WebView Stuff **********************************
			WebView myWebView = (WebView) dialog.findViewById(R.id.DialogGaWebView);
			WebSettings webSettings = myWebView.getSettings();			// Get WebView Settings, set font size
			webSettings.setDefaultFontSize(13);
			myWebView.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
			
	    	
	    } // END prepareSelectedEBHIDialog
	    

	    
}// END Class GlobalAlertsActivity












