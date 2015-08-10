package io.github.minime89.keepasstransfer.keyboard;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.minime89.keepasstransfer.KeePassTransfer;

public class MappingFileManager {
    private static final String TAG = MappingFileManager.class.getSimpleName();
    private static final String INSTALL_DIRECTORY = "app";
    private static final String CHARACTER_MAPPING_DIRECTORY = "characters";
    private static final String SCANCODE_MAPPING_DIRECTORY = "scancodes";

    private static MappingFileManager instance;

    public static MappingFileManager getInstance() {
        if (instance == null) {
            instance = new MappingFileManager();
        }

        return instance;
    }

    private MappingFileManager() {

    }

    /**
     * Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }

        return false;
    }

    private void install(String path) {
        String targetPath = path.substring(INSTALL_DIRECTORY.length());

        //list assets
        Context context = KeePassTransfer.getContext();
        AssetManager assetManager = context.getAssets();
        String assets[];
        try {
            assets = assetManager.list(path);
        } catch (IOException e) {
            Log.e(TAG, String.format("error listing assets under path '%s': %s", path, e.getMessage()));

            return;
        }

        //install file
        if (assets.length == 0) {
            Log.i(TAG, "install file: " + path);

            InputStream is = null;
            OutputStream os = null;
            try {
                //input
                is = KeePassTransfer.getContext().getAssets().open(path);

                //output
                File outputFile = new File(context.getExternalFilesDir(null), targetPath);
                os = new FileOutputStream(outputFile);

                //write data
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
            } catch (IOException e) {
                Log.e(TAG, String.format("error installing file '%s': %s", path, e.getMessage()));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.w(TAG, String.format("error closing file: %s", e.getMessage()));
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, String.format("error closing file: %s", e.getMessage()));
                    }
                }
            }
        }
        //install directory
        else {
            Log.i(TAG, "install directory: " + path);

            File outputDir = new File(context.getExternalFilesDir(null), targetPath);
            if (!outputDir.exists()) {
                if (!outputDir.mkdir()) {
                    Log.e(TAG, String.format("error installing directory '%s': couldn't create directory", path));
                }
            }

            for (int i = 0; i < assets.length; ++i) {
                install(path + "/" + assets[i]);
            }
        }
    }

    public void install() {
        install(INSTALL_DIRECTORY);
    }

    private String readFile(File file) throws IOException {
        if (!isExternalStorageReadable()) {
            throw new IOException("can't access external storage");
        }

        Context context = KeePassTransfer.getContext();
        InputStream is = null;
        try {
            file = new File(context.getExternalFilesDir(null), file.getPath());
            is = new FileInputStream(file);

            //read data
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                data.write(bytes, 0, read);
            }

            return data.toString();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, String.format("error closing file: %s", e.getMessage()));
                }
            }
        }
    }

    public String loadScancodeMapping(String id) throws IOException {
        File file = new File(SCANCODE_MAPPING_DIRECTORY, id);

        return readFile(file);
    }

    public String loadCharacterMapping(String id) throws IOException {
        File file = new File(CHARACTER_MAPPING_DIRECTORY, id);

        return readFile(file);
    }
}
