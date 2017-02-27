package com.ahmednts.vivantor.filepicker;

import android.media.MediaMetadataRetriever;

import java.io.File;

/**
 * Created by AhmedNTS on 2016-05-31.
 */
public class VFileInfo
{
	public String filePath;
	private String fileExtension;
	private long fileSize;

	private int fileType;

	public VFileInfo(String filePath)
	{
		this.filePath = filePath;
		this.fileType = getFileType();
	}

	public File getFileObject()
	{
		if (!Utils.isLocal(filePath))
			return null;

		return new File(filePath);
	}

	public int getFileType()
	{
		File file = getFileObject();
		if (file == null)
			throw new NullPointerException("getFileObject() returned null!");

		String typeString = Utils.getMimeType(getFileObject());

		if (typeString != null)
		{
			if (typeString.contains("image"))
				fileType = VFilePicker.IMAGE;
			else if (typeString.contains("video"))
				fileType = VFilePicker.VIDEO;
			else if (typeString.contains("audio"))
				fileType = VFilePicker.AUDIO;
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
			fileExtension = Utils.getExtension(filePath);

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
		if (!Utils.isLocal(filePath))
			return null;

		try
		{
			if (fileType == VFilePicker.IMAGE)
			{
//			    return Utils.getBitmapBase64String(Utils.getBitmap(filePath, 512));

				return Utils.getBitmapBase64String(Utils.rotateImageIfRequired(Utils.getBitmap(filePath, 1024), filePath));
			}
			else
			{
				return Utils.getFileBase64String(getFileObject());
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
		if (!Utils.isLocal(filePath))
			return null;

		try
		{
			if (fileType == VFilePicker.IMAGE)
			{
//			    return Utils.getBitmapByteArray(Utils.getBitmap(filePath, 512));

				return Utils.getBitmapByteArray(Utils.rotateImageIfRequired(Utils.getBitmap(filePath, 1024), filePath));
			}
			else
			{
				return Utils.getFileByteArray(getFileObject());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return null;
		}
	}
}
