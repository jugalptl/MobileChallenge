package com.example.igroup.currencyconversion;

import android.Manifest;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String CURRENCY_RATE_PREFERENCE = "currency_rate_preference";
    static String CURRENCY_RATE_URI = "http://api.fixer.io/latest";
    //HashMap<String,Double > hashMapRates = new HashMap<>();
    Spinner spn_currency;
    EditText edtxt_amount;
    GridView gridview;
    List<String> ITEM_LIST;
    ArrayAdapter<String> arrayAdapter;
    double convertedRate;

    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        spn_currency =(Spinner)findViewById(R.id.spn_currency);
        edtxt_amount =(EditText)findViewById(R.id.edtxt_amount) ;
        gridview =(GridView)findViewById(R.id.gridView) ;
        ITEM_LIST = new ArrayList<>();

        /*Check for Internet Connection*/

        if(!isNetworkAvailable()) {
            Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
            setSpnner();
        } else
        {
                    fetchCurrencyRates();

        }
     /*GridView WIth Converted values*/

        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,ITEM_LIST);
        gridview.setAdapter(arrayAdapter);


     /*Calculate the amount(Euro) into selected currency rate equivalent*/

        spn_currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
     public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

         if(i>0){
         try {
            // convertedRate= (Double.parseDouble(edtxt_amount.getText().toString()) * hashMapRates.get(spn_currency.getSelectedItem()));
            convertedRate = Math.round((Double.parseDouble(edtxt_amount.getText().toString()) * (Double.parseDouble(preferences.getString(spn_currency.getSelectedItem().toString(),""))))*10000.0)/10000.0;
         }catch (NumberFormatException e)
         {
             Log.e("Exception",e.toString());
         }

        ITEM_LIST.add(ITEM_LIST.size(),edtxt_amount.getText().toString()+"EUR = "+String.valueOf(convertedRate)+spn_currency.getSelectedItem());
        arrayAdapter.notifyDataSetChanged();

     }
     }

     @Override
     public void onNothingSelected(AdapterView<?> adapterView) {

     }
 });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isNetworkAvailable())
        {
            Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
            setSpnner();
        } else
        {
            fetchCurrencyRates();

        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return ITEM_LIST;

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchCurrencyRates() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,CURRENCY_RATE_URI, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                   //hashMapRates.clear();
                    SharedPreferences.Editor editor = getSharedPreferences(CURRENCY_RATE_PREFERENCE,MODE_PRIVATE).edit();
                    JSONObject rates = response.getJSONObject("rates");
                   Iterator<?> keys = rates.keys();
                   while(keys.hasNext())
                   {
                       String currencyName = (String)keys.next();
                       Double currencyRate =  rates.getDouble(currencyName);
                      // hashMapRates.put(currencyName,currencyRate);
                       editor.putString(currencyName,currencyRate.toString());
                   }
                    editor.commit();
                   setSpnner();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY RESPONSE ERROR",error.toString());

                handleVolleyError(error);


            }
        });

        MySingleton.getmInstance(MainActivity.this).addToRequestque(request);
    }

    private void handleVolleyError(VolleyError error) {
        VolleyErrorHandler errorHandler = new VolleyErrorHandler(MainActivity.this);
        String message = errorHandler.handleVolleyError(error);
        /** View to display Error */
        RelativeLayout errorLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
        Snackbar.make(errorLayout,message,Snackbar.LENGTH_LONG).show();

    }

    /*Set Currency type into drop down spinner*/
    private void setSpnner() {
        List<String> array_spinner = new ArrayList<>();
        array_spinner.add("Select Currency");
        preferences = getSharedPreferences(CURRENCY_RATE_PREFERENCE,MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        if (!allEntries.isEmpty()) {
            array_spinner.addAll(allEntries.keySet());
        }
        //array_spinner.addAll(hashMapRates.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,array_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_currency.setAdapter(adapter);
    }

}
