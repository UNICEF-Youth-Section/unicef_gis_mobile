package org.unicef.gis.services;

import org.unicef.gis.infrastructure.data.TouchDbStoreAdapter;

import android.app.IntentService;
import android.content.Intent;

public class ReportAttachmentsService extends IntentService {

	public static final String IMAGE_URI = "image_uri";
	public static final String REPORT_ID = "report_id";

	public ReportAttachmentsService() {
		super("ReportAttachmentsService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		TouchDbStoreAdapter store = new TouchDbStoreAdapter(getApplicationContext());		
		store.addAttachment(intent.getStringExtra(REPORT_ID), intent.getStringExtra(IMAGE_URI));
	}
}
