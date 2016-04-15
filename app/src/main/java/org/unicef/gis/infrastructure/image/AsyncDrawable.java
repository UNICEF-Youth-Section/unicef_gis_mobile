package org.unicef.gis.infrastructure.image;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<BitmapWorkerTask> worker;
	
	public AsyncDrawable(final Resources res, final Bitmap bitmap, final BitmapWorkerTask bitmapWorkerTask){
		super(res, bitmap);
		this.worker = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	}
	
	public BitmapWorkerTask getBitmapWorkerTask() {
		return worker.get();
	}
}
