package com.versaggi.android.disasteralerts;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
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
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;

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

/**CATStoryActivity:
 * 
 * This class is the CatastropheMap Story Application itself. It sets up the UI, 
 * fetches the online XML data, calls CATStoryProvider methods to insert it 
 * into an SQLite DB, populates the Activity screen, constructs the dialog  
 * for selected CATStories.
 *   
 */
public class CATStoryActivity extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = CATStoryActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	   
		////  CATStory Stuff
		ListView catstory_listView;				 	// Blank ListView Object
		CATStory catstory;					 	 	// Blank CATStory object 
		int selectedCatstoryId;					 	// CATStory object id
		MatrixCursor catstoryCursor;			 	// Blank MatrixCursor Object (for debug)
		Cursor catstoryDBCursor;				 	// Blank Cursor Object 
		SimpleCursorAdapter catstory_cursorAdapter; // Blank SimpleCursorAdapter Object 
		GetCATStory getCsTask;				 		// Blank CATStory [AsyncTask] Object   
		String[] catstory_columns;					// Blank String Array {'catstory_columns'} (for debug)
		String[] catstoryDB_Columns;				// Blank String Array {selected 'catstoryDB_columns'}
		int[] db_to;							 	// Blank Integer Array {R.TextView's from 'row_catstory_multi.xml'}
		String catstoryFeed;						// Blank 'R.string.capmap_feed' Resource variable, holds XML URL
		
		//// DIALOG Stuff
		View catstoryDetailsView;					 	// Blank View Object
		
		static final int GET_CATS_DIALOG_ID = 47;	 	 // Dialog ID for the GetCATStory Dialog (arbitrary)
		static final int INFO_CATSTORY_DIALOG_ID = 92; // Dialog ID for the SelectCATStory Dialog (arbitrary)
		static final int QUICKVIEW_CATSTORY_DIALOG_ID = 93; // Dialog ID for the Quickview CATStory Dialog (arbitrary)
		
		// UI Stuff
		Button buttonGetCsData;					 // Blank Button object 
		

		//// LIFE CYCLE Overrides  *****************************************************
	   
		@Override  
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// Set up UI 
			setContentView(R.layout.catstory);    									// Inflate View
			catstory_listView = (ListView)this.findViewById(R.id.cs_listView);   	// Get ListView Handle			
			DA_App.searchParameters = "catastrophe map"; // Initialize Global Search Parameters
			displayCATStoryScreen();	  				 // Displays Catastrophe Story Activity Screen 
		}// END onCreate ... 
		
		

		
		// onResume:
		// Calls 'displayCATStoryScreen' to refresh the screen any time it becomes active again
		  @Override
		protected void onResume() {
			super.onResume();
			DA_App.searchParameters = "catastrophe map"; // Initialize Global Search Parameters
			displayCATStoryScreen();	  				 // Displays CATStory Activity Screen 
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
			catstoryDBCursor.close();
			stopManagingCursor(catstoryDBCursor);
			DA_App.catStoryProvider.close();
			DA_App.searchParameters = null;
		}


		// onStop:
		// Close the Database and stop managing the Cursor 
		@Override
		protected void onStop() {
			super.onStop();
			catstoryDBCursor.close();
			stopManagingCursor(catstoryDBCursor);
			DA_App.catStoryProvider.close();
			DA_App.searchParameters = null;
		}

		
		//// UTILITY METHODS  *****************************************************
		
		
		// onClickRefreshCatStory:
		// Called from Dashboard Title Bar GET [DATAELEMENT]
		//
		public void onClickRefreshCatStory (View v) {
			getOnlineData();
		}
		
		
		/** displayCATStoryScreen:
		 * This method prepares the String Array ('catstoryDB_Columns')and the Integer Array ('db_to') 
		 * as input for the construction of the SimpleCursorAdapter  ('catstory_cursorAdapter'). It also
		 * queries the DB for ALL of the CATStory records and returns them in a Cursor ('catstoryDBCursor').
		 * Once the Adapter is constructed the ListView's ('catstory_listView') Adapter is set populating
		 * the screen w/the Quake information. 
		 */
		private void displayCATStoryScreen() {
			

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
						catstoryDBCursor.moveToPosition(selectedCatstoryId);
						final String link = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("link"));
						Uri uri = Uri.parse(link);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
						startActivity(intent);
						
					} else if (pos == 1) { 											// **** Map It BTN ****
						catstoryDBCursor.moveToPosition(selectedCatstoryId);
						final float lng = catstoryDBCursor.getFloat(catstoryDBCursor.getColumnIndex("geopoint_lng"));
						final float lat = catstoryDBCursor.getFloat(catstoryDBCursor.getColumnIndex("geopoint_lat"));
						Intent intent = new Intent(Intent.ACTION_DEFAULT, null, DA_App, DisasterMapActivity.class);
						intent.putExtra("longitude", lng);		// Add LNG to Extra Data Passed w/Intent
						intent.putExtra("latitude", lat);		// Add LAT to Extra Data Passed w/Intent
						startActivity(intent);
						
					} else if (pos == 2) { 											// **** Info Dialog BTN ****
						showDialog(INFO_CATSTORY_DIALOG_ID);
						
					} else if (pos == 3) { 											// **** Search Dialog BTN ****
						catstoryDBCursor.moveToPosition(selectedCatstoryId);
						  final String details = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("details"));
						  DA_App.searchParameters = "toxic " + details ; 
				    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
				    	  startActivity(search);
				    	  
					} else if (pos == 4) { 											// **** Share Dialog BTN ****
						// share text with 'any' APPS on your Android device that accept text Intents
						catstoryDBCursor.moveToPosition(selectedCatstoryId);
						final String details = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("details"));
						final String link = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("link"));
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
			catstory_listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
	        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
					selectedCatstoryId = _index;	// Get ID of CATStory Cursor Object for later retrieval using
													// catstoryDBCursor.moveToPosition(selectedQuakeId) in 
													// 'prepareSelectedCsDialog' Dialog method
					
					//// ******** Set Activity Search Parameters:
			        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
			        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
			        // extra parameter to a Google web search when someone clicks on the looking glass 
			        // search icon on this activities Dashboard title bar. In this case it'll be the details  
					// of the 'selected' story. 
					
					catstoryDBCursor.moveToPosition(selectedCatstoryId);
					final String details = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("details"));
					DA_App.searchParameters = "toxic " + details ; 
					
					mQuickAction.show(_v);		// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
	    		}
	    	});
			
			
			// 'catstoryDB_Columns': A String Array of the COLUMN (Field) names from our Quake Database 
			String[] catstoryDB_Columns = {CATStoryProvider.KEY_ID, CATStoryProvider.KEY_DETAILS, 
											CATStoryProvider.KEY_DESCRIPTION, CATStoryProvider.KEY_LINK };
		
			// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_catstory_multi.xml' 
			int[] db_to = new int[] {R.id.textViewCsID, R.id.textViewCsDetails,  R.id.textViewCsDescription, R.id.textViewCsLink };
			
			// Query *DB*, capture the result in Standard Cursor Object
			catstoryDBCursor = DA_App.catStoryProvider.query();	

			
			// Manage the Life Cycle of the Standard Cursor
			startManagingCursor(catstoryDBCursor);   
			
			// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
			catstory_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_catstory_multi, catstoryDBCursor, catstoryDB_Columns, db_to);
			catstory_listView.setAdapter(catstory_cursorAdapter);		// Display the view ...

		}// END displayCATStoryScreen ...
		
		
		
		/** getOnlineData: 
		 *	 Utility function used to start the AsyncTask that fetches the XML data from 
		 * 	our online XML feed and store it into the database.
		 */
		private void getOnlineData() {	
			
			getCsTask = new GetCATStory(this);    		// Instantiate an AsynchTask GetCATSTory object
			getCsTask.execute();							// Execute AsynchTask(s), URL read in by resource ID 
															// embedded in 'load_online_data' load_online_data ...
		}// END getOnlineData ...
		
		
		
		/** GetCATStory: 
		 * 	[doInBackground -> load_online_data] => (insert EQ_DB)
		 * 	This method fetches the online CATStory data using AsyncTask as a vehicle
		 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
		 * 	and a SAX parser to interrogate the XML DOM object.
		 */	
		private class GetCATStory extends AsyncTask<Void, Void, MatrixCursor> {
			
			private Context mContext;
			
			GetCATStory(Context context) {        // Constructor 
				Context mContext = context;
			}
		
			
			// onPreExecute
			@Override
			protected void onPreExecute() {
				//super.onPreExecute();
				
				// Delete the Current DB of CATStory due to our TYPE of insert statement.
				DA_App.catStoryProvider.delete();
				
				// START Process Dialog widget ....
				showDialog(GET_CATS_DIALOG_ID);
			}
		
			
			// onPostExecute
		    @Override
		    protected void onPostExecute(MatrixCursor result) {
				super.onPostExecute(result);        
				
				// STOP Process Dialog widget ....
				dismissDialog(GET_CATS_DIALOG_ID);
				
				// Displays (refresh) CATStory Activity Screen
				displayCATStoryScreen();
		    }

		    
		    // doInBackground
			@Override
		    protected  MatrixCursor doInBackground(Void... params) {
				return load_online_data();   	// Do the actual AsyncTask work ....
			}


			/** load_online_data:
			 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
			 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
			 * 	object (CATStory) and then puts that new object in the MatrixCursor object for later display.
			 * 	It also inserts that CATStory object into our database for later retrieval. 
			 * 	@param VOID
			 * 	@return MatrixCUrsor
			 */
		    private MatrixCursor load_online_data(Void... params) {
		    	
		      // Get the XML
		      URL url;
		      try { 
		    	  
		    	  // HTTP Connection Setup ********************************
		    	  // R.string.catmap_feed (OLD) **OR** R.string.toxic_apocalypse_feed (NEW)
		    	  String catstoryFeed = CATStoryActivity.this.getString(R.string.toxic_apocalypse_feed);  // <= Change this to change feed URL
		    	  url = new URL(catstoryFeed);        
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
		    		  DocumentBuilder db = dbf.newDocumentBuilder();						// Get the Document Builder	
		    		  																		// object from the 'dbf' object.
		    		  Document dom = db.parse(in);      				// Parse in the HTTP input stream into the DOM object
		    		  Element docEle = dom.getDocumentElement();    	// Get the ROOT element ....
		          
			          // Get a list of EACH XML 'entry' using it's XML NODE TAG NAME.
			          // NOTE: At this stage you REALLY have to KNOW what your XML data looks like  ..
		    		  
		    		  // The CATStory XML Data Source looks like this:
		    		  		    		   
//		    		  <item>
//		    			<title> *TITLE* </title>
//		    			<link> *LINK* </link>
//		    			<pubDate>Sat, 03 Dec 2011 11:08:38 +0000</pubDate>
//		    			<description><![CDATA[ ... [...]]]></description>
//		    			<content:encoded><![CDATA[ ... ]]></content:encoded>
//		    			<georss:point featurename="35.0077519, -97.092877">35.006523 -97.086874</georss:point>
//		    		</item>
		    		
		    		
		    		// This application only uses: ['item', 'title', 'content:encoded', 'georss:point', 'link']]

		    		  NodeList nl = docEle.getElementsByTagName("item");
		          		
		    		  if (nl != null && nl.getLength() > 0) {				// Cycle through the ALL the Nodes
		    			  for (int i = 0 ; i < nl.getLength(); i++) {		// Interrogate EACH node								  
		            	
			            	  // Element Level Parsing  ********************************
			            	  //
		    				  Element item = (Element)nl.item(i);		
		    				  Element title_e 		= (Element)item.getElementsByTagName("title").item(0);			  
		    				  Element description_e = (Element)item.getElementsByTagName("content:encoded").item(0);  // Note 'content:encoded' Tag..
		    				  Element georsspt_e	= (Element)item.getElementsByTagName("georss:point").item(0);
		    				  Element link_e 		= (Element)item.getElementsByTagName("link").item(0);	
		    				  
			            	  // String Level Parsing  ********************************
			            	  //
		    				  String details_str 		= title_e.getFirstChild().getNodeValue();							  
		    				  String georsspt_str 		= georsspt_e.getFirstChild().getNodeValue();
		    				  String link_str		 	= link_e.getFirstChild().getNodeValue();
		    				  //String description_str 	= description_e.getFirstChild().getNodeValue();	 // Normal Usage ...
		          				
		          				// XML Data may contain malformed HTML so we have to do this... (WordPress oye!)
		          				String description_str = null;
								try {
									description_str = description_e.getTextContent();  // <== Data contains HTML so we do this...
								} catch (DOMException e1) {
										description_str = "Sorry, the App received BAD XML DATA for this record ....";
									e1.printStackTrace();
								} 
								
		    				  
			            	  ///// Supplemental Parsing 
		              
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
					            
					            
			            	  // CATStory stores the TITLE in it's DETAILS field so we pass 'title_str' here to be received in the
			            	  // 'details' field of the CATStory Object. 
		    				  // Create a new CATStory object (4 columns: ID DETAILS DESCRIPTION LINK)
		    				  catstory = new CATStory(i, details_str, description_str, lat, lng, link_str);
		              
		    				  // INSERT CATStory Object into the Database
		    				  DA_App.catStoryProvider.insert(catstory);
		              	              
		    				  
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
      			return catstoryCursor;  	// Return the catstoryCursor
		      
		    } // END doInBackground ...
		    
		}// END Class GetCATStory ...



		//// MANAGED Dialog Infrastructure **************************************************************
		
			
		// onCreateDialog:
		// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
		@Override
		protected Dialog onCreateDialog(int id) {
			 switch (id) {
	         	case GET_CATS_DIALOG_ID: {     	 
	         		// Progression Dialog used when downloading XML data from online source.
	        	 	return createGetCsDialog();
	         		}
	         	case INFO_CATSTORY_DIALOG_ID: {
	        	 	// Selection Dialog displayed when an individual catstory object has been selected 
	        	 	return createSelectedCsDialog();
	         		}
			 	}
			 return null;
		}// END onCreateDialog 
		
		
		
		/** createGetCsDialog:
		 * 
		 * 	Creates the "Please Wait while downloading dialog object, canceled in 
	   	 * 	'onPostExecute' method of 'GetCATStory' class.
		 */
		private Dialog createGetCsDialog() {
	        ProgressDialog dialog = new ProgressDialog(this);
	        // Set the parameters of the dialog:
	        dialog.setMessage(this.getString(R.string.progressDialogue));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog; 
		}
		
		
		
		/** createSelectedCsDialog:
		 * 
		 * Creates and populates the Dialog screen responsible for displaying individual CATStory 
		 * details and a live link to it's web page. 
		 */
		private Dialog createSelectedCsDialog() {
			
			// Set up UI
			LayoutInflater layinflt = LayoutInflater.from(CATStoryActivity.this);
			View catstoryDetailsView = layinflt.inflate(R.layout.catstory_details, null);
			
			// Dialog Builder Stuff
			AlertDialog.Builder catstoryDialog = new AlertDialog.Builder(CATStoryActivity.this);
			catstoryDialog.setView(catstoryDetailsView);
			
			return catstoryDialog.create();			// Invokes onPrepareDialog before display ...
		}
		
		
		
		// onPrepareDialog:
		// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
	    @Override
	    public void onPrepareDialog(int id, Dialog dialog) {
	    	switch(id) {
				case (GET_CATS_DIALOG_ID) :
					 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_CATS_DIALOG_ID'd " ); }
					break;
	    		case (INFO_CATSTORY_DIALOG_ID) :
	    			prepareSelectedCsDialog(dialog);		// Prepare the dialog to display selected catstory
	    			break;
	    		}
	    	}// END onPrepareDialog 
		
	    
	    
	    /** prepareSelectedCsDialog:
	     * 	Prepares the dialog for displaying the Selected Quake information 
	     */
	    private void prepareSelectedCsDialog(Dialog dialog) {
	    	
	    	AlertDialog selectedcatstoryDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
	    	
	    	// Infrastructure NOTES:
			// 'selectedCatstoryId' is instantiated in the 'setOnItemClickListener' method is 
			// an index to the cursor, corresponding to the list item just selected. 'catstoryDBCursor' is
	    	// populated via the DB query call made to initially populate the screen. 
	    		    	
	    	catstoryDBCursor.moveToPosition(selectedCatstoryId);
			
			// Grab CATStory field values from 'catstoryDBCursor' object created from DB Query.
			
	    	// STRINGS **********************************
	    	final String _id 		 = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("_id"));
	    	final String details 	 = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("details"));
	    		  String description = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("description"));
			final String link 		 = catstoryDBCursor.getString(catstoryDBCursor.getColumnIndex("link"));
			description = description + " ... [Read More Online]";			// To counter funky formatting in the XML
			
//			// FLOATS **********************************
//			final float lat = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lat"));
//			final float lng = globalalertsDBCursor.getFloat(globalalertsDBCursor.getColumnIndex("geopoint_lng"));
//
//	    	// DATE Parsing (Relative time) *********************************
//	    	long timestamp = globalalertsDBCursor.getLong(globalalertsDBCursor.getColumnIndex("date"));
//	    	CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);

	    	
	    	// WebView Stuff **********************************
			WebView myWebView = (WebView) dialog.findViewById(R.id.DialogCsWebView);
			WebSettings webSettings = myWebView.getSettings();			// Get WebView Settings, set font size
			webSettings.setDefaultFontSize(13);
			myWebView.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
			
			
	    } // END prepareSelectedCsDialog

	    
	
}// END CLASS CATStoryActivity









