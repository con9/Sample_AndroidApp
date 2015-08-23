package com.peeyush.apppactera.data;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageResponseDelegate {

	void onImageLoad(ImageView imageView, String url, Object identifier, Bitmap bitmap, boolean inValidate);
}
