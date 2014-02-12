package com.versaggi.android.disasteralerts;


import android.os.Bundle;
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

/** DisasterEventsActivity:
 * This is the activity screen which effectively starts off the interaction 
 * with this App. It's fairly simple in that it simply inflates an XML layout,
 * instantiates a few variables and implements a few lifecycle overrides.
 */
public class DisasterEventsActivity extends BaseActivity {
	
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = DisasterEventsActivity.class.getSimpleName();
	
	
	//// LIFE CYCLE Overrides  *****************************************************
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disasterevents);
        
        // Set Activity Search Parameters to be passed into the onClickSearch method in the 
        // BaseActivity Class. It's used to capture an arbitrary search string to pass as an 
        // extra parameter to a Google web search when someone clicks on the looking glass 
        // search icon on the Dashboard title bar. 
        
        DA_App.searchParameters = "worldwide disasters";
        
    }// END onCreate ....

	@Override
	protected void onResume() {
		super.onResume();
		DA_App.searchParameters = "worldwide disasters";
	}
	
	  
	// onPause:
	@Override
	protected void onPause() {
		super.onPause();
	}
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DA_App.searchParameters = null;
	}


	@Override
	protected void onStop() {
		super.onStop();
		DA_App.searchParameters = null;
	}

 
    
    
}// END Class DisasterActivity ....

