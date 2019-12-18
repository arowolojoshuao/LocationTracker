package com.ecosa.mapdirections.util;

import android.app.Activity;
import android.content.Context;

import com.ecosa.mapdirections.interfaces.ConstantInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class AppUtility implements ConstantInterface {

    private Context context;

    public AppUtility(Context context) {
        this.context = context;
    }



    // Check weather google play services is available or not.
    public boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();

        int result = googleAPI.isGooglePlayServicesAvailable(context);

        if (result != ConnectionResult.SUCCESS) {

            if (googleAPI.isUserResolvableError(result)) {

                googleAPI.getErrorDialog((Activity) context, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();

            }

            return false;
        }

        return true;

    }
}
