package com.peeyush.apppactera.constants;

import android.os.Environment;

public class ApplicationConstants {

	
	public class JSONNodes{

		public static final String ROWS = "rows";
		public static final String ROOT_TITLE = "title";
		public static final String TITLE = "title";
		public static final String DESC = "description";
		public static final String IMAGE = "imageHref";
		
	}
	public class HttpStatus{
		
		public static final int OK = 200;
	}
	
	public static final String ImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/PacteraImages";
	public static final String SharedPreferenceKey = "PacteraUserContentApp";
	public static String JSONKEY_USER_CONTENT = "UserContentJKey";
	public static String jsonUrl = "http://dl.dropboxusercontent.com/u/746330/facts.json";
	
}
