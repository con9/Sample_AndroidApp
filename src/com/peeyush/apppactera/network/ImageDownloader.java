package com.peeyush.apppactera.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import com.peeyush.apppactera.constants.ApplicationConstants;
import com.peeyush.apppactera.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class ImageDownloader {

	private static String TAG = "ImageDownloader";
	
	/**
     * Fetches the downloaded image from disk or if not found, downloads if from the given url 
     *
     * @param url url to download image from
     * @return Downloaded bitmap
     */
	public Bitmap downloadBitmap(final String url) {

		Log.i(TAG, "downloadBitmap:" + " download bitmap for url: " + url);
        InputStream is = null;
        BufferedInputStream bis = null;
		try {
			
            Bitmap bitmap=null;
            //get image from disk if already downloaded previously
			bitmap = FileUtils.getImageFromDisk(url);

			if(bitmap!=null)
				return bitmap;
					
            URL imageUrl = new URL(url);
            final HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            //in case, we do not receive a response back in 10 secs, disconnect 
            new Thread(new Runnable() {
				
				@Override
				 public void run() {
	                try {
	                    Thread.sleep(10000);
	                } catch (InterruptedException e) {
	                	Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + e.getMessage());
	                }
	                Log.i(TAG, "downloadBitmap:" + " Force quitting for url: " + url);
	                ((HttpURLConnection)conn).disconnect();
	            }
			}).start();
           
            is=conn.getInputStream();
            
            bis = new BufferedInputStream(is, 8190);

            ByteArrayBuffer buffer = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
            	buffer.append((byte)current);
            }
            byte[] imageData = buffer.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            //if there was no exception but bitmap is still null, tring retrieving bitmap using legacy httpClient
            if(bitmap==null)
            	bitmap = retryDownloadWithUrlConnection(url);
            //scale the bitmap to a predefined size
            bitmap = scaleBitmap(bitmap);
            
            conn.disconnect();
            //save image to disk with url as the file name
            FileUtils.saveImageToDisk(bitmap, url);
            return bitmap;
        } 
		catch (java.net.SocketTimeoutException e) {
			Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + e.getMessage());
		} 
		catch (java.io.IOException e) {
			Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + e.getMessage());
		}
		catch (Throwable ex){
			Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + ex.getMessage());
            if(ex instanceof OutOfMemoryError)
        	   return null;
        }
		finally{
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + e.getMessage());
				}
			if(bis!=null)
				try {
					bis.close();
				} catch (IOException e) {
					Log.e(TAG, "downloadBitmap: for url: " + url + "\n" + e.getMessage());
				}
		}
		
		return null;
	}
	
	/**
     * Scales the bitmap to a predefined value
     * 
     * @param bitmap bitmap to scale 
     * @return Scaled bitmap
     */
	private Bitmap scaleBitmap(Bitmap bitmap) {

			if(bitmap == null)
				return null;
		    int width = bitmap.getWidth();
		    int height = bitmap.getHeight();
		    float scaleWidth = ((float) 280) / width;
		    float scaleHeight = ((float) 280) / height;
		    // Create a matrix for manipulation
		    Matrix matrix = new Matrix();
		    // resize the bitmap
		    matrix.postScale(scaleWidth, scaleHeight);

		    // create a new bitmap based on matrix
		    Bitmap resizedBitmap = Bitmap.createBitmap(
		        bitmap, 0, 0, width, height, matrix, false);
		    return resizedBitmap;
        
	}
	
	/**
     * Downloads a bitmap using legacy HTTPClient
     * 
     * @param url url to download image from
     * @return Downloaded bitmap
     */
	private Bitmap retryDownloadWithUrlConnection(String url){
		
		HttpGet getRequest = null;
		Bitmap bitmap = null;
		try {

			final DefaultHttpClient client = new DefaultHttpClient();
			getRequest = new HttpGet(url);
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != ApplicationConstants.HttpStatus.OK) {
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {

					inputStream = entity.getContent();
					bitmap = BitmapFactory.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			if(getRequest!=null)
				getRequest.abort();
		} finally{
		}
		return bitmap;
	}
	
}
