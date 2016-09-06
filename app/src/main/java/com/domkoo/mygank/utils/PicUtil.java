package com.domkoo.mygank.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

public final class PicUtil {
    public static final String TAG = "PicUtil";
    public static final int SAVE_DONE_TOAST = 666;
    public static final String IMAGE_SAVE_PATH = "/Meizhi/";
    public static final String FILEPATH = "file_path";

    private PicUtil() {
    }

    /**
     * Get the directionary of SD card
     *
     * @return
     */
    public static String getSDCardPath() {
        File sdcardDir;
        // check whether SD card exist
        boolean sdcardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        } else {
            sdcardDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
        return sdcardDir.toString();
    }

    public static File saveBitmapToSDCard(Bitmap bitmap, String url, Handler handler) {
        String SavePath = getSDCardPath() + IMAGE_SAVE_PATH;
        // save Bitmap
        try {
            File path = new File(SavePath);
            if (!path.exists())
                path.mkdirs();

            String filepath = SavePath + getLastStringFromUrl(url);
            File file = new File(filepath);
            if (!file.exists())
                file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                Message message = Message.obtain();
                message.arg1 = SAVE_DONE_TOAST;
                Bundle bundle = new Bundle();
                bundle.putString(FILEPATH, filepath);
                message.setData(bundle);
                handler.sendMessage(message);
                Log.i(TAG, "saveBitmapToSDCard: " + filepath);
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getLastStringFromUrl(String url) {
        String[] splitStrs = url.split("\\/");
        return splitStrs[splitStrs.length - 1];
    }

    public static void saveBitmapFromUrl(Context context, String url, Handler handler) throws ExecutionException, InterruptedException {
        Bitmap bitmap = Glide.with(context)
                .load(url).asBitmap()
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
        PicUtil.saveBitmapToSDCard(bitmap, url, handler);
    }

    public static String getImgPathFromUrl(String url) {
        return getSDCardPath() + IMAGE_SAVE_PATH + getLastStringFromUrl(url);
    }

}
