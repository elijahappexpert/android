package com.versaggi.android.disasteralerts;

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

/** HSIncident:
 * 
 * This class is a effectively just a data structure for the object HSIncident.
 * It contains simple getters.
 *
 * THIS DOES NOT REPRESENT THE UNDERLYING DATABASE STRUCTURE, JUST A HSIncident OBJECT.
 */
public class HSIncident {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = HSIncident.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	// Column Constants 
	public static final String HSINCIDENT_ID 		  	= "_id";
	public static final String HSINCIDENT_DETAILS 		= "details";    // (aka) Title
	public static final String HSINCIDENT_DESCRIPTION 	= "description";
	public static final String HSINCIDENT_LINK 			= "link";
	
	  // Local variable instantiations ...
	  public int hsiid; 
	  public String details;	// (aka) Title
	  public String description;
	  public String link;
	  
	  // GETTER Functions 
	  public int getId() 			 { return hsiid; }
	  public String getDetails() 	 { return details; }
	  public String getDescription() { return description; }
	  public String getLink() 		 { return link; }
	  
	  
	  //Constructor Function for the CATStory Object ...
	  public HSIncident(int _hsiid, String _details, String _desc, String _link) {
		hsiid 		= _hsiid;
	    details 	= _details;		// (aka) Title
	    description = _desc;
	    link 		= _link;
	  }
	
}// END CLASS HSIncident 

