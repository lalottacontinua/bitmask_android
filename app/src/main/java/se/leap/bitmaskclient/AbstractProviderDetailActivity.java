package se.leap.bitmaskclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.InjectView;

import static se.leap.bitmaskclient.Constants.PROVIDER_ALLOW_ANONYMOUS;
import static se.leap.bitmaskclient.Constants.PROVIDER_KEY;

public abstract class AbstractProviderDetailActivity extends ConfigWizardBaseActivity {

    final public static String TAG = "providerDetailActivity";

    @InjectView(R.id.provider_detail_description)
    AppCompatTextView description;

    @InjectView(R.id.provider_detail_options)
    ListView options;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_detail_fragment);

        try {
            JSONObject providerJson = new JSONObject(preferences.getString(Provider.KEY, ""));
            setProviderHeaderText(getProviderName());
            description.setText(providerJson.getJSONObject(Provider.DESCRIPTION).getString("en"));

            // Show only the options allowed by the provider
            ArrayList<String> optionsList = new ArrayList<>();
            if (registrationAllowed(providerJson)) {
                optionsList.add(getString(R.string.login_to_profile));
                optionsList.add(getString(R.string.create_profile));
            }
            if (anonAllowed(providerJson)) {
                optionsList.add(getString(R.string.use_anonymously_button));
            }

            options.setAdapter(new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    optionsList.toArray(new String[optionsList.size()])
            ));
            options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String text = ((TextView) view).getText().toString();
                    Intent intent;
                    if (text.equals(getString(R.string.login_to_profile))) {
                        Log.d(TAG, "login selected");
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                    } else if (text.equals(getString(R.string.create_profile))) {
                        Log.d(TAG, "signup selected");
                        intent = new Intent(getApplicationContext(), SignupActivity.class);
                    } else {
                        Log.d(TAG, "use anonymously selected");
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            // TODO show error and return
        }
    }

    private boolean anonAllowed(JSONObject providerJson) {
        try {
            JSONObject serviceDescription = providerJson.getJSONObject(Provider.SERVICE);
            return serviceDescription.has(PROVIDER_ALLOW_ANONYMOUS) && serviceDescription.getBoolean(PROVIDER_ALLOW_ANONYMOUS);
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean registrationAllowed(JSONObject providerJson) {
        try {
            JSONObject serviceDescription = providerJson.getJSONObject(Provider.SERVICE);
            return serviceDescription.has(Provider.ALLOW_REGISTRATION) && serviceDescription.getBoolean(Provider.ALLOW_REGISTRATION);
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Provider.KEY).remove(PROVIDER_ALLOW_ANONYMOUS).remove(PROVIDER_KEY).apply();
        super.onBackPressed();
    }

}
