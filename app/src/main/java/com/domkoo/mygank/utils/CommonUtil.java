package com.domkoo.mygank.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommonUtil {
    private CommonUtil() {
    }

    /**
     * Show toast
     *
     * @param context
     * @param str
     * @param lengthShort
     */
    public static void toast(Context context, String str, int lengthShort) {
        Toast.makeText(context, str, lengthShort).show();
    }

    /**
     * Show snackbar
     *
     * @param parentView
     * @param str
     * @param length
     */
    public static void makeSnackBar(View parentView, String str, int length) {
        final Snackbar snackbar = Snackbar.make(parentView, str, length);
        snackbar.show();
    }

    public static void makeSnackBarWithAction(View parentView, String msg, int length, View.OnClickListener listener, String actionMsg) {
        final Snackbar snackbar = Snackbar.make(parentView, msg, length);
        snackbar.setAction(actionMsg, listener);
        snackbar.show();
    }

    /**
     * Open URL using system browser
     *
     * @param context
     * @param url
     */
    public static void openUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Generate md5 key for picture
     *
     * @param imagePath
     * @return
     */
    public static String keyForImage(String imagePath) {

        String key = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(imagePath.getBytes());
            key = byteToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Transform binary to hex
     *
     * @param digest
     * @return
     */
    private static String byteToHex(byte[] digest) {

        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * Get APP version name
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context) throws Exception {
        PackageInfo packInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        return packInfo.versionName;
    }

    /**
     * Remove useless and unsafe characters.
     * Only Chinese, numbers, English characters and space are allowed.
     *
     * @param searchText
     * @return
     */
    public static String stringFilterStrict(String searchText) {
        return searchText.replaceAll("[^ a-zA-Z0-9\\u4e00-\\u9fa5]", "");
    }

    /**
     * Clear cache
     *
     * @param context
     */
    public static void clearCache(Context context) {
        cleanExternalCache(context);
        cleanInternalCache(context);
    }

    /**
     * Clear internal cache
     *
     * @param context
     */
    public static void cleanInternalCache(Context context) {
        File directory = context.getCacheDir();
        deleteFilesByDirectory(directory);
    }

    /**
     * Clear external cache
     *
     * @param context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            deleteFilesByDirectory(context.getExternalCacheDir());
    }

    /**
     * Delete all files in given directory
     *
     * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory())
            for (File item : directory.listFiles())
                item.delete();
    }
}
