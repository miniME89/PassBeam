package io.github.minime89.keepasstransfer.activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.Collection;
import java.util.Comparator;

import io.github.minime89.keepasstransfer.FileManager;
import io.github.minime89.keepasstransfer.KeePassTransferApplication;
import io.github.minime89.keepasstransfer.R;
import io.github.minime89.keepasstransfer.keyboard.Layout;
import io.github.minime89.keepasstransfer.keyboard.Layouts;

public class KeyboardLayoutActivity extends AppCompatActivity {
    private static final String TAG = KeyboardLayoutActivity.class.getSimpleName();
    private ArrayAdapter<LayoutWrapper> keyboardLayoutListAdapter;
    private ListView keyboardLayoutList;
    private EditText keyboardLayoutSearch;

    private class LayoutWrapper {
        public Layout layout;

        LayoutWrapper(Layout layout) {
            this.layout = layout;
        }

        @Override
        public String toString() {
            String string = "???";
            if (layout.getVariantDescription() != null && !layout.getVariantDescription().isEmpty()) {
                string = layout.getVariantDescription();
            } else if (layout.getLayoutDescription() != null && !layout.getLayoutDescription().isEmpty()) {
                string = layout.getLayoutDescription();
            } else if (layout.getLayoutName() != null && !layout.getLayoutName().isEmpty() && layout.getVariantName() != null && !layout.getVariantName().isEmpty()) {
                string = layout.getLayoutName() + "-" + layout.getVariantName();
            }

            return string;
        }
    }

    private class UpdateLayoutsTask extends AsyncTask<Void, Void, Layouts> {
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
        protected void onPostExecute(Layouts result) {
            setSupportProgressBarIndeterminateVisibility(false);

            if (result != null) {
                Collection<LayoutWrapper> layouts = new ArrayList<>();
                for (Layout layout : result.all()) {
                    layouts.add(new LayoutWrapper(layout));
                }

                keyboardLayoutListAdapter.clear();
                keyboardLayoutListAdapter.addAll(layouts);
                keyboardLayoutListAdapter.sort(new Comparator<LayoutWrapper>() {
                    @Override
                    public int compare(LayoutWrapper lhs, LayoutWrapper rhs) {
                        return lhs.toString().compareTo(rhs.toString());
                    }
                });
            } else {
                //TODO handle
            }
        }
    }

    private void setupKeyboardLayoutMenu() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setVisibility(View.GONE);
            progressBar.setIndeterminate(true);

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL);
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
        keyboardLayoutListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        keyboardLayoutList = (ListView) findViewById(R.id.keyboardLayoutList);
        keyboardLayoutList.setAdapter(keyboardLayoutListAdapter);
        keyboardLayoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                keyboardLayoutList.setOnItemClickListener(null);

                LayoutWrapper layoutWrapper = keyboardLayoutListAdapter.getItem(position);
                String layoutId = layoutWrapper.layout.getId();

                SharedPreferences sharedPreferences = KeePassTransferApplication.getInstance().getSharedPreferences();
                sharedPreferences.edit().putString(getString(R.string.settings_keyboard_layout_key), layoutId).apply();

                NavUtils.navigateUpFromSameTask(KeyboardLayoutActivity.this);
            }
        });

        updateKeyboardLayoutList();
    }

    private void updateKeyboardLayoutList() {
        UpdateLayoutsTask updateLayoutsTask = new UpdateLayoutsTask();
        updateLayoutsTask.execute();
    }

    private void selectedMenuRefresh() {
        updateKeyboardLayoutList();
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
