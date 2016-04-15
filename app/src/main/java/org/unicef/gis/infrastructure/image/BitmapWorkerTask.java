package org.unicef.gis.infrastructure.image;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
	private static HashMap<String, Bitmap> cachedThumbnails = new HashMap<String, Bitmap>();
	
	private final WeakReference<ImageView> imageViewReference;
	private final Camera camera;
	private Uri file;
	private final int scale;
	
	private Bitmap thumbnailPlaceholder = null;
	
	public BitmapWorkerTask(Camera camera, int scale, ImageView imageView) { 
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.camera = camera;
		this.scale = scale;
	}

	@Override
	protected Bitmap doInBackground(Uri... params) {
		file = params[0];
		String plainUri = file.toString();
		
		updateThumbnailCache(plainUri);					
				
		return thumbnailOrPlaceholder(plainUri);
	}
	
	private Bitmap thumbnailOrPlaceholder(String plainUri) {
		Bitmap thumbnail = cachedThumbnails.get(plainUri);
		
		if (thumbnail == null) {
			if (thumbnailPlaceholder == null)
				thumbnailPlaceholder = camera.getPlaceholder();
			
			thumbnail = thumbnailPlaceholder;
		}
			
		return thumbnail;
	}

	private synchronized void updateThumbnailCache(String plainUri) {
		if (!cachedThumbnails.containsKey(plainUri)) {
			Bitmap thumbnail = camera.getThumbnail(file, scale);
			
			if (thumbnail != null)
				cachedThumbnails.put(plainUri, thumbnail);			
		}			
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		if (isCancelled()) {
			result = null;
		}
		
		if (imageViewReference != null && result != null){
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);			
			
			if (this == bitmapWorkerTask && imageView != null) {
				imageView.setImageBitmap(result);
			}
		}
	}
	
	public Uri getImageUri() {
		return file;
	}
	
	public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
	   if (imageView != null) {
	       final Drawable drawable = imageView.getDrawable();
	       if (drawable instanceof AsyncDrawable) {
	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	           return asyncDrawable.getBitmapWorkerTask();
	       }
	    }
	    return null;
	}
}
