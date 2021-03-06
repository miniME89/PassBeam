/*
 * Copyright (C) 2015 Marcel Lehwald
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.minime89.passbeam;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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

import io.github.minime89.passbeam.keyboard.Keycodes;
import io.github.minime89.passbeam.keyboard.Keysyms;
import io.github.minime89.passbeam.keyboard.Layout;
import io.github.minime89.passbeam.keyboard.Scancodes;
import io.github.minime89.passbeam.xml.TransformMatcher;

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
     * Resolve a path relative to the application directory to the absolute path.
     *
     * @param path The path.
     * @return Returns the absolute filepath.
     */
    public File resolvePath(String path) {
        Context context = PassBeamApplication.getInstance().getContext();
        return new File(context.getExternalFilesDir(null), path);
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
        Context context = PassBeamApplication.getInstance().getContext();
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
                File outputFile = resolvePath(targetPath);
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

            File outputDir = resolvePath(targetPath);
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

    /**
     * Read from file.
     *
     * @param file The {@link File}.
     * @return Returns the content of the file.
     */
    public byte[] loadFile(File file) throws FileManagerException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));

            int read;
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            while ((read = is.read(bytes)) != -1) {
                data.write(bytes, 0, read);              //TODO not tested
            }

            return data.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, String.format("error reading from file '%s': %s", file.getPath(), e.getMessage()));

            throw new FileManagerException(String.format("error reading from file '%s'", file.getPath()), e);
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

    /**
     * Write to file.
     *
     * @param file The {@link File}.
     * @param data The data to write.
     */
    public void storeFile(File file, byte[] data) throws FileManagerException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(data);
        } catch (IOException e) {
            Log.e(TAG, String.format("error writing to file '%s': %s", file.getPath(), e.getMessage()));

            throw new FileManagerException(String.format("error writing to file '%s'", file.getPath()), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, String.format("error closing file: %s", e.getMessage()));
                }
            }
        }
    }

    public <T> T loadXmlFile(File file, Class<T> c) throws FileManagerException {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy, new TransformMatcher());

        T instance;
        try {
            instance = serializer.read(c, file);
        } catch (Exception e) {
            throw new FileManagerException(String.format("couldn't decode XML of file '%s'", file.getPath()), e);
        }

        return instance;
    }

    public <T> void storeXmlFile(File file, T instance) throws FileManagerException {
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy, new TransformMatcher());

        try {
            serializer.write(instance, file);
        } catch (Exception e) {
            throw new FileManagerException(String.format("couldn't encode object into file '%s'", file.getPath()), e);
        }
    }

    public Collection<File> getKeycodesFiles() {
        File directory = resolvePath(KEYCODES_MAPPING_DIRECTORY);

        File[] directoryList = directory.listFiles();

        return Arrays.asList(directoryList);
    }

    public Collection<String> getKeysymsFiles() {
        File directory = resolvePath(KEYSYMS_MAPPING_DIRECTORY);

        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Collection<String> getScancodesFiles() {
        File directory = resolvePath(SCANCODES_MAPPING_DIRECTORY);

        String[] directoryList = directory.list();

        return Arrays.asList(directoryList);
    }

    public Keycodes loadKeycodes(String keycodesId) throws FileManagerException {
        Log.v(TAG, String.format("load keycodes with ID '%s'", keycodesId));

        File file = resolvePath(KEYCODES_MAPPING_DIRECTORY + "/" + keycodesId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find keycodes file with ID '%s'", keycodesId));
        }

        return loadXmlFile(file, Keycodes.class);
    }

    public Keysyms loadKeysyms(String keysymsId) throws FileManagerException {
        Log.v(TAG, String.format("load keysyms with ID '%s'", keysymsId));

        File file = resolvePath(KEYSYMS_MAPPING_DIRECTORY + "/" + keysymsId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find keysyms file with ID '%s'", keysymsId));
        }

        return loadXmlFile(file, Keysyms.class);
    }

    public Scancodes loadScancodes(String scancodesId) throws FileManagerException {
        Log.v(TAG, String.format("load scancodes with ID '%s'", scancodesId));

        File file = resolvePath(SCANCODES_MAPPING_DIRECTORY + "/" + scancodesId);

        if (!file.exists()) {
            throw new FileManagerException(String.format("couldn't find scancodes file with ID '%s'", scancodesId));
        }

        return loadXmlFile(file, Scancodes.class);
    }

    public Collection<Layout> loadLayouts() throws FileManagerException {
        Log.v(TAG, "load layouts");

        Collection<Layout> layouts = new ArrayList<>();

        Collection<File> keycodesFiles = getKeycodesFiles();

        XmlPullParser parser;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
        } catch (XmlPullParserException e) {
            throw new FileManagerException("unable to instantiate XML parser");
        }

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
                if (!layout.getId().equals(keycodesFile.getName())) {
                    throw new FileManagerException("layout ID doesn't match the filename");
                }

                layouts.add(layout);
            } catch (IOException e) {
                Log.w(TAG, String.format("couldn't decode keycodes file '%s' (IO error): %s", keycodesFile, e.getMessage()));
            } catch (XmlPullParserException e) {
                Log.w(TAG, String.format("couldn't decode keycodes file '%s' (XML error): %s", keycodesFile, e.getMessage()));
            } catch (FileManagerException e) {
                Log.w(TAG, String.format("couldn't decode keycodes file '%s' (constraint error): %s", keycodesFile, e.getMessage()));
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
