package com.versaggi.android.disasteralerts;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

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


/** AboutEarthquakes
 * 
 * This class implements the Information (i) page displayed when the user pressed 
 * the (i) icon. It effectively creates a standard WebView and populates it with
 * simple text, links and images. 
 *
 */
public class AboutEarthquakes extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = AboutEarthquakes.class.getSimpleName();
	
	
	//// LIFE CYCLE Overrides  *****************************************************
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutearthquakes);
        
        
        // WebView: Startup Processing ***********************************************
        WebView myWebView = (WebView) this.findViewById(R.id.AboutEarthquakesWebView);
		WebSettings webSettings = myWebView.getSettings();					
		webSettings.setDefaultFontSize(14);
		
		// WebView: HTML SETUP Processing ***********************************************
       
		
        final StringBuilder s = new StringBuilder();	// Create a new StringBuilder object 
        												// which holds the HTML we are building
        
     // WebView: Construct HTML string to display later  ********************************
        
        s.append("<html>");		
        s.append("<STYLE><!--A{text-decoration:none}--></STYLE>");
        s.append("<body bgcolor=#ffffff vlink=#ffffff alink=#ffffff>");
        
        s.append("<font face=Arial color=#000000>");
        s.append("<B>");

        s.append("<div align=center>");
        s.append("<p><u>EARTHQUAKES INFO PAGE</u></p>");
        s.append("</div>");
        
        s.append("<ul>");								
        s.append("<li>This application pulls realtime earthquake data from the <a href=http://earthquake.usgs.gov>US Geological Survey - http://earthquake.usgs.gov</a>.</li>");
        s.append("<BR>");
        s.append("<li>Click <font color=#FF0000>GetEarthquakes</font> to refresh the list.</li>");
        s.append("<BR>");
        s.append("<li>Click on any item to get its <i>Quick Action Menu</i> which provides more options.</li>");
        s.append("</ul>");								
        
        s.append("<p>Quick Action Menu:</p>");
        
        s.append("<ul>");								
        s.append("<li><u>WebSite</u>: - Go to the USGS page for this quake. </li>");
        s.append("<li><u>Map</u>: Use Google Maps to locate this quake.</li>");
        s.append("<li><u>Quick View</u>: Quick picture / info on the quake.</li>");
        s.append("<li><u>Details</u>: USGS specific summary data on the quake.</li>");
        s.append("<li><u>Google It</u>: Do a google search on this quake.</li>");
        s.append("<li><u>Share</u>: Send friends this quake info.</li>");
        s.append("</ul>");	       
          
        s.append("<BR>");
        s.append("<div align=center>");
        s.append("<img src=\"file:///android_asset/info_webview_earthquakes_activity.png\" />");
        s.append("</div>");
        
        s.append("<BR>");
        s.append("<BR>");
        
        s.append("</B>");
        s.append("</font>");							
        s.append("</body>");							
        s.append("</html>");
       
		// WebView: Load HTML into WebView Widget on the Screen *************************
		myWebView.loadDataWithBaseURL(null, s.toString(), "text/html", "UTF-8", null);

		
    }// END onCreate ....
    
    
    
    
	// onClickRefreshQuakes 
	public void onClickBackToQuakes (View v)
	{
		startActivity(new Intent(this, EarthquakeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	}
    
    
    
    
}//END Class AboutEarthquakes

