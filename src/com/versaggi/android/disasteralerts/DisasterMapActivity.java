package com.versaggi.android.disasteralerts;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

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

/** DisasterMapActivity: 
 * 
 *  This is the Google Map Class to visually display the Disaster location.
 *  It's pretty standard Map Overlay stuff ... 
 *  
 */
public class DisasterMapActivity extends MapActivity {

	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = DisasterMapActivity.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	
	// Initialize Class Wide Map objects ...
    private MapView mapView = null;
    private int zoomLevel = 4; 
    private float disasterLongitude;	// Intent Extra
    private float disasterLatitude;		// Intent Extra


    
    ///// Map LifeCycle Stuff **********************************************************
    
    // onCreate:
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disaster_map);
        
        // Get Bundled Data passed in via the Intent (Disaster Longitude and Latitude)
        Bundle extras 		= getIntent().getExtras();
        disasterLongitude 	= extras.getFloat("longitude");
        disasterLatitude	= extras.getFloat("latitude");
                
        // Set up Map UI: Get Handle, Set Zoom Controls, Force Satellite Images 
        mapView = (MapView)findViewById(R.id.geoDisasterMap);
        mapView.setBuiltInZoomControls(true);				// Use built in Zoom Controls
        mapView.setSatellite(true);							// Forces Map to be Satellite

        // Overlay Stuff: Get Marker Icon, Set it's bounds
        Drawable marker=getResources().getDrawable(R.drawable.mapmarker);
        marker.setBounds((int)(-marker.getIntrinsicWidth()/2), -marker.getIntrinsicHeight(), (int) (marker.getIntrinsicWidth()/2), 0);
        
        // Interesting Locations 
        DisasterLocations disaster = new DisasterLocations(marker);
        mapView.getOverlays().add(disaster);
        
        GeoPoint pt = disaster.getCenterPt();		// Find the middle points of all the entered points.
        int latSpan = disaster.getLatSpanE6();		// Get Height (Latitude) of Overlay w/'zoomToSpan'
        int lonSpan = disaster.getLonSpanE6();		// Get Width (Longitude) of Overlay w/'zoomToSpan'
    

        // Map Controller actions: Set Center and Zoom 
        MapController mc = mapView.getController();
        mc.setCenter(pt);							// Center Point of the map
        mc.setZoom(zoomLevel);						// Set Zoom Level (H: 1-> L: 21) {Our default is 4}

    }// END onCreate    
    
	
	// isRouteDisplayed:
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// isLocationDisplayed:
	@Override
	protected boolean isLocationDisplayed() {
		return super.isLocationDisplayed();
	}


	// onDestroy:
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// onPause: 
	@Override
	protected void onPause() {
		super.onPause();
	}
	

	// onResume:
	@Override
	protected void onResume() {
    	super.onResume();
	}

	
	
	//// OVERLAY Stuff ****************************************************************
	
	/** InterestingLocations:
	 * 	This class extends a very useful class Map 'ItemizedOverlay', the general usage 
	 * 	being Extend that class and add the items (aka interesting locations) in the 
	 * 	Constructor. 
	 * 	[InterestingLocations] <== [ItemizedOverlay] <== [Overlay] : Extends Hierarchy 
	 * 	OVERLAY Class defines the untimate contract for an overlay.
	 */
    class DisasterLocations extends ItemizedOverlay {
        private ArrayList<OverlayItem> locations = new ArrayList<OverlayItem>();
        private GeoPoint center = null;

        // Constructor: Add Locations here ...
        public DisasterLocations(Drawable marker) {
            super(marker);
            
            // Create locations of interest (GeoPoints):
            // The API operates on 'MicroDegrees' so they have to be multiplied by 1000000 
            // and then caste as an integer. 'disasterLatitude and disasterLongitude' are passed in
            // in as Bundled Extra Data to the Activity from the calling Activity Class 
            
            GeoPoint disasterCenter = new  GeoPoint((int)(disasterLatitude*1000000),(int)(disasterLongitude*1000000));
            
            // Add the Disaster Epicenter Location
            locations.add(new OverlayItem(disasterCenter, "Disaster Center", "Disaster Center"));
            
            // A utility that caches any OverlayItems. To show markers on a Map, create the points
            // and call 'populate()'; the Overlay Class contract manages the rest.
            populate();
            
        }// END Constructor 
        


        /** getCenterPt:
        *  We added this method to find the middle point of the cluster.
        *  Start each edge on its opposite side and move across with each point.
        *  The top of the world is +90, the bottom -90, the west edge is -180, 
        *  the east +180.
        */
        public GeoPoint getCenterPt() {
            if(center == null) {
                int northEdge = -90000000;   // i.e., -90E6 microdegrees
                int southEdge = 90000000;
                int eastEdge = -180000000;
                int westEdge = 180000000;
                Iterator<OverlayItem> iter = locations.iterator();
                while(iter.hasNext()) {
                    GeoPoint pt = iter.next().getPoint();
                    if(pt.getLatitudeE6() > northEdge) northEdge = pt.getLatitudeE6();
                    if(pt.getLatitudeE6() < southEdge) southEdge = pt.getLatitudeE6();
                    if(pt.getLongitudeE6() > eastEdge) eastEdge = pt.getLongitudeE6();
                    if(pt.getLongitudeE6() < westEdge) westEdge = pt.getLongitudeE6();
                }
                center = new GeoPoint((int)((northEdge + southEdge)/2), (int)((westEdge + eastEdge)/2));
            }
            return center;
        } // END GeoPoint 

        
        @Override
        public void draw(Canvas canvas, MapView mapview, boolean shadow) {
        	// Here is where we can eliminate shadows by setting to false
        	super.draw(canvas, mapview, shadow);
        }


        @Override
        protected OverlayItem createItem(int i) {
            return locations.get(i);
        }


        @Override
        public int size() {
            return locations.size();
        }
        
    }// END Class InterestingLocations 
    
    
	
}//End Class DisasterMapActivity ....
