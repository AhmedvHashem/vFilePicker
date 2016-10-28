package com.ahmednts.vivantor.filepicker;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by AhmedNTS on 2016-05-31.
 */
public class VFileInfos
{
	private File fileObject;
	public String filePath;
	private String fileExtension;
	private long fileSize;

	private VFileType fileType;

	String fileBase64String;

	public VFileInfos(String filePath)
	{
		this.filePath = filePath;
		this.fileType = getFileType();
	}

	public File getFileObject()
	{
		if (!VFilePickerUtilities.isLocal(filePath))
			return null;

		if (fileObject == null)
			fileObject = new File(filePath);
		return fileObject;
	}

	public VFileType getFileType()
	{
		if (fileType == null)
		{
			String typeString = VFilePickerUtilities.getMimeType(getFileObject());

			if (typeString != null)
			{
				if (typeString.contains("image"))
					fileType = VFileType.IMAGE;
				else if (typeString.contains("video"))
					fileType = VFileType.VIDEO;
				else if (typeString.contains("audio"))
					fileType = VFileType.AUDIO;
			}
		}

		return fileType;
	}

	public long getFileSize()
	{
		return getFileObject().length();//(1024*1024)*1 = 1 MB
	}

	public String getFileExtension()
	{
		if (fileExtension == null)
			fileExtension = VFilePickerUtilities.getExtension(filePath);

		return fileExtension;
	}

	public long getFileDuration()
	{
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(filePath);
		String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		long timeInmillisec = Long.parseLong(time);
		long duration = timeInmillisec / 1000;
//		long hours = duration / 3600;
//		long minutes = (duration - hours * 3600) / 60;
//		long seconds = duration - (hours * 3600 + minutes * 60);

		return duration;
	}

	public String getFileBase64String()
	{
		if (!VFilePickerUtilities.isLocal(filePath))
			return null;
		try
		{
			if (fileType == VFileType.IMAGE)
			{
//			    return VFilePickerUtilities.getBitmapBase64String(VFilePickerUtilities.getBitmap(filePath, 512));

				return VFilePickerUtilities.getBitmapBase64String(VFilePickerUtilities.rotateImageIfRequired(VFilePickerUtilities.getBitmap(filePath, 1024), filePath));
			}
			else
			{
				return VFilePickerUtilities.getFileBase64String(getFileObject());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}
	}

	public byte[] getFileByteArray()
	{
		if (!VFilePickerUtilities.isLocal(filePath))
			return null;

		try
		{
			if (fileType == VFileType.IMAGE)
			{
//			    return VFilePickerUtilities.getBitmapByteArray(VFilePickerUtilities.getBitmap(filePath, 512));

				return VFilePickerUtilities.getBitmapByteArray(VFilePickerUtilities.rotateImageIfRequired(VFilePickerUtilities.getBitmap(filePath, 1024), filePath));
			}
			else
			{
				return VFilePickerUtilities.getFileByteArray(getFileObject());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}
	}
}
