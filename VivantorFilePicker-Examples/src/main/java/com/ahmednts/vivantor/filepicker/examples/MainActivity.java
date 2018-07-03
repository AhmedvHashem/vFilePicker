package com.ahmednts.vivantor.filepicker.examples;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    PickupMediaFile();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    VFilePicker.getInstance().destroyInstance();
  }

  void PickupMediaFile() {
    final CharSequence[] items = { "Gallery", "Camera", "Cancel" };
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    builder.setTitle("Select...");
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {
        if (items[item].equals("Gallery")) {
          VFilePicker.getInstance()
              .pick(VFilePicker.IMAGE)
              .from(VFilePicker.GALLERY)
              .saveTo("vFilePicker")
              .show(MainActivity.this);
        } else if (items[item].equals("Camera")) {
          VFilePicker.getInstance()
              .pick(VFilePicker.IMAGE)
              .from(VFilePicker.CAMERA)
              .saveTo("vFilePicker")
              .show(MainActivity.this);
        } else if (items[item].equals("Cancel")) {
          dialog.dismiss();
        }
      }
    });
    builder.show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    VFilePicker.getInstance().onRequestPermissions(this, requestCode, permissions, grantResults);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    VFilePicker.getInstance().onActivityResult(this, requestCode, resultCode, data);

    VFileInfo fileInfo = VFilePicker.getInstance().getFileInfo();

    if (fileInfo != null) {
      Log.d(TAG, "FileSize=" + fileInfo.getFileSize() + " Byte");

      Log.d(TAG, "FileType=" + fileInfo.getFileType());

      Log.d(TAG, "FileDuration=" + fileInfo.getFileDuration() + " sec");
    }
  }
}
