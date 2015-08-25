package io.github.minime89.keepasstransfer;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.github.minime89.keepasstransfer.binding.IntegerMatcher;
import io.github.minime89.keepasstransfer.keyboard.Keycodes;
import io.github.minime89.keepasstransfer.keyboard.Keysyms;
import io.github.minime89.keepasstransfer.keyboard.Layout;
import io.github.minime89.keepasstransfer.keyboard.Scancodes;

public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();
    private static final String INSTALL_DIRECTORY = "app";
    private static final String SCANCODES_MAPPING_DIRECTORY = "scancodes";
    private static final String KEYSYMS_MAPPING_DIRECTORY = "keysyms";
    private static final String KEYCODES_MAPPING_DIRECTORY = "keycodes";

    /**
     *
     */
    public static class FileManagerException extends Exception {
        FileManagerException() {
            super();
        }

        FileManagerException(String message) {
            super(message);
        }

        FileManagerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Constructor.
     */
    public FileManager() {

    }

    /**
     * Checks if external storage is available for read and write
     *
     * @return Returns true if the external storage is writable.
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     *
     * @return Returns true if the external storage is readable.
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Install the provided asset path into the files folder of the external storage, available to
     * the application. If the path is a folder, it will recursively try to install the files and
     * directories in that folder. If the path is a file, the file will be copied to the target
     * destination.
     *
     * @param path The path to install.
     * @return Returns true if recursive copying of all files and folder was successful. If a single
     * file or directory operation failed, than false will be returned.
     */
    private boolean install(String path) {
        Context context = KeePassTransfer.getInstance().getContext();
        String targetPath = path.substring(INSTALL_DIRECTORY.length());

        //list assets
        AssetManager assetManager = context.getAssets();
        String assets[];
        try {
            assets = assetManager.list(path);
        } catch (IOException e) {
            Log.e(TAG, String.format("error listing assets under path '%s': %s", path, e.getMessage()));

            return false;
        }

        //install file
        if (assets.length == 0) {
            Log.v(TAG, String.format("install file: %s", path));

            InputStream is = null;
            OutputStream os = null;
            try {
                //input
                is = context.getAssets().open(path);

                //output
                File outputFile = new File(context.getExternalFilesDir(null), targetPath);
                os = new FileOutputStream(outputFile);

                //write data
                int read;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
            } catch (IOException e) {
                Log.e(TAG, String.format("error installing file '%s': %s", path, e.getMessage()));

                return false;
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
            Log.v(TAG, String.format("install directory: %s", path));

            File outputDir = new File(context.getExternalFilesDir(null), targetPath);
            if (!outputDir.exists()) {
                if (!outputDir.mkdir()) {
                    Log.e(TAG, String.format("error installing directory '%s': couldn't load directory", path));

                    return false;
                }
            }

            for (String asset : assets) {
                if (!install(path + "/" + asset)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Install all assets into the files folder of the external storage, available to the
     * application.
     *
     * @return Returns true if installation of all files was successful.
     */
    public boolean install() {
        return install(INSTALL_DIRECTORY);
    }

    private <T> T loadXmlFile(File file, Class<T> c) throws FileManagerException {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy, new IntegerMatcher());

        T instance;
        try {
            instance = serializer.read(c, file);
        } catch (Exception e) {
            throw new FileManagerException(String.format("couldn't decode XML of file '%s'", file.getPath()), e);
        }

        return instance;
    }

    private <T> T storeXmlFile(File file, T instance) throws FileManagerException {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy, new IntegerMatcher());

        try {
            serializer.write(instance, file);
        } catch (Exception e) {
            throw new FileManagerException(String.format("couldn't encode object into file '%s'", file.getPath()), e);
        }

        return instance;
    }

    public Collection<File> getKeycodesFiles() {
        Context context = KeePassTransfer.getInstance().getContext();
        File directory = new File(context.getExternalFilesDir(null), KEYCODES_MAPPING_DIRECTORY);

        File[] directoryList = directory.listFiles();

        return Arrays.asList(directoryList);
    }

    public Collection<String> getKeysymsFiles() {
        Context context = KeePassTransfer.getInstance().getContext();
        File directory = new File(context.getExternalFilesDir(null), KEYSYMS_MAPPING_DIRECTORY);

        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Collection<String> getScancodesFiles() {
        Context context = KeePassTransfer.getInstance().getContext();
        File directory = new File(context.getExternalFilesDir(null), SCANCODES_MAPPING_DIRECTORY);

        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Keycodes loadKeycodes(String keycodesId) throws FileManagerException {
        Log.v(TAG, String.format("load keycodes with ID '%s'", keycodesId));

        Context context = KeePassTransfer.getInstance().getContext();
        File file = new File(context.getExternalFilesDir(null), KEYCODES_MAPPING_DIRECTORY + "/" + keycodesId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find keycodes file with ID '%s'", keycodesId));
        }

        return loadXmlFile(file, Keycodes.class);
    }

    public Keysyms loadKeysyms(String keysymsId) throws FileManagerException {
        Log.v(TAG, String.format("load keysyms with ID '%s'", keysymsId));

        Context context = KeePassTransfer.getInstance().getContext();
        File file = new File(context.getExternalFilesDir(null), KEYSYMS_MAPPING_DIRECTORY + "/" + keysymsId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find keysyms file with ID '%s'", keysymsId));
        }

        return loadXmlFile(file, Keysyms.class);
    }

    public Scancodes loadScancodes(String scancodesId) throws FileManagerException {
        Log.v(TAG, String.format("load scancodes with ID '%s'", scancodesId));

        Context context = KeePassTransfer.getInstance().getContext();
        File file = new File(context.getExternalFilesDir(null), SCANCODES_MAPPING_DIRECTORY + "/" + scancodesId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find scancodes file with ID '%s'", scancodesId));
        }

        return loadXmlFile(file, Scancodes.class);
    }

    public Collection<Layout> loadLayouts() {
        Log.v(TAG, "load layouts");

        Collection<Layout> layouts = new ArrayList<>();

        Collection<File> keycodesFiles = getKeycodesFiles();
        XmlPullParser parser = Xml.newPullParser();
        for (File keycodesFile : keycodesFiles) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(keycodesFile), 1024);
                parser.setInput(is, null);

                Map<String, String> elements = new HashMap<>();
                elements.put("layoutName", "");
                elements.put("layoutDescription", "");
                elements.put("variantName", "");
                elements.put("variantDescription", "");

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tag = parser.getName();
                        for (Map.Entry<String, String> entry : elements.entrySet()) {
                            if (entry.getKey().equals(tag)) {
                                String text = parser.nextText();
                                entry.setValue(text);

                                break;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.getName().equals("layout")) {
                        break;
                    }
                    eventType = parser.next();
                }

                Layout layout = new Layout(elements.get("layoutName"), elements.get("layoutDescription"), elements.get("variantName"), elements.get("variantDescription"));
                layouts.add(layout);
            } catch (IOException | XmlPullParserException e) {
                Log.w(TAG, String.format("couldn't decode keycodes file '%s'", keycodesFile));
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

        return layouts;
    }
}
