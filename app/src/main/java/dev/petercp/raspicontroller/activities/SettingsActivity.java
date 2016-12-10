package dev.petercp.raspicontroller.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.utils.ConfigManager;

public class SettingsActivity extends AppCompatActivity {

    private static class Language {
        private String code, name;

        private Language(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Language)
                return code.equals(((Language) obj).code);
            else if (obj instanceof String)
                return code.equals(obj);
            return super.equals(obj);
        }
    }

    private static final ArrayList<Language> LANGUAGES = new ArrayList<>();
    static {
        LANGUAGES.add(new Language("", "Default"));
        LANGUAGES.add(new Language("en", "English"));
        LANGUAGES.add(new Language("es", "Español"));
    }

    public static final int CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager.updateContextLocale(this);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ((EditText) findViewById(R.id.input_pref_server)).setText(ConfigManager.getServerUrl(this));
        Spinner spinner = (Spinner) findViewById(R.id.input_pref_locale);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, LANGUAGES));
        spinner.setSelection(LANGUAGES.indexOf(new Language(ConfigManager.getLocale(this), "")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_done:
                String serverUrl = ((EditText) findViewById(R.id.input_pref_server)).getText().toString();
                String code = ((Language) ((Spinner) findViewById(R.id.input_pref_locale)).getSelectedItem()).code;
                ConfigManager.setServerUrl(this, serverUrl);
                ConfigManager.setLocale(this, code);
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
