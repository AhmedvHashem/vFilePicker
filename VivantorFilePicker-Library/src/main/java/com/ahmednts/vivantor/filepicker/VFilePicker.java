package com.ahmednts.vivantor.filepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import java.io.File;

/**
 * Created by AhmedNTS on 2015-09-07.
 */

/**
 * Make sure to edit your activity`s "configChanges" for rotation changes
 * by adding this to your activity tag in your app Manifest.xml
 * android:configChanges="orientation|screenSize"
 */
/*
AndroidManifest.xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

file_paths.xml
<paths>
    <external-path name="external_files" path="." />
</paths>
 */
public class VFilePicker {
  public static final int IMAGE = 1000;
  public static final int AUDIO = 2000;
  public static final int VIDEO = 3000;
  public static final int ANY = 4000;

  public static final int GALLERY = 10000;
  public static final int CAMERA = 20000;
  public static final int FILE_MANAGER = 30000;

  private final static int ANY_FILEMANAGER = 50;
  private final static int IMAGE_FILEMANAGER = 51;
  private final static int VIDEO_FILEMANAGER = 52;
  private final static int AUDIO_FILEMANAGER = 53;

  private final static int IMAGE_CAMERA = 11;
  private final static int VIDEO_CAMERA = 12;
  private final static int IMAGE_CAMERA_EXTERNAL = 20;
  private final static int VIDEO_CAMERA_EXTERNAL = 21;

  private final static int ANY_GALLERY = 30;
  private final static int IMAGE_GALLERY = 31;
  private final static int VIDEO_GALLERY = 32;

  private int pickerType;
  private int fromType;

  private String cameraDirectoryName;               // for camera with file only
  private String filePath;

  private static volatile VFilePicker instance;

  public synchronized static VFilePicker getInstance() {
    if (instance == null) instance = new VFilePicker();

    return instance;
  }

  public static void destroyInstance() {
    instance = null;
  }

  private VFilePicker() {
  }

  //till i create a builder
  public VFilePicker pick(int pickerType) {
    this.pickerType = pickerType;

    return instance;
  }

  public VFilePicker from(int fromType) {
    this.fromType = fromType;

    return instance;
  }

  public VFilePicker saveTo(String cameraDirectoryName) {
    this.cameraDirectoryName = cameraDirectoryName;

    return instance;
  }

  public void show(Activity context) {
    if (fromType == GALLERY) {
      FromGallery(context);
    } else if (fromType == CAMERA) {
      if (cameraDirectoryName == null || cameraDirectoryName.isEmpty()) {
        FromCamera(context);
      } else {
        FromCameraWithFile(context);
      }
    } else {
      FromFileManager(context);
    }
  }

  public VFileInfo getFileInfo() {
    if (filePath == null || filePath.isEmpty()) return null;

    return new VFileInfo(filePath);
  }

  private void FromGallery(Activity context) {
    if (pickerType == AUDIO) return;

    String setType = "*/*";
    int requestCode = ANY_GALLERY;

    if (pickerType == IMAGE) {
      setType = "image/*";
      requestCode = VIDEO_GALLERY;
    } else if (pickerType == VIDEO) {
      setType = "video/*";
      requestCode = IMAGE_GALLERY;
    }

    if (RequestPermissions(context, requestCode)) {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType(setType);
      intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
      context.startActivityForResult(intent, requestCode);
    }
  }

  private void FromCamera(Activity context) {
    if (pickerType == AUDIO || pickerType == ANY) return;

    String action = MediaStore.ACTION_IMAGE_CAPTURE;
    int requestCode = IMAGE_CAMERA;

    if (pickerType == IMAGE) {
      action = MediaStore.ACTION_IMAGE_CAPTURE;
      requestCode = IMAGE_CAMERA;
    } else if (pickerType == VIDEO) {
      action = MediaStore.ACTION_VIDEO_CAPTURE;
      requestCode = VIDEO_CAMERA;
    }

    if (RequestPermissions(context, requestCode)) {
      Intent intent = new Intent(action);
      context.startActivityForResult(intent, requestCode);
    }
  }

  private void FromCameraWithFile(Activity context) {
    if (pickerType == AUDIO || pickerType == ANY) return;
    if (cameraDirectoryName == null || cameraDirectoryName.isEmpty()) return;

    String action = MediaStore.ACTION_IMAGE_CAPTURE;
    int requestCode = IMAGE_CAMERA_EXTERNAL;

    if (pickerType == IMAGE) {
      action = MediaStore.ACTION_IMAGE_CAPTURE;
      requestCode = IMAGE_CAMERA_EXTERNAL;
    } else if (pickerType == VIDEO) {
      action = MediaStore.ACTION_VIDEO_CAPTURE;
      requestCode = VIDEO_CAMERA_EXTERNAL;
    }

    if (RequestPermissions(context, requestCode)) {
      filePath = Utils.GenerateFilePath(cameraDirectoryName, pickerType == IMAGE ? 1 : 3);

      if (filePath == null) return;

      //Intent intent = new Intent(action);
      //intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
      //context.startActivityForResult(intent, requestCode);
      Intent intent = new Intent(action);
      Uri fileURI;
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        fileURI = FileProvider.getUriForFile(context,
            context.getApplicationContext().getPackageName() + ".fileprovider", new File(filePath));
      } else {
        fileURI = Uri.fromFile(new File(filePath));
      }

      intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileURI);
      intent.addFlags(
          Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      if (intent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivityForResult(intent, requestCode);
      }
    }
  }

  private void FromFileManager(Activity context) {
    String setType = "*/*";
    int requestCode = ANY_FILEMANAGER;

    if (pickerType == ANY) {
      if (Build.VERSION.SDK_INT >= 19) {
        setType = "*/*";
      } else {
        setType = "file/*";
      }

      requestCode = ANY_FILEMANAGER;
    } else if (pickerType == IMAGE) {
      setType = "image/*";
      requestCode = IMAGE_FILEMANAGER;
    } else if (pickerType == VIDEO) {
      setType = "video/*";
      requestCode = VIDEO_FILEMANAGER;
    } else if (pickerType == AUDIO) {
      setType = "audio/*";
      requestCode = AUDIO_FILEMANAGER;
    }

    if (RequestPermissions(context, requestCode)) {
      Intent intent = new Intent();
      if (Build.VERSION.SDK_INT >= 19) {
        // For Android KitKat, we use a different intent to ensure
        // we can get the file path from the returned intent URI
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
      } else {
        intent.setAction(Intent.ACTION_GET_CONTENT);
      }

      intent.setType(setType);
      context.startActivityForResult(intent, requestCode);
    }
  }

  private boolean RequestPermissions(Activity context, int requestCode) {
    boolean permissionGranted = true;

    String[] cameraPermissions = new String[] {
        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    String[] galleryPermissions = new String[] {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (requestCode == IMAGE_CAMERA
          || requestCode == VIDEO_CAMERA
          || requestCode == IMAGE_CAMERA_EXTERNAL
          || requestCode == VIDEO_CAMERA_EXTERNAL) {
        permissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
      } else {
        permissionGranted =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
      }

      if (!permissionGranted) {
        if (requestCode == IMAGE_CAMERA
            || requestCode == IMAGE_CAMERA_EXTERNAL
            || requestCode == VIDEO_CAMERA
            || requestCode == VIDEO_CAMERA_EXTERNAL) {
          ActivityCompat.requestPermissions(context, cameraPermissions, requestCode);
        } else {
          ActivityCompat.requestPermissions(context, galleryPermissions, requestCode);
        }
      }
    }

    return permissionGranted;
  }

  public void onRequestPermissions(Activity context,
      int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if (requestCode == IMAGE_GALLERY
          || requestCode == VIDEO_GALLERY
          || requestCode == ANY_GALLERY) {
        FromGallery(context);
      } else if (requestCode == IMAGE_CAMERA || requestCode == VIDEO_CAMERA) {
        FromCamera(context);
      } else if (requestCode == IMAGE_CAMERA_EXTERNAL || requestCode == VIDEO_CAMERA_EXTERNAL) {
        FromCameraWithFile(context);
      } else if (requestCode >= ANY_FILEMANAGER && requestCode <= AUDIO_FILEMANAGER) {
        FromFileManager(context);
      }
    }
  }

  public void onActivityResult(Activity context, int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode != IMAGE_CAMERA_EXTERNAL && requestCode != VIDEO_CAMERA_EXTERNAL) {
        if (data.getData() == null) return;

        filePath = Utils.getFilePathFromURI(context, data.getData());
      }
    }
  }
}


