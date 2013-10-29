package org.unicef.gis.infrastructure.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unicef.gis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Camera {
	private static Bitmap PLACEHOLDER = null;  
	
	public static final int TAKE_PICTURE_INTENT = 10;
	
	private static String UNICEF_GIS_ALBUM = "UNICEF-GIS-ALBUM";
	private static String JPEG_PREFIX = "pic";
	private static String JPEG_FILE_SUFFIX = ".jpg";

	private final Context context;
	
	public Camera(Context context) {
		this.context = context;
	}
	
	private Bitmap loadPlaceholder() {
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.content_picture);
	}
	
	public Bitmap getPlaceholder() {
		if (PLACEHOLDER == null)
			PLACEHOLDER = loadPlaceholder();
		
		return PLACEHOLDER;
	}
	

	public File takePicture() throws IOException {
		File f = createImageFile();
		Uri uri = Uri.fromFile(f);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);				
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		
		((Activity)context).startActivityForResult(takePictureIntent, TAKE_PICTURE_INTENT);
		
		return f;
	}
	
	//Expects a uri of the form file://FILE_PATH
	public static File fileFromUri(Uri uri) {
		return fileFromString(uri.toString());
	}
	
	//Expects a string of the form file://FILE_PATH
	public static File fileFromString(String uri) {
		return new File(uri.substring(8));
	}
	
	public void addPicToGallery(File imageFile) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    mediaScanIntent.setData(Uri.fromFile(imageFile));
	    ((Activity)context).sendBroadcast(mediaScanIntent);
	}
	
	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {		
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = JPEG_PREFIX + "_" + timeStamp;
	    
	    File image = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumDir());	    
	    return image;
	}

	private File getAlbumDir() throws IOException {
		File albumDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), UNICEF_GIS_ALBUM);
		
		if (!albumDir.exists() || !albumDir.isDirectory()) {
			if (albumDir.exists()) albumDir.delete();
			
			albumDir.mkdir();
		}
					
		return albumDir;
	}

	public Bitmap getThumbnail(File imageFile, int scaleFactor) {	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;		
		options.inSampleSize = scaleFactor;
	    
		Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		
		//Image was deleted from storage. Maybe we should launch an exception here. 
		if (originalBitmap == null)
			return null;
		
		return tryToRotate(imageFile, originalBitmap);
	}
	
	public Bitmap getThumbnail(Uri imageUri, int scaleFactor) {
        return getThumbnail(fileFromUri(imageUri), scaleFactor);
	}	

	private Bitmap tryToRotate(File imageFile, Bitmap bitmap) {
		try {
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			
			Matrix matrix = new Matrix();
			matrix.postRotate(exifOrientationToDegrees(orientation));
			
			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		} catch (IOException e) {
			Log.d("Camera", "Couldn't open file to extract EXIF data");
			e.printStackTrace();
			return bitmap;
		}		
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

	public static Uri getUri(File imageFile) {
		return Uri.fromFile(imageFile);
	}
	
	public File rotateImageIfNecessary(String imageUri) {
		Log.d("Camera", "Rotating image" + imageUri);
		
		File imageFile = fileFromString(imageUri);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;		
		options.inSampleSize = 1;
		
		Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		
		if (originalBitmap == null)
			return null;
		
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
			Log.d("Camera", "Couldn't save rotated image, settling with original image.");
			return imageFile;
		} catch (IOException e) {
			e.printStackTrace();	
			Log.d("Camera", "Couldn't save rotated image, settling with original image.");
			return imageFile;
		} finally {
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("Camera", "Couldn't save rotated image, settling with original image.");
					return imageFile;
				}
			}
		}
		
		return new File(rotatedFileName);
	}
}
