package io.github.minime89.keepasstransfer;


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
import java.util.Arrays;
import java.util.Collection;

public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();
    private static final String INSTALL_DIRECTORY = "app";
    private static final String SCANCODES_MAPPING_DIRECTORY = "scancodes";
    private static final String KEYSYMS_MAPPING_DIRECTORY = "keysyms";
    private static final String KEYCODES_MAPPING_DIRECTORY = "keycodes";

    private static FileManager instance;
    private final Context context;

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }

        return instance;
    }

    private FileManager() {
        context = KeePassTransfer.getContext();
    }

    /**
     * Checks if external storage is available for read and write
     *
     * @return
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
     *
     * @return
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
            Log.i(TAG, String.format("install file: %s", path));

            InputStream is = null;
            OutputStream os = null;
            try {
                //input
                is = context.getAssets().open(path);

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
            Log.i(TAG, String.format("install directory: %s", path));

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
        File file = new File(SCANCODES_MAPPING_DIRECTORY, id);

        return readFile(file);
    }

    public String loadKeysymMapping(String id) throws IOException {
        File file = new File(KEYSYMS_MAPPING_DIRECTORY, id);

        return readFile(file);
    }

    public String loadKeycodeMapping(String id) throws IOException {
        File file = new File(KEYCODES_MAPPING_DIRECTORY, id);

        return readFile(file);
    }

    public Collection<String> listScancodeMappings() {
        File directory = new File(context.getExternalFilesDir(null), SCANCODES_MAPPING_DIRECTORY);
        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Collection<String> listKeysymMappings() {
        File directory = new File(context.getExternalFilesDir(null), KEYSYMS_MAPPING_DIRECTORY);
        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Collection<String> listKeycodeMappings() {
        File directory = new File(context.getExternalFilesDir(null), KEYCODES_MAPPING_DIRECTORY);
        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }
}
