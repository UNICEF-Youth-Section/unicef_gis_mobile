package org.unicef.gis.sync;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.data.CouchDbLiteStoreAdapter;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Report;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
	{
	    CBLURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	private static String charset = Charset.defaultCharset().displayName();
	
	private CouchDbLiteStoreAdapter store = null;
	
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        store = new CouchDbLiteStoreAdapter(getContext());
    }
    
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        store = new CouchDbLiteStoreAdapter(getContext());
    }
	
	@Override
	public void onPerformSync(Account account, Bundle bundle, String authority,
			ContentProviderClient provider, SyncResult syncResult) {								
		List<Report> reportsToSync = store.getPendingSyncReports();
		
		for (Report report : reportsToSync) {
			sync(report);
		}		
	}
	
	private File rotateImageIfNecessary(File imageFile) {
		Log.d("SyncAdapter", "Rotating image");
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;		
		options.inSampleSize = 1;
		
		Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(imageFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("SyncAdapter", "Couldn't open EXIF data, settling with the original image.");
			return imageFile;
		}
		
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		
		Matrix matrix = new Matrix();
		matrix.postRotate(exifOrientationToDegrees(orientation));
		
		Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
		
		String rotatedFileName = imageFile.getParent() + "/rotated-" + imageFile.getName();
		
		File rotated = null;
		FileOutputStream out = null;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try {
			rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
			rotated = new File(rotatedFileName);
			rotated.createNewFile();
			
			out = new FileOutputStream(rotated);
			out.write(bytes.toByteArray());			
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
			Log.d("SyncAdapter", "Couldn't save rotated image, settling with original image.");
			return imageFile;
		} catch (IOException e) {
			e.printStackTrace();	
			Log.d("SyncAdapter", "Couldn't save rotated image, settling with original image.");
			return imageFile;
		} finally {
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("SyncAdapter", "Couldn't save rotated image, settling with original image.");
					return imageFile;
				}
			}
		}
		
		return new File(rotatedFileName);
	}

	private float exifOrientationToDegrees(int orientation) {
		switch (orientation) {
		case ExifInterface.ORIENTATION_NORMAL:
			return 0;
		case ExifInterface.ORIENTATION_ROTATE_180:
			return 180;
		case ExifInterface.ORIENTATION_ROTATE_90:
			return 90;
		case ExifInterface.ORIENTATION_ROTATE_270:
			return 270;
		default:
			return 0;
		}
	}
	
	private void sync(Report report) {
		OutputStream out = null;
		HttpURLConnection conn = null;
		
		File originalImage = Camera.fileFromString(report.getImageUri());
		File image = rotateImageIfNecessary(originalImage);
		
		int status = 0;
		try {
		    String boundary = Long.toHexString(System.currentTimeMillis());
			
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(report);
			
			Log.d("SyncAdapter", "Sending JSON string to server: " + json);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			writeMultipart(boundary, bos, false, json, image);										
			
			byte[] extra = bos.toByteArray();			
            int contentLength = extra.length;            
            contentLength += image.length();            
            contentLength += json.getBytes(Charset.defaultCharset()).length;
            
            conn = openConnection(boundary, contentLength);						
			
			out = conn.getOutputStream();
			writeMultipart(boundary, out, true, json, image);
			
			status = conn.getResponseCode();					
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServerUrlPreferenceNotSetException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (conn != null) {
                conn.disconnect();
            }
			
			if (status == HttpURLConnection.HTTP_OK)
				markAsSyncd(report);
			else
				markAsNotSyncd(report);
		}
	}

	private void markAsNotSyncd(Report report) {
		updateSyncData(report, false);
	}

	private void markAsSyncd(Report report) {
		updateSyncData(report, true);
	}

	private void updateSyncData(Report report, boolean synced) {
		report.setAttempts(report.getAttempts() + 1);
		report.setSyncedData(synced);
		report.setSyncedImage(synced);
		
		store.updateReport(report);
	}

	private HttpURLConnection openConnection(String boundary, int contentLength) throws IOException,
			MalformedURLException, ServerUrlPreferenceNotSetException {		
		
		RoutesResolver r = new RoutesResolver(getContext());
		
		HttpURLConnection conn = (HttpURLConnection) r.syncReport().openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setDoOutput(true);
		
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setFixedLengthStreamingMode(contentLength);
        
		conn.connect();
		return conn;
	}
	
	private void writeMultipart(String boundary, OutputStream output, boolean writeContent, String jsonBody, File image) throws IOException {		
	    BufferedWriter writer = null;
	    try {
	        writer = new BufferedWriter(new OutputStreamWriter(output, Charset.forName(charset)), 8192);
	        if (jsonBody != null) {
	            writer.write("--" + boundary);
	            writer.write("\r\n");
	            writer.write(
	                    "Content-Disposition: form-data; name=\"parameters\"");
	            writer.write("\r\n");
	            writer.write("Content-Type: application/json; charset=" + charset);
	            writer.write("\r\n");
	            writer.write("\r\n");
	            if (writeContent) {
	                writer.write(jsonBody);
	            }
	            writer.write("\r\n");
	            writer.flush();
	        }
	        
	        // Send binary file.
	        writer.write("--" + boundary);
	        writer.write("\r\n");
	        writer.write("Content-Disposition: form-data; name=\""
	                + image.getName() + "\"; filename=\""
	                + image.getName()  + "\"");
	        writer.write("\r\n");
	        writer.write("Content-Type: " + URLConnection.guessContentTypeFromName(image.getName()));
	        writer.write("\r\n");
	        writer.write("Content-Transfer-Encoding: binary");
	        writer.write("\r\n");
	        writer.write("\r\n");
	        writer.flush();
	        
	        if (writeContent) {
	            InputStream input = null;
	            try {
	                input = new FileInputStream(image);
	                byte[] buffer = new byte[1024];
	                
	                for (int length = 0; (length = input.read(buffer)) > 0;) {
	                    output.write(buffer, 0, length);
	                }
	                
	                // Don't close the OutputStream yet
	                output.flush();
	            } catch (IOException e) {
	                Log.w("SyncAdapter", e);
	            } finally {
	            	if (input != null) {
	                    try {
	                        input.close();
	                    } catch (IOException e) {}
	            	} 
	            }
	        }
	        
	        // This CRLF signifies the end of the binary data chunk
	        writer.write("\r\n");
	        writer.flush();
	        
	        // End of multipart/form-data.
	        writer.write("--" + boundary + "--");
	        writer.write("\r\n");
	        writer.flush();
	    } finally {
	        if (writer != null) {
	            writer.close();
	        }
	    } 
	}
}
