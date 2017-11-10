package com.example.admin.composerblocknote;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.File;

/**
 * Created by narval on 03/11/2017.
 */

public class FileManager {
    public FileManager(Context context, Activity activity) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(activity, new String[]
                    {permission}, 123);
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File createMusicStorageDir(String albumName) {
        File file = null;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), albumName);
            if (!file.mkdirs()) {
                System.out.println("Directory not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public File getMusicStorageDir(String albumName) {
        File file = null;
        File[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).listFiles();
        for (File f : files){
            System.out.println("DEBUG : " + f.getName());
            if (f.getName().equals(albumName)){
                System.out.println("DEBUG : " + "found file");
                file = f;
            }
        }
        return file;
    }

}
