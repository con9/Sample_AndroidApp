package com.peeyush.apppactera.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.peeyush.apppactera.constants.ApplicationConstants;

public class FileUtils {
	
	private static String TAG = "FileUtils"; 
	
	/**
     * Recursively deletes a directory or deletes the file
     * @param file or directory to recursively delete
     */
	public static void deleteRecursively(File file){
		if(file.isDirectory()){
			if(file.list().length==0){
				file.delete();
			}else{
				String files[] = file.list();
				for (String f : files) {
					File fileDelete = new File(file, f);
					//recursive delete
					deleteRecursively(fileDelete);
				}
				if(file.list().length==0){
					file.delete();
				}
			}
		}else{
			file.delete();
		}
	}
	
	/**
     * Saves the bitmap to the disk
     * @param bitmap bitmap to save to disk
     * @param urlIdentifier name to use for the bitmap
     */
	public static void saveImageToDisk(Bitmap bitmap, String urlIdentifier){
		
		if(bitmap == null)
			return;
		FileOutputStream fOut = null;
		
		try {

			String filePath = ApplicationConstants.ImagePath;
			File dir = new File(filePath);
			if(!dir.exists())
				dir.mkdirs();
			//encode the url to avoid characters not allowed in file name
			urlIdentifier = URLEncoder.encode(urlIdentifier, "UTF-8");
			File file = new File(dir, urlIdentifier);
			fOut = new FileOutputStream(file);
			
			bitmap.compress(urlIdentifier.toLowerCase(Locale.getDefault()).endsWith(".png")?Bitmap.CompressFormat.PNG:Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			
		} catch (FileNotFoundException e1) {
			Log.e(TAG, "saveImageToDisk: for url: " + urlIdentifier + "\n" + e1.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "saveImageToDisk: for url: " + urlIdentifier + "\n" + e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, "saveImageToDisk: for url: " + urlIdentifier + "\n" + e.getMessage());
		}
		
		finally{
			try {
				fOut.close();
			} catch (IOException e) {
				Log.e(TAG, "saveImageToDisk: for url: " + urlIdentifier + "\n" + e.getMessage());
			}
		}
	}
	
	/**
     *  Retrieves the image from the disk
     * @param url url used to identify the image name
     * @return bitmap stored in the disk
     */
	public static Bitmap getImageFromDisk(String url){
		
		Bitmap bitmap = null;
		
		try {
			url = URLEncoder.encode(url, "UTF-8");
			String filePath = ApplicationConstants.ImagePath;
			if(!new File(filePath+"/"+ url).exists())
				return bitmap;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bitmap = BitmapFactory.decodeFile(filePath+"/"+ url, options);
			
		} catch (Exception e) {

			Log.e(TAG, "getImageFromDisk: for url: " + url + "\n" + e.getMessage());
		}
		
		return bitmap;
	}
}
