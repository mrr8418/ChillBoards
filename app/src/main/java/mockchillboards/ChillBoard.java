package mockchillboards;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import chillboards.aurei.com.chillboards.R;

/**
 * Created by Matt on 7/2/2017.
 */

public class ChillBoard {

    private String locationName;
    private LatLng latlong;
    private Marker marker;
    private MarkerOptions markerOptions;

    public ChillBoard(LatLng latLng, String locationName){
        this.latlong = latLng;
        this.locationName = locationName;
//        this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.billboard));
//        this.marker.setPosition(this.latlong);
        this.markerOptions = new MarkerOptions();
        this.markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.billboardsmall));
        this.markerOptions.position(this.latlong);
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LatLng getLatlong() {
        return latlong;
    }

    public void setLatlong(LatLng latlong) {
        this.latlong = latlong;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
