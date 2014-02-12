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


/** CATStory:
 * 
 * This class is a effectively just a data structure for the object CATStory.
 * It contains simple getters.
 *
 * THIS DOES NOT REPRESENT THE UNDERLYING DATABASE STRUCTURE, JUST A CATStory OBJECT.
 */
public class CATStory {
	
	// Column Constants 
	public static final String CATSTORY_ID 				= "_id";
	public static final String CATSTORY_DETAILS 		= "details";	// (aka) Title
	public static final String CATSTORY_DESCRIPTION 	= "description";
	public static final String CATSTORY_GEOPOINT_LAT  	= "geopoint_lat";
	public static final String CATSTORY_GEOPOINT_LNG  	= "geopoint_lng";
	public static final String CATSTORY_LINK 			= "link";
	 
	  // Local variable instantiations ...
	  public int csid; 
	  public String details;		// (aka) Title
	  public String description;
	  public float geopoint_lat;
	  public float geopoint_lng;
	  public String link;
	  
	  // GETTER Functions 
	  public int getId() 			 { return csid; }
	  public String getDetails() 	 { return details; }		// (aka) Title
	  public String getDescription() { return description; }
	  public float getGeoPointLat()  { return geopoint_lat; }
	  public float getGeoPointLng()  { return geopoint_lng; }
	  public String getLink() 		 { return link; }
	  
	  
	  //Constructor Function for the CATStory Object ...
	  public CATStory(int _csid, String _details, String _desc, float _geopt_lat, float _geopt_lng, String _link) {
		csid 		 	= _csid;
	    details 		= _details;
	    description 	= _desc;
	    geopoint_lat 	= _geopt_lat;
	    geopoint_lng 	= _geopt_lng;
	    link 			= _link;
	  }

	 	


}// END Class CATStory 








