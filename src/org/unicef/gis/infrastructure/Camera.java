package org.unicef.gis.infrastructure;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
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
	public static final int TAKE_PICTURE_INTENT = 10;

	private static final int DEFAULT_SCALE_FACTOR = 0;
	
	private static String UNICEF_GIS_ALBUM = "UNICEF-GIS-ALBUM";
	private static String JPEG_PREFIX = "pic";
	private static String JPEG_FILE_SUFFIX = ".jpg";

	private final Activity activity;
	
	public Camera(Activity activity) {
		this.activity = activity;
	}
	
	public File takePicture() throws IOException {
		File f = createImageFile();
		Uri uri = Uri.fromFile(f);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);				
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		
		activity.startActivityForResult(takePictureIntent, TAKE_PICTURE_INTENT);
		
		return f;
	}

	public void addPicToGallery(File imageFile) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    mediaScanIntent.setData(Uri.fromFile(imageFile));
	    activity.sendBroadcast(mediaScanIntent);
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

	public Bitmap getThumbnail(File imageFile, int targetWidth, int targetHeight) {	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;		
		options.inSampleSize = DEFAULT_SCALE_FACTOR;
	    
		Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
		return tryToRotate(imageFile, originalBitmap);
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

	public Uri getUri(File imageFile) {
		return Uri.fromFile(imageFile);
	}
}
