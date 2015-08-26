package io.github.minime89.keepasstransfer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.minime89.keepasstransfer.FileManager;
import io.github.minime89.keepasstransfer.KeePassTransferApplication;
import io.github.minime89.keepasstransfer.R;

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
            progressBar = (ProgressBar) SetupActivity.this.findViewById(R.id.progressBar);
            progressLabel = (TextView) SetupActivity.this.findViewById(R.id.progressLabel);

            progressBar.setVisibility(View.VISIBLE);
            progressLabel.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                progressLabel.setText("finished");

                SharedPreferences sharedPreferences = KeePassTransferApplication.getInstance().getSharedPreferences();
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
