package com.example.streetline;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayList markerPoints= new ArrayList();

    private Button button;

    private RelativeLayout root;

    private boolean routeBuildingMode = false;

    private List<Route> routes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        root = findViewById(R.id.root_element);

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMode();
            }
        });

        //Database db = new Database();
    }

    private void changeMode(){
        if(routeBuildingMode){
            Snackbar.make(root, "Выход из режима оценки дороги", Snackbar.LENGTH_SHORT).show();
            if (markerPoints.size() > 1) {
                markerPoints.clear();
                mMap.clear();
            }
            routeBuildingMode = false;
        } else
        {
            Snackbar.make(root, "Отметьте две точки", Snackbar.LENGTH_SHORT).show();
            routeBuildingMode = true;
        }
    }

    private void showWindow(List<Location> locations){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Оцените качество дороги");
        dialog.setMessage("Оценка");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final MaterialEditText ratingOfRoute = register_window.findViewById(R.id.ratingOfRouteField);
        final MaterialEditText typeOfRoute = register_window.findViewById(R.id.typeOfRouteField);
        final MaterialEditText commentOfRoute = register_window.findViewById(R.id.commemtField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {

            String rating;

            String typeOfRoad;

            String comment;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(ratingOfRoute.getText().toString())){
                    Snackbar.make(root, "Ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                } else{
                    rating = ratingOfRoute.getText().toString();
                }

                if(TextUtils.isEmpty(typeOfRoute.getText().toString())){
                    Snackbar.make(root, "Ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                } else{
                    typeOfRoad = typeOfRoute.getText().toString();
                }

                if(TextUtils.isEmpty(commentOfRoute.getText().toString())){
                    Snackbar.make(root, "Ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                } else{
                    comment = commentOfRoute.getText().toString();
                }

                routes.add(new Route(locations,rating, typeOfRoad, comment));

                Snackbar.make(root, "Маршрут отправлен", Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(routeBuildingMode) {

                    if (markerPoints.size() > 1) {
                        markerPoints.clear();
                        mMap.clear();
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(latLng);

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(latLng);

                    if (markerPoints.size() == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    } else if (markerPoints.size() == 2) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }

                    // Add new marker to the Google Map Android API V2
                    mMap.addMarker(options);

                    // Checks, whether start and end locations are captured
                    if (markerPoints.size() >= 2) {
                        LatLng origin = (LatLng) markerPoints.get(0);
                        LatLng dest = (LatLng) markerPoints.get(1);

                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);

                        DownloadTask downloadTask = new DownloadTask();

                        // Start downloading json data from Google Directions API

                        downloadTask.execute(url);
                    }
                }
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            String sqlCommand = "INSERT INTO streetline.marker_table(\n" +
                    "\tmarker_id, marker_latitude, marker_longitude, marker_type)\n" +
                    "\tVALUES (4, 3, 3, 'a')";

            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://10.0.2.2:5432/postgres", "postgres", "1234")){
                Statement statement = connection.createStatement();
                statement.executeUpdate(sqlCommand);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<Location>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<Location> doInBackground(String... jsonData) {

            JSONObject jObject;

            List<Location> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<Location> locations) {

            List<LatLng>points = new ArrayList();

            PolylineOptions lineOptions = new PolylineOptions();

            for (int i = 0; i < locations.size(); i++) {

                double lat = locations.get(i).latitude;
                double lng = locations.get(i).longitude;
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.RED);
            lineOptions.geodesic(true);

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

            showWindow(locations);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = origin.longitude + "," + origin.latitude;

        // Destination of route
        String str_dest = dest.longitude + "," + dest.latitude;

        // Building the url to the web service
        String url = "https://router.project-osrm.org/route/v1/driving/" + str_origin + ";" +
                str_dest + "?alternatives=true&geometries=polyline";

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}