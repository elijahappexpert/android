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

/**TopStoriesActivity:
 * 
 * This class is the Top Stories Application itself. It sets up the UI, 
 * fetches the online XML data, calls TopStoryProvider methods to insert it 
 * into an SQLite DB, populates the Activity screen, constructs the dialog  
 * for selected TopStory.
 *   
 */
public class TopStoriesActivity extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = TopStoriesActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	   
		////  TopStories Stuff
		ListView topstories_listView;				 	// Blank ListView Object
		TopStories topstories;					 		// Blank TopStories object 
		int selectedTopStoriesId;					 	// TopStories object id
		MatrixCursor topstoriesCursor;			 		// Blank MatrixCursor Object (for debug)
		Cursor topstoriesDBCursor;				 		// Blank Cursor Object 
		SimpleCursorAdapter topstories_cursorAdapter; 	// Blank SimpleCursorAdapter Object 
		GetTopStories getTSTask;				 		// Blank TopStories [AsyncTask] Object   
		String[] topstories_columns;				    // Blank String Array {'topstories_columns'} (for debug)
		String[] topstoriesDB_Columns;					// Blank String Array {selected 'topstoriesDB_Columns'}
		int[] db_to;							 		// Blank Integer Array {R.TextView's from 'row_topstories_multi.xml'}
		String topstoriesFeed;							// Blank 'R.string.topstories_feed' Resource variable, holds XML URL
		
		//// DIALOG Stuff
		View topstoryDetailsView;					 	// Blank View Object
		
		static final int GET_TS_DIALOG_ID = 49;	 	 		  // Dialog ID for the GetTopStories Dialog (arbitrary)
		static final int INFO_TOPSTORIES_DIALOG_ID = 94; 	  // Dialog ID for the InfoTopStories Dialog (arbitrary)
		

		//// LIFE CYCLE Overrides  *****************************************************
		   
		// OnCreate
		@Override  
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
																						// Set up UI 
			setContentView(R.layout.topstories);    									// Inflate View
			topstories_listView = (ListView)this.findViewById(R.id.ts_listView);   		// Get ListView Handle
			DA_App.searchParameters = "Emergency and Disaster Information";		// Initialize Global Search Parameters
			displayTopStoriesScreen();	  										// Displays Catastrophe Story Activity Screen 
		}// END onCreate ... 
				
		
		// onResume:
		// Calls 'displayTopStoriesScreen' to refresh the screen any time it becomes active again
		  @Override
		protected void onResume() {
			super.onResume();
			DA_App.searchParameters = "Emergency and Disaster Information";
			displayTopStoriesScreen();	  		// Displays TopStories Activity Screen 
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
			topstoriesDBCursor.close();
			stopManagingCursor(topstoriesDBCursor);
			DA_App.topStoriesProvider.close();
			DA_App.searchParameters = null;
		}


		// onStop:
		// Close the Database and stop managing the Cursor 
		@Override
		protected void onStop() {
			super.onStop();
			topstoriesDBCursor.close();
			stopManagingCursor(topstoriesDBCursor);
			DA_App.topStoriesProvider.close();
			DA_App.searchParameters = null;
		}

		
		//// UTILITY METHODS  *****************************************************
		
		
		
		// onClickRefreshTopStories:
		// Called from Dashboard Title Bar GET [DATAELEMENT]
		//
		public void onClickRefreshTopStories (View v) {
			getOnlineData();
		}
		
		
		/** displayTopStoriesScreen:
		 * This method prepares the String Array ('topstoriesDB_Columns')and the Integer Array ('db_to') 
		 * as input for the construction of the SimpleCursorAdapter  ('topstories_cursorAdapter'). It also
		 * queries the DB for ALL of the Quake records and returns them in a Cursor ('topstoriesDBCursor').
		 * Once the Adapter is constructed the ListView's ('topstories_listView') Adapter is set populating
		 * the screen w/the TopStories information. 
		 */
		private void displayTopStoriesScreen() {
			
			//// ********** QUICKACTION PATTERN: ActionItem Setup 
			
	        ActionItem websiteAction = new ActionItem();
	        websiteAction.setTitle("WebSite");
	        websiteAction.setIcon(getResources().getDrawable(R.drawable.qa_website));

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
						topstoriesDBCursor.moveToPosition(selectedTopStoriesId);
						final String link = topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("link"));
						Uri uri = Uri.parse(link);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
						startActivity(intent);
					} 
					else if (pos == 1) { 											// **** Info Dialog BTN ****
						showDialog(INFO_TOPSTORIES_DIALOG_ID);
					}	
					else if (pos == 2) { 											// **** Search Dialog BTN ****
						  topstoriesDBCursor.moveToPosition(selectedTopStoriesId);
						  final String details = topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("details"));
						  DA_App.searchParameters = details ; 
				    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
				    	  startActivity(search);
					}
					else if (pos == 3) { 											// **** Share Dialog BTN ****
						// share text with 'any' APPS on your Android device that accept text Intents
						topstoriesDBCursor.moveToPosition(selectedTopStoriesId);
						final String details = topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("details"));
						final String link = topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("link"));
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
			topstories_listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
	        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
					selectedTopStoriesId = _index;		// Get ID of TopStories Cursor Object for later retrieval using
														// topstoriesDBCursor.moveToPosition(selectedTopStoriesId) in 
														// 'prepareSelectedTsDialog' Dialog method 
										
					//// ******** Set Activity Search Parameters:
			        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
			        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
			        // extra parameter to a Google web search when someone clicks on the looking glass 
			        // search icon on this activities Dashboard title bar. In this case it'll be the details  
					// of the 'selected' earthquake. 
					
					topstoriesDBCursor.moveToPosition(selectedTopStoriesId);
					final String details = topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("details"));
					DA_App.searchParameters = details ; 
					
					mQuickAction.show(_v);			// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
	    		}
	    	});
			
			// 'topstoriesDB_Columns': A String Array of the COLUMN (Field) names from our TopStories Database 
			String[] topstoriesDB_Columns = {TopStoriesProvider.KEY_ID, TopStoriesProvider.KEY_DATE, TopStoriesProvider.KEY_DETAILS, 
											TopStoriesProvider.KEY_DESCRIPTION, TopStoriesProvider.KEY_LINK };
		
			// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_topstories_multi.xml' 
			int[] db_to = new int[] {R.id.textViewTsID, R.id.textViewTsDate, R.id.textViewTsDetails,  R.id.textViewTsDescription, R.id.textViewTsLink };
			
			// Query *DB*, capture the result in Standard Cursor Object
			topstoriesDBCursor = DA_App.topStoriesProvider.query();	
			
			// Manage the Life Cycle of the Standard Cursor
			startManagingCursor(topstoriesDBCursor);   
			
			// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
			topstories_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_topstories_multi, topstoriesDBCursor, topstoriesDB_Columns, db_to);
			topstories_cursorAdapter.setViewBinder(VIEW_BINDER);			// Use VIEW_BINDER to convert System Data/Time to Relative 
			topstories_listView.setAdapter(topstories_cursorAdapter);		// Display the view ...

		}// END displayTopStoriesScreen ...
		
		
		
		
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
			
			getTSTask = new GetTopStories(this);    		// Instantiate an AsynchTask GetTopStories object
			getTSTask.execute();							// Execute AsynchTask(s), URL read in by resource ID 
															// embedded in 'load_online_data' load_online_data ...
		}// END getOnlineData ...
		
		
		
		
		/** GetTopStories: 
		 * 	[doInBackground -> load_online_data] => (insert TS_DB)
		 * 	This method fetches the online TopStories data using AsyncTask as a vehicle
		 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
		 * 	and a SAX parser to interrogate the XML DOM object.
		 */	
		private class GetTopStories extends AsyncTask<Void, Void, MatrixCursor> {
			
			private Context mContext;
			
			GetTopStories(Context context) {        // Constructor 
				Context mContext = context;
			}
		
			
			// onPreExecute
			@Override
			protected void onPreExecute() {
				//super.onPreExecute();
				
				// Delete the Current DB of TopStories due to our TYPE of insert statement.
				DA_App.topStoriesProvider.delete();
				
				// START Process Dialog widget ....
				showDialog(GET_TS_DIALOG_ID);
			}
		
			
			// onPostExecute
		    @Override
		    protected void onPostExecute(MatrixCursor result) {
				super.onPostExecute(result);        
				
				// STOP Process Dialog widget ....
				dismissDialog(GET_TS_DIALOG_ID);
				
				// Displays (refresh) Earthquakes Activity Screen
				displayTopStoriesScreen();
		    }

		    
		    // doInBackground
			@Override
		    protected  MatrixCursor doInBackground(Void... params) {
				return load_online_data();   	// Do the actual AsyncTask work ....
			}


			/** load_online_data:
			 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
			 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
			 * 	object (TopStories) and then puts that new object in the MatrixCursor object for later display.
			 * 	It also inserts that TopStories object into our database for later retrieval. 
			 * 	@param VOID
			 * 	@return MatrixCUrsor
			 */
		    private MatrixCursor load_online_data(Void... params) {
		    	
		      // Get the XML
		      URL url;
		      try { 
		    	  
		    	  // HTTP Connection Setup ********************************
		    	  //  
		    	  String quakeFeed = TopStoriesActivity.this.getString(R.string.topstory_feed); // <= Change this to change feed URL
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
		    		  
		    		  // The TopStories XML Data Source looks like this:
		    		  // **********************************************
//		    		  <entry>
//		    				<title type="text"> *TITLE* </title>
//		    				<link *LINK* />				<== link INSIDE of TAG ???
//		    				<updated> *DATE* </updated>
//		    				<summary type="html"> *SUMMARY*</summary>
//		    				<content type="html"> *CDATA* </content>    <== NO FirstChilds, Just Text.
//		    				<id> *ID* </id>
//		    				<feedburner:origLink> *FEEDBURNERLINK* </feedburner:origLink>
//		    		</entry>
		    		  
		    		// This application only uses: ['entry', 'title', 'updated', 'summary', 'content', 'link']]

		    		  
		    		  NodeList nl = docEle.getElementsByTagName("entry");
		          	
		          		if (nl != null && nl.getLength() > 0) {					// Cycle through the ALL the Nodes
		          			for (int i = 0 ; i < nl.getLength(); i++) {			// Interrogate EACH node						
			            		
			            		// Element Level Parsing  ********************************
			            		//
		          				Element item 	  = (Element)nl.item(i);		
		          				Element title_e   = (Element)item.getElementsByTagName("title").item(0);			   
		          				Element updated_e = (Element)item.getElementsByTagName("updated").item(0);
		          				Element summary_e = (Element)item.getElementsByTagName("summary").item(0);
		          				Element content_e = (Element)item.getElementsByTagName("content").item(0);
		          				Element link_e 	  = (Element)item.getElementsByTagName("link").item(0);				     

			            		// String Level Parsing  ********************************
			            		//
		          				String title_str 	  	= title_e.getFirstChild().getNodeValue();							
		          				String description_str 	= summary_e.getFirstChild().getNodeValue();				
		          				//String cdata_str 		= content_e.getFirstChild().getTextContent();
		          				String cdata_str 		= content_e.getTextContent();    // Data contains HTML so we do this...
		          				String link_str 		= link_e.getAttribute("href");
		          				String updated_str		= updated_e.getFirstChild().getNodeValue();

		                   		///// Supplemental Parsing 
			            				          				
			            		// DATE Processing ********************************
			            		// XML Date Format => <updated>2011-08-22T17:39:38Z</updated>
			            		// Format: ("yyyy-MM-dd'T'hh:mm:ss'Z'")
			            		
			            		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
			            		Date date = new GregorianCalendar(0,0,0).getTime();								// Blank Date Object
			            		try {  														// Populate Date Object w/Parsed Date String
			            			date = sdf.parse(updated_str);
			            		} catch (ParseException e) {
			            			e.printStackTrace();
			            		}
			            		
			            		// TopStories stores the TITLE in it's DETAILS field so we pass 'title_str' here to be received in the
			            		// 'details' field of the TopStories Object. 
		          				// Create a new TopStories object (4 columns: ID DATE DETAILS DESCRIPTION CDATA LINK)
		          				topstories = new TopStories(i, date, title_str, description_str, cdata_str, link_str);
		              
		          				
		          				// DATABASE INSERT ********************************
		          				DA_App.topStoriesProvider.insert(topstories);
		              	              
		              
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
		      		 return topstoriesCursor;  	// Return the TopStoriesCursor
		      
		    } // END doInBackground ...
		    
		}// END Class GetTopStories ...



		//// MANAGED Dialog Infrastructure **************************************************************
		
		
		// onCreateDialog:
		// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
		@Override
		protected Dialog onCreateDialog(int id) {
			 switch (id) {
	         	case GET_TS_DIALOG_ID: {     	 
	         		// Progression Dialog used when downloading XML data from online source.
	        	 	return createGetTsDialog();
	         		}
	         	case INFO_TOPSTORIES_DIALOG_ID: {
	        	 	// Selection Dialog displayed when an individual quake object has been selected 
	        	 	return createSelectedTsDialog();
	         		}
			 	}
			 return null;
		}// END onCreateDialog 
		
		
		/** createGetTsDialog:
		 * 
		 * 	Creates the "Please Wait while downloading dialog object, canceled in 
	   	 * 	'onPostExecute' method of 'GetENHIncidents' class.
		 */
		private Dialog createGetTsDialog() {
	        ProgressDialog dialog = new ProgressDialog(this);
	        // Set the parameters of the dialog:
	        dialog.setMessage(this.getString(R.string.progressDialogue));
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(true);
	        return dialog; 
		}
		
		
		/** createSelectedTsDialog:
		 * 
		 * Creates and populates the Dialog screen responsible for displaying individual Earthquake 
		 * details and a live link to it's web page. 
		 */
		private Dialog createSelectedTsDialog() {

			// Set up UI
			LayoutInflater layinflt = LayoutInflater.from(TopStoriesActivity.this);
			View topstoriesDetailsView = layinflt.inflate(R.layout.topstories_details, null);

			// Dialog Builder Stuff
			AlertDialog.Builder topstoriesDialog = new AlertDialog.Builder(TopStoriesActivity.this);
			topstoriesDialog.setView(topstoriesDetailsView);
			
			return topstoriesDialog.create();			// Invokes onPrepareDialog before display ...
		}
		
		
		// onPrepareDialog:
		// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
	    @Override
	    public void onPrepareDialog(int id, Dialog dialog) {
	    	switch(id) {
				case (GET_TS_DIALOG_ID) :
					 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_TS_DIALOG_ID'd " ); }
					break;
	    		case (INFO_TOPSTORIES_DIALOG_ID) :
	    			prepareSelectedTSDialog(dialog);		// Prepare the dialog to display selected quakes
	    			break;
	    		}
	    	}// END onPrepareDialog 
	    
	    
	    
	    /** prepareSelectedTSDialog:
	     * 	Prepares the dialog for displaying the Selected TopStory information 
	     */
	    private void prepareSelectedTSDialog(Dialog dialog) {
	    	
	    	AlertDialog selectedTopStoriesDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
	    	
	    	// Infrastructure NOTES:
			// 'selectedeTopStoriesId' is instantiated in the 'setOnItemClickListener' method is 
			// an index to the cursor, corresponding to the list item just selected. 'topstoriesDBCursor' is
	    	// populated via the DB query call made to initially populate the screen. 
	    	
	    	topstoriesDBCursor.moveToPosition(selectedTopStoriesId);
			
			// Grab key  field values from 'ebhincidentDBCursor' object created from DB Query.
	    	
			// STRINGS **********************************
			final String link 	= topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("link"));
			final String _id 	= topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("_id"));
			String cdata 		= topstoriesDBCursor.getString(topstoriesDBCursor.getColumnIndex("cdata"));
						
	    	// DATE Parsing (Relative time) *********************************
	    	long timestamp = topstoriesDBCursor.getLong(topstoriesDBCursor.getColumnIndex("date"));
	    	CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);
	    	
	    	
	    	// WebView Processing **********************************
			WebView myWebView = (WebView) dialog.findViewById(R.id.DialogTsWebView);
			WebSettings webSettings = myWebView.getSettings();			// Get WebView Settings, set font size
			webSettings.setDefaultFontSize(13);
			myWebView.loadDataWithBaseURL(null, cdata, "text/html", "UTF-8", null);
			
	    	
	    } // END prepareSelectedEBHIDialog
	    
	    
		
		
	
}// END Class TopStory Activity ....














