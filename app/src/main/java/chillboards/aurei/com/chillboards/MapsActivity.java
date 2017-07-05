package chillboards.aurei.com.chillboards;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import mockchillboards.MockChillBoardLocations;
import services_and_singletons.LocationService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MockChillBoardLocations mockChillBoardLocations;
    boolean mBound = false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger mService;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
        if(mBound){
            Toast.makeText(getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*Rudimentary Database of ChillBoards*/
        mockChillBoardLocations = new MockChillBoardLocations();


        /*Registering Broadcast Receiver for my Location Service*/
        IntentFilter locationPermissionIntentFilter = new IntentFilter(Constants.LOCATION_STATUS);
        LocationServiceResponseReceiver locationServiceResponseReceiver = new LocationServiceResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationServiceResponseReceiver, locationPermissionIntentFilter);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(mockChillBoardLocations.getDowntownHouston().getMarkerOptions());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mockChillBoardLocations.getDowntownHouston().getLatlong()));
        mMap.addMarker(mockChillBoardLocations.getBlock334apartments().getMarkerOptions());


    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = new Messenger(service);
            try{
                Message msg = Message.obtain(null, Constants.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }catch (RemoteException e){}
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }


    };

    void doUnBindService(){
        if(mBound){
            if(mService != null){
                try{
                    Message msg = Message.obtain(null, Constants.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }catch(RemoteException e){}

                unbindService(mConnection);
                mBound = false;

            }

        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }
    }

    private class LocationServiceResponseReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getIntExtra(Constants.LOCATION_STATUS, 0)){
                case Constants.LOCATION_PERMISSION_REJECTED:
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(getApplicationContext(), "ChillBoard Requires your location to identify boards and users close to you", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_GRANTED);
                    } else {
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_GRANTED);
                    }

                    break;

                case Constants.ADDRESS_FAILURE_RESULT:
                    Toast.makeText(getApplicationContext(), "NO ADDRESS FOUND", Toast.LENGTH_LONG).show();

                    break;

                case Constants.ADDRESS_SUCCESS_RESULT:
                    String Address = intent.getStringExtra("Address");
                    Toast.makeText(getApplicationContext(), Address, Toast.LENGTH_LONG).show();


                    break;

                case Constants.PLACE_CONFIRMED:

                    break;


            }

        }
    }
}
