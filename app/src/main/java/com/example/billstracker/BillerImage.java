package com.example.billstracker;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BillerImage {

    public String storeImage (Drawable drawable, String billerName, boolean custom) throws IOException {

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 75, bytes);
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"billerImages");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outFile;
        if (custom) {
            outFile = new File(dir, billerName + "custom.png");
        }
        else {
            outFile = new File(dir, billerName + ".png");
        }
        FileOutputStream outStream = new FileOutputStream(outFile, false);
        outStream.write(bytes.toByteArray());

        outStream.flush();
        outStream.close();

        return outFile.getAbsolutePath();
    }
}
