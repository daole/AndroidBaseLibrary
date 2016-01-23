package com.dreamdigitizers.androidbaselibrary.utilities;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class UtilsFile {
    private static final String TAG = UtilsFile.class.getName();

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean deleteFile(String pFilePath) {
        boolean result = true;
        File file = new File(pFilePath);
        if (file.exists()) {
            result = file.delete();
        }

        return result;
    }

    public static File getOutputMediaFile(String pFolderName, String pFileName) {
        if (!UtilsFile.isExternalStorageWritable()) {
            return null;
        }

        File mediaStorageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), pFolderName);
        if (!mediaStorageFolder.exists()) {
            if (!mediaStorageFolder.mkdirs()) {
                Log.d(TAG, "Failed to create directory");
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageFolder.getPath() + File.separator + pFileName);

        return mediaFile;
    }
}
