package com.versaggi.android.disasteralerts;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;

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


/** Quake:
 * 
 * This class is a effectively just a data structure for the object QUAKE.
 * It contains simple getters and a toString Override for Date formatting
 * purposes. 
 *
 * THIS DOES NOT REPRESENT THE UNDERLYING DATABASE STRUCTURE, JUST A QUAKE OBJECT.
 */
public class Quake {
	
	// Column Constants 
	public static final String QUAKE_ID 	   = "_id";
	public static final String QUAKE_DATE 	   = "date";      // (aka) Title
	public static final String QUAKE_DETAILS   = "details";
	public static final String QUAKE_LOCATION  = "location";
	public static final String QUAKE_MAGNITUDE = "magnitude";
	public static final String QUAKE_CDATA 	   = "cdata";
	public static final String QUAKE_LINK 	   = "link";
	
	  // Local variable instantiations ...
	  public int eqid; 
	  public Date date;				// (aka) Title
	  public String details;
	  public Location location;
	  public double magnitude;
	  public String cdata;
	  public String link;
	  
	  // GETTER Functions 
	  public int getId() 			{ return eqid; }
	  public Date getDate() 		{ return date; }
	  public String getDetails() 	{ return details; }		// (aka) Title
	  public Location getLocation() { return location; }
	  public double getMagnitude() 	{ return magnitude; }
	  public String getCdata() 	{ return cdata; }
	  public String getLink() 		{ return link; }
	  
	  
	  //Constructor Function for the Quake Object ...
	  public Quake(int _eqid, Date _date, String _details, Location _loc, double _mag, String _cdata, String _link) {
		eqid 		= _eqid;
	    date 		= _date;
	    details 	= _details;		// (aka) Title
	    location 	= _loc;
	    magnitude 	= _mag;
	    cdata		= _cdata;
	    link 		= _link;
	  }

	  
	  @Override
	  public String toString() {
	    SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
	    String dateString = sdf.format(date);
	    return dateString + ": " + magnitude + " " + details;
	  }
	  
	  
}// END Class Quake ...




