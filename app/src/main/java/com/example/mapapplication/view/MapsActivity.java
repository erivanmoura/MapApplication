package com.example.mapapplication.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.mapapplication.R;
import com.example.mapapplication.model.Clinica;
import com.example.mapapplication.util.NetworkUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final private static String TAG = "MapsActivity";
    private GoogleMap mMap;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this.context;

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        int PLACE_PICKER_REQUEST = 1;
//        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);

        listClinicas();
    }

    public void listClinicas() {
        try{
            Log.d(TAG,"method listClinicas");
            URL url = NetworkUtil.buildUrl();
            MapsActivity.CallWebAsyncTask task = new MapsActivity.CallWebAsyncTask();
            task.execute(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class CallWebAsyncTask extends AsyncTask<URL, Void, List<Clinica>> {

        @Override
        protected List<Clinica> doInBackground(URL... urls) {
            URL url = urls[0];
            Log.d(TAG, "url utilizada: " + url.toString());
            Object json = "";
            try {
                json = NetworkUtil.getResponseFromHttpUrl(url);
                Log.d(TAG, "async task retorno: " + json);
            } catch (IOException e) {
                Log.d(TAG, "pegou erro.."+e);
                e.printStackTrace();
            }
            TypeToken<List<Clinica>> token = new TypeToken<List<Clinica>>() {
            };
            if (json == null) {
                throw new AssertionError("objeto não pode ser nulo");
            }
            return new Gson().fromJson(json.toString(), token.getType());
        }

        @Override
        protected void onPreExecute() {
//            showLoading();
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(List<Clinica> clinicas) {
            if (clinicas == null) {
                Toast.makeText(context,"Houve um erro. Verifique sua internet.",Toast.LENGTH_LONG);
            } else {
                mostrarClinicasNoMapa(clinicas);
            }
 //           stopLoading();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Do other setup activities here too, as described elsewhere in this tutorial.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (marker != null) {
                    String s = marker.getTitle();
                    Log.d(TAG,s);
                    //Toast.makeText(context,s,Toast.LENGTH_LONG);
                }
                return null;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void mostrarClinicasNoMapa(List<Clinica> clinicas) {
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.stylegta_json));

        Log.d(TAG, "mostrarClinicasNoMapa");
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.medical);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 64, 64, true);
        BitmapDescriptor bmpDescriptor =  BitmapDescriptorFactory.fromBitmap(bMapScaled);
        for (Clinica cli: clinicas) {
            LatLng mark = new LatLng(Double.parseDouble(cli.getLatitude()),Double.parseDouble(cli.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(mark).title(cli.getNome()).icon(bmpDescriptor).snippet(cli.getEndereco()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mark));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13),2000,null);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

}
