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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.minime89.passbeam.FileManager;
import io.github.minime89.passbeam.R;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = SetupActivity.class.getSimpleName();

    private class SetupTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressBar progressBar;
        private TextView progressLabel;
        private RelativeLayout rootLayout;

        @Override
        protected Boolean doInBackground(Void... voids) {
            FileManager fileManager = new FileManager();
            if (!fileManager.install()) {
                Log.e(TAG, "installation of at least one file failed");

                return false;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            rootLayout = (RelativeLayout) SetupActivity.this.findViewById(R.id.rootLayout);
            progressBar = (ProgressBar) SetupActivity.this.findViewById(R.id.verifyRootProgressBar);
            progressLabel = (TextView) SetupActivity.this.findViewById(R.id.progressLabel);

            progressBar.setVisibility(View.VISIBLE);
            progressLabel.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                progressLabel.setText("finished");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
                sharedPreferences.edit().putBoolean("setup", true).apply();

                Intent intent = new Intent(SetupActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            } else {
                progressLabel.setText("error");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity);

        new SetupTask().execute();

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //sharedPreferences.edit().putBoolean("setup", true).apply();
    }
}
