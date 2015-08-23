package com.peeyush.apppactera.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.peeyush.apppactera.constants.ApplicationConstants;
import com.peeyush.apppactera.network.ImageDownloader;
import com.peeyush.apppactera.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/*
 * Class to manage image downloads 
 */
public class ImageDownloadManager {
	
	private static ImageDownloadManager instance = new ImageDownloadManager();
    
	//in-memory cache of downloaded bitmaps
	private Map<String, BitmapWrapper> imageCache = new ConcurrentHashMap<String, BitmapWrapper>();
	
	//stores additional information of the requested url eg. view, identifier, callback
	private Map<String, ImageRequest> imageRequestInformation = new ConcurrentHashMap<String, ImageRequest>();

	//stores image requests that have to processed
	List<String> pendingRequests = Collections.synchronizedList(new LinkedList<String>());
	
	//maintains list of ImageDownloadTasks(to handle cancellation of all currently executing tasks)
	List<ImageDownloadTask> executingTasks = new ArrayList<ImageDownloadTask>();
	
	//max concurrent downloads allowed
	int MAX_REQUEST_COUNT = 10;
	
	//number of downloads happening
	int requestInProcessCount = 0;
	
	static Object lock = "";
	private static String TAG = "ImageDownloadManager";
	
	public static ImageDownloadManager getInstance()
    {
        synchronized (ImageDownloadManager.class)
        {
            if (instance == null)
            {
                instance = new ImageDownloadManager();
            }
        }

        return instance;
    }
	
	/**
     * Resets the image manager to default state, interrupting existing dowloads(if any),
     * clearing all the caches 
     * @param 
     */
	public void resetImageManager(){
		
		new Thread(new Runnable() {
			@Override
			public void run() {

				String filePath = ApplicationConstants.ImagePath;
				File dir = new File(filePath);
				if(dir.exists())
					FileUtils.deleteRecursively(dir);
			}
		}).start();
		
		Iterator<ImageDownloadTask> iter = executingTasks.iterator();
		while(iter.hasNext()){  
			iter.next().cancel(true);
		}
		
		imageCache.clear();
		imageRequestInformation.clear();
		pendingRequests.clear();
		requestInProcessCount=0;

	}
	
	/**
     * Calls the passed delegate with the image from cache, if exists or 
     * enqueues the url for the image to be downloaded
     * @param delegate delegate to callback with the downloaded inage
     * @param imageView imageView associated with the current url (this may change to recycling!)
     * @param identifier identifier the delegate method can use to find the view associated with the url
     * @param url url to download the image from
     */
	public void RequestImage(ImageResponseDelegate delegate, ImageView imageView, Object identifier, String url)
	{
		//check for containsKey to avoid requesting same url when called multiple times through getView(...). 
		//imageCache uses a wrapper(BitmapWrapper) for null bitmaps. Consequently we will not make
		//a request again for bitmaps already in imageCache(even null bitmaps) until user refreshes the list
		if(imageCache.containsKey(url))
		{	
			Bitmap bitmap = imageCache.get(url).getBitmap();
			delegate.onImageLoad(imageView, url, identifier, bitmap, false);
			return;
		}
		Log.i(TAG, "imageData: " + imageCache.toString() + " check queue for " + url);
		
		synchronized (lock) {
			
			if(pendingRequests.contains(url)){
				//update the information object for the url as it could have changed due to recycling of convertView by android
				imageRequestInformation.put(url, new ImageRequest(delegate, imageView, identifier, url));
				return;
			}
			
			Log.i(TAG, "pendingRequests: " + pendingRequests.toString() + "queue download for " + url);
			//enqueue the url in the downloading queue
			pendingRequests.add(url);
		}
		imageRequestInformation.put(url, new ImageRequest(delegate, imageView, identifier, url));
		processNextRequest();
	}
	
	/**
     * Processes the next url if concurrent requests' count is less than the predefined concurrency limit
     */
	private void processNextRequest(){
		String url = null;
		synchronized (lock) {
			
			if(pendingRequests.size()==0)
				return;
			if(requestInProcessCount>=MAX_REQUEST_COUNT || requestInProcessCount >= pendingRequests.size())
				return;
			
			url = pendingRequests.get(0+requestInProcessCount);

			
			if(url!=null){
				ImageDownloadTask imageDownloadTask = new ImageDownloadTask();
				executingTasks.add(imageDownloadTask);
				requestInProcessCount++;
				//call executor to request a parallel download
				imageDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			}
		}
	}
	
	/**
     * Extended Async class to download images
     */
	class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

		//url associated with the current download
		String url ;
        public ImageDownloadTask() {
        }
        
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(final String... urls) {

	        Bitmap bitmap = null;
	        
			try {
				url = urls[0];
				Log.i(TAG, "doInBackground: " + "Calling download for: " + url);
				
				bitmap = new ImageDownloader().downloadBitmap(url);
				//we will keep null bitmaps(wrapped due to imageCache being a ConcurrentHashMap) as well to avoid re-requesting every-time view is drawn
				imageCache.put(url, new BitmapWrapper(bitmap));
			} catch (Exception e) {
				Log.e(TAG, "doInBackground: " + e.getMessage());
			}
	        
	        return bitmap;
        }

        protected void onPostExecute(final Bitmap bitmap) {
        	
        	requestInProcessCount--;
        	if(pendingRequests.size()>0){
	        	pendingRequests.remove(url);
	        	processNextRequest();
        	}
        	ImageRequest imgRequest = imageRequestInformation.get(url);
        	
        	String tag = (String)imgRequest.mImageView.getTag();
        	
        	//check if url stored in tag of the view is same or not: it is possible that this view was recycled and now belongs to some other cell
        	if(tag!=null && tag.equals(url) ){
        		imgRequest.mDelegate.onImageLoad(imgRequest.mImageView, url, imgRequest.mIdentifier, bitmap, true);
        	}
        	//if view did get recycled, call the delegate in case it wants to update the view based on identifier 
        	if(tag!=null && !tag.equals(url)){
        		imgRequest.mDelegate.onImageLoad(null, url, imgRequest.mIdentifier, bitmap, true);
        	}
        }
    }
	
	/*
	 * Information object for a url request
	 */
	private class ImageRequest{

		ImageResponseDelegate mDelegate;
		ImageView mImageView;
		Object mIdentifier;
		String mUrl;
		
		public ImageRequest(ImageResponseDelegate delegate, ImageView imageView, Object identifier, String url) {
			mDelegate = delegate;
			mImageView = imageView;
			mIdentifier = identifier;
			mUrl = url;
		}
		
	}
	/*
	 * Class to help save null bitmaps in imageCache
	 */
	private class BitmapWrapper{
		
		private Bitmap mBitmap;
		
		BitmapWrapper(Bitmap bitmap){
			mBitmap = bitmap;
		}

		public Bitmap getBitmap() {
			return mBitmap;
		}
	}

}
