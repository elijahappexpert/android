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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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


/**HSIncidentActivity:
 * 
 * This class is the Homeland Security Incident Application itself. It sets up the UI, 
 * fetches the online XML data, calls HSIncidentProvider methods to insert it 
 * into an SQLite DB, populates the Activity screen, constructs the dialog  
 * for selected HSIncident.
 *   
 */
public class HSIncidentActivity extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = HSIncidentActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	   
		////  HSIncident Stuff
		ListView hsincident_listView;				 	// Blank ListView Object
		HSIncident hsincident;					 	 	// Blank HSIncident object 
		int selectedHSIncidentId;					 	// HSIncident object id
		MatrixCursor hsincidentCursor;			 	 	// Blank MatrixCursor Object (for debug)
		Cursor hsincidentDBCursor;				 	 	// Blank Cursor Object 
		SimpleCursorAdapter hsincident_cursorAdapter; 	// Blank SimpleCursorAdapter Object 
		GetHSIncident getHSITask;				 	 	// Blank HSIncident [AsyncTask] Object   
		String[] hsincident_columns;				 	// Blank String Array {'hsincident_columns'} (for debug)
		String[] hsincidentDB_Columns;				 	// Blank String Array {selected 'hsincidentDB_Columns'}
		int[] db_to;							 	 	// Blank Integer Array {R.TextView's from 'row_hsincident_multi.xml'}
		String hsincidentFeed;						 	// Blank 'R.string.hsincident_feed' Resource variable, holds XML URL
		
		//// DIALOG Stuff
		View hsincidentDetailsView;					 	// Blank View Object
		
		static final int GET_HSI_DIALOG_ID = 49;	 	 // Dialog ID for the GetEarthquakes Dialog (arbitrary)
		static final int INFO_HSINCIDENT_DIALOG_ID = 94; // Dialog ID for the SelectEarthquakes Dialog (arbitrary)
		
		
		//// LIFE CYCLE Overrides  *****************************************************
	   
		// OnCreate
		@Override  
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
																						// Set up UI 
			setContentView(R.layout.hsincident);    									// Inflate View
			hsincident_listView = (ListView)this.findViewById(R.id.hsi_listView);   	// Get ListView Handle
			DA_App.searchParameters = "homeland security incidents ";		// Initialize Global Search Parameters
			displayHSIncidentScreen();	  									// Displays Catastrophe Story Activity Screen 
		}// END onCreate ... 
			
		
		// onResume:
		// Calls 'displayHSIncidentScreen' to refresh the screen any time it becomes active again
		  @Override
		protected void onResume() {
			super.onResume();
			DA_App.searchParameters = "homeland security incidents ";
			displayHSIncidentScreen();	  		// Displays Earthquakes Activity Screen 
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
			hsincidentDBCursor.close();
			stopManagingCursor(hsincidentDBCursor);
			DA_App.hsIncidentProvider.close();
			DA_App.searchParameters = null;
		}


		// onStop:
		// Close the Database and stop managing the Cursor 
		@Override
		protected void onStop() {
			super.onStop();
			hsincidentDBCursor.close();
			stopManagingCursor(hsincidentDBCursor);
			DA_App.hsIncidentProvider.close();
			DA_App.searchParameters = null;
		}

		
		//// UTILITY METHODS  *****************************************************

		
		// onClickRefreshHSIncidents: 
		// Called from Dashboard Title Bar GET [DATAELEMENT]
		//
		public void onClickRefreshHSIncidents (View v)	{
			getOnlineData();
		}
		
		
		/** displayHSIncidentScreen:
		 * This method prepares the String Array ('hsincidentDB_Columns')and the Integer Array ('db_to') 
		 * as input for the construction of the SimpleCursorAdapter  ('hsincident_cursorAdapter'). It also
		 * queries the DB for ALL of the Quake records and returns them in a Cursor ('hsincidentDBCursor').
		 * Once the Adapter is constructed the ListView's ('hsincident_listView') Adapter is set populating
		 * the screen w/the Quake information. 
		 */
		private void displayHSIncidentScreen() {
			
			
			//// ********** QUICKACTION PATTERN: ActionItem Setup 
			
	        ActionItem websiteAction = new ActionItem();
	        websiteAction.setTitle("WebSite");
	        websiteAction.setIcon(getResources().getDrawable(R.drawable.qa_website));
			
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
			mQuickAction.addActionItem(infoAction);
			mQuickAction.addActionItem(searchAction);
			mQuickAction.addActionItem(shareAction);
			
			//// ********** QUICKACTION PATTERN: QuickAction Listener Setup
			
			mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
				@Override
				public void onItemClick(int pos) {
					
					if (pos == 0) { 												// **** Web Site BTN ****
						hsincidentDBCursor.moveToPosition(selectedHSIncidentId);
						final String link = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("link"));
						Uri uri = Uri.parse(link);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
						startActivity(intent);
					} 
					else if (pos == 1) { 											// **** Info Dialog BTN ****
						showDialog(INFO_HSINCIDENT_DIALOG_ID);
					}
					else if (pos == 2) { 											// **** Search Dialog BTN ****
						hsincidentDBCursor.moveToPosition(selectedHSIncidentId);
						  final String details = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("details"));
						  DA_App.searchParameters = details ; 
				    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
				    	  startActivity(search);
					}
					else if (pos == 3) { 											// **** Share Dialog BTN ****
						// **** share text with 'any' APPS on your Android device that accept text Intents ****
						hsincidentDBCursor.moveToPosition(selectedHSIncidentId);
						final String details = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("details"));
						final String link = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("link"));
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
			hsincident_listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
	        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
					selectedHSIncidentId = _index;		// Get ID of HSIncident Cursor Object for later retrieval using
														// hsincidentDBCursor.moveToPosition(selectedHsincidentId) in 
														// 'prepareSelectedHsiDialog' Dialog method 
					
					//// ******** Set Activity Search Parameters:
			        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
			        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
			        // extra parameter to a Google web search when someone clicks on the looking glass 
			        // search icon on this activities Dashboard title bar. In this case it'll be the details  
					// of the 'selected' earthquake. 
					
					hsincidentDBCursor.moveToPosition(selectedHSIncidentId);
					final String details = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("details"));
					DA_App.searchParameters = details ; 
					
					mQuickAction.show(_v);			// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
					
	    		}
	    	});
			
			// 'quakeDB_Columns': A String Array of the COLUMN (Field) names from our Quake Database 
			String[] hsincidentDB_Columns = {HSIncidentProvider.KEY_ID, HSIncidentProvider.KEY_DETAILS, 
											HSIncidentProvider.KEY_DESCRIPTION, HSIncidentProvider.KEY_LINK };
		
			// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_earthquakes_multi.xml' 
			int[] db_to = new int[] {R.id.textViewHsiID, R.id.textViewHsiDetails,  R.id.textViewHsiDescription, R.id.textViewHsiLink };
			
			// Query *DB*, capture the result in Standard Cursor Object
			hsincidentDBCursor = DA_App.hsIncidentProvider.query();	
			
			// Manage the Life Cycle of the Standard Cursor
			startManagingCursor(hsincidentDBCursor);   
			
			// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
			hsincident_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_hsincident_multi, hsincidentDBCursor, hsincidentDB_Columns, db_to);
			hsincident_listView.setAdapter(hsincident_cursorAdapter);		// Display the view ...

		}// END displayHSIncidentsScreen ...
		
		
		
		
		/** getOnlineData: 
		 *	 Utility function used to start the AsyncTask that fetches the XML data from 
		 * 	our online XML feed and store it into the database.
		 */
		private void getOnlineData() {	
			
			getHSITask = new GetHSIncident(this);    		// Instantiate an AsynchTask GetHSIncident object
			getHSITask.execute();							// Execute AsynchTask(s), URL read in by resource ID 
															// embedded in 'load_online_data' load_online_data ...
		}// END getOnlineData ...
		
		
		
		/** GetHSIncident: 
		 * 	[doInBackground -> load_online_data] => (insert EQ_DB)
		 * 	This method fetches the online HSIncident data using AsyncTask as a vehicle
		 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
		 * 	and a SAX parser to interrogate the XML DOM object.
		 */	
		private class GetHSIncident extends AsyncTask<Void, Void, MatrixCursor> {
			
			private Context mContext;
			
			GetHSIncident(Context context) {        // Constructor 
				Context mContext = context;
			}
		
			
			// onPreExecute
			@Override
			protected void onPreExecute() {
				//super.onPreExecute();
				
				// Delete the Current DB of Earthquakes due to our TYPE of insert statement.
				DA_App.hsIncidentProvider.delete();
				
				// START Process Dialog widget ....
				showDialog(GET_HSI_DIALOG_ID);
			}
		
			
			// onPostExecute
		    @Override
		    protected void onPostExecute(MatrixCursor result) {
				super.onPostExecute(result);        
				
				// STOP Process Dialog widget ....
				dismissDialog(GET_HSI_DIALOG_ID);
				
				// Displays (refresh) Earthquakes Activity Screen
				displayHSIncidentScreen();
		    }

		    
		    // doInBackground
			@Override
		    protected  MatrixCursor doInBackground(Void... params) {
				return load_online_data();   	// Do the actual AsyncTask work ....
			}


			/** load_online_data:
			 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
			 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
			 * 	object (HSIncident) and then puts that new object in the MatrixCursor object for later display.
			 * 	It also inserts that HSIncident object into our database for later retrieval. 
			 * 	@param VOID
			 * 	@return MatrixCUrsor
			 */
		    private MatrixCursor load_online_data(Void... params) {
		    	
		      // Get the XML
		      URL url;
		      try { 
		    	  
		    	  // HTTP Connection Setup ********************************
		    	  //  
		    	  String quakeFeed = HSIncidentActivity.this.getString(R.string.hsincident_feed); // <= Change this to change feed URL
		    	  url = new URL(quakeFeed);        
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
		    		  
		    		  // The HSIncidents XML Data Source looks like this:
		    		  // ************************************************
//		    		  - <item>
//		    		  		<title> *TITLE* </title> 
//		    		  		- <description>
//		    		  			- <![CDATA[ *CDATA* ]]> 
//		    		  		</description>
//		    		  		<link> *LINK* </link> 
//		    		  </item>
		    		  
		    		// This application only uses: ['item', 'title', 'description', 'link']]

		    		  
		    		  NodeList nl = docEle.getElementsByTagName("item");
		          	
		          		if (nl != null && nl.getLength() > 0) {					// Cycle through the ALL the Nodes
		          			for (int i = 0 ; i < nl.getLength(); i++) {			// Interrogate EACH node						
			            		
			            		// Element Level Parsing  ********************************
			            		//
		          				Element item 		  = (Element)nl.item(i);												  
		          				Element title_e 	  = (Element)item.getElementsByTagName("title").item(0);			  
		          				Element description_e = (Element)item.getElementsByTagName("description").item(0); 
		          				Element link_e 		  = (Element)item.getElementsByTagName("link").item(0);				     
		          				
			            		// String Level Parsing  ********************************
			            		//
		          				String details_str 	   = title_e.getFirstChild().getNodeValue();							
		          				String description_str = description_e.getFirstChild().getNodeValue();				
		          				String link_str		   = link_e.getFirstChild().getNodeValue();	

			            		///// Supplemental Parsing 
		          				
			            		// HSIncident stores the TITLE in it's DETAILS field so we pass 'title_str' here to be received in the
			            		// 'details' field of the HSIncident Object. 
		          				// Create a new HSIncident object (4 columns: ID DETAILS DESCRIPTION LINK)
		          				hsincident = new HSIncident(i, details_str, description_str, link_str);
		              
		          				// DATABASE INSERT ********************************
		          				DA_App.hsIncidentProvider.insert(hsincident);
		              	              
		              
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
		      		 return hsincidentCursor;  	// Return the hsincidentCursor
		      
		    } // END doInBackground ...
		    
		}// END Class GetHSIncident ...



		//// MANAGED Dialog Infrastructure **************************************************************
		
			
		// onCreateDialog:
		// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
		@Override
		protected Dialog onCreateDialog(int id) {
			 switch (id) {
	         	case GET_HSI_DIALOG_ID: {     	 
	         		// Progression Dialog used when downloading XML data from online source.
	        	 	return createGetHsiDialog();
	         		}
	         	case INFO_HSINCIDENT_DIALOG_ID: {
	        	 	// Selection Dialog displayed when an individual quake object has been selected 
	        	 	return createSelectedHsiDialog();
	         		}
			 	}
			 return null;
		}// END onCreateDialog 
		
		
		
		/** createGetHsiDialog:
		 * 
		 * 	Creates the "Please Wait while downloading dialog object, canceled in 
	   	 * 	'onPostExecute' method of 'GetHSIncident' class.
		 */
		private Dialog createGetHsiDialog() {
	        ProgressDialog dialog = new ProgressDialog(this);
	        // Set the parameters of the dialog:
	        dialog.setMessage(this.getString(R.string.progressDialogue));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog; 
		}
		
		
		
		/** createSelectedHsiDialog:
		 * 
		 * Creates and populates the Dialog screen responsible for displaying individual HSIncident 
		 * details and a live link to it's web page. 
		 */
		private Dialog createSelectedHsiDialog() {
			
			// Set up UI
			LayoutInflater layinflt = LayoutInflater.from(this);
			View hsincidentDetailsView = layinflt.inflate(R.layout.hsincident_details, null);
			
			// Dialog Builder Stuff
			AlertDialog.Builder hsincidentDialog = new AlertDialog.Builder(this);
			hsincidentDialog.setView(hsincidentDetailsView);
			
			return hsincidentDialog.create();			// Invokes onPrepareDialog before display ...
		}
		
		
		
		
		// onPrepareDialog:
		// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
	    @Override
	    public void onPrepareDialog(int id, Dialog dialog) {
	    	switch(id) {
				case (GET_HSI_DIALOG_ID) :
					 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_CATS_DIALOG_ID'd " ); }
					break;
	    		case (INFO_HSINCIDENT_DIALOG_ID) :
	    			prepareSelectedHsiDialog(dialog);		// Prepare the dialog to display selected quakes
	    			break;
	    		}
	    	}// END onPrepareDialog 
		
	    
	    
	    /** prepareSelectedHsiDialog:
	     * 	Prepares the dialog for displaying the Selected Quake information 
	     */
	    private void prepareSelectedHsiDialog(Dialog dialog) {
	    	
	    	AlertDialog selectedhsincidentDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
	    	
			// 'selectedQuakeId' is instantiated in the 'setOnItemClickListener' method is 
			// an index to the cursor, corresponding to the list item just selected.
	    	
	    	hsincidentDBCursor.moveToPosition(selectedHSIncidentId);
			
			// Grab QUAKE field values from 'quakeDBCursor' object created from DB Query.
			
	    	final String _id 		 = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("_id"));
	    	final String details 	 = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("details"));
	    	final String description = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("description"));
			final String link 		 = hsincidentDBCursor.getString(hsincidentDBCursor.getColumnIndex("link"));
			
	    	
	    	// WebView Stuff **********************************
			WebView myWebView = (WebView) dialog.findViewById(R.id.DialogHsiWebView);
			WebSettings webSettings = myWebView.getSettings();			// Get WebView Settings, set font size
			webSettings.setDefaultFontSize(13);
			myWebView.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
	    	
	    } // END prepareSelectedEQDialog
	
	
}// END Class HSIncidentActivity







