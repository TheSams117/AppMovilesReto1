package appmoviles.com.reto1.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import appmoviles.com.reto1.R;
import appmoviles.com.reto1.comunication.HTTPSWebUtilDomi;
import appmoviles.com.reto1.comunication.HoleWorker;
import appmoviles.com.reto1.comunication.UserWorker;
import appmoviles.com.reto1.model.Hole;
import appmoviles.com.reto1.model.User;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, DialogAddHole.Listener, LocationListener {

    final public static int PERMISSIONS_CALLBACK = 11;
    public static final String BASEURL = "https://appmoviles-reto1.firebaseio.com/";

    private LocationManager locationManager;
    private HTTPSWebUtilDomi http;
    private Gson gson;

    private User user;
    private Marker userMarker;

    private Marker markerHole;

    private GoogleMap mMap;
    private TextView txtHoleDistance;
    private Button butAddHole;
    private Button butConfirmHole;
    private DialogAddHole dialogAddHole;


    private ArrayList<Marker> users;
    private ArrayList<Marker> holes;

    private UserWorker userWorker;
    private HoleWorker holeWorker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        user = new User(UUID.randomUUID().toString());
        http = new HTTPSWebUtilDomi();
        gson = new Gson();

        users = new ArrayList<>();
        holes = new ArrayList<>();


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        txtHoleDistance = findViewById(R.id.txtHoleDistance);
        butAddHole = findViewById(R.id.butAddHole);
        butConfirmHole = findViewById(R.id.butConfirmHole);
        butAddHole.setOnClickListener(this);
        butConfirmHole.setOnClickListener(this);

        butConfirmHole.setVisibility(View.INVISIBLE);
        txtHoleDistance.setVisibility(View.INVISIBLE);


        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_CALLBACK);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_CALLBACK);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        userWorker = new UserWorker(this);
        holeWorker = new HoleWorker(this);
        userWorker.start();
        holeWorker.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.butAddHole:
                if (userMarker != null) {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    dialogAddHole = DialogAddHole.newInstance();
                    dialogAddHole.setListener(this);
                    dialogAddHole.setLatlng(userMarker.getPosition().latitude + "," + userMarker.getPosition().longitude);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(userMarker.getPosition().latitude, userMarker.getPosition().longitude, 1);
                        Address address = addresses.get(0);
                        String text = "";
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            text += address.getAddressLine(i);
                        }
                        dialogAddHole.setAd(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialogAddHole.show(getSupportFragmentManager(), "AddHoleDialog");
                } else {
                    Toast.makeText(this, "Activa tu ubicación para agregar un hueco, si ya esta activada espera un momento...", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.butConfirmHole:
                Hole holeConfirm = (Hole) markerHole.getTag();
                if (holeConfirm != null) {
                    holeConfirm.setConfirmed(true);
                    markerHole.setTitle("Hueco confirmado");
                    markerHole.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.confirmedhole));

                    new Thread(
                            () -> {
                                String response = http.PUTrequest(BASEURL + "holes/" + holeConfirm.getId() + ".json", gson.toJson(holeConfirm));
                            }
                    ).start();

                    butConfirmHole.setVisibility(View.INVISIBLE);
                    markerHole = null;
                }

                break;
        }
    }

    @Override
    public void onOk() {
        Hole hole = new Hole(UUID.randomUUID().toString(), userMarker.getPosition().latitude, userMarker.getPosition().longitude, false);
        LatLng currentPos = new LatLng(hole.getLat(), hole.getLng());
        Marker holeMarker = mMap.addMarker(new MarkerOptions().position(currentPos).title("Hueco sin confirmar"));
        holeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.unconfirmedhole));
        holeMarker.setTag(hole);
        holes.add(holeMarker);
        new Thread(
                () -> {
                    String response = http.PUTrequest(BASEURL + "holes/" + hole.getId() + ".json", gson.toJson(hole));
                }
        ).start();

        dialogAddHole.dismiss();
    }

    @Override
    public void onCancelar() {
        dialogAddHole.dismiss();
    }


    @Override
    public void onLocationChanged(Location location) {
        updatePosition(location);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "GPS activado!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Activa tu GPS!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        userWorker.finish();
        holeWorker.finish();
        super.onDestroy();

    }

    @Override
    protected void onResume() {

        new Thread(
                () -> {
                    String response = http.PUTrequest(BASEURL + "users/" + user.getId() + ".json", gson.toJson(user));
                }
        ).start();

        super.onResume();
    }

    @Override
    protected void onStop() {
        new Thread(
                () -> {
                    String response = http.DELETErequest(BASEURL + "users/" + user.getId() + ".json");
                }
        ).start();
        super.onStop();
    }

    public void updatePosition(Location location) {
        LatLng currentPos = null;
        if(location!=null){
            currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            if (userMarker == null) {
                MarkerOptions marker = new MarkerOptions().position(currentPos).title("Yo");
                userMarker = mMap.addMarker(marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 20));
                userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.user));
            } else {
                userMarker.setPosition(currentPos);
            }
            user.setLat(currentPos.latitude);
            user.setLng(currentPos.longitude);

            new Thread(
                    () -> {
                        String response = http.PUTrequest(BASEURL + "users/" + user.getId() + ".json", gson.toJson(user));
                    }
            ).start();

        }

    }

    public void updateUsersMarkers(HashMap<String, User> us) {
        runOnUiThread(() -> {
            for (int i = 0; i < users.size(); i++) {
                Marker m = users.get(i);
                m.remove();
            }
            users.clear();
            if (us != null) {
                us.forEach((key, value) -> {

                    LatLng currentPosition = new LatLng(value.getLat(), value.getLng());
                    if (!value.getId().equals(user.getId())) {
                        Marker m = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Otro Usuario"));
                        m.setTag(value);
                        m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.users));
                        users.add(m);
                    }
                });
            }
        });

    }

    public void updateHolesMarkers(HashMap<String, Hole> ho) {
        runOnUiThread(
                () -> {

                    for (int i = 0; i < holes.size(); i++) {
                        Marker m = holes.get(i);
                        m.remove();
                    }
                    holes.clear();

                    if (ho != null) {
                        ho.forEach((key, value) -> {
                            LatLng currentPosition = new LatLng(value.getLat(), value.getLng());
                            if (value.isConfirmed()) {
                                Marker m = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Hueco confirmado"));
                                m.setTag(value);
                                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.confirmedhole));
                                holes.add(m);
                            } else {
                                Marker m = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Hueco sin confirmar"));
                                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.unconfirmedhole));
                                m.setTag(value);
                                holes.add(m);
                            }

                        });
                    }
                }
        );
    }

    public void setHoleToConfirm() {
        runOnUiThread(() -> {
            if (userMarker != null) {
                double minDistance = 3;
                double moreNearbyHole = 100;
                Marker currentHole = null;
                for (int i = 0; i < holes.size(); i++) {
                    currentHole = holes.get(i);
                    double meters = SphericalUtil.computeDistanceBetween(currentHole.getPosition(), userMarker.getPosition());
                    if (meters < minDistance && meters < moreNearbyHole) {
                        moreNearbyHole = meters;
                        markerHole = currentHole;
                    }
                }

                if (moreNearbyHole <= minDistance && markerHole != null && !((Hole) markerHole.getTag()).isConfirmed()) {
                    butConfirmHole.setVisibility(View.VISIBLE);
                } else {
                    markerHole = null;
                    butConfirmHole.setVisibility(View.INVISIBLE);
                }
            }

        });
    }

    public void setNearbyHole() {
        runOnUiThread(() -> {
            if (userMarker != null) {
                double minDistance = 200;
                double moreNearbyHole = 300;
                Marker currentHole = null;
                for (int i = 0; i < holes.size(); i++) {
                    currentHole = holes.get(i);
                    double meters = SphericalUtil.computeDistanceBetween(currentHole.getPosition(), userMarker.getPosition());
                    if (meters < minDistance && meters < moreNearbyHole) {
                        moreNearbyHole = meters;
                    }
                }

                if (moreNearbyHole <= minDistance) {
                    txtHoleDistance.setVisibility(View.VISIBLE);
                    txtHoleDistance.setText("Hueco a " + String.format("%.2f", moreNearbyHole) + " metros");
                } else {
                    txtHoleDistance.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public Marker getUserMarker() {
        return userMarker;
    }

    public void setUserMarker(Marker userMarker) {
        this.userMarker = userMarker;
    }
}