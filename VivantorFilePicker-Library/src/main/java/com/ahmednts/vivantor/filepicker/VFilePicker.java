package com.ahmednts.vivantor.filepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by AhmedNTS on 2015-09-07.
 */
public class VFilePicker
{
	public final static int ANY_FILEMANAGER = 50;
	public final static int IMAGE_FILEMANAGER = 51;
	public final static int VIDEO_FILEMANAGER = 52;
	public final static int AUDIO_FILEMANAGER = 53;

	public final static int IMAGE_CAMERA = 11;
	public final static int VIDEO_CAMERA = 12;

	public final static int ANY_GALLERY = 20;
	public final static int IMAGE_GALLERY = 21;
	public final static int VIDEO_GALLERY = 22;

	Activity context;

	VFileType fileType;

	public VFilePicker(Activity context)
	{
		this.context = context;
	}

	public void PickupMediaFile(final VFileType fileType)
	{
		if (fileType == VFileType.AUDIO)
			PickupMediaFile_FileManager(fileType);
		else
		{
			final CharSequence[] items = {"Gallery", "Camera", "Cancel"};
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Select...");
			builder.setItems(items, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int item)
				{
					if (items[item].equals("Gallery"))
					{
						FromGallery(fileType);
					}
					else if (items[item].equals("Camera"))
					{
						FromCamera(fileType);
					}
					else if (items[item].equals("Cancel"))
					{
						dialog.dismiss();
					}
				}
			});
			builder.show();
		}
	}

	public void FromGallery(VFileType fileType)
	{
		if (fileType == VFileType.AUDIO)
			return;

		String setType = "*/*";
		int requestCode = ANY_GALLERY;

		if (fileType == VFileType.IMAGE)
		{
			setType = "image/*";
			requestCode = VIDEO_GALLERY;
		}
		else if (fileType == VFileType.VIDEO)
		{
			setType = "video/*";
			requestCode = IMAGE_GALLERY;
		}

		this.fileType = fileType;

		if (RequestPermissions(requestCode))
		{
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType(setType);
			intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			context.startActivityForResult(intent, requestCode);
		}
	}

	public void FromCamera(VFileType fileType)
	{
		if (fileType == VFileType.AUDIO || fileType == VFileType.ANY)
			return;

		String action = MediaStore.ACTION_IMAGE_CAPTURE;
		int requestCode = IMAGE_CAMERA;

		if (fileType == VFileType.IMAGE)
		{
			action = MediaStore.ACTION_IMAGE_CAPTURE;
			requestCode = IMAGE_CAMERA;
		}
		else if (fileType == VFileType.VIDEO)
		{
			action = MediaStore.ACTION_VIDEO_CAPTURE;
			requestCode = VIDEO_CAMERA;
		}

		this.fileType = fileType;

		if (RequestPermissions(requestCode))
		{
			Intent intent = new Intent(action);
//		intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 50 * 1048 * 1048);//1*1048*1048=1MB
//		if (fileType == VFileType.VIDEO)
//		{
//			intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
//		}
			context.startActivityForResult(intent, requestCode);
		}
	}

	public void PickupMediaFile_FileManager(VFileType fileType)
	{
		String setType = "*/*";
		int requestCode = ANY_FILEMANAGER;

		if (fileType == VFileType.ANY)
		{
			if (Build.VERSION.SDK_INT >= 19)
				setType = "*/*";
			else
				setType = "file/*";

			requestCode = ANY_FILEMANAGER;
		}
		else if (fileType == VFileType.IMAGE)
		{
			setType = "image/*";
			requestCode = IMAGE_FILEMANAGER;
		}
		else if (fileType == VFileType.VIDEO)
		{
			setType = "video/*";
			requestCode = VIDEO_FILEMANAGER;
		}
		else if (fileType == VFileType.AUDIO)
		{
			setType = "audio/*";
			requestCode = AUDIO_FILEMANAGER;
		}

		this.fileType = fileType;

		if (RequestPermissions(requestCode))
		{
			Intent intent = new Intent();
			if (Build.VERSION.SDK_INT >= 19)
			{
				// For Android KitKat, we use a different intent to ensure
				// we can get the file path from the returned intent URI
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			}
			else
			{
				intent.setAction(Intent.ACTION_GET_CONTENT);
			}

			intent.setType(setType);
			context.startActivityForResult(intent, requestCode);
		}
	}

	boolean RequestPermissions(int requestCode)
	{
		boolean permissionGranted = true;

		String[] cameraPermissions = new String[]
				{
						Manifest.permission.CAMERA,
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				};

		String[] galleryPermissions = new String[]
				{
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			if (requestCode == IMAGE_CAMERA || requestCode == VIDEO_CAMERA)
				permissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
			else
				permissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

			if (!permissionGranted)
			{
				if (requestCode == IMAGE_CAMERA || requestCode == VIDEO_CAMERA)
					ActivityCompat.requestPermissions(context, cameraPermissions, requestCode);
				else
					ActivityCompat.requestPermissions(context, galleryPermissions, requestCode);
			}
		}

		return permissionGranted;
	}

	public void onVFilePickerRequestPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			if (requestCode == IMAGE_GALLERY || requestCode == VIDEO_GALLERY || requestCode == ANY_GALLERY)
				FromGallery(fileType);
			else if (requestCode == IMAGE_CAMERA || requestCode == VIDEO_CAMERA)
				FromCamera(fileType);
			else if (requestCode >= ANY_FILEMANAGER && requestCode <= AUDIO_FILEMANAGER)
				PickupMediaFile_FileManager(fileType);
		}
	}

	/**
	 * Make sure to edit your activity`s "configChanges" for rotation changes
	 * by adding this to your activity tag in your app Manifest.xml
	 * android:configChanges="orientation|screenSize"
	 */
	public VFileInfos GetFileInfos(int requestCode, Intent data)
	{
		if (data.getData() == null)
			return null;

		Uri selectedFileURI = data.getData();
		String filePath = VFilePickerUtilities.getFilePathFromURI(context, selectedFileURI);

		if (filePath == null)
			return null;

		return new VFileInfos(filePath);
	}
}


