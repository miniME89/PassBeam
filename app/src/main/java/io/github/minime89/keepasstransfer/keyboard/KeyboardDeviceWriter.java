package io.github.minime89.keepasstransfer.keyboard;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.Collection;

import io.github.minime89.keepasstransfer.Utils;

/**
 * Service for writing strings to the HID keyboard device which outputs them over USB. Superuser
 * privileges are requested to write to the HID keyboard device file. The service handles any
 * operations in a background thread.
 */
public class KeyboardDeviceWriter extends IntentService {
    private static final String TAG = KeyboardDeviceWriter.class.getSimpleName();
    private static final int CHARACTER_TIMEOUT = 20;

    /**
     * Request the service to write the given string as a HID keyboard. The request will start the
     * service, encode the string into the appropriate output format and write the data to the
     * device using superuser privileges.
     * <p/>
     * The method will return immediately and no feedback is returned by the service (for now).
     *
     * @param context The context.
     * @param str     The string.
     */
    public static void write(Context context, String str) {
        if (context == null) {
            return;
        }

        //start service
        Intent i = new Intent(context, KeyboardDeviceWriter.class);
        i.putExtra("str", str);
        context.startService(i);
    }

    public KeyboardDeviceWriter() {
        super("KeyboardDeviceWriter");
    }

    private void suWrite(String str) {
        CharacterConverter characterConverter = CharacterConverter.getInstance();

        Process p;
        try {
            // create new process as superuser
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            //convert string to keyboard event
            Collection<byte[]> data = characterConverter.convert(str);

            for (byte[] bytes : data) {
                // write
                String cmd = "echo -n -e \"" + Utils.bytesToHex(bytes, Utils.HexFormat.UNIX) + "\" > /dev/hidg0\n";
                Log.i(TAG, cmd);

                os.writeBytes(cmd);
                os.flush();

                Thread.sleep(CHARACTER_TIMEOUT);
            }

            // close
            os.writeBytes("exit\n");
            os.flush();

            // check execution result
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new RuntimeException();
            }

            //Toast.makeText(KeePassTransfer.getContext(), "success", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "SU success");
        } catch (Exception e) {
            //Toast.makeText(KeePassTransfer.getContext(), "failed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "SU failed");
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        String str = intent.getStringExtra("str");
        suWrite(str);
    }
}
