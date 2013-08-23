package org.unicef.gis.ui.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.data.UnicefGisDbContract;
import org.unicef.gis.infrastructure.image.AsyncDrawable;
import org.unicef.gis.infrastructure.image.BitmapWorkerTask;
import org.unicef.gis.infrastructure.image.Camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ReportRowAdapter extends SimpleCursorAdapter {

	private static final int THUMBNAIL_TO_SCREEN_FACTOR = 16;
	private final Camera camera;
	private final int layout;	

	public ReportRowAdapter(Activity context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.layout = layout;
		this.camera = new Camera(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView thumbnailView = (ImageView) view.findViewById(R.id.row_report_thumbnail);
		TextView descriptionView = (TextView) view.findViewById(R.id.row_report_description);
		TextView timestampView = (TextView) view.findViewById(R.id.row_report_date_time);

		int thumbnailCol = cursor.getColumnIndex(UnicefGisDbContract.Report.COLUMN_NAME_IMAGE);
		int descriptionCol = cursor.getColumnIndex(UnicefGisDbContract.Report.COLUMN_NAME_TITLE);
		int timestampCol = cursor.getColumnIndex(UnicefGisDbContract.Report.COLUMN_NAME_TIMESTAMP);

		Uri imageUri = Uri.parse(cursor.getString(thumbnailCol));
		String description = cursor.getString(descriptionCol);
		String timestamp = cursor.getString(timestampCol);

		asyncLoadThumbnail(thumbnailView, imageUri);
		descriptionView.setText(description);
		timestampView.setText(formatTimestamp(timestamp));
	}

	@SuppressLint("SimpleDateFormat")
	private CharSequence formatTimestamp(String timestamp) {
        String s = timestamp.replace("Z", "+00:00");
        
        try {
            s = s.substring(0, 22) + s.substring(23);
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);                                 
            return DateFormat.format("MM/dd/yyyy, kk:mm", date);
        } catch (IndexOutOfBoundsException e) {
        	Log.d("ReportRowAdapter", "Invalid timestamp: " + timestamp);            
        } catch (ParseException e) {
			Log.d("ReportRowAdapter", "Invalid timestamp: " + timestamp);			
		}
        
        return "invalid date";
	}

	private void asyncLoadThumbnail(ImageView thumbnailView, Uri imageUri) {
		if (cancelPotentialWork(thumbnailView, imageUri)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(camera,
					THUMBNAIL_TO_SCREEN_FACTOR, thumbnailView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(Resources.getSystem(), Camera.PLACEHOLDER, task);
			thumbnailView.setImageDrawable(asyncDrawable);
			task.execute(imageUri);
		}
	}

	private boolean cancelPotentialWork(ImageView thumbnailView, Uri imageUri) {
		final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask
				.getBitmapWorkerTask(thumbnailView);

		if (bitmapWorkerTask != null) {
			final Uri taskUri = bitmapWorkerTask.getImageUri();
			if (!taskUri.equals(imageUri)) {
				bitmapWorkerTask.cancel(true);
			} else {
				return false;
			}
		}

		return true;
	}
}
