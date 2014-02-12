package com.versaggi.android.disasteralerts;

import java.util.Date;

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

/** AirCraftIncidents:
 * 
 * This class is a effectively just a data structure for the object AirCraftIncidents.
 * It contains simple getters.
 *
 * THIS DOES NOT REPRESENT THE UNDERLYING DATABASE STRUCTURE, JUST A AirCraftIncidents OBJECT.
 */
public class AirCraftIncidents {


	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = AirCraftIncidents.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	
		// Column Constants 
		public static final String AIRCRAFTINCIDENTS_ID 			= "_id";
		public static final String AIRCRAFTINCIDENTS_DATE 			= "date";
		public static final String AIRCRAFTINCIDENTS_DETAILS 		= "details";    // (aka) Title
		public static final String AIRCRAFTINCIDENTS_DESCRIPTION 	= "description";
		public static final String AIRCRAFTINCIDENTS_LINK 			= "link";
		
		// Local variable instantiations ...
		public int aciid; 
		public Date date;				
		public String details;	// (aka) Title
		public String description;
		public String link;
		
		
		// GETTER Functions 
		public int getId() 			 	{ return aciid; }
		public Date getDate() 		 	{ return date; }
		public String getDetails() 	 	{ return details; }		// (aka) Title
		public String getDescription() 	{ return description; }
		public String getLink() 		{ return link; }		
		  
	
		//Constructor Function for the EBHIncident Object ...
		public AirCraftIncidents(int _aciid, Date _date, String _details, String _descr, String _link) {
			aciid 		 = _aciid;
		    date 		 = _date;
		    details 	 = _details;		// (aka) Title
		    description	 = _descr;
		    link 		 = _link;
		  }		  
		  
		  
}// End Class AirCraftIncidents
