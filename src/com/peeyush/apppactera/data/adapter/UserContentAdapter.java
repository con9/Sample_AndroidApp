package com.peeyush.apppactera.data.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Identity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.peeyush.apppactera.data.ImageDownloadManager;
import com.peeyush.apppactera.data.ImageResponseDelegate;
import com.peeyush.apppactera.ui.RowData;
import com.peeyush.apppactera1.R;

/*
 * Adapter class for our listview
 */
public class UserContentAdapter extends BaseAdapter implements ImageResponseDelegate{

	private Context mContext;
	//layoutID that defines our list items
	private int layoutID;
	//list to hold each row data
	private List<RowData> mRowDataList;
	private ListView mlistView = null;

	/**
     * Adapter for our listview
     * @param context context to use
     * @param resourceId resourdId containing list item definitions
     * @param rowDataList list containing the data
     */
	public UserContentAdapter(Context context, int resourceId,
			List<RowData> rowDataList){
		mContext = context;
		layoutID = resourceId;
		mRowDataList = rowDataList;
	}
	
	@Override
	public int getCount() {

		if(mRowDataList==null)
			return 0;
		return mRowDataList.size();
	}

	@Override
	public RowData getItem(int position) {
		return mRowDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(mlistView==null)
			mlistView = (ListView)parent;
		final ViewHolder holder;
		if (convertView != null && convertView.getTag() != null) {
			holder = (ViewHolder) convertView.getTag();
		}
		else
		{
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) mContext)
						.getLayoutInflater();
				convertView = inflater.inflate(layoutID, null);
			}
			
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.desc = (TextView) convertView.findViewById(R.id.desc);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		}
		RowData rowData = mRowDataList.get(position);
		holder.image.setTag(rowData.getImageUrl());
		holder.image.setImageDrawable(null);
		
		holder.title.setText(rowData.getTitle());
		
		if(rowData.getDesc()!=null){
			holder.desc.setText(rowData.getDesc());
			holder.desc.setVisibility(View.VISIBLE);
		}
		else{
			holder.desc.setText("");
			holder.desc.setVisibility(View.GONE);
		}

		holder.image.setVisibility(View.GONE);
		if(rowData.getImageUrl()!=null){
			ImageDownloadManager.getInstance().RequestImage(this, holder.image, position, rowData.getImageUrl());
		}
		
		return convertView;
	}
	
	/*
	 * Class to hold a reference to the list items
	 */
	static class ViewHolder{
		public TextView title;
		public TextView desc;
		public ImageView image;
	}
	
	/**
     * This method is called when image request has been completed.
     * @param imageView view that was associated with the url when image was requested
     * @param url url of the requested image
     * @param identifier identifier the caller can use to obtain additional information
     * @param bitmap fetched bitmap
     * @param inValidate true if view is visible and hence it should be redrawn
     */
	@Override
	public void onImageLoad(ImageView imageView, String url, Object identifier, Bitmap bitmap, boolean inValidate) {
		
		if(inValidate){
			int pos = (Integer)identifier;
			//find the correct imageView based on the position(if visible)
			if(pos<=mlistView.getLastVisiblePosition() && pos>=mlistView.getFirstVisiblePosition()){

				View v = mlistView.getChildAt(pos-mlistView.getFirstVisiblePosition());
				imageView = (ImageView)v.findViewById(R.id.image);
				mlistView.invalidateViews();
			}
		}
		if(imageView!=null){
			imageView.setImageBitmap(bitmap);
			if(bitmap!=null){
				imageView.setVisibility(View.VISIBLE);
				imageView.invalidate();
				//adjust the imageview width
				imageView.setLayoutParams(new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
			}else
				imageView.setVisibility(View.GONE);
		}
	}

}
