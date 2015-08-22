package io.github.minime89.keepasstransfer.activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import io.github.minime89.keepasstransfer.FileManager;
import io.github.minime89.keepasstransfer.R;
import io.github.minime89.keepasstransfer.keyboard.Layout;
import io.github.minime89.keepasstransfer.keyboard.Layouts;

public class KeyboardLayoutActivity extends AppCompatActivity {
    private static final String TAG = KeyboardLayoutActivity.class.getSimpleName();
    private static Layouts layouts;
    private ListView keyboardLayoutList;
    private EditText keyboardLayoutSearch;
    private ArrayAdapter<Layout> keyboardLayoutListAdapter;

    private class LoadLayoutsTask extends AsyncTask<Void, Void, Layouts> {
        @Override
        protected Layouts doInBackground(Void... voids) {
            try {
                return Layouts.load();
            } catch (FileManager.FileManagerException e) {
                Log.e(TAG, "couldn't load layouts index file");
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            setSupportProgressBarIndeterminateVisibility(true);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Layouts result) { //TODO handle error when result=null?
            setSupportProgressBarIndeterminateVisibility(false);
            KeyboardLayoutActivity.layouts = result;
            KeyboardLayoutActivity.this.updateKeyboardLayoutList();
        }
    }

    private void setupKeyboardLayoutMenu() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setVisibility(View.GONE);
            progressBar.setIndeterminate(true);

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            progressBar.setLayoutParams(layoutParams);

            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(progressBar);
        }
    }

    private void setupKeyboardLayoutSearch() {
        keyboardLayoutSearch = (EditText) findViewById(R.id.keyboardLayoutSearch);
        keyboardLayoutSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                KeyboardLayoutActivity.this.keyboardLayoutListAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupKeyboardLayoutList() {
        keyboardLayoutList = (ListView) findViewById(R.id.keyboardLayoutList);
        keyboardLayoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                keyboardLayoutList.setOnItemClickListener(null);

                Layout file = keyboardLayoutListAdapter.getItem(position);
                String layoutId = file.getLayoutName();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(KeyboardLayoutActivity.this);
                sharedPreferences.edit().putString(getString(R.string.settings_keyboard_layout_key), layoutId).apply();

                NavUtils.navigateUpFromSameTask(KeyboardLayoutActivity.this);
            }
        });

        updateKeyboardLayoutList();
    }

    private void updateKeyboardLayoutList() {
        if (layouts == null) {
            LoadLayoutsTask loadLayoutsTask = new LoadLayoutsTask();
            loadLayoutsTask.execute();
        }
        else {
            List<Layout> list = new ArrayList<>(layouts.all());
            keyboardLayoutListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            keyboardLayoutList.setAdapter(keyboardLayoutListAdapter);
        }
    }

    private void selectedMenuRefresh() {
        LoadLayoutsTask loadLayoutsTask = new LoadLayoutsTask();
        loadLayoutsTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_layout_activity);

        setupKeyboardLayoutMenu();
        setupKeyboardLayoutSearch();
        setupKeyboardLayoutList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keyboard_layout_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.keyboard_layout_refresh:
                selectedMenuRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.getCustomView().setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

}
