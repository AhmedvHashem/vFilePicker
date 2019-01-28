package com.ahmednts.vivantor.filepicker;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AhmedNTS on 2016-05-31.
 */
@SuppressWarnings("all")
public class Utils {
  private Utils() {
  }

  public static final String MIME_TYPE_TEXT = "text/*";
  public static final String MIME_TYPE_IMAGE = "image/*";
  public static final String MIME_TYPE_VIDEO = "video/*";
  public static final String MIME_TYPE_AUDIO = "audio/*";
  public static final String MIME_TYPE_APP = "application/*";

  /**
   * @return Whether the URI is a local one.
   */
  public static boolean isLocal(String url) {
    if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
      return true;
    }
    return false;
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is ExternalStorageProvider.
   */
  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is Google Photos.
   */
  public static boolean isGooglePhotosUri(Uri uri) {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }

  public static String getMimeType(File file) {
    String extension = getExtension(file.getName());

    if (extension.length() > 0) {
      return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
    }

    return "application/octet-stream";
  }

  public static String getExtension(String uri) {
    if (uri == null) {
      return null;
    }

    int dot = uri.lastIndexOf(".");
    if (dot >= 0) {
      return uri.substring(dot).toLowerCase();
    } else// No extension.
    {
      return "";
    }
  }

  @Nullable
  public static String GenerateFilePath(String appMediaFolderName, int type) {
    // To be safe, you should check that the SDCard is mounted
    if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
      return null;
    }

    if (appMediaFolderName == null || appMediaFolderName.isEmpty()) return null;

    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), appMediaFolderName);

    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("GenerateFilePath", "failed to create directory");
        return null;
      }
    }

    String filePath;
    String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
    if (type == 1) {
      filePath = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
    } else if (type == 2) {
      filePath = mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".3gp";
    } else if (type == 3) {
      filePath = mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4";
    } else {
      return null;
    }

    return filePath;
  }

  /**
   * This method is responsible for solving the rotation issue if exist. Also scale the images to
   * 1024x1024 resolution
   *
   * @param context The current context
   * @param selectedImage The Image URI
   * @return Bitmap image results
   * @throws IOException
   */
  public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
      throws IOException {
    int MAX_HEIGHT = 1024;
    int MAX_WIDTH = 1024;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
    BitmapFactory.decodeStream(imageStream, null, options);
    imageStream.close();

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    imageStream = context.getContentResolver().openInputStream(selectedImage);
    Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

    img = rotateImageIfRequired(img, selectedImage.getPath());
    return img;
  }

  /**
   * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
   * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
   * the closest inSampleSize that will result in the final decoded bitmap having a width and
   * height equal to or larger than the requested width and height. This implementation does not
   * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
   * results in a larger bitmap which isn't as useful for caching purposes.
   *
   * @param options An options object with out* params already populated (run through a decode*
   * method with inJustDecodeBounds==true
   * @param reqWidth The requested width of the resulting bitmap
   * @param reqHeight The requested height of the resulting bitmap
   * @return The value to be used for inSampleSize
   */
  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
      int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      // Calculate ratios of height and width to requested height and width
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);

      // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
      // with both dimensions larger than or equal to the requested height and width.
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

      // This offers some additional logic in case the image has a strange
      // aspect ratio. For example, a panorama may have a much larger
      // width than height. In these cases the total pixels might still
      // end up being too large to fit comfortably in memory, so we should
      // be more aggressive with sample down the image (=larger inSampleSize).

      final float totalPixels = width * height;

      // Anything more than 2x the requested pixels we'll sample down further
      final float totalReqPixelsCap = reqWidth * reqHeight * 2;

      while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
        inSampleSize++;
      }
    }
    return inSampleSize;
  }

  /**
   * Rotate an image if required.
   *
   * @param img The image bitmap
   * @param selectedImage Image URI
   * @return The resulted Bitmap after manipulation
   */
  public static Bitmap rotateImageIfRequired(Bitmap img, String selectedImage) throws IOException {
    ExifInterface ei = new ExifInterface(selectedImage);
    int orientation =
        ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        return rotateImage(img, 90);
      case ExifInterface.ORIENTATION_ROTATE_180:
        return rotateImage(img, 180);
      case ExifInterface.ORIENTATION_ROTATE_270:
        return rotateImage(img, 270);
      default:
        return img;
    }
  }

  private static Bitmap rotateImage(Bitmap img, int degree) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    Bitmap rotatedImg =
        Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    img.recycle();
    return rotatedImg;
  }

  @Nullable
  public static String getFilePathFromURI(Context context, Uri uri) {
    final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

    // Uri is different in versions after KITKAT (Android 4.4), we need to
    // deal with different Uris.
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      if (Utils.isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        return Environment.getExternalStorageDirectory() + "/" + split[1];
      } else if (Utils.isDownloadsDocument(uri)) {
        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri =
            ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
      } else if (Utils.isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String selection = "_id=?";
        String[] selectionArgs = new String[] { split[1] };

        return getDataColumn(context, contentUri, selection, selectionArgs);
      }
    }
    // MediaStore
    else if ("content".equalsIgnoreCase(uri.getScheme())) {
      if (isGooglePhotosUri(uri)) {
        return uri.getLastPathSegment();
      }

      return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }
    return null;
  }

  @Nullable
  public static String getDataColumn(Context context, Uri uri, String selection,
      String[] selectionArgs) {
    Cursor cursor = null;
    final String column = MediaStore.MediaColumns.DATA;
    final String[] projection = { column };
    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        final int column_index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(column_index);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  public static Bitmap getBitmap(String imagePath, int imageResolution) {
    Bitmap image = BitmapFactory.decodeFile(imagePath);
    return getSizedBitmap(image, imageResolution);
  }

  public static Bitmap getSizedBitmap(Bitmap bitmap, int imageResolution) {
    float new_width = 0;
    float new_height = 0;
    if (bitmap.getWidth() > bitmap.getHeight() && bitmap.getWidth() > imageResolution) {
      float aspect = (float) bitmap.getWidth() / (float) bitmap.getHeight();
      new_width = imageResolution;
      new_height = imageResolution / aspect;
    } else if (bitmap.getHeight() > bitmap.getWidth() && bitmap.getHeight() > imageResolution) {
      float aspect = (float) bitmap.getHeight() / (float) bitmap.getWidth();
      new_height = imageResolution;
      new_width = imageResolution / aspect;
    } else {
      new_width = bitmap.getWidth();
      new_height = bitmap.getHeight();
    }

    return Bitmap.createScaledBitmap(bitmap, (int) new_width, (int) new_height, false);
  }

  public static String getBitmapBase64String(Bitmap bitmap) {
    byte[] imageArray = getBitmapByteArray(bitmap);

    return Base64.encodeToString(imageArray, Base64.DEFAULT);
  }

  public static String getFileBase64String(File file) {
    if (file == null) return null;

    byte[] byteArray = getFileByteArray(file);

    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }

  public static byte[] getBitmapByteArray(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
    return stream.toByteArray();
  }

  public static byte[] getFileByteArray(File file) {
    if (file == null) return null;

    byte[] byteArray = new byte[(int) file.length()];

    try {
      FileInputStream stream = new FileInputStream(file);
      stream.read(byteArray);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return byteArray;
  }
}
