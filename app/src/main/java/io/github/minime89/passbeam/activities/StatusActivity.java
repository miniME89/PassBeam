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

package io.github.minime89.passbeam.activities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.minime89.passbeam.R;

public class StatusActivity extends AppCompatActivity {
    private static final String TAG = StatusActivity.class.getSimpleName();
    private static final String PROC_VERSION_FILENAME = "/proc/version";

    private ProgressBar verifyRootProgressBar;
    private ImageView verifyRootImage;
    private ProgressBar verifyHidProgressBar;
    private ImageView verifyHidImage;

    private class StatusResult {
        public boolean rootAvailable = false;
        public boolean hidAvailable = false;
    }

    private class TaskStatusCheck extends AsyncTask<Void, Void, StatusResult> {
        @Override
        protected StatusResult doInBackground(Void... params) {
            StatusResult result = new StatusResult();

            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                DataInputStream is = new DataInputStream(process.getInputStream());

                os.writeBytes("test -c /dev/hidg0\n");
                os.writeBytes("echo $?\n");
                os.flush();

                byte[] buffer = new byte[1024];
                int length = is.read(buffer);
                if (length > 0) {
                    String str = new String(buffer, 0, length, "UTF8").trim();
                    result.hidAvailable = str.equals("0");
                }

                os.writeBytes("exit\n");
                os.flush();

                result.rootAvailable = process.waitFor() == 0;
            } catch (IOException | InterruptedException e) {
                //ignore
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            verifyRootImage.setVisibility(View.INVISIBLE);
            verifyRootProgressBar.setVisibility(View.VISIBLE);
            verifyHidImage.setVisibility(View.INVISIBLE);
            verifyHidProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(StatusResult result) {
            if (result.rootAvailable) {
                verifyRootImage.setImageResource(R.drawable.ic_check);
            } else {
                verifyRootImage.setImageResource(R.drawable.ic_cross);
            }

            if (result.hidAvailable) {
                verifyHidImage.setImageResource(R.drawable.ic_check);
            } else {
                verifyHidImage.setImageResource(R.drawable.ic_cross);
            }

            Animation shrinkAnimation = AnimationUtils.loadAnimation(StatusActivity.this, R.anim.shrink);
            verifyRootProgressBar.startAnimation(shrinkAnimation);
            verifyHidProgressBar.startAnimation(shrinkAnimation);

            Animation popinAnimation = AnimationUtils.loadAnimation(StatusActivity.this, R.anim.popin);
            popinAnimation.setStartOffset(400);
            verifyRootImage.startAnimation(popinAnimation);
            verifyRootImage.setVisibility(View.VISIBLE);
            verifyHidImage.startAnimation(popinAnimation);
            verifyHidImage.setVisibility(View.VISIBLE);
        }
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private String getKernelVersion() {
        String kernel = getString(R.string.status_device_unknown);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(PROC_VERSION_FILENAME), 256);

            kernel = reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, String.format("error reading from file '%s': %s", PROC_VERSION_FILENAME, e.getMessage()));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, String.format("error closing file: %s", e.getMessage()));
                }
            }
        }

        String procVersionRegex = "Linux version (\\S+) \\((\\S+?)\\) (?:\\(gcc.+? \\)) (#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)";
        Matcher m = Pattern.compile(procVersionRegex).matcher(kernel);
        if (m.matches() && m.groupCount() == 5) {
            kernel = m.group(1) + "\n" + m.group(2) + " " + m.group(3) + "\n" + m.group(4);
        }

        return kernel;
    }

    private String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    private void updateDeviceInfo() {
        TextView deviceNameText = (TextView) findViewById(R.id.deviceName);
        TextView deviceAndroidVersionText = (TextView) findViewById(R.id.deviceAndroidVersion);
        TextView deviceKernelVersionText = (TextView) findViewById(R.id.deviceKernelVersion);

        String deviceName = getDeviceName();
        deviceNameText.setText(deviceName);

        String androidVersion = getAndroidVersion();
        deviceAndroidVersionText.setText(androidVersion);

        String kernelVersion = getKernelVersion();
        deviceKernelVersionText.setText(kernelVersion);
    }

    private void updateStatus() {
        TaskStatusCheck taskStatusCheck = new TaskStatusCheck();
        taskStatusCheck.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_activity);

        verifyRootProgressBar = (ProgressBar) findViewById(R.id.verifyRootProgressBar);
        verifyRootImage = (ImageView) findViewById(R.id.verifyRootImage);

        verifyHidProgressBar = (ProgressBar) findViewById(R.id.verifyHidProgressBar);
        verifyHidImage = (ImageView) findViewById(R.id.verifyHidImage);

        updateDeviceInfo();
        updateStatus();
    }
}
