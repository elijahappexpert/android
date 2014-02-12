package com.versaggi.android.disasteralerts;

import android.content.Intent;
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


/** AboutGlobalAlerts
 * 
 * This class implements the Information (i) page displayed when the user pressed 
 * the (i) icon. It effectively creates a standard WebView and populates it with
 * simple text, links and images. 
 *
 */
public class AboutGlobalAlerts extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = AboutGlobalAlerts.class.getSimpleName();
	
	
	//// LIFE CYCLE Overrides  *****************************************************
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutglobalalerts);
        
        // WebView: Startup Processing ***********************************************
        WebView myWebView = (WebView) this.findViewById(R.id.AboutGlobalAlertsWebView);
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
        s.append("<p><u>GLOBAL ALERTS INFO PAGE</u></p>");
        s.append("</div>");
        
        s.append("<ul>");								
        s.append("<li>This application pulls realtime alerts data from the <a href=http://www.gdacs.org>Global Disaster Alert and Coordination System - http://www.gdacs.org</a>.</li>");
        s.append("<BR>");
        s.append("<li>Click <font color=#FF0000>GetGlobalAlerts</font> to refresh the alert list.</li>");
        s.append("<BR>");
        s.append("<li>Click on any alert to get its <i>Quick Action Menu</i> which provides more options.</li>");
        s.append("</ul>");								
        
        s.append("<p>Quick Action Menu:</p>");
        
        s.append("<ul>");								
        s.append("<li><u>WebSite</u>: - Go to the GDACS page for this alert. </li>");
        s.append("<li><u>Map</u>: Use Google Maps to goto alert location.</li>");
        s.append("<li><u>Details</u>: GDACS specific data on the alert.</li>");
        s.append("<li><u>Google It</u>: Do a google search on this alert.</li>");
        s.append("<li><u>Share</u>: Send friends this alert info.</li>");
        s.append("</ul>");	       
        
        
        s.append("<BR>");
        s.append("<div align=center>");
        s.append("<img src=\"file:///android_asset/info_webview_globalalerts_activity.png\" />");
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
	
	
    
	// onClickBackToGlobalAlerts 
	public void onClickBackToGlobalAlerts (View v)
	{
		startActivity(new Intent(this, GlobalAlertsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	}
	
	
}// END Class AboutGlobalAlerts
