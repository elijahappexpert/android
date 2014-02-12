package com.versaggi.android.disasteralerts;


import android.os.Bundle;
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


/** AboutDisasterEvents
 * 
 * This class implements the Information (i) page displayed when the user pressed 
 * the (i) icon. It effectively creates a standard WebView and populates it with
 * simple text, links and images. 
 *
 */
public class AboutDisasterEvents extends BaseActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = AboutDisasterEvents.class.getSimpleName();
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutdisasterevents);
        
        
        // WebView: Startup Processing ***********************************************
        WebView myWebView = (WebView) this.findViewById(R.id.AboutDisasterEventsWebView);
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
        s.append("<p><u>DISASTER EVENTS INFO PAGE</u></p>");
        s.append("</div>");
        
        s.append("<ul>");								
        s.append("<li>This App pulls realtime XML/RSS disaster incident data from multiple online sources. Each data provider is listed in their respective info (i) pages, and in detail on the App Support Site.</li>");
        s.append("<BR>");
        s.append("<li>Click the <font color=#FF0000>'GetEvent'</font> Title to refresh the data on any page.</li>");
        s.append("<BR>");
        s.append("<li>Click any item in the list to get its <i>Quick Action Menu</i> for more options.</li>");
        s.append("<BR>");
        s.append("<li>Click the Info 'i' icon on any screen for <i>information</i> on each activity, and its corresponding data feed provider.</li>");
        s.append("<BR>");
        s.append("<li>'Like' us on Facebook - <a href=http://www.facebook.com/disasterevents>www.facebook.com/disasterevents</a>.</li>");
        s.append("<BR>");
        s.append("<li>'Follow' us on Twitter - <a href=http://twitter.com/#!/disasterevents>@DisasterEvents</a>.</li>");
        s.append("<BR>");
        s.append("<li>Visit our App Support Site - <a href=http://www.versaggi.biz>www.versaggi.biz</a> to <font color=#FF0000>download</font> the <U>source code</U>, <i>design documents, developer notes, shared resources</i> and talk to the developers.</li>");
        s.append("<BR>");
        s.append("<li>Look for our <font color=#FF0000>'Featured Data Provider'</font> when ever we release an update.  We value these folks highly and will feature a new one at the top each time we do an update.</li>");
        s.append("<BR>");
        s.append("<li>We are constantly searching for new and interesting data feed providers for inclusion into this App. If you are such a provider and want to be represented in the next update of the App, please contact us.</li>");
        s.append("<BR>");
        s.append("<li>Known Bugs: Pulling XML data from internet sources is a very tricky business, things go wrong if providers suddenly change formats without informing us. When that happens it usually gets fixed fast, but in the meantime it will cause that feed of the App to break temporarily. If that ever happens, please be patient, we are probably aware of it already and will either notify the feed provider to fix it, or issue an immediate update ourselves to fix the problem.</li>");
        s.append("<BR>");
        s.append("</ul>");								
        
        s.append("<p>Quick Action Menu Exampe:</p>");
        
        s.append("<ul>");								
        s.append("<li><u>WebSite</u>: - Go to the web site for the event. </li>");
        s.append("<li><u>Map</u>: Use Google Maps to locate the event.</li>");
        s.append("<li><u>Quick View</u>: Quick picture or info on the event.</li>");
        s.append("<li><u>Details</u>: Get specific summary information for an event.</li>");
        s.append("<li><u>Google It</u>: Do a google search on this event.</li>");
        s.append("<li><u>Share</u>: Send friends this event info.</li>");
        s.append("</ul>");	       
          
        s.append("<BR>");
        s.append("<div align=center>");
        s.append("<img src=\"file:///android_asset/info_webview_disasterevents_activity.png\" />");
        s.append("</div>");
        
        s.append("<BR>");
        s.append("<p>An Example Activity:</p>");
        s.append("<div align=center>");
        s.append("<img src=\"file:///android_asset/info_webview_aircraft_activity.png\" />");
        s.append("</div>");
        
        s.append("<BR>");
        s.append("<BR>");
        
        s.append("<div align=left>");
        s.append("<p><u>DEVELOPER INFORMATION</u></p>");
        s.append("</div>");
        
        s.append("Versaggi Information Systems <BR>[Maastricht, Netherlands / Chicago, USA.] <BR> Visit our Support Site - <a href=http://www.versaggi.biz>www.versaggi.biz</a> for more information.");
        s.append("<BR>");
        s.append("<BR>");
        s.append("<BR>");
        s.append("<BR>");
        
        s.append("</B>");
        s.append("</font>");							
        s.append("</body>");							
        s.append("</html>");
       
		// WebView: Load HTML into WebView Widget on the Screen *************************
		myWebView.loadDataWithBaseURL(null, s.toString(), "text/html", "UTF-8", null);
        
    }// END onCreate ....
    
}// END Class AboutDisasterEvents



