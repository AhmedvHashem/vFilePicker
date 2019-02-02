package com.ahmednts.vivantor.filepicker.examples;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.ahmednts.vivantor.filepicker.VFileInfo;
import com.ahmednts.vivantor.filepicker.VFilePicker;

public class MainActivity
    extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  private VFilePicker filePicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    PickupMediaFile();
  }

  void PickupMediaFile() {
    final CharSequence[] items = { "Gallery", "Camera", "Cancel" };
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    builder.setTitle("Select...");
    builder.setItems(items, (dialog, item) -> {
      if (items[item].equals("Gallery")) {
        filePicker = new VFilePicker()
            .pick(VFilePicker.IMAGE)
            .from(VFilePicker.GALLERY)
            .saveTo("vFilePicker");
        filePicker.show(MainActivity.this);
      } else if (items[item].equals("Camera")) {
        filePicker = new VFilePicker()
            .pick(VFilePicker.IMAGE)
            .from(VFilePicker.CAMERA)
            .saveTo("vFilePicker");
        filePicker.show(MainActivity.this);
      } else if (items[item].equals("Cancel")) {
        dialog.dismiss();
      }
    });
    builder.show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    filePicker.onRequestPermissions(this, requestCode, permissions, grantResults);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    VFileInfo fileInfo = filePicker.onActivityResult(this, requestCode, resultCode, data);

    if (fileInfo != null) {
      Log.d(TAG, "FileSize=" + fileInfo.getFileSize() + " Byte");

      Log.d(TAG, "FileType=" + fileInfo.getFileType());

      //Log.d(TAG, "FileDuration=" + fileInfo.getFileDuration() + " sec");
    }
  }
}
