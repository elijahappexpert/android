package com.versaggi.android.disasteralerts;

import android.app.Application;
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

/** DisasterEventsApplication:
 * This is our actual Application and is a good place to put things 
 * (data/methods) that we want to SHARE across multiple parts of our 
 * system. It is meant to represent our DisasterAlerts application. 
 * The APP is created when ever one of the main building blocks 
 * (entry points) is needed.
 */
public class DisasterEventsApplication extends Application{

	///// Define stuff shared globally in this application here ...
	
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = DisasterEventsApplication.class.getSimpleName();

	EarthquakeProvider earthquakeProvider; 		// Blank Object of class EarthquakeProvider, knows all about data
	CATStoryProvider catStoryProvider; 			// Blank Object of class CATStoryProvider, knows all about data
	HSIncidentProvider hsIncidentProvider; 		// Blank Object of class CATStoryProvider, knows all about data
	EBHIncidentProvider ebhIncidentProvider;	// Blank Object of class EBHIncidentProvider, knows all about data
	TopStoriesProvider topStoriesProvider;		// Blank Object of class TopStoriesProvider, knows all about data
	GlobalAlertsProvider globalAlertsProvider;	// Blank Object of class GlobalAlertsProvider, knows all about data
	VolcanoProvider volcanoProvider;			// Blank Object of class VolcanoProvider, knows all about data
	AirCraftIncidentsProvider airCraftIncidentsProvider; //BLANK of class AirCraftIncidentsProvider, knows all about data
	PollutionAlertsProvider pollutionAlertsProvider;     //BLANK of class PollutionAlertsProvider, knows all about data

	// Search String (Global): 
	// This global variable can get anywhere but is only used in the onClickSearch method of the BaseActivity 
	// class. It's used to capture an arbitrary search string to pass as an extra parameter to a Google
	// web search when someone clicks on the looking glass search icon on the Dashboard title bar. 
	
	String searchParameters = null;
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	
	   
	//// LIFECYCLE Overrides   *******************************************************	
	   
	   
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (debug) { Log.d(TAG, "OnCreate'd - Making Application Provider Ojbects ..."); }	
		
		// Instantiate our Application wide Data Object(s) which handles ALL of the low level data interactions  
		//
		earthquakeProvider 			= new EarthquakeProvider(this);
		catStoryProvider   			= new CATStoryProvider(this);
		hsIncidentProvider 			= new HSIncidentProvider(this);
		ebhIncidentProvider 		= new EBHIncidentProvider(this);
		topStoriesProvider 			= new TopStoriesProvider(this);
		globalAlertsProvider 		= new GlobalAlertsProvider(this);
		volcanoProvider				= new VolcanoProvider(this);
		airCraftIncidentsProvider 	= new AirCraftIncidentsProvider(this);
		pollutionAlertsProvider		= new PollutionAlertsProvider(this);
		
	} // END onCreate ...

	
	
	@Override
	public void onTerminate() {
		super.onTerminate();	
		
		// Close the Application DBs to prevent memory leaks ... 
		earthquakeProvider.close();
		catStoryProvider.close();
		hsIncidentProvider.close();
		ebhIncidentProvider.close();
		topStoriesProvider.close();
		globalAlertsProvider.close();
		volcanoProvider.close();
		airCraftIncidentsProvider.close();
		pollutionAlertsProvider.close();
		
		
		if (debug) { Log.d(TAG, "onTerminate'd ..."); }
	}
	
	
	
}// END Class DisasterEventsApplication ...









