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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayList markerPoints= new ArrayList();

    private Button buttonChangeMarkerMode;

    private Button buttonChangeRouteMode;

    private RelativeLayout root;

    private boolean routeMode = false;

    private boolean markerMode = false;

    private List<Route> routes = new ArrayList<>();

    private String currentId;

    private Polyline currentPolyline;

    private List<Polyline> allPolyline = new ArrayList<>();

    private List<Marker> currentMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        root = findViewById(R.id.root_element);

        buttonChangeMarkerMode = findViewById(R.id.button);

        buttonChangeMarkerMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMarkerMode();
            }
        });

        buttonChangeRouteMode = findViewById(R.id.buttonRouteMode);

        buttonChangeRouteMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeRouteMode();
            }
        });
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
        //LatLng sydney = new LatLng(-34, 151);
        LatLng ekb = new LatLng(56.8519, 60.6122);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ekb, 16));

        SelectTaskLocations selectTaskLocations = new SelectTaskLocations();

        selectTaskLocations.execute("AMOGUS");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(routeMode) {

                    if (markerPoints.size() > 1) {
                        markerPoints.clear();
                        for (int i = 0; i < currentMarkers.size(); i++) {
                            currentMarkers.get(i).remove();
                        }
                        //mMap.clear();
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
                    Marker marker = mMap.addMarker(options);
                    currentMarkers.add(marker);

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
            currentPolyline = mMap.addPolyline(lineOptions);

            showFormToRateRoute(locations);
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

    private void changeMarkerMode(){
        if(routeMode){
            Snackbar.make(root, "Выход из режима оценки дороги", Snackbar.LENGTH_SHORT).show();
            if (markerPoints.size() > 0) {
                markerPoints.clear();
                for (int i = 0; i < currentMarkers.size(); i++) {
                    currentMarkers.get(i).remove();
                }
                //mMap.clear();
            }
            routeMode = false;
        } else
        {
            Snackbar.make(root, "Отметьте две точки", Snackbar.LENGTH_SHORT).show();
            routeMode = true;
        }
    }

    private void changeRouteMode(){
        if(markerMode){
            Snackbar.make(root, "Выход из поиска пути", Snackbar.LENGTH_SHORT).show();
            for (int i = 0; i < allPolyline.size(); i++) {
                allPolyline.get(i).setVisible(true);
                allPolyline.get(i).setClickable(true);
            }
            markerMode = false;
        } else
        {
            Snackbar.make(root, "Отметьте две точки", Snackbar.LENGTH_SHORT).show();
            for (int i = 0; i < allPolyline.size(); i++) {
                allPolyline.get(i).setVisible(false);
                allPolyline.get(i).setClickable(false);
            }
            markerMode = true;
        }
    }

    private void showFormToRateRoute(List<Location> locations){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Оцените качество дороги");
        dialog.setMessage("Оценка");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.rating_window, null);
        dialog.setView(register_window);

        final MaterialEditText ratingOfRoute = register_window.findViewById(R.id.ratingOfRouteField);
        final MaterialEditText typeOfRoute = register_window.findViewById(R.id.typeOfRouteField);
        final MaterialEditText commentOfRoute = register_window.findViewById(R.id.commemtField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                currentPolyline.remove();
                for (int j = 0; j < currentMarkers.size(); j++) {
                    currentMarkers.get(j).remove();
                }
            }
        });
        dialog.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {

            int rating;

            String typeOfRoad;

            String comment;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(ratingOfRoute.getText().toString())){
                    Snackbar.make(root, "Ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                } else{
                    rating = Integer.parseInt(ratingOfRoute.getText().toString());
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

                Route route = new Route(locations,rating, typeOfRoad, comment);

                routes.add(route);

                //polyline.setTag(route);

                InsertTaskRoute insertTaskRoute = new InsertTaskRoute();

                insertTaskRoute.execute(route);

                InsertTaskLocations insertTaskLocations = new InsertTaskLocations();

                currentId = route.getAreaId();

                insertTaskLocations.execute(locations);

                Snackbar.make(root, "Маршрут отправлен", Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private class InsertTaskRoute extends AsyncTask<Route, Void, Void> {

        @Override
        protected Void doInBackground(Route... routes) {

            RouteDAO routeDAO = new RouteDAO();

            routeDAO.insertRoute(routes[0]);

            return null;
        }
    }

    private class InsertTaskLocations extends AsyncTask<List<Location>, Void, Void> {

        @Override
        protected Void doInBackground(List<Location>... locations) {

            RouteDAO routeDAO = new RouteDAO();

            routeDAO.insertLocations(locations[0], currentId);

            return null;
        }
    }

    private class SelectTaskLocations extends AsyncTask<String, List<Route>, List<Route>> {

        @Override
        protected List<Route> doInBackground(String... str) {

            RouteDAO routeDAO = new RouteDAO();

            routeDAO.selectAllLocations();

            return routeDAO.selectAllLocations();
        }

        @Override
        protected void onPostExecute(List<Route> allRoutes) {

            for (int i = 0; i < allRoutes.size(); i++) {
                drawLines(allRoutes.get(i));
            }
        }
    }

    private void drawLines(Route route){

        List<LatLng>points = new ArrayList();

        PolylineOptions lineOptions = new PolylineOptions();

        for (int i = 0; i < route.getLocations().size(); i++) {

            //так оно и работает, где то меняеются местами долгота и широта
            double lat = route.getLocations().get(i).longitude;
            double lng = route.getLocations().get(i).latitude;
            LatLng position = new LatLng(lat, lng);

            points.add(position);
        }

        int rating = route.getRating();

        lineOptions.addAll(points);
        lineOptions.width(12);
        switch (rating){
            case (1):
                lineOptions.color(Color.parseColor("#FF0000"));
                break;
            case (2):
                lineOptions.color(Color.parseColor("#FF8C00"));
                break;
            case (3):
                lineOptions.color(Color.parseColor("#FFFF00"));
                break;
            case (4):
                lineOptions.color(Color.parseColor("#ADFF2F"));
                break;
            case (5):
                lineOptions.color(Color.parseColor("#32CD32"));
                break;
        }
        lineOptions.geodesic(true);

        // Drawing polyline in the Google Map for the i-th route
        Polyline polyline = mMap.addPolyline(lineOptions);
        polyline.setClickable(true);
        polyline.setTag(route);

        allPolyline.add(polyline);

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
        {
            @Override
            public void onPolylineClick(Polyline polyline)
            {
                Route route1 = (Route) polyline.getTag();
                showInfo(route1);
            }
        });

    }


    private class SelectTaskAvg extends AsyncTask<Route, Void, Double> {

        @Override
        protected Double doInBackground(Route... routes) {

            RouteDAO routeDAO = new RouteDAO();

            routeDAO.selectAvgScoreOfRoute(routes[0]);

            return routes[0].getAvgScore();
        }

    }

    private void showInfo(Route route){

        SelectTaskAvg selectTaskAvg = new SelectTaskAvg();

        try {
            selectTaskAvg.execute(route).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Информация о качестве дороги");
        dialog.setMessage("Оценка");

        LayoutInflater inflater = LayoutInflater.from(this);
        View info = inflater.inflate(R.layout.info, null);
        dialog.setView(info);

        final TextView ratingOfRoute = info.findViewById(R.id.rating);
        final TextView typeOfRoute = info.findViewById(R.id.type_of_road);
        final TextView commentOfRoute = info.findViewById(R.id.comment);

        ratingOfRoute.setText(Double.toString(route.getAvgScore()));
        typeOfRoute.setText(route.getTypeOfRoad());
        commentOfRoute.setText(route.getComment());

        dialog.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Добавить оценку дороги", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showFormToRateOtherRoute(route.getAreaId());
            }
        });

        dialog.show();
    }

    private void showFormToRateOtherRoute(String areaId){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Оцените качество дороги");
        dialog.setMessage("Оценка");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.rating_window, null);
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

            int rating;

            String typeOfRoad;

            String comment;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(ratingOfRoute.getText().toString())){
                    Snackbar.make(root, "Ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                } else{
                    rating = Integer.parseInt(ratingOfRoute.getText().toString());
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

                Route route = new Route(rating, typeOfRoad, comment, areaId);

                InsertTaskRoute insertTaskRoute = new InsertTaskRoute();

                insertTaskRoute.execute(route);

                Snackbar.make(root, "Маршрут отправлен", Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}