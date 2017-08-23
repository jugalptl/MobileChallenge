package com.example.igroup.currencyconversion;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;



public class VolleyErrorHandler {

    public Context context;

    public VolleyErrorHandler(Context context) {
        this.context = context;
    }

    public String handleVolleyError(VolleyError error) {


        String errorMessage = "";
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            errorMessage = context.getResources().getString(R.string.error_timeout);
        } else if (error instanceof NetworkError) {
            errorMessage = context.getResources().getString(R.string.network_error);
        } else if (error instanceof ParseError) {
            errorMessage = context.getResources().getString(R.string.parse_error);
        } else if (error.networkResponse.statusCode == 404) {
            errorMessage = context.getResources().getString(R.string.error_404);
        } else {
            errorMessage = context.getResources().getString(R.string.error_universal);
        }

        return errorMessage;
    }
}
