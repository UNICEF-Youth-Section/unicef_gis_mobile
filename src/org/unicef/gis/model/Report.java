package org.unicef.gis.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.ektorp.support.OpenCouchDbDocument;

import com.couchbase.cblite.CBLRevision;

import android.location.Location;
import android.net.Uri;

public class Report extends OpenCouchDbDocument {	
	public static final String TYPE = "report";
	
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String ID_KEY = "_id";
	public static final String TITLE_KEY = "title";
	public static final String LATITUDE_KEY = "latitude";
	public static final String LONGITUDE_KEY = "longitude";
	public static final String IMAGE_URI_KEY = "imageUri";
	public static final String TAGS_KEY = "tags";
	public static final String REVISION_KEY = "_rev";
	public static final String SYNCED_DATA_KEY = "syncedData";
	public static final String SYNCED_IMAGE_KEY = "syncedImage";
	public static final String TYPE_KEY = "type";
	public static final String ATTEMPTS_KEY = "attempts";
	
	private static final long serialVersionUID = 1L;
		
	private String title;
	private Double latitude;
	private Double longitude;
	private List<String> tags;
	private String imageUri;
	private String timestamp;
	private Boolean syncedData;
	private Boolean syncedImage;
	private String type;
	private Integer attempts;
	
	public Report(String title, Location location, Uri imageUri, List<String> tags) {		
		this(title, location.getLatitude(), location.getLongitude(), imageUri.toString(), tags);
	}
	
	public Report(String title, Double latitude, Double longitude, String imageUri, List<String> tags) {
		type = TYPE;
		
		this.title = title;
		
		this.latitude = latitude;
		this.longitude = longitude;
				
		this.tags = tags;
		this.imageUri = imageUri.toString();
		
		this.timestamp = generateTimestamp();
		
		setSyncedData(false);
		setSyncedImage(false);
		setAttempts(0);
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

	@SuppressWarnings("unchecked")
	public static List<Report> collectionFromMap(
			List<Map<String, Object>> plainReports) {
		List<Report> reports = new ArrayList<Report>();

		for (Map<String, Object> plainReport : plainReports) {
			reports.add(fromMap((Map<String, Object>) plainReport.get("value")));
		}
		
		return reports;
	}

	@SuppressWarnings("unchecked")
	public static Report fromMap(Map<String, Object> objReport) {		
		String plainTitle = (String)objReport.get(TITLE_KEY);
		Double plainLatitude = (Double) objReport.get(LATITUDE_KEY); 
		Double plainLongitude = (Double) objReport.get(LONGITUDE_KEY);
		String imageUri = (String) objReport.get(IMAGE_URI_KEY); 
		List<String> tags = (List<String>) objReport.get(TAGS_KEY);
		String timestamp = (String) objReport.get(TIMESTAMP_KEY);
		Boolean syncedData = (Boolean) objReport.get(SYNCED_DATA_KEY);
		Boolean syncedImage = (Boolean) objReport.get(SYNCED_IMAGE_KEY);
		Integer attempts = (Integer) objReport.get(ATTEMPTS_KEY);
		
		Report report = new Report(plainTitle, plainLatitude, plainLongitude, imageUri, tags);		
		report.setId((String) objReport.get(ID_KEY));
		report.setRevision((String) objReport.get(REVISION_KEY));
		report.setTimestamp(timestamp);
		report.setSyncedData(syncedData);
		report.setSyncedImage(syncedImage);
		report.setAttempts(attempts);
		
		return report;
	}

	public static Report fromRevision(CBLRevision documentWithIDAndRev) {		
		return null;
	}

	public Boolean getSyncedData() {
		return syncedData;
	}

	public void setSyncedData(Boolean syncedData) {
		this.syncedData = syncedData;
	}

	public Boolean getSyncedImage() {
		return syncedImage;
	}

	public void setSyncedImage(Boolean syncedImage) {
		this.syncedImage = syncedImage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}
}
