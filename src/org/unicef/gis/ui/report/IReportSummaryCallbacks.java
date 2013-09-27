package org.unicef.gis.ui.report;

public interface IReportSummaryCallbacks {
	void descriptionChanged(String string);
	void postToTwitterChanged(boolean shouldPost);
	void postToFacebookChanged(boolean shouldPost);
}
