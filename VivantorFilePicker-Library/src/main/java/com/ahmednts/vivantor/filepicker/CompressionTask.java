package com.ahmednts.vivantor.filepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Kerollos Kromer on 17-Mar-19.
 */
public class CompressionTask extends AsyncTask<File, Integer, File> {

  private static final String TAG = CompressionTask.class.getName();

  private enum SupportedFormats {
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png");

    private final String extension;

    SupportedFormats(String extension) {
      this.extension = extension;
    }

    @NonNull
    @Override
    public String toString() {
      return this.extension;
    }
  }

  /**
   * file length/size in bytes.
   */
  private static final long MAX_SIZE = 2 * 1024 * 1024; // 2 MB

  private WeakReference<Context> contextWeakReference;
  private CompressionListener compressionListener;
  private boolean delete;

  private File file = null;
  private boolean isOriginal;

  /**
   * @param context context
   * @param compressionListener compression process lifecycle callback
   * @param delete set to true if you want to delete the new compressed file when you are done with it
   */
  public CompressionTask(Context context, CompressionListener compressionListener, boolean delete) {
    this.contextWeakReference = new WeakReference<>(context);
    this.compressionListener = compressionListener;
    this.delete = delete;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (compressionListener != null) compressionListener.onStart();
  }

  @Override
  protected File doInBackground(File... files) {
    Context context = contextWeakReference.get();
    if (context != null) {
      try {
        file = compressFile(context, files[0]);
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
    }
    return file;
  }

  @Override
  protected void onPostExecute(File compressedFile) {
    super.onPostExecute(compressedFile);
    if (compressionListener != null && contextWeakReference.get() != null) {
      if (compressedFile != null) {
        compressionListener.onSuccess(compressedFile);
      } else {
        compressionListener.onFailure();
      }
    }
  }

  public interface CompressionListener {
    void onStart();

    void onSuccess(File compressedFile);

    void onFailure();
  }

  public void dispose() {
    contextWeakReference.clear();
    compressionListener = null;
    if (delete) deleteCompressedFile();
  }

  private void deleteCompressedFile() {
    if (!isOriginal && file.exists() && file.isFile() && file.canWrite()) {
      new Thread(() -> file.delete()).start();
    }
  }

  /**
   * we only compress if the file is bigger than {@value MAX_SIZE} , otherwise return original.
   *
   * @param originalFile file to be compressed {@link SupportedFormats}.
   * @return original file if it is {@value MAX_SIZE} or less , otherwise compress and return the compressed file.
   * @throws Exception if the file is not supported or some problem occurred during the process.
   */
  @Nullable
  private File compressFile(Context context, File originalFile)
      throws Exception {
    String fileExtension = VFileUtils.getExtension(originalFile.getPath());

    if (!isValidFormat(fileExtension)) {
      throw new Exception(fileExtension + " is not supported");
    }

    if (originalFile.length() <= MAX_SIZE) {
      isOriginal = true;
      return originalFile;
    }

    Log.d(TAG, originalFile.length() + "");

    Uri fileUri = VFileUtils.getUriForFile(context, originalFile);
    Bitmap b =
        VFileUtils.handleSamplingAndRotationBitmap(context, fileUri);

    Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

    String fileName = "_compressed";
    File compressedFile = new File(context.getCacheDir(), fileName + fileExtension);
    Log.d(TAG, compressedFile.getPath());
    Bitmap.CompressFormat compressFormat =
        fileExtension.contains("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

    FileOutputStream out = new FileOutputStream(compressedFile);
    b.compress(compressFormat, 100, out);
    out.flush();
    out.close();

    Log.d(TAG, compressedFile.length() + "");
    Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

    return compressedFile;
  }

  private boolean isValidFormat(String fileExtension) {
    SupportedFormats[] supportedFormats = SupportedFormats.values();
    for (SupportedFormats supportedFormat : supportedFormats) {
      if (supportedFormat.toString().equals(fileExtension)) {
        return true;
      }
    }
    return false;
  }
}