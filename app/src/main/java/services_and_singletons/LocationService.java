package services_and_singletons;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.LocationSource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import chillboards.aurei.com.chillboards.Constants;

public class LocationService extends Service {

    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<>();

    //Current Location Variables
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    public ArrayList<String> nearbyPlaces = new ArrayList<>();



    public LocationService() {



    }

    @Override
    public IBinder onBind(Intent intent) {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.v("Permission Issue", "True");
                                    /*Determining the permission status within the service, broadcast to activity*/
                            Intent permissionRequestIntent = new Intent(Constants.LOCATION_STATUS)
                                    .putExtra(Constants.LOCATION_STATUS, Constants.LOCATION_PERMISSION_REJECTED);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(permissionRequestIntent);

                        } else {

                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            List<Address> addresses = null;

                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude(),
                                        //Just getting ONE result for now
                                        1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (IllegalArgumentException i) {
                                i.printStackTrace();
                            }

                            if (addresses == null || addresses.size() == 0) {
                                Log.e("Address Error", "no address found");
                                Intent addressIntent = new Intent(Constants.LOCATION_STATUS)
                                        .putExtra(Constants.LOCATION_STATUS, Constants.ADDRESS_FAILURE_RESULT);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(addressIntent);

                            } else {
                                        /*Longitude and Latitude Calls from Google Play Services API*/
                                android.location.Address address = addresses.get(0);
                                ArrayList<String> addressFragments = new ArrayList<>();
                                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                    addressFragments.add(address.getAddressLine(i));
                                    Log.v("Address", address.getAddressLine(i));
                                }
                                Log.i("Address", "Found");


//                                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                                    @Override
//                                    public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
//                                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                                            Log.i("PLACES", String.format("Place '%s' has likeihood: %g", placeLikelihood.getPlace().getName(),
//                                                    placeLikelihood.getLikelihood()));
//                                            Log.v("Place Name", String.valueOf(placeLikelihood.getPlace().getName()));
//                                            Log.v("ArrayList Size", String.valueOf(nearbyPlaces.size()));
//                                            nearbyPlaces.add(String.valueOf(placeLikelihood.getPlace().getName()));
//
//                                        }
//
//                                        likelyPlaces.release();
//                                        Intent placeNameIntent = new Intent(Constants.LOCATION_STATUS)
//                                                .putExtra(Constants.LOCATION_STATUS, Constants.PLACE_CONFIRMED)
//                                                .putExtra("Place Name", nearbyPlaces.get(0));
//
//                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(placeNameIntent);
//                                    }
//
//
//                                });

                                Intent addressIntent = new Intent(Constants.LOCATION_STATUS)
                                        .putExtra(Constants.LOCATION_STATUS, Constants.ADDRESS_SUCCESS_RESULT)
                                        .putExtra("Address", TextUtils.join(System.getProperty("line.separator"),addressFragments))
                                        .putExtra("Latitude", mLastLocation.getLatitude())
                                        .putExtra("Longitude", mLastLocation.getLongitude());

                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(addressIntent);


                            }
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }) .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();


        mGoogleApiClient.connect();

        /*Google Places API calls for Place Names*/

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {



            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                Log.v("IN PLACES", "RESULT");
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.v("IN PLACES", "FOR");
                    Log.i("Locale", String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }
                likelyPlaces.release();
            }
        });


        return mMessenger.getBinder();



    }

    class IncomingHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.v("Handler Message", String.valueOf(msg.what));
            switch(msg.what){
                case Constants.MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);

                    break;

                case Constants.MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;

                default:
                    super.handleMessage(msg);
            }


        }
    }
}
