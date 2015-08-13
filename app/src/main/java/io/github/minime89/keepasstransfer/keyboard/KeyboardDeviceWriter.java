package io.github.minime89.keepasstransfer.keyboard;

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

import io.github.minime89.keepasstransfer.KeePassTransfer;
import io.github.minime89.keepasstransfer.R;
import io.github.minime89.keepasstransfer.Utils;

/**
 * Service for writing strings to the HID keyboard device which outputs them over USB. Superuser
 * privileges are requested to write to the HID keyboard device file. The service handles any
 * operations in a background thread.
 */
public class KeyboardDeviceWriter extends IntentService {
    private static final String TAG = KeyboardDeviceWriter.class.getSimpleName();
    /**
     * The time the service waits for new string write requests before shutting down.
     */
    private static final int SERVICE_TIMEOUT = 2;

    /**
     * The queue which contains the string write requests.
     */
    private static BlockingQueue<String> stringQueue = new LinkedBlockingQueue<>();

    /**
     * The list of requested intents processed by the service.
     */
    private final List<Intent> intentsList = new ArrayList<>();

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
        stringQueue.add(str);

        Context context = KeePassTransfer.getContext();
        Intent intent = new Intent(context, KeyboardDeviceWriter.class);
        context.startService(intent);
    }

    public KeyboardDeviceWriter() {
        super("KeyboardDeviceWriter");
    }

    /**
     * Process the string write requests triggered by {@link KeyboardDeviceWriter#write(String)}.
     * The method will proceed as following:<br><br>
     * <p/>
     * 1. Create new process which switches to super user<br>
     * 2. Process all strings added to {@link KeyboardDeviceWriter#stringQueue}.<br>
     * &nbsp;&nbsp;2.1 Convert string to encoded keyboard event<br>
     * &nbsp;&nbsp;2.2 Write each event to the device<br>
     * 3. End the process<br><br>
     * <p/>
     * In step 2 all strings will be processed from {@link KeyboardDeviceWriter#stringQueue} and
     * some time (determined by {@link KeyboardDeviceWriter#SERVICE_TIMEOUT} will be waited for new
     * string write requests to arrive. If no new string write requests arrive within this time, the
     * process will be exited and the service shuts down.
     */
    private void suWrite() {
        CharacterConverter characterConverter = CharacterConverter.getInstance();

        //get preference characterTimeout
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String characterTimeoutStr = sharedPreferences.getString(getString(R.string.settings_character_timeout_key), "20");
        int characterTimeout = Integer.parseInt(characterTimeoutStr);

        Process process;
        int processReturnCode = -1;
        try {
            // create new process which switches to superuser
            process = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            while (true) {
                String str = stringQueue.poll(SERVICE_TIMEOUT, TimeUnit.SECONDS);
                if (str == null) {
                    break;
                }

                try {
                    Collection<byte[]> encodedCharacters = characterConverter.convert(str);

                    for (byte[] encodedCharacter : encodedCharacters) {
                        String cmd = "";
                        cmd += String.format("echo -n -e \"%s\" > /dev/hidg0\n", Utils.bytesToHex(encodedCharacter, Utils.HexFormat.UNIX));
                        cmd += String.format("sleep %f\n", (characterTimeout / 1000.0));

                        os.writeBytes(cmd);
                        os.flush();

                        Log.i(TAG, cmd);
                    }
                } catch (CharacterConverter.CharacterConverterException e) {
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
     * Stops any requested intents, except for one. This is necessary because {@link KeyboardDeviceWriter#write(String)}
     * adds the string to {@link KeyboardDeviceWriter#stringQueue} and requests an intent every time
     * a new string is requested to be written to the device. The service itself however, processes
     * all elements in {@link KeyboardDeviceWriter#stringQueue} and waits for some time for new
     * string write requests before shutting down; hence the service does not need to be started for
     * every string write request separately.
     *
     * @see KeyboardDeviceWriter#addIntentQueue(Intent)
     */
    private void clearIntentQueue() {
        synchronized (intentsList) {
            for (int i = 1; i < intentsList.size(); i++) {
                stopService(intentsList.get(i));
            }
        }
    }

    /**
     * Add an intent to {@link KeyboardDeviceWriter#intentsList}. This keeps track of any requested
     * intents which will be processed automatically one after another.
     *
     * @param intent The intent.
     * @see KeyboardDeviceWriter#clearIntentQueue()
     */
    private void addIntentQueue(Intent intent) {
        synchronized (intentsList) {
            intentsList.add(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addIntentQueue(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        suWrite();
        clearIntentQueue();
    }
}
