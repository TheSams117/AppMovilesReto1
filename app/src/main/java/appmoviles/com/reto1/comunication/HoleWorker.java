package appmoviles.com.reto1.comunication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import appmoviles.com.reto1.activity.MapsActivity;
import appmoviles.com.reto1.model.Hole;
import appmoviles.com.reto1.model.User;

public class HoleWorker extends Thread {
    private MapsActivity mapsActivity;
    private boolean isAlive;
    private HTTPSWebUtilDomi httpsWebUtilDomi;
    private Gson gson;

    public HoleWorker(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
        this.isAlive = true;
        this.httpsWebUtilDomi = new HTTPSWebUtilDomi();
        this.gson = new Gson();
    }

    @Override
    public void run() {

        while (isAlive){
            delay(2000);
            getHoles();

        }
    }

    public void getHoles(){

        String response = httpsWebUtilDomi.GETrequest(MapsActivity.BASEURL+"holes.json");
        Type type = new TypeToken<HashMap<String, Hole>>(){}.getType();
        HashMap<String, Hole> holes = gson.fromJson(response,type);

        mapsActivity.updateHolesMarkers(holes);
        mapsActivity.setNearbyHole();
        mapsActivity.setHoleToConfirm();

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
