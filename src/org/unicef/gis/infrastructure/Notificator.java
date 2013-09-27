package org.unicef.gis.infrastructure;

import org.unicef.gis.model.Report;
import org.unicef.gis.ui.MyReportsActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class Notificator {
	
	public static final String BROADCAST_ACTION = "org.unicef.gis.infrastructure.notificator.BROADCAST";
	public static final String REPORTS_UPDATED = "notificator_reports_updated";
	
	private Context context;
	
	public Notificator(Context context) {
		this.context = context;
	}
	
	public void notifyReportUploaded(Report report) {
		createNotificationItem(report);
	}

	private void createNotificationItem(Report report) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
			.setSmallIcon(android.R.drawable.ic_menu_upload)
			.setContentTitle("Report was uploaded.")
			.setContentText("Report " + report.getTitle() + " was uploaded.");
		
		Intent resultIntent = new Intent(context, MyReportsActivity.class);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MyReportsActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(report.getId().hashCode(), mBuilder.build());
	}
}
