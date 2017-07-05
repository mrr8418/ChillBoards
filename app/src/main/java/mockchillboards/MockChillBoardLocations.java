package mockchillboards;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Matt on 7/2/2017.
 */

public class MockChillBoardLocations {

    private ChillBoard downtownHouston;
    private ChillBoard block334apartments;

    public MockChillBoardLocations(){

        this.downtownHouston = new ChillBoard(new LatLng(29.761037, -95.361707), "Downtown Houston");
        this.block334apartments = new ChillBoard(new LatLng(29.752505, -95.367756), "Block 334 Apartments");

    }


    public ChillBoard getDowntownHouston() {
        return downtownHouston;
    }

    public ChillBoard getBlock334apartments() {
        return block334apartments;
    }

}



