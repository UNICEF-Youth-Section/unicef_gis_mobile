package org.unicef.gis.ui.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.image.AsyncDrawable;
import org.unicef.gis.infrastructure.image.BitmapWorkerTask;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Report;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportRowAdapter extends ArrayAdapter<Report> {
	private LayoutInflater inflater; 
	
	private static final int THUMBNAIL_TO_SCREEN_FACTOR = 16;
	private final Camera camera;
	private final int layout;	

	public ReportRowAdapter(Activity context, int layout) {		
		super(context, layout);
		this.layout = layout;
		this.camera = new Camera(context);
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		if (convertView == null) {
			view = inflater.inflate(layout, parent, false);
		} else {
			view = convertView;
		}
		
		Report report = getItem(position);
	
		ImageView thumbnailView = (ImageView) view.findViewById(R.id.row_report_thumbnail);
		TextView descriptionView = (TextView) view.findViewById(R.id.row_report_description);
		TextView timestampView = (TextView) view.findViewById(R.id.row_report_date_time);
		TextView syncedView = (TextView) view.findViewById(R.id.row_report_synced);

		Uri imageUri = Uri.parse(report.getImageUri());
		String description = report.getTitle();
		String timestamp = report.getTimestamp();
		boolean synced = report.getSyncedImage();

		asyncLoadThumbnail(thumbnailView, imageUri);
		descriptionView.setText(description);
		timestampView.setText(formatTimestamp(timestamp));
		syncedView.setText(synced ? "Uploaded" : "");
		
		return view;
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
			final AsyncDrawable asyncDrawable = new AsyncDrawable(Resources.getSystem(), camera.getPlaceholder(), task);
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
