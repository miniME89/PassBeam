package io.github.minime89.passbeam.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.minime89.passbeam.FileManager;
import io.github.minime89.passbeam.R;
import io.github.minime89.passbeam.keyboard.Keycodes;
import io.github.minime89.passbeam.keyboard.Layout;
import io.github.minime89.passbeam.keyboard.Layouts;

public class KeyboardLayoutActivity extends AppCompatActivity {
    private static final String TAG = KeyboardLayoutActivity.class.getSimpleName();
    private KeyboardLayoutAdapter keyboardLayoutAdapter;
    private ListView keyboardLayoutList;
    private EditText keyboardLayoutSearch;
    private Layouts layouts;

    private static abstract class Item {
        public abstract View createView(LayoutInflater layoutInflater);

        public abstract int getType();
    }

    private static class LayoutItem extends Item {
        public Layout layout;

        public LayoutItem(Layout layout) {
            this.layout = layout;
        }

        @Override
        public View createView(LayoutInflater layoutInflater) {
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
            view.setPadding(view.getPaddingLeft() + 10, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());

            return view;
        }

        @Override
        public int getType() {
            return KeyboardLayoutAdapter.Type.LIST_ITEM.ordinal();
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

    private static class HeaderItem extends Item {
        private String text;

        public HeaderItem(String text) {
            this.text = text;
        }

        @Override
        public View createView(LayoutInflater layoutInflater) {
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
            TextView textView = (TextView) view;
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            return view;
        }

        @Override
        public int getType() {
            return KeyboardLayoutAdapter.Type.HEADER_ITEM.ordinal();
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private static class KeyboardLayoutAdapter extends BaseAdapter {
        public enum Type {
            LIST_ITEM, HEADER_ITEM
        }

        private Context context;

        private LayoutInflater layoutInflater;
        private List<Layout> layouts;
        private Layout currentLayout;

        private List<LayoutItem> wrappedLayoutItems;
        private List<LayoutItem> wrappedLayoutItemsFiltered;
        private LayoutItem wrappedCurrentLayout;

        private List<Item> wrappedItems;

        public KeyboardLayoutAdapter(Context context) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);

            update();
        }

        public void filter(String filter) {
            if (filter == null || filter.isEmpty()) {
                wrappedLayoutItemsFiltered = null;
            } else {
                filter = filter.toLowerCase();

                wrappedLayoutItemsFiltered = new ArrayList<>();

                for (LayoutItem layoutItem : wrappedLayoutItems) {
                    if (layoutItem.toString().toLowerCase().contains(filter)) {
                        wrappedLayoutItemsFiltered.add(layoutItem);
                    }
                }
            }

            update();
        }

        private void update() {
            wrappedItems = new ArrayList<>();

            boolean isFiltered = wrappedLayoutItemsFiltered != null;

            //current layout
            if (!isFiltered) {
                if (wrappedCurrentLayout != null) {
                    wrappedItems.add(new HeaderItem(context.getString(R.string.keyboard_layout_header_current)));
                    wrappedItems.add(wrappedCurrentLayout);
                }
            }

            //all layouts
            if (!isFiltered) {
                if (wrappedLayoutItems != null) {
                    wrappedItems.add(new HeaderItem(context.getString(R.string.keyboard_layout_header_layout)));
                    wrappedItems.addAll(wrappedLayoutItems);
                }
            } else {
                wrappedItems.addAll(wrappedLayoutItemsFiltered);
            }

            notifyDataSetChanged();
        }

        public List<Layout> getLayouts() {
            return layouts;
        }

        public void setLayouts(List<Layout> layouts) {
            this.layouts = layouts;

            //wrap Layout in LayoutItem
            wrappedLayoutItems = new ArrayList<>();
            for (Layout layout : layouts) {
                LayoutItem item = new LayoutItem(layout);
                wrappedLayoutItems.add(item);
            }

            //sort by name
            Collections.sort(wrappedLayoutItems, new Comparator<Item>() {
                @Override
                public int compare(Item lhs, Item rhs) {
                    return lhs.toString().compareTo(rhs.toString());
                }
            });

            update();
        }

        public Layout getCurrentLayout() {
            return currentLayout;
        }

        public void setCurrentLayout(Layout currentLayout) {
            this.currentLayout = currentLayout;

            //wrap Layout in LayoutItem
            wrappedCurrentLayout = new LayoutItem(currentLayout);

            update();
        }

        @Override
        public boolean isEnabled(int position) {
            return !(getItem(position) instanceof HeaderItem);
        }

        @Override
        public int getCount() {
            return wrappedItems.size();
        }

        @Override
        public Item getItem(int position) {
            return wrappedItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return Type.values().length;

        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getItem(position).createView(layoutInflater);
            }

            TextView textView = (TextView) convertView;
            textView.setText(getItem(position).toString());

            return convertView;
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

            List<Layout> layouts = new ArrayList<>(result.getLayouts());
            keyboardLayoutAdapter.setLayouts(layouts);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(KeyboardLayoutActivity.this);
            String id = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), Keycodes.DEFAULT_ID);
            Layout layout = result.find(id);
            if (layout != null) {
                keyboardLayoutAdapter.setCurrentLayout(layout);
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
                keyboardLayoutAdapter.filter(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupKeyboardLayoutList() {
        keyboardLayoutAdapter = new KeyboardLayoutAdapter(this);

        keyboardLayoutList = (ListView) findViewById(R.id.keyboardLayoutList);
        keyboardLayoutList.setAdapter(keyboardLayoutAdapter);
        keyboardLayoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                keyboardLayoutList.setOnItemClickListener(null);

                Item item = keyboardLayoutAdapter.getItem(position);
                if (item instanceof LayoutItem) {
                    LayoutItem layoutItem = (LayoutItem) item;
                    String layoutId = layoutItem.layout.getId();

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(KeyboardLayoutActivity.this);
                    sharedPreferences.edit().putString(getString(R.string.settings_keyboard_layout_key), layoutId).apply();

                    NavUtils.navigateUpFromSameTask(KeyboardLayoutActivity.this);
                }
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
