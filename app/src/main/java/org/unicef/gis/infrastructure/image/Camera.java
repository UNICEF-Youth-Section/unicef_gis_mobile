package org.unicef.gis.infrastructure.image;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
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
	

	/***
	 * Causes the system to rescan storage looking for changes in media,
	 * so that the media galleries reflect the most up to date state. 
	 * For example, if the user deleted reports and we didn't rescan the system,
	 * the pics would be gone from storage but the photo gallery would still show
	 * black placeholders where the deleted pics used to be until the next time the
	 * cellphone is turned off and on. 
	 * By calling this after removing pics, the gallery is kept in sync. 
	 */
	public void rescanMedia() {
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
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
		/*
		 * Due to a bug in certain phone's brands that caused the images to be corrupted after rotation,
		 * we're rotating the images server side.
		 * 
		 * The uploaded picture's name will tell the server which rotation to apply, coded in the filename:
		 * rotate0, rotate90, rotate180 and rotate270
		 */
		Log.d("Camera", "Rotating image" + imageUri);
		
		File imageFile = fileFromString(imageUri);
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(imageFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("SyncAdapter", "Couldn't open EXIF data, settling with the original image.");
			return imageFile;
		}
		
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		
		String newFilename = "rotate" + Float.valueOf(exifOrientationToDegrees(orientation)).intValue() + "-" + imageFile.getName();
		File destFile = new File(imageFile.getParentFile(), newFilename);
		
		try {
			FileUtils.copyFile(imageFile, destFile);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("Camera", "Couldn't save rotated image, settling with original image.");
			return imageFile;
		}
		
		return destFile;
	}

	public void deleteOriginalAndRotatedImage(String imageUri) {
		//Delete original image (if it's still there)
		File originalImage = fileFromString(imageUri);
		deleteIfExists(originalImage);
		
		//Delete rotated image (if it's still there)
		File rotatedImage = fileFromString(rotatedFileNameFromOriginal(originalImage));
		deleteIfExists(rotatedImage);
	}
	
	private String rotatedFileNameFromOriginal(File file) {
		return file.getParent() + "/rotated-" + file.getName();
	}

	private void deleteIfExists(File file) {
		if (file.exists())
			file.delete();
	}
}
