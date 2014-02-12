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

/**AirCraftIncidentActivity:
 * 
 * This class is the AirCraftIncidents Application itself. It sets up the UI, 
 * fetches the online XML data, calls AirCraftIncidentsProvider methods to insert it 
 * into an SQLite DB, populates the Activity screen, constructs the dialog  
 * for selected AirCraftIncident.
 *   
 */
public class AirCraftIncidentsActivity extends BaseActivity {

	
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = AirCraftIncidentsActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	   
		////  TopStories Stuff
		ListView aircraftincidents_listView;				 	// Blank ListView Object
		AirCraftIncidents aircraftincidents;					// Blank AirCraftIncidents object 
		int selectedAirCraftIncidentsId;					 	// AirCraftIncidents object id
		MatrixCursor aircraftincidentsCursor;			 		// Blank MatrixCursor Object (for debug)
		Cursor aircraftincidentsDBCursor;				 		// Blank Cursor Object 
		SimpleCursorAdapter aircraftincidents_cursorAdapter; 	// Blank SimpleCursorAdapter Object 
		GetAirCraftIncidents getACITask;				 		// Blank AirCraftIncidents [AsyncTask] Object   
		String[] aircraftincidents_columns;				    	// Blank String Array {'aircraftincidents_columns'} (for debug)
		String[] aircraftincidentsDB_Columns;					// Blank String Array {selected 'aircraftincidentsDB_Columns'}
		int[] db_to;							 				// Blank Integer Array {R.TextView's from 'row_aircraftincidents_multi.xml'}
		String aircraftincidentsFeed;							// Blank 'R.string.aircraftincidents_feed' Resource variable, holds XML URL
		
		//// DIALOG Stuff
		View aircraftincidentsDetailsView;					 	// Blank View Object
		
		static final int GET_ACI_DIALOG_ID = 49;	 	 			// Dialog ID for the GetEarthquakes Dialog (arbitrary)
		static final int INFO_AIRCRAFTINCIDENTS_DIALOG_ID = 94; 	// Dialog ID for the SelectEarthquakes Dialog (arbitrary)
		
		//// LIFE CYCLE Overrides  *****************************************************
		   
		// OnCreate
		@Override  
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
																							// Set up UI 
			setContentView(R.layout.aircraftincidents);    									// Inflate View
			aircraftincidents_listView = (ListView)this.findViewById(R.id.aci_listView);   	// Get ListView Handle
			DA_App.searchParameters = "aircraft incidents ";		// Initialize Global Search Parameters
			displayAirCraftIncidentsScreen();	  					// Displays Catastrophe Story Activity Screen 
		}// END onCreate ... 
		
		
		// onResume:
		// Calls 'displayTopStoriesScreen' to refresh the screen any time it becomes active again
		  @Override
		protected void onResume() {
			super.onResume();
			DA_App.searchParameters = "aircraft incidents ";
			displayAirCraftIncidentsScreen();	  		// Displays TopStories Activity Screen 
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
			aircraftincidentsDBCursor.close();
			stopManagingCursor(aircraftincidentsDBCursor);
			DA_App.airCraftIncidentsProvider.close();
			DA_App.searchParameters = null;
		}


		// onStop:
		// Close the Database and stop managing the Cursor 
		@Override
		protected void onStop() {
			super.onStop();
			aircraftincidentsDBCursor.close();
			stopManagingCursor(aircraftincidentsDBCursor);
			DA_App.airCraftIncidentsProvider.close();
			DA_App.searchParameters = null;
		}

		
		//// UTILITY METHODS  *****************************************************
		
		
		// onClickRefreshAirCraftIncidents: 
		// Called from Dashboard Title Bar GET [DATAELEMENT]
		//
		public void onClickRefreshAirCraftIncidents (View v)	{
			getOnlineData();
		}
		
		
		
		/** displayAirCraftIncidentsScreen:
		 * This method prepares the String Array ('topstoriesDB_Columns')and the Integer Array ('db_to') 
		 * as input for the construction of the SimpleCursorAdapter  ('topstories_cursorAdapter'). It also
		 * queries the DB for ALL of the Quake records and returns them in a Cursor ('topstoriesDBCursor').
		 * Once the Adapter is constructed the ListView's ('topstories_listView') Adapter is set populating
		 * the screen w/the Quake information. 
		 */
		private void displayAirCraftIncidentsScreen() {
			
			
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
						aircraftincidentsDBCursor.moveToPosition(selectedAirCraftIncidentsId);
						final String link = aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("link"));
						Uri uri = Uri.parse(link);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);				
						startActivity(intent);
					} 
					else if (pos == 1) { 											// **** Info Dialog BTN ****
						showDialog(INFO_AIRCRAFTINCIDENTS_DIALOG_ID);
					}
					else if (pos == 2) { 											// **** Search Dialog BTN ****
						aircraftincidentsDBCursor.moveToPosition(selectedAirCraftIncidentsId);
						  final String details = aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("details"));
						  DA_App.searchParameters = details ; 
				    	  Intent search = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				    	  search.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
				    	  startActivity(search);
					}
					else if (pos == 3) { 											// **** Share Dialog BTN ****
						// **** share text with 'any' APPS on your Android device that accept text Intents ****
						aircraftincidentsDBCursor.moveToPosition(selectedAirCraftIncidentsId);
						final String details = aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("details"));
						final String link = aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("link"));
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
			aircraftincidents_listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
	        	public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
					selectedAirCraftIncidentsId = _index;		// Get ID of TopStories Cursor Object for later retrieval using
																// topstoriesDBCursor.moveToPosition(selectedTopStoriesId) in 
																// 'prepareSelectedTsDialog' Dialog method 
					
					//// ******** Set Activity Search Parameters:
			        // Set the Activity Search Parameters to be passed into the onClickSearch method in the 
			        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
			        // extra parameter to a Google web search when someone clicks on the looking glass 
			        // search icon on this activities Dashboard title bar. In this case it'll be the details  
					// of the 'selected' earthquake. 
					
					aircraftincidentsDBCursor.moveToPosition(selectedAirCraftIncidentsId);
					final String details = aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("details"));
					DA_App.searchParameters = details ; 
					
					mQuickAction.show(_v);			// ********** QUICKACTION PATTERN: Shows the QuickAction Widget
					
	    		}
	    	});
			
			// 'aircraftincidentsDB_Columns': A String Array of the COLUMN (Field) names from our AirCraftIncidents Database 
			String[] aircraftincidentsDB_Columns = {AirCraftIncidentsProvider.KEY_ID, AirCraftIncidentsProvider.KEY_DATE, AirCraftIncidentsProvider.KEY_DETAILS, 
													AirCraftIncidentsProvider.KEY_DESCRIPTION, AirCraftIncidentsProvider.KEY_LINK };
		
			// 'db_to': An Integer Array of 'some' Resource ID's of the TextViews created in the file 'row_topstories_multi.xml' 
			int[] db_to = new int[] {R.id.textViewAciID, R.id.textViewAciDate, R.id.textViewAciDetails,  R.id.textViewAciDescription, R.id.textViewAciLink };
			
			// Query *DB*, capture the result in Standard Cursor Object
			aircraftincidentsDBCursor = DA_App.airCraftIncidentsProvider.query();	
			
			// Manage the Life Cycle of the Standard Cursor
			startManagingCursor(aircraftincidentsDBCursor);   
			
			// Create the SimpleCursorAdapter, the View_Binder, and set the adapter
			aircraftincidents_cursorAdapter = new SimpleCursorAdapter(this, R.layout.row_aircraftincidents_multi, aircraftincidentsDBCursor, aircraftincidentsDB_Columns, db_to);
			aircraftincidents_cursorAdapter.setViewBinder(VIEW_BINDER);					// Use VIEW_BINDER to convert System Data/Time to Relative 
			aircraftincidents_listView.setAdapter(aircraftincidents_cursorAdapter);		// Display the view ...

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
				
				getACITask = new GetAirCraftIncidents(this);    	// Instantiate an AsynchTask GetTopStories object
				getACITask.execute();								// Execute AsynchTask(s), URL read in by resource ID 
																	// embedded in 'load_online_data' load_online_data ...
			}// END getOnlineData ...
			
			
			/** GetAirCraftIncidents: 
			 * 	[doInBackground -> load_online_data] => (insert TS_DB)
			 * 	This method fetches the online AirCraftIncidents data using AsyncTask as a vehicle
			 * 	to free up the UI Thread, an HttpURLConnection to establish connectivity, 
			 * 	and a SAX parser to interrogate the XML DOM object.
			 */	
			private class GetAirCraftIncidents extends AsyncTask<Void, Void, MatrixCursor> {
				
				private Context mContext;
				
				GetAirCraftIncidents(Context context) {        // Constructor 
					Context mContext = context;
				}
			
				
				// onPreExecute
				@Override
				protected void onPreExecute() {
					//super.onPreExecute();
					
					// Delete the Current DB of AirCraftIncidents due to our TYPE of insert statement.
					DA_App.airCraftIncidentsProvider.delete();
					
					// START Process Dialog widget ....
					showDialog(GET_ACI_DIALOG_ID);
				}
			
				
				// onPostExecute
			    @Override
			    protected void onPostExecute(MatrixCursor result) {
					super.onPostExecute(result);        
					
					// STOP Process Dialog widget ....
					dismissDialog(GET_ACI_DIALOG_ID);
					
					// Displays (refresh) Earthquakes Activity Screen
					displayAirCraftIncidentsScreen();
			    }

			    
			    // doInBackground
				@Override
			    protected  MatrixCursor doInBackground(Void... params) {
					return load_online_data();   	// Do the actual AsyncTask work ....
				}


				/** load_online_data:
				 * 	This method takes the URL for our online XML data feed, parses it, establishes an HTTP
				 * 	connection, retrieves the data, parses the key components of the DOM, creates a new data 
				 * 	object (AirCraftIncidents) and then puts that new object in the MatrixCursor object for later display.
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
			    	  String quakeFeed = AirCraftIncidentsActivity.this.getString(R.string.airplane_feed); // <= Change this to change feed URL
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
			    		  
			    		// The AirCraftIncidents XML Data Source looks like this:
			    		// ******************************************************  
			    		// <item>
			    		//	<title>*TITLE*</title>
			    		//	<link>*LINK*</link>
			    		//	<description><![CDATA[*DESCRIPTION*]]></description>
			    		//	<content:encoded><![*CDATA*]]></content:encoded>
			    		//	<category'>*CATEGORY*</category>
			    		//	<dc:creator>*CREATOR*</dc:creator>
			    		//	<pubDate>*DATE*</pubDate>
			    		//	<guid isPermaLink="true">*PERMALINK*</guid>
			    		// </item>
			    		  
			    		// This application only uses: ['entry', 'title', 'link', 'description', 'link']]

			    		    
			    		  NodeList nl = docEle.getElementsByTagName("item");
			          	
			          		if (nl != null && nl.getLength() > 0) {					// Cycle through the ALL the Nodes
			          			for (int i = 0 ; i < nl.getLength(); i++) {			// Interrogate EACH node						
				            		
				            		// Element Level Parsing  ********************************
				            		//
			          				Element item 	  	  = (Element)nl.item(i);		
			          				Element title_e   	  = (Element)item.getElementsByTagName("title").item(0);
			          				Element description_e = (Element)item.getElementsByTagName("content:encoded").item(0);
			          				Element date_e 		  = (Element)item.getElementsByTagName("pubDate").item(0);
			          				Element link_e 	  	  = (Element)item.getElementsByTagName("link").item(0);				     

				            		// String Level Parsing  ********************************
				            		//
			          				String title_str 	  	= title_e.getFirstChild().getNodeValue();			 
			          				String link_str 	  	= link_e.getFirstChild().getNodeValue();
			          				String date_str			= date_e.getFirstChild().getNodeValue();
			          				//String description_str = description_e.getFirstChild().getNodeValue();	 // Normal Usage ...
			          				
			          				// XML Data may contain malformed HTML so we have to do this... (WordPress oye!)
			          				String description_str = null;
									try {
										description_str = description_e.getTextContent();  // <== Data contains HTML so we do this...
									} catch (DOMException e1) {
											description_str = "Sorry, the App received BAD XML DATA for this record ....";
										e1.printStackTrace();
									} 

			                   		///// Supplemental Parsing 
			          				
				            		// DATE Processing ********************************
				            		// XML Date Format => <pubDate>Wed, 24 Aug 2011 03:00:24 -0500</pubDate>
			        				// FORMAT: ("EEE, dd MMM yyyy hh:mm:ss ZZZZZ")
			        				
			        				SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss ZZZZZ");
				            		Date date = new GregorianCalendar(0,0,0).getTime();								// Blank Date Object
				            		try {  														// Populate Date Object w/Parsed Date String
				            			date = sdf.parse(date_str);
				            		} catch (ParseException e) {
				            			e.printStackTrace();
				            		}
				            		
				            		// AirCraftIncidents stores the TITLE in it's DETAILS field so we pass 'title_str' here to be received in the
				            		// 'details' field of the TopStories Object. 
			          				// Create a new TopStories object (4 columns: ID DATE DETAILS DESCRIPTION CDATA LINK)
			          				aircraftincidents = new AirCraftIncidents(i, date, title_str, description_str, link_str);
			              
			          				
			          				// DATABASE INSERT ********************************
			          				DA_App.airCraftIncidentsProvider.insert(aircraftincidents);
			              	              
			              
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
			      		 return aircraftincidentsCursor;  	// Return the TopStoriesCursor
			      
			    } // END doInBackground ...
			    
			}// END Class GetAirCraftIncidents ...


			
		//// MANAGED Dialog Infrastructure **************************************************************
			
			
			// onCreateDialog:
			// Managed Dialog Infrastructure to manage CREATE and REUSE Activity Dialog processes  
			@Override
			protected Dialog onCreateDialog(int id) {
				 switch (id) {
		         	case GET_ACI_DIALOG_ID: {     	 
		         		// Progression Dialog used when downloading XML data from online source.
		        	 	return createGetAciDialog();
		         		}
		         	case INFO_AIRCRAFTINCIDENTS_DIALOG_ID: {
		        	 	// Selection Dialog displayed when an individual quake object has been selected 
		        	 	return createSelectedAciDialog();
		         		}
				 	}
				 return null;
			}// END onCreateDialog 
			
			
			/** createGetAciDialog:
			 * 
			 * 	Creates the "Please Wait while downloading dialog object, canceled in 
		   	 * 	'onPostExecute' method of 'GetAirCraftIncidents' class.
			 */
			private Dialog createGetAciDialog() {
		        ProgressDialog dialog = new ProgressDialog(this);
		        // Set the parameters of the dialog:
		        dialog.setMessage(this.getString(R.string.progressDialogue));
		        dialog.setIndeterminate(true);
		        dialog.setCancelable(true);
		        return dialog; 
			}
			
			
			/** createSelectedAciDialog:
			 * 
			 * Creates and populates the Dialog screen responsible for displaying individual Earthquake 
			 * details and a live link to it's web page. 
			 */
			private Dialog createSelectedAciDialog() {

				// Set up UI
				LayoutInflater layinflt = LayoutInflater.from(AirCraftIncidentsActivity.this);
				View aircraftincidentsDetailsView = layinflt.inflate(R.layout.aircraftincidents_details, null);

				// Dialog Builder Stuff
				AlertDialog.Builder aircraftincidentsDialog = new AlertDialog.Builder(AirCraftIncidentsActivity.this);
				aircraftincidentsDialog.setView(aircraftincidentsDetailsView);
				
				return aircraftincidentsDialog.create();			// Invokes onPrepareDialog before display ...
			}
			
			
			// onPrepareDialog:
			// Managed Dialog Infrastructure to ALTER reusable dialogs 'each' time they are reused. 
		    @Override
		    public void onPrepareDialog(int id, Dialog dialog) {
		    	switch(id) {
					case (GET_ACI_DIALOG_ID) :
						 // if (debug) { Log.d(TAG, "in onPrepareDialog -> GET_TS_DIALOG_ID'd " ); }
						break;
		    		case (INFO_AIRCRAFTINCIDENTS_DIALOG_ID) :
		    			prepareSelectedACIDialog(dialog);		// Prepare the dialog to display selected quakes
		    			break;
		    		}
		    	}// END onPrepareDialog 
		    
		    
		    
		    /** prepareSelectedACIDialog:
		     * 	Prepares the dialog for displaying the Selected Quake information 
		     */
		    private void prepareSelectedACIDialog(Dialog dialog) {
		    	
		    	AlertDialog selectedAirCraftIncidentsDialog = (AlertDialog)dialog;   // Create Blank Dialog Object
		    	
		    	// Infrastructure NOTES:
				// 'selectedeAirCraftIncidentsId' is instantiated in the 'setOnItemClickListener' method is 
				// an index to the cursor, corresponding to the list item just selected. 'aircraftincidentsDBCursor' is
		    	// populated via the DB query call made to initially populate the screen. 
		    	
		    	aircraftincidentsDBCursor.moveToPosition(selectedAirCraftIncidentsId);
				
				// Grab key  field values from 'aircraftincidentsDBCursor' object created from DB Query.
		    	
				// STRINGS ...
				final String link 	= aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("link"));
				final String _id 	= aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("_id"));
				String description 	= aircraftincidentsDBCursor.getString(aircraftincidentsDBCursor.getColumnIndex("description"));
				

		    	// DATE Parsing (Relative time) *********************************
		    	long timestamp = aircraftincidentsDBCursor.getLong(aircraftincidentsDBCursor.getColumnIndex("date"));
		    	CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timestamp);
		    	
		    	// WebView Stuff **********************************
				WebView myWebView = (WebView) dialog.findViewById(R.id.DialogAciWebView);
				WebSettings webSettings = myWebView.getSettings();			// Get WebView Settings, set font size
				webSettings.setDefaultFontSize(13);
				myWebView.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
		    	
		    } // END prepareSelectedEBHIDialog
		    
		
		
		
}// END Class AirCraftIncidentActivity








