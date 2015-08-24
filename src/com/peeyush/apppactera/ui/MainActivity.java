package com.peeyush.apppactera.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.peeyush.apppactera.constants.ApplicationConstants;
import com.peeyush.apppactera.data.ImageDownloadManager;
import com.peeyush.apppactera.data.adapter.UserContentAdapter;
import com.peeyush.apppactera.network.HTTPEngine;
import com.peeyush.apppactera.network.ImageDownloader;
import com.peeyush.apppactera.preferences.Preferences;
import com.peeyush.apppactera.utils.FileUtils;
import com.peeyush.apppactera1.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private ProgressDialog mDialog;
	//list containing the listview data
	List<RowData> rowDataList = null;
	//title of the activity
	String pageTitle = "";
	//a reference to the current context
	Context ctx;
	ListView mListView;
	private CustomSwipeRefreshLayout mRefreshLayout;
	private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        rowDataList = new ArrayList<RowData>();
        mListView = ( ListView )findViewById( R.id.list );  
        
        //instantiate the CustomSwipeRefreshLayout layout
        mRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mRefreshLayout.init(mListView);

        //set a listener for pull to refresh
        mRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
		      @Override
		      public void onRefresh() {
		    	  
		    	  refreshData();			
		      }
		  });
        
        ctx = this;
        getData(getString(R.string.loading_data));
    }
    
    /**
     * Calls AsyncTask to request the data from jsonUrl
     * @param message to show to user by the time data is fetched
     */
    void getData(final String message){
    	
    	//if there is not connectivity and no previously loaded data, prompt user
		if(!isInternetEnabled() && Preferences.getInstance(ctx).getValue(ApplicationConstants.JSONKEY_USER_CONTENT, null)==null){
		        	
        	new AlertDialog.Builder(this)
            .setTitle(getString(R.string.check_connection))
            .setMessage(getString(R.string.no_internet))
            .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                	getData(message);
                }
             })
            .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                   ((Activity)ctx).finish();
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
        }
        else{
        	
        	new GetDataTask(message).execute();
        }
    	
    }

    /**
     * Called on pull to refresh. Cleans all the data and re-requests it
     */
    protected void refreshData() {

    	if(!isInternetEnabled()){
    		Toast.makeText(ctx, getString(R.string.retry_check_connection), Toast.LENGTH_SHORT).show();
    		mRefreshLayout.setRefreshing(false);
    		return;
    	}
    	
    	try {
    		mRefreshLayout.setRefreshing(true);
			ImageDownloadManager.getInstance().resetImageManager();
			Preferences.getInstance(ctx).setValue(ApplicationConstants.JSONKEY_USER_CONTENT, null);
			
		} catch (Exception e) {
			Log.e(TAG, "refreshData: " + "\n" + e.getMessage());
		}

		getData(null);
	}
    
    /**
     * Checks if device is connected to the internet
     * @return true if connected 
     */
    public boolean isInternetEnabled(){
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }

          }
          return false;
    }
    
	private class GetDataTask extends AsyncTask<Void, Void, Void> {
    	 
		String mMessage;
		List<RowData> tempRowDataList = new ArrayList<RowData>();
		GetDataTask(String message){
			mMessage = message;
		}
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            if(mMessage!=null){
	            mDialog = new ProgressDialog(MainActivity.this);
	            if(MainActivity.this.isFinishing()){
	            	this.cancel(false);
	            	return;
	            }
	            mDialog.setMessage(mMessage);
	            mDialog.setCancelable(false);
	            mDialog.show();
            }
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {

        	//check if data is already cached
        	String jsonData = Preferences.getInstance(ctx).getValue(ApplicationConstants.JSONKEY_USER_CONTENT, null);
        	
        	if(jsonData==null)
        		jsonData = HTTPEngine.GetData(ApplicationConstants.jsonUrl);
 
            if (jsonData != null) {
                try {
                	//cache the data for next time use
                	Preferences.getInstance(ctx).setValue(ApplicationConstants.JSONKEY_USER_CONTENT, jsonData);
                	
                    JSONObject jsonObj = new JSONObject(jsonData);
                     
                    //title of Activity
                    pageTitle = jsonObj.getString(ApplicationConstants.JSONNodes.ROOT_TITLE);
                    JSONArray rowsJson = jsonObj.getJSONArray(ApplicationConstants.JSONNodes.ROWS);
                    String title = null, desc = null, imageUrl = null;
                    
                    for (int i = 0; i < rowsJson.length(); i++) {
                        JSONObject rowDataJson = rowsJson.getJSONObject(i);
                         
                        title = rowDataJson.optString(ApplicationConstants.JSONNodes.TITLE);
                        desc = rowDataJson.optString(ApplicationConstants.JSONNodes.DESC);
                        imageUrl = rowDataJson.optString(ApplicationConstants.JSONNodes.IMAGE);
                        
                        //add the data to the list if title exists (treating title as primary key)
                        if(!title.equals("null"))
                        	tempRowDataList.add(new RowData(title, desc.equals("null")?null:desc, imageUrl.equals("null")?null:imageUrl));
                        
                    }
                } catch (JSONException e) {
                	Log.e(TAG, e.getMessage());
                }
            } else {
                Log.e(TAG, "Failed to get data, jsonData null");
            }
 
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(mRefreshLayout!=null && mRefreshLayout.isRefreshing())
            	mRefreshLayout.setRefreshing(false);
            //Dismiss the progress dialog
    		if (mDialog!= null && mDialog.isShowing())
                mDialog.dismiss();
    		rowDataList.clear();
    		rowDataList.addAll(tempRowDataList);
            if(mAdapter==null){
            	mAdapter = new UserContentAdapter(ctx, R.layout.listitem, rowDataList);
            	mListView.setAdapter(mAdapter);
            }
            else
            	((BaseAdapter)mAdapter).notifyDataSetChanged();
            //set activity title
            setTitle(pageTitle);
            
        }
    }
	ListAdapter mAdapter = null;
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDialog!= null && mDialog.isShowing())
            mDialog.dismiss();
	}
}
