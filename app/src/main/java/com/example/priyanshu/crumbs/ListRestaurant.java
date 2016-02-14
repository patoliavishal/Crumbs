package com.example.priyanshu.crumbs;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ListRestaurant extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_restaurant);
    }

    private ArrayList<String[]> getLocationDataFromJson(String JsonStr)
            throws JSONException {
        Log.d("FRIENDS", JsonStr);
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "objects";
        final String OWM_GEOMETRY = "name";
        final String OWM_LOCATION = "street_address";
        HashMap hm = new HashMap();

        JSONObject locationJson = new JSONObject(JsonStr);
        JSONArray locationArray = locationJson.getJSONArray(OWM_RESULTS);
        ArrayList<String[]> result= new ArrayList<>();
        for(int i=0;i<locationArray.length();++i){
            JSONObject JObject= locationArray.getJSONObject(i);
            String[] result_temp = new String[2];
            result_temp[0] =JObject.getString(OWM_GEOMETRY);
            result_temp[1] =JObject.getString(OWM_LOCATION);
            result.add(result_temp);
        }
        return result;
    }
    public class FetchLocationTask extends AsyncTask<String, Void, ArrayList<String[]>> {

        @Override
        protected ArrayList<String[]> doInBackground(String... strings) {
            strings[0].replaceAll(" ", "+");
            strings[0].replaceAll(",", ",+");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonStr = null;

            try {
                final String BASE_ADDR = "https://api.locu.com/v1_0/venue/search/?";
                final String ADDRESS_PARAM = "country";
                final String ADDRESS_VALUE = "Singapore";
                final String KEY_PARAM = "api_key";
                final String KEY_VALUE = "f4f39bf9ed72132112cd25e9ddcdc4063fd553fd";
                Uri builtUri = Uri.parse(BASE_ADDR).buildUpon()
                        .appendQueryParameter(ADDRESS_PARAM, ADDRESS_VALUE)
                        .appendQueryParameter(KEY_PARAM, KEY_VALUE)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("FRIENDS", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("FRIENDS", "Error closing stream", e);
                    }
                }
            }
            try {
                return getLocationDataFromJson(JsonStr);
            } catch (JSONException e) {
                Log.e("FRIENDS", e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                ArrayList<String> names = new ArrayList<>() , address = new ArrayList<>();
                for(int i=0;i<result.size();++i){
                    names.add(result.get(i)[0]);
                    address.add(result.get(i)[1]);
                }

                String[] names_list = new String[names.size()];
                names_list = names.toArray(names_list);
                ArrayAdapter<String> rest_list_adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,names_list);
                ListView lw = (ListView) findViewById(R.id.rest_list_view);
                lw.setAdapter(rest_list_adapter);
                AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.friends_list_search);
                actv.setAdapter(rest_list_adapter);
                actv.setThreshold(1);

            }
        }
    }
}