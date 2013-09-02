package org.unicef.gis.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.ektorp.support.OpenCouchDbDocument;

import android.location.Location;
import android.net.Uri;

public class Report extends OpenCouchDbDocument {	
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String ID_KEY = "_id";
	private static final Object TITLE_KEY = "title";
	private static final Object LATITUDE_KEY = "latitude";
	private static final Object LONGITUDE_KEY = "longitude";
	private static final Object IMAGE_URI_KEY = "imageUri";
	private static final Object TAGS_KEY = "tags";
	private static final Object REVISION_KEY = "_rev";
	
	private static final long serialVersionUID = 1L;
		
	private String title;
	private Double latitude;
	private Double longitude;
	private List<String> tags;
	private String imageUri;
	private String timestamp;
	
	public Report(String title, Location location, Uri imageUri, List<String> tags) {		
		this(title, location.getLatitude(), location.getLongitude(), imageUri.toString(), tags);
	}
	
	public Report(String title, Double latitude, Double longitude, String imageUri, List<String> tags) {
		this.title = title;
		
		this.latitude = latitude;
		this.longitude = longitude;
				
		this.tags = tags;
		this.imageUri = imageUri.toString();
		
		this.timestamp = generateTimestamp();		
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public String getImageUri() {
		return imageUri;
	}
	
	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	private String generateTimestamp() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(new Date());		
	}
	
	@Override
    public boolean equals(Object o) {
        if(o instanceof Report) {
            Report other = (Report)o;
            if(getId() != null && other.getId() != null && getId().equals(other.getId())) {
                return true;
            }
        }
        return false;
    }

	public static List<Report> collectionFromMap(
			List<Map<String, Object>> plainReports) {
		List<Report> reports = new ArrayList<Report>();

		for (Map<String, Object> plainReport : plainReports) {
			reports.add(fromMap(plainReport));
		}
		
		return reports;
	}

	@SuppressWarnings("unchecked")
	private static Report fromMap(Map<String, Object> plainReport) {
		Map<String, Object> objReport = (Map<String, Object>) plainReport.get("value");
		
		String plainTitle = (String)objReport.get(TITLE_KEY);
		Double plainLatitude = (Double) objReport.get(LATITUDE_KEY); 
		Double plainLongitude = (Double) objReport.get(LONGITUDE_KEY);
		String imageUri = (String) objReport.get(IMAGE_URI_KEY); 
		List<String> tags = (List<String>) objReport.get(TAGS_KEY);
		String timestamp = (String) objReport.get(TIMESTAMP_KEY);
		
		Report report = new Report(plainTitle, plainLatitude, plainLongitude, imageUri, tags);		
		report.setId((String) objReport.get(ID_KEY));
		report.setRevision((String) objReport.get(REVISION_KEY));
		report.setTimestamp(timestamp);

		return report;
	}
}
