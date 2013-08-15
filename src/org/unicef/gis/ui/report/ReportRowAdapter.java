package org.unicef.gis.ui.report;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.Camera;
import org.unicef.gis.infrastructure.data.UnicefGisDbContract;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ReportRowAdapter extends SimpleCursorAdapter {

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

	    bind(cursor, v);
	    
	    return v;
	}

	private void bind(Cursor cursor, View v) {
		ImageView thumbnailView = (ImageView) v.findViewById(R.id.row_report_thumbnail);
	    TextView descriptionView = (TextView) v.findViewById(R.id.row_report_description);
	    	    
	    int thumbnailCol = cursor.getColumnIndex(UnicefGisDbContract.Report.COLUMN_NAME_IMAGE);
	    int descriptionCol = cursor.getColumnIndex(UnicefGisDbContract.Report.COLUMN_NAME_TITLE);
	    
	    String description = cursor.getString(descriptionCol);
	    Uri imageUri = Uri.parse(cursor.getString(thumbnailCol));
	    
	    Bitmap thumbnail = camera.getThumbnail(imageUri, thumbnailView.getLayoutParams().width, thumbnailView.getLayoutParams().height);

	    thumbnailView.setImageBitmap(thumbnail);
	    descriptionView.setText(description);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		bind(cursor, view);
	}
}
