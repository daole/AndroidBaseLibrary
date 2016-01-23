package com.dreamdigitizers.androidbaselibrary.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UtilsBitmap {
    public static Bitmap scaleBitmap(Bitmap pBitmap, int pWidth, int pHeight) {
        double scaleFactor = Math.min(((double) pWidth) / pBitmap.getWidth(), ((double) pHeight) / pBitmap.getHeight());
        return Bitmap.createScaledBitmap(pBitmap, (int) (pBitmap.getWidth() * scaleFactor), (int) (pBitmap.getHeight() * scaleFactor), false);
    }

    public static Bitmap decodeBitmapFromFile(String pImagePath) {
        return BitmapFactory.decodeFile(pImagePath);
    }

    public static Bitmap decodeSampledBitmapFromFile(String pImagePath, int pWidth, int pHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pImagePath, options);

        options.inSampleSize = UtilsBitmap.calculateInSampleSize(options, pWidth, pHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pImagePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options pOptions, int pWidth, int pHeight) {
        int width = pOptions.outWidth;
        int height = pOptions.outHeight;
        int inSampleSize = 1;

        if (width > pWidth || height > pHeight) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            while ((halfWidth / inSampleSize) >= pWidth && (halfHeight / inSampleSize) >= pHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
