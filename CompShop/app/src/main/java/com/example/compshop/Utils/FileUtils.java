package com.example.compshop.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

public class FileUtils {
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }
    
    public static String getPath(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(column_index);
            cursor.close();
            return filePath;
        }
        return null;
    }

    public static Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage", "");
        return Uri.parse(path);
    }
}
