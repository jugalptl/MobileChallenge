package com.example.igroup.currencyconversion;

import android.Manifest;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
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

    HashMap<String,Double > hashMapRates = new HashMap<>();
    Button tryAgainButton;
    Spinner spn_currency;
    EditText edtxt_amount;
    GridView gridview;
    List<String> ITEM_LIST;
    ArrayAdapter<String> arrayAdapter;
    double convertedRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        spn_currency =(Spinner)findViewById(R.id.spn_currency);
        edtxt_amount =(EditText)findViewById(R.id.edtxt_amount) ;
        gridview =(GridView)findViewById(R.id.gridView) ;
        ITEM_LIST = new ArrayList<>();



        /*Check for Internet permission*/

        if(!isNetworkAvailable()) {
            Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
        } else
        {
                    fetchCurrencyRates();

        }
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,ITEM_LIST);
        gridview.setAdapter(arrayAdapter);
        spn_currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
     @Override
     public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

         if(i>0){
         try {
             convertedRate= (Double.parseDouble(edtxt_amount.getText().toString()) * hashMapRates.get(spn_currency.getSelectedItem()));
         }catch (NumberFormatException e)
         {
             Log.e("Exception",e.toString());
         }

        ITEM_LIST.add(ITEM_LIST.size(),String.valueOf(convertedRate)+spn_currency.getSelectedItem());
        arrayAdapter.notifyDataSetChanged();

     }
     }

     @Override
     public void onNothingSelected(AdapterView<?> adapterView) {

        spn_currency.setSelection(0);
     }
 });


    }

    @Override
    protected void onResume() {
        Toast.makeText(MainActivity.this,"Hi",Toast.LENGTH_SHORT).show();
        super.onResume();


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
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://api.fixer.io/latest", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rates = response.getJSONObject("rates");
                   Iterator<?> keys = rates.keys();
                   while(keys.hasNext())
                   {
                       String currencyName = (String)keys.next();
                       Double currencyRate =  rates.getDouble(currencyName);
                       hashMapRates.put(currencyName,currencyRate);
                   }

                   setSpnner();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY RESPONSE ERRROR",error.toString());

                handleVolleyError(error);


            }
        });

        MySingleton.getmInstance(MainActivity.this).addToRequestque(request);
    }

    private void handleVolleyError(VolleyError error) {
        VolleyErrorHandler errorHandler = new VolleyErrorHandler(MainActivity.this);
        String message = errorHandler.handleVolleyError(error);
        /** View to display Error */
        LinearLayout errorLayout = (LinearLayout) findViewById(R.id.errorContainer);
        TextView messageText2 = (TextView) findViewById(R.id.message2);

        tryAgainButton = (Button) findViewById(R.id.tryAgainButton);
        /** make it visible */
        errorLayout.setVisibility(View.VISIBLE);
        messageText2.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.VISIBLE);
        edtxt_amount.setVisibility(View.GONE);
        spn_currency.setVisibility(View.GONE);
        gridview.setVisibility(View.GONE);

        messageText2.setText(message);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Load Activity Again */
                finish();
                startActivity(getIntent());
            }
        });
    }

    private void setSpnner() {
        List<String> array_spinner = new ArrayList<>();
        array_spinner.add("Select Currency");
        array_spinner.addAll(hashMapRates.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,array_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_currency.setAdapter(adapter);
    }

}
