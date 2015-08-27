package io.github.minime89.passbeam.keyboard;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.github.minime89.passbeam.PassBeamApplication;
import io.github.minime89.passbeam.R;
import io.github.minime89.passbeam.Utils;

/**
 * Service for writing strings to the HID keyboard device which outputs them over USB. Superuser
 * privileges are requested to write to the HID keyboard device file. The service handles any
 * operations in a background thread.
 */
public class DeviceWriter extends IntentService {
    private static final String TAG = DeviceWriter.class.getSimpleName();
    /**
     * The time the service waits for new string write requests before shutting down.
     */
    private static final int SERVICE_TIMEOUT = 2;

    /**
     * The keyboard symbol converter.
     */
    private static Converter converter = new Converter();

    /**
     * The queue which contains the string write requests.
     */
    private static BlockingQueue<String> stringQueue = new LinkedBlockingQueue<>();

    /**
     * The list of requested intents processed by the service.
     */
    private static List<Intent> intentsList = new ArrayList<>();

    /**
     * Constructor.
     */
    public DeviceWriter() {
        super("DeviceWriter");
    }

    /**
     * Process the string write requests triggered by {@link DeviceWriter#write(String)}.
     * The method will proceed as following:<br><br>
     * <p/>
     * 1. Create new process which switches to super user<br>
     * 2. Process all strings added to {@link DeviceWriter#stringQueue}.<br>
     * &nbsp;&nbsp;2.1 Convert string to encoded keyboard event<br>
     * &nbsp;&nbsp;2.2 Write each event to the device<br>
     * 3. End the process<br><br>
     * <p/>
     * In step 2 all strings will be processed from {@link DeviceWriter#stringQueue} and
     * some time (determined by {@link DeviceWriter#SERVICE_TIMEOUT} will be waited for new
     * string write requests to arrive. If no new string write requests arrive within this time, the
     * process will be exited and the service shuts down.
     */
    private void suWrite() {
        Log.v(TAG, "starting superuser keyboard device writer");

        //get preference characterTimeout
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String characterTimeoutStr = sharedPreferences.getString(getString(R.string.settings_character_timeout_key), "20");
        int characterTimeout = Integer.parseInt(characterTimeoutStr);

        Process process;
        int processReturnCode = -1;
        try {
            // load new process which switches to superuser
            process = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            while (true) {
                String str = stringQueue.poll(SERVICE_TIMEOUT, TimeUnit.SECONDS);
                if (str == null) {
                    break;
                }

                Log.v(TAG, String.format("process string from keyboard device writer queue '%s'", str));

                try {
                    Collection<byte[]> encodedCharacters = converter.convert(str);

                    for (byte[] encodedCharacter : encodedCharacters) {
                        String cmd = "";
                        cmd += String.format("echo -n -e \"%s\" > /dev/hidg0\n", Utils.bytesToHex(encodedCharacter, Utils.HexFormat.UNIX));
                        cmd += String.format("sleep %f\n", (characterTimeout / 1000.0));

                        os.writeBytes(cmd);
                        os.flush();
                    }
                } catch (Converter.ConverterException e) {
                    Log.e(TAG, String.format("couldn't convert string '%s'", str));
                }
            }

            // close
            os.writeBytes("exit\n");
            os.flush();

            // check execution result
            process.waitFor();
            processReturnCode = process.exitValue();
            if (processReturnCode != 0) {
                throw new RuntimeException();
            }
        } catch (IOException e) {
            Log.e(TAG, String.format("couldn't write to device file: %s", e.getMessage()));
        } catch (InterruptedException e) {
            Log.e(TAG, "keyboard device writer was interrupted");
        } catch (RuntimeException e) {
            Log.e(TAG, String.format("superuser process exited with code %d", processReturnCode));
        }
    }

    /**
     * Stops any requested intents, except for one. This is necessary because {@link DeviceWriter#write(String)}
     * adds the string to {@link DeviceWriter#stringQueue} and requests an intent every time
     * a new string is requested to be written to the device. The service itself however, processes
     * all elements in {@link DeviceWriter#stringQueue} and waits for some time for new
     * string write requests before shutting down; hence the service does not need to be started for
     * every string write request separately.
     *
     * @see DeviceWriter#addIntentQueue(Intent)
     */
    private void clearIntentQueue() {
        synchronized (intentsList) {
            for (int i = 1; i < intentsList.size(); i++) {
                stopService(intentsList.get(i));
            }
        }
    }

    /**
     * Add an intent to {@link DeviceWriter#intentsList}. This keeps track of any requested
     * intents which will be processed automatically one after another.
     *
     * @param intent The intent.
     * @see DeviceWriter#clearIntentQueue()
     */
    private void addIntentQueue(Intent intent) {
        synchronized (intentsList) {
            intentsList.add(intent);
        }
    }

    /**
     * Request the service to write the given string as a HID keyboard. The request will start the
     * service, encode the string into the appropriate output format and write the data to the
     * device using superuser privileges.
     * <p/>
     * The method will return immediately and no feedback is returned by the service (for now).
     *
     * @param str The string.
     */
    public static void write(String str) {
        Log.v(TAG, String.format("added string to keyboard device writer queue '%s'", str));

        stringQueue.add(str);

        Context context = PassBeamApplication.getInstance().getContext();
        Intent intent = new Intent(context, DeviceWriter.class);
        context.startService(intent);
    }

    /**
     * Get the converter.
     *
     * @return Returns the converter.
     */
    public static Converter getConverter() {
        return converter;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addIntentQueue(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "started keyboard device writer service");

        suWrite();
        clearIntentQueue();

        Log.v(TAG, "stopped keyboard device writer service");
    }
}
