package com.versaggi.android.disasteralerts;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

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

/* Class QuickAction
 * 
 */
public class QuickAction extends PopupWindows {
	
	// Used to get the Log.D TAG from the strings.XML file so its flexible
	private static final String TAG = QuickAction.class.getSimpleName();
	
	// Debug Flags ....
	// Boolean debug = Boolean.FALSE;
	   Boolean debug = Boolean.TRUE;
	   
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private Animation mTrackAnim;
	private LayoutInflater inflater;
	private ViewGroup mTrack;
	private OnActionItemClickListener mListener;
	  
	private int animStyle;
	private int mChildPos;
	private boolean animateTrack;
	
	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_AUTO = 4;
	
	
	/** Constructor
	 * @param context Context
	 */
	public QuickAction(Context context) {
		super(context);
		
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTrackAnim 	= AnimationUtils.loadAnimation(context, R.anim.rail);
		
		mTrackAnim.setInterpolator(new Interpolator() {		// Set acceleration curve for animation 
			public float getInterpolation(float t) {
	              	// Pushes past the target area, then snaps back into place.
	                // Equation for graphing: 1.2-((x*1.6)-1.1)^2
				final float inner = (t * 1.55f) - 1.1f;
	            return 1.2f - inner * inner;
	        }
		});
	        
		// KEY layout for QuickAction Animation 
		setRootViewId(R.layout.quickaction);
		
		animStyle		= ANIM_AUTO;
		animateTrack	= true;
		mChildPos		= 0;
		
	} // End Constructor ....
	
	
	
	/** Set root view.
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView	= (ViewGroup) inflater.inflate(id, null);
		mTrack 		= (ViewGroup) mRootView.findViewById(R.id.tracks);
		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);
		setContentView(mRootView);
	}
	
	
	
	/** Animate track.
	 * @param animateTrack flag to animate track
	 */
	public void animateTrack(boolean animateTrack) {
		this.animateTrack = animateTrack;
	}
	
	
	
	/** Set animation style.
	 * @param animStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	
	/** Add action item
	 * @param action  {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		
		String title 	= action.getTitle();
		Drawable icon 	= action.getIcon();
		
		View container	= (View) inflater.inflate(R.layout.action_item, null);
		
		ImageView img 	= (ImageView) container.findViewById(R.id.iv_icon);
		TextView text 	= (TextView)  container.findViewById(R.id.tv_title);
		
		if (icon != null) 
			img.setImageDrawable(icon);
		else
			img.setVisibility(View.GONE);
		
		if (title != null)
			text.setText(title);
		else
			text.setVisibility(View.GONE);
		
		final int pos =  mChildPos;
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) mListener.onItemClick(pos);
				if (debug) { Log.d(TAG, "in container.setOnClickListener -> onClick'd " ); }
				//dismiss();  
			}
		});
		container.setFocusable(true);
		container.setClickable(true);
		mTrack.addView(container, mChildPos+1);
		mChildPos++;
		
	} // END addActionItem
	
	
	
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mListener = listener;
	}
	
	/**
	 * Show popup mWindow
	 */
	public void show (View anchor) {
		
		preShow();
		
		int[] location 		= new int[2];
		anchor.getLocationOnScreen(location);
		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
		
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		int rootWidth 		= mRootView.getMeasuredWidth();
		int rootHeight 		= mRootView.getMeasuredHeight();
		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();
		//int screenHeight 	= mWindowManager.getDefaultDisplay().getHeight();
		int xPos 			= (screenWidth - rootWidth) / 2;
		int yPos	 		= anchorRect.top - rootHeight;
		boolean onTop		= true;
		
		// display on bottom
		if (rootHeight > anchor.getTop()) {
			yPos 	= anchorRect.bottom;
			onTop	= false;
		}
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX());
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		
		if (animateTrack) mTrack.startAnimation(mTrackAnim);
		
	}// END show
	
	

	/** Set animation style
	 * @param screenWidth Screen width
	 * @param requestedX distance from left screen
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		switch (animStyle) {
		
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;
					
		case ANIM_GROW_FROM_CENTER:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			break;
					
		case ANIM_AUTO:
			if (arrowPos <= screenWidth/4) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right : R.style.Animations_PopDownMenu_Right);
			}	
			break;
		}
	}// END setAnimationStyle
	
	
	
	/** Show arrow
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
		
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;
        final int arrowWidth = mArrowUp.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        hideArrow.setVisibility(View.INVISIBLE);
    }
	
	
	
	
	/** Listener for item click:
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(int pos);
	}
	
	
	
	
} // END Class QuickAction







