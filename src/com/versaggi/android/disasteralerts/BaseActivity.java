package com.versaggi.android.disasteralerts;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

/** BaseActivity
 * 
 * This class defines data and functionality that gets shared among those classes 
 * in this application which EXTEND this class. Of key importance is the instance 
 * of DA_App which is a handle to the global Application object. We also instantiate
 * a NotificationManager Object, and our MENU options infrastructure.
 *
 */
public class BaseActivity extends Activity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = BaseActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	DisasterEventsApplication DA_App ; 		// Create a blank instance of our Application to share data, 
											// methods, and the ability to respond to system changes, 
											// available to any class which extends this one ...
		
	//// LIFECYCLE Overrides   ******************************************************* 
	
	
	// WE PUT STUFF IN HERE THAT YOU WANT SHARED WITH EXTENDING CLASSES APPLICATION WIDE ...
	//
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (debug) { Log.d(TAG, "onCreate'd - Instanitating DA_App object ..."); }
		
		// Instantiate an LIVE instance of our Application to share data, methods, and
		// ability to respond to system changes through out the classes which inherit 
		// from this one ...
		//
		DA_App = ((DisasterEventsApplication) getApplication());
		
	}// END onCreate .... 
	
	
	// onResume:
	  @Override
	protected void onResume() {
		super.onResume();
		if (debug) { Log.d(TAG, "onResume'd: "); }
	}

	  
	// onPause:
	@Override
	protected void onPause() {
		super.onPause();
		if (debug) { Log.d(TAG, "onPause'd: "); }
	}
	
	// onDestroy:
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (debug) { Log.d(TAG, "onDestroy'd: "); }
	}


	// onStop
	@Override
	protected void onStop() {
		super.onStop();
		if (debug) { Log.d(TAG, "onStop'd:  "); }
	}
	
	
	
	///// MENU Options Stuff  **************************************************

	// Lazy initializer of our Menu ...
	// This gets called the FIRST time the user presses the MENU button,
	// then gets saved afterward.
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate from resource the file menu.xml, create the menu
		// and attached the menu item to it ...
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}// END ....
	
	
	// Called every time a user clicks on a menu item ...
	// This code defines the actions taken when a user clicks on a MENU Item ...
	// NOTE: We incorporate the use of Intent FLAGS: FLAG_ACTIVITY_REORDER_TO_FRONT,
	// which govern the behavior of intents IF they are called a 2nd and 3rd time so 
	// that we can be REUSE their instances rather than creating entirely new ones. 
	// 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		case R.id.itemGlobalAlerts:
			if (debug) { Log.d(TAG, "onClick'd on itemGlobalAlerts"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, GlobalAlertsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
			
		case R.id.itemTopStories: 
			if (debug) { Log.d(TAG, "onClick'd on TopStories"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, TopStoriesActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
			
		case R.id.itemEarthquakes:
			if (debug) { Log.d(TAG, "onClick'd on EarthQuakes"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, EarthquakeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;

		case R.id.itemVolcanos:
			if (debug) { Log.d(TAG, "onClick'd on Volcanos"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, VolcanoActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		
		case R.id.itemPollutionAlerts:
			if (debug) { Log.d(TAG, "onClick'd on PollutionAlerts"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, PollutionAlertsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;	
			
		case R.id.itemEBHIncidents:
			if (debug) { Log.d(TAG, "onClick'd on EBHIncidents"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, EBHIncidentActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;			
			
		case R.id.itemCATStory:
			if (debug) { Log.d(TAG, "onClick'd on CatStory"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, CATStoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;			
						
		case R.id.itemHSIncidents:
			if (debug) { Log.d(TAG, "onClick'd on HSIncidents"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, HSIncidentActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;				
			
		case R.id.itemAirCraftIncidents:
			if (debug) { Log.d(TAG, "onClick'd on AirCraftIncidents"); }
			// Start an ACTIVITY ...
			startActivity(new Intent(this, AirCraftIncidentsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
			
		}// END Switch ...

		return true;
	} // END onOptionsItemSelected ....
	
	
	
	///// DASHBOARD INFRASTRUCTURE ************************************************************************
		

	/** HOME BUTTON
	 * @param v View
	 * @return void
	 */
	public void onClickHome (View v){
	    goHome (this);
	}

	
	/** ABOUT BUTTON
	 * @param v View
	 * @return void
	 */
	public void onClickAbout (View v) 	{
		int id = v.getId ();
	    switch (id) {
	      case R.id.info_btn_disasterevents :
	    	  startActivity(new Intent(this, AboutDisasterEvents.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.info_btn_earthquakes :
	    	  startActivity(new Intent(this, AboutEarthquakes.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.info_btn_topstories :
	    	  startActivity(new Intent(this, AboutTopStories.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;     
	      case R.id.info_btn_globalalerts :
	    	  startActivity(new Intent(this, AboutGlobalAlerts.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;  
	      case R.id.info_btn_pollution :
	    	  startActivity(new Intent(this, AboutPollutionAlerts.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;    
	      case R.id.info_btn_volcanos :
	    	  startActivity(new Intent(this, AboutVolcanos.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break; 	 
	      case R.id.info_btn_ebhincidents :
	    	  startActivity(new Intent(this, AboutEBHIncidents.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break; 
	      case R.id.info_btn_aircraft :
	    	  startActivity(new Intent(this, AboutAircraftIncidents.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break; 
	      case R.id.info_btn_hsincidents :
	    	  startActivity(new Intent(this, AboutHSIncidents.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break; 	       
	      case R.id.info_btn_catstory :
	    	  startActivity(new Intent(this, AboutCATStories.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;      
	      default: 
	    	   break;
	    } // END Switch 
	}

	 
	
	/** SEARCH BUTTON  
	 * @param v View
	 * @return void
	 */
	public void onClickSearch (View v){
		int id = v.getId ();
		Uri uriUrl = Uri.parse("http://www.google.com/");
		
	    switch (id) {
	      case R.id.search_btn_disasterevents :
	    	  Intent search_disasterevents = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_disasterevents.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_disasterevents);
	           break;
	      case R.id.search_btn_earthquakes :
	    	  Intent search_earthquakes = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_earthquakes.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_earthquakes);
	           break;     
	      case R.id.search_btn_topstories :
	    	  Intent search_topstories = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_topstories.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_topstories);
	           break;  
	      case R.id.search_btn_globalalerts :
	    	  Intent search_globalalerts = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_globalalerts.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_globalalerts);
	           break;   
	      case R.id.search_btn_pollution :
	    	  Intent search_pollution = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_pollution.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_pollution);
	           break;      
	      case R.id.search_btn_volcanos :
	    	  Intent search_volcanos = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_volcanos.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_volcanos);
	           break;    
	      case R.id.search_btn_aircraft :
	    	  Intent search_aircraft = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_aircraft.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_aircraft);
	    	  break; 
	      case R.id.search_btn_ebhincidents :
	    	  Intent search_ebhincidents = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_ebhincidents.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_ebhincidents);	  
	           break;    
	      case R.id.search_btn_hsincidents :
	    	  Intent search_hsincidents = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_hsincidents.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_hsincidents);	  
	           break;   
	      case R.id.search_btn_catstory :
	    	  Intent search_catstory = new Intent(Intent.ACTION_WEB_SEARCH).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	  search_catstory.putExtra(SearchManager.QUERY, DA_App.searchParameters);  
	    	  startActivity(search_catstory);	  
	           break;     
	      default: 
	    	   break;
	    } // END Switch 
	}
	
	
	
	//// DASHBOARD CLICK FEATURE STUFF **********************************************
	
	/** FEATURE BUTTONS 
	 * @param v View
	 * @return void
	 */
	public void onClickFeature (View v) {
	    int id = v.getId ();
	    switch (id) {
	      case R.id.home_btn_earthquakes :
	    	  startActivity(new Intent(this, EarthquakeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_volcanos:
	    	  startActivity(new Intent(this, VolcanoActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_aircraft :
	    	  startActivity(new Intent(this, AirCraftIncidentsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_globalalerts :
	    	  startActivity(new Intent(this, GlobalAlertsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_topstories :
	    	  startActivity(new Intent(this, TopStoriesActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_biohazards :
	    	  startActivity(new Intent(this, EBHIncidentActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;
	      case R.id.home_btn_pollution :
	    	  startActivity(new Intent(this, PollutionAlertsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break; 
	      case R.id.home_btn_security :
	    	  startActivity(new Intent(this, HSIncidentActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;     
	      case R.id.home_btn_catmap :
	    	  startActivity(new Intent(this, CATStoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	           break;      
	      default: 
	    	   break;
	    } // END Switch 
	} // END onClickFeature
	
	

	////DASHBOARD UTILITY METHODS  **********************************************

	
	/** GOHOME
	 * Go back to the home activity.
	 * @param context Context
	 * @return void
	 */
	public void goHome(Context context) {
	    final Intent intent = new Intent(context, DisasterEventsActivity.class);
	    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    context.startActivity (intent);
	}

	
	/** setTitleFromActivityLabel:
	 * Use the activity label to set the text in the activity's title text view.
	 * The argument gives the name of the view.
	 *
	 * <p> This method is needed because we have a custom title bar rather than the default Android title bar.
	 * See the theme definitions in styles.xml.
	 * 
	 * @param textViewId int
	 * @return void
	 */
	public void setTitleFromActivityLabel (int textViewId) {
	    TextView tv = (TextView) findViewById (textViewId);
	    if (tv != null) tv.setText (getTitle ());
	} // END setTitleText
		

    
}// END Class BaseActivity ...










