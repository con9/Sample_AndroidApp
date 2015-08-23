package com.peeyush.apppactera.preferences;

import java.lang.ref.WeakReference;

import com.peeyush.apppactera.constants.ApplicationConstants;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	
	private static String TAG = "Preferences";
	private static SharedPreferences mPref;
    
    private static WeakReference<Context> context;
	
    private static Preferences instance = null;
    
    private Preferences(Context ctx)
    {
        context = new WeakReference<Context>(ctx);
    }

    public static Preferences getInstance(Context ctx)
    {
    	synchronized (Preferences.class)
        {
            if (instance == null)// && context.get()!=null
            {
                instance = new Preferences(ctx);
            }
        }
        if(mPref == null){
            mPref = context.get().getApplicationContext().getSharedPreferences(ApplicationConstants.SharedPreferenceKey, android.content.Context.MODE_PRIVATE);
        }

        return instance;
    }
	
    public void setValue(String key, String value) {
    	
    	mPref.edit().putString(key, value).commit();
   
   }

    public String getValue(String key, String defValue){
        return mPref.getString(key, defValue);
    }
    
	
}
