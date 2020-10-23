package appmoviles.com.reto1.comunication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import appmoviles.com.reto1.R;
import appmoviles.com.reto1.activity.MapsActivity;
import appmoviles.com.reto1.model.Hole;
import appmoviles.com.reto1.model.User;

public class UserWorker extends Thread {

    private MapsActivity mapsActivity;
    private boolean isAlive;
    private HTTPSWebUtilDomi httpsWebUtilDomi;
    private Gson gson;


    public UserWorker(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
        this.isAlive = true;
        this.httpsWebUtilDomi = new HTTPSWebUtilDomi();
        this.gson = new Gson();
    }


    @Override
    public void run() {

        while (isAlive){
            delay(3000);
            getUsers();
            updatePositionAndpermissions();


        }
    }

    public void getUsers(){
        String response = httpsWebUtilDomi.GETrequest(MapsActivity.BASEURL+"users.json");
        Type type = new TypeToken<HashMap<String, User>>(){}.getType();
        HashMap<String, User> users = gson.fromJson(response,type);

        mapsActivity.updateUsersMarkers(users);


    }

    public void updatePositionAndpermissions(){
        mapsActivity.runOnUiThread(
                ()->{
                    if (ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mapsActivity.getUserMarker()==null){
                            mapsActivity.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, mapsActivity);
                            Location latLng = mapsActivity.getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            mapsActivity.updatePosition(latLng);
                        }
                    }else {
                        Toast.makeText(mapsActivity,"Acepta los permisos de ubiación para poder usar la app",Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }


    public void delay(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        isAlive = false;
    }
}
