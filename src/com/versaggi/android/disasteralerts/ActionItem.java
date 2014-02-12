package com.versaggi.android.disasteralerts;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

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


/** 
*********************** BEGIN Developers Notes **********************
*
This file is in it's original state as it was when used for inspiration.

********************** END Developers Notes *****************************
*/


/**
 * Action item, displayed as menu with icon and text.
 */
public class ActionItem {
	private Drawable icon;
	private Bitmap thumb;
	private String title;
	private boolean selected;
	
	// Constructor
	public ActionItem() {}
	
	// Constructor: @param icon {@link Drawable} action icon
	public ActionItem(Drawable icon) {
		this.icon = icon;
	}
	
	
	
	
	
	/** Set action title
	 * @param title action title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/** Get action title
	 * @return action title
	 */
	public String getTitle() {
		return this.title;
	}
	
	
	
	
	/** Set action icon
	 * @param icon {@link Drawable} action icon
	 */
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	/** Get action icon
	 * @return  {@link Drawable} action icon
	 */
	public Drawable getIcon() {
		return this.icon;
	}
	
	
	
	
	/** Set selected flag;
	 * @param selected Flag to indicate the item is selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/** Check if item is selected
	 * @return true or false
	 */
	public boolean isSelected() {
		return this.selected;
	}

	
	
	/** Set thumb
	 * @param thumb Thumb image
	 */
	public void setThumb(Bitmap thumb) {
		this.thumb = thumb;
	}
	
	/** Get thumb image
	 * @return Thumb image
	 */
	public Bitmap getThumb() {
		return this.thumb;
	}
	
	
}// END Class ActionItem









