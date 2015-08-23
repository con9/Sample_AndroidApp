package com.peeyush.apppactera.ui;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ListView;

/*
 * Custom layout for Swipe to Refresh
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout{

	Context mContext;
	ListView mListView;
	
	public CustomSwipeRefreshLayout(Context context) {
		super(context);
		mContext = context;
	}
	
	public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	public void init(ListView listview){
		
		mListView = listview;
	}
	
	@Override
	public boolean canChildScrollUp() {
		return ViewCompat.canScrollVertically(mListView, -1);
	}

}
