package org.unicef.gis.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.location.Location;
import android.net.Uri;

import com.couchbase.lite.QueryRow;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Report {
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
	public static final String POST_TO_TWITTER_KEY = "postToTwitter";
	public static final String POST_TO_FACEBOOK_KEY = "postToFacebook";
	
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
	
	private Boolean postToTwitter;
	private Boolean postToFacebook;

	private String _id;
	private String _revision;

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
		
		this.setPostToTwitter(false);
		this.setPostToFacebook(false);
		
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
			List<QueryRow> plainReports) {
		List<Report> reports = new ArrayList<Report>();

		for (QueryRow plainReport : plainReports) {
//			This cast is here to transform the received full document into a Map object.
// 			Which will then be worked into whatever extra class is needed
			reports.add(fromMap((Map<String, Object>) plainReport.getValue()));
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
		Boolean postToTwitter = (Boolean) objReport.get(POST_TO_TWITTER_KEY);
		Boolean postToFacebook = (Boolean) objReport.get(POST_TO_FACEBOOK_KEY);
		
		Report report = new Report(plainTitle, plainLatitude, plainLongitude, imageUri, tags);		
		report.setId((String) objReport.get(ID_KEY));
		report.setRevision((String) objReport.get(REVISION_KEY));
		report.setTimestamp(timestamp);
		report.setSyncedData(syncedData);
		report.setSyncedImage(syncedImage);
		report.setAttempts(attempts);
		report.setPostToTwitter(postToTwitter);
		report.setPostToFacebook(postToFacebook);
		
		return report;
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

	public String json() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}

	public Boolean getPostToTwitter() {
		return postToTwitter;
	}

	public void setPostToTwitter(Boolean postToTwitter) {
		this.postToTwitter = postToTwitter;
	}

	public Boolean getPostToFacebook() {
		return postToFacebook;
	}

	public void setPostToFacebook(Boolean postToFacebook) {
		this.postToFacebook = postToFacebook;
	}

	public void setId(String id) {
		this._id = id;
	}

	public void setRevision(String revision) {
		this._revision = revision;
	}

	@JsonProperty("_id")
	public String getId() {
		return _id;
	}

	@JsonProperty("_rev")
	public String getRevision() {
		return _revision;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(LATITUDE_KEY, getLatitude());
		map.put(LONGITUDE_KEY, getLongitude());
		map.put(TITLE_KEY, getTitle());
		map.put(TAGS_KEY, getTags());
		map.put(IMAGE_URI_KEY, getImageUri());
		map.put(TIMESTAMP_KEY, getTimestamp());
		map.put(SYNCED_DATA_KEY, getSyncedData());
		map.put(SYNCED_IMAGE_KEY, getSyncedImage());
		map.put(ATTEMPTS_KEY, getAttempts());
		map.put(POST_TO_TWITTER_KEY, getPostToTwitter());
		map.put(POST_TO_FACEBOOK_KEY, getPostToFacebook());
		map.put(TYPE_KEY, TYPE);
		return map;
	}
}
