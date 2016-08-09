package com.ahmednts.vivantor.filepicker.examples;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ahmednts.vivantor.filepicker.VFileInfos;
import com.ahmednts.vivantor.filepicker.VFilePicker;
import com.ahmednts.vivantor.filepicker.VFileType;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = MainActivity.class.getSimpleName();

	VFilePicker filePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		filePicker = new VFilePicker(this);
		filePicker.PickupMediaFile(VFileType.VIDEO);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (filePicker != null)
			filePicker.onVFilePickerRequestPermissions(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK)
		{
			VFileInfos fileInfos = filePicker.GetFileInfos(requestCode, data);

			if (fileInfos != null)
			{
				long size = fileInfos.getFileSize();
				Log.d(TAG, size + " MB");

				VFileType type = fileInfos.getFileType();
				Log.d(TAG, type.toString());

				Log.d(TAG, fileInfos.getFileDuration() + " sec");
			}
		}
	}
}
