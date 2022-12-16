package com.example.streetline;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
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

import org.json.JSONArray;
import org.json.JSONException;
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

    private boolean markerMode = false;

    private boolean routeMode = false;

    private List<Route> routes = new ArrayList<>();

    private String currentId;

    private Polyline currentPolyline;

    private List<Polyline> allPolyline = new ArrayList<>();

    private List<Marker> currentMarkers = new ArrayList<>();

    static final String SAVE_LOGIN = "save_login";

    static final String SAVE_PASSWORD = "save_password";

    static final String SAVE_USERID = "save_userid";

    private String login;

    private String password;

    private String userid;

    private SharedPreferences prefs = null;

    private Object lock = new Object();

    private DBHelper dbHelper;

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

        prefs = getSharedPreferences("com.example.streetline", MODE_PRIVATE);

        //prefs.edit().remove("firstrun").commit();

        System.out.println(getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_LOGIN, ""));
        System.out.println(getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_PASSWORD, ""));
        System.out.println(getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_USERID, ""));
        System.out.println(prefs.getBoolean("firstrun", false));

        if (!prefs.getBoolean("firstrun", false)) {
            loadSettings();
        }
        dbHelper = new DBHelper(this);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //dbHelper.dropALl(database);

        //dbHelper.onCreate(database);

        //NULL В ЭТИХ СОХРАНИТЬ НАСТРОЙКИ
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            GetBDFromApiTask getBDFromApiTask = new GetBDFromApiTask();
            getBDFromApiTask.execute("get");
            showRegistrationForm();
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng ekb = new LatLng(56.8383, 60.6036);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ekb, 16));

        SelectTaskLocations selectTaskLocations = new SelectTaskLocations();

        selectTaskLocations.execute("AMOGUS");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(markerMode || routeMode) {

                    if (markerPoints.size() > 1) {
                        markerPoints.clear();
                        for (int i = 0; i < currentMarkers.size(); i++) {
                            currentMarkers.get(i).remove();
                        }
                        currentPolyline.remove();
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

    private class GetBDFromApiTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... str) {

            String data = "";

            InputStream iStream = null;
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://92.255.79.73:8080/api/new");

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");

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
                urlConnection.disconnect();
            }

            try {
                JSONArray jsonArray= new JSONArray(data);
                DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();

                List<Route> routes = directionsJSONParser.parseRouteJSON(jsonArray);

                List<Integer> integers = new ArrayList<>();

                for (int i = 0; i < routes.size(); i++) {
                    RouteDAO routeDAO = new RouteDAO(routes.get(i).getUserId(),dbHelper);

                    if(!integers.contains(Integer.parseInt(routes.get(i).getAreaId()))) {
                        routeDAO.insertLocations(routes.get(i).getLocations(), routes.get(i).getAreaId());
                    }

                    integers.add(Integer.parseInt(routes.get(i).getAreaId()));

                    routeDAO.insertRouteFromApi(routes.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
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
                StringBuffer stringBuffer = new StringBuffer(jsonData[0]);
                stringBuffer.deleteCharAt(stringBuffer.length()-2);
                stringBuffer.deleteCharAt(9);
                jsonData[0] = stringBuffer.toString();
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

            if(markerMode) {
                showFormToRateRoute(locations);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = dest.latitude + "," + dest.longitude;

        // Building the url to the web service
        String url = "";

        if(markerMode) {
            url = "http://92.255.79.73:8080/api/segment/" + str_origin + "," + str_dest;
        }
        if(routeMode){
            url = "http://92.255.79.73:8080/api/navigation/" + str_origin + "," + str_dest;
        }


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
        if(markerMode){
            Snackbar.make(root, "Выход из режима оценки дороги", Snackbar.LENGTH_SHORT).show();
            if (markerPoints.size() > 0) {
                markerPoints.clear();
                for (int i = 0; i < currentMarkers.size(); i++) {
                    currentMarkers.get(i).remove();
                }
                //mMap.clear();
            }
            markerMode = false;
        } else
        {
            Snackbar.make(root, "Отметьте две точки для оценки дороги", 4000).show();
            markerMode = true;
            routeMode = false;
            for (int i = 0; i < allPolyline.size(); i++) {
                allPolyline.get(i).setVisible(true);
                allPolyline.get(i).setClickable(true);
            }
            markerPoints.clear();
            for (int i = 0; i < currentMarkers.size(); i++) {
                currentMarkers.get(i).remove();
            }
            if(currentPolyline != null) {
                currentPolyline.remove();
            }
        }
    }

    private void changeRouteMode(){
        if(routeMode){
            Snackbar.make(root, "Выход из поиска пути", Snackbar.LENGTH_SHORT).show();
            for (int i = 0; i < allPolyline.size(); i++) {
                allPolyline.get(i).setVisible(true);
                allPolyline.get(i).setClickable(true);
            }
            routeMode = false;
            markerPoints.clear();
            for (int i = 0; i < currentMarkers.size(); i++) {
                currentMarkers.get(i).remove();
            }
            if(currentPolyline != null) {
                currentPolyline.remove();
            }
        } else
        {
            Snackbar.make(root, "Отметьте две точки для построения маршрута", 4000).show();
            for (int i = 0; i < allPolyline.size(); i++) {
                allPolyline.get(i).setVisible(false);
                allPolyline.get(i).setClickable(false);
            }
            routeMode = true;
            markerMode = false;
        }
    }

    private void showFormToRateRoute(List<Location> locations){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Оцените качество дороги")
                .setMessage("Оценка")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.rating_window, null);
        dialog.setView(register_window);

        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setText("Отправить");
        negativeButton.setText("Отменить");

        final RatingBar ratingOfRoute = register_window.findViewById(R.id.ratingBar);
        final MaterialEditText typeOfRoute = register_window.findViewById(R.id.typeOfRouteField);
        final MaterialEditText commentOfRoute = register_window.findViewById(R.id.commemtField);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                currentPolyline.remove();
                for (int j = 0; j < currentMarkers.size(); j++) {
                    currentMarkers.get(j).remove();
                }
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {

            int rating;

            String typeOfRoad;

            String comment;

            @Override
            public void onClick(View view) {
                if(ratingOfRoute.getRating()==0 ||
                        TextUtils.isEmpty(typeOfRoute.getText().toString()) ||
                        TextUtils.isEmpty(commentOfRoute.getText().toString())){
                    Snackbar.make(view, "Ошибка, заполните все данные", Snackbar.LENGTH_SHORT).show();
                } else{
                    rating = (int)ratingOfRoute.getRating();
                    typeOfRoad = typeOfRoute.getText().toString();
                    comment = commentOfRoute.getText().toString();

                    Route route = new Route(locations,rating, typeOfRoad, comment);

                    routes.add(route);

                    InsertTaskRoute insertTaskRoute = new InsertTaskRoute();

                    InsertTaskLocations insertTaskLocations = new InsertTaskLocations();

                    currentId = route.getAreaId();

                    insertTaskLocations.execute(locations);

                    insertTaskRoute.execute(route);

                    Snackbar.make(root, "Маршрут отправлен", Snackbar.LENGTH_SHORT).show();

                    markerPoints.clear();
                    for (int j = 0; j < currentMarkers.size(); j++) {
                        currentMarkers.get(j).remove();
                    }
                    if(currentPolyline != null) {
                        currentPolyline.remove();
                    }

                    SelectTaskLocations selectTaskLocations = new SelectTaskLocations();

                    selectTaskLocations.execute("AMOGUS");

                    dialog.dismiss();
                }

            }
        });
    }

    private class InsertTaskRoute extends AsyncTask<Route, Void, Void> {

        @Override
        protected Void doInBackground(Route... routes) {

            RouteDAO routeDAO = new RouteDAO(userid,dbHelper);

            routeDAO.insertRoute(routes[0]);

            return null;
        }
    }

    private class InsertTaskLocations extends AsyncTask<List<Location>, Void, Void> {

        @Override
        protected Void doInBackground(List<Location>... locations) {

            RouteDAO routeDAO = new RouteDAO(userid,dbHelper);

            routeDAO.insertLocations(locations[0], currentId);

            return null;
        }
    }

    private class SelectTaskLocations extends AsyncTask<String, List<Route>, List<Route>> {

        @Override
        protected List<Route> doInBackground(String... str) {

            RouteDAO routeDAO = new RouteDAO(userid,dbHelper);

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

            RouteDAO routeDAO = new RouteDAO(userid, dbHelper);

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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Информация о качестве дороги")
                .setMessage("Оценка")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View info = inflater.inflate(R.layout.info, null);
        dialog.setView(info);

        dialog.show();

        final TextView ratingOfRoute = info.findViewById(R.id.rating);
        final TextView login = info.findViewById(R.id.login);
        final TextView typeOfRoute = info.findViewById(R.id.type_of_road);
        final TextView commentOfRoute = info.findViewById(R.id.comment);

        login.setText("Пользователь: " + route.getLogin());
        ratingOfRoute.setText("Оценка: " +(int)route.getAvgScore());
        typeOfRoute.setText("Тип дороги: " + route.getTypeOfRoad());
        commentOfRoute.setText("Комментарий: " +route.getComment());

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setText("Добавить оценку дороги");
        negativeButton.setText("Выход");

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormToRateOtherRoute(route.getAreaId());
                dialog.dismiss();
            }
        });

        SelectTaskInfo selectTaskInfo = new SelectTaskInfo();
        selectTaskInfo.execute(route);

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<RouteInfoUtility> infoList = selectTaskInfo.infoListTask;
        RecyclerView recyclerView = info.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(getApplicationContext(),infoList));
    }

    private class SelectTaskInfo extends AsyncTask<Route, Void, Void> {

        public List<RouteInfoUtility> infoListTask;

        @Override
        protected Void doInBackground(Route... routes) {

            RouteDAO routeDAO = new RouteDAO(dbHelper);

            infoListTask = routeDAO.selectInfoOfRoute(routes[0]);

            synchronized (lock){
                lock.notify();
            }

            return null;
        }
    }

    private void showFormToRateOtherRoute(String areaId){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Оцените качество дороги")
                .setMessage("Оценка")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View rating_window = inflater.inflate(R.layout.rating_window, null);
        dialog.setView(rating_window);
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setText("Отправить");
        negativeButton.setText("Отменить");

        final RatingBar ratingOfRoute = rating_window.findViewById(R.id.ratingBar);
        final MaterialEditText typeOfRoute = rating_window.findViewById(R.id.typeOfRouteField);
        final MaterialEditText commentOfRoute = rating_window.findViewById(R.id.commemtField);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {

            int rating;

            String typeOfRoad;

            String comment;

            @Override
            public void onClick(View view) {
                if(ratingOfRoute.getRating()==0 ||
                        TextUtils.isEmpty(typeOfRoute.getText().toString()) ||
                        TextUtils.isEmpty(commentOfRoute.getText().toString())){
                    Snackbar.make(view, "Ошибка, заполните все данные", Snackbar.LENGTH_SHORT).show();
                } else{
                    rating = (int)ratingOfRoute.getRating();
                    typeOfRoad = typeOfRoute.getText().toString();
                    comment = commentOfRoute.getText().toString();

                    Route route = new Route(rating, typeOfRoad, comment, areaId);

                    InsertTaskRoute insertTaskRoute = new InsertTaskRoute();

                    insertTaskRoute.execute(route);

                    Snackbar.make(root, "Оценка маршрута отправлена", Snackbar.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            }
        });
    }

    private void showRegistrationForm(){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Регистрация")
                .setMessage("Введите логин и пароль")
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        final MaterialEditText loginField = register_window.findViewById(R.id.login);
        final MaterialEditText passwordField = register_window.findViewById(R.id.password);

        positiveButton.setText("Зарегистрироваться");

        positiveButton.setOnClickListener(new View.OnClickListener() {

            String login;

            String password;

            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(loginField.getText().toString()) || TextUtils.isEmpty(passwordField.getText().toString())){
                    Snackbar.make(view, "Ошибка, введите логин и пароль", Snackbar.LENGTH_SHORT).show();
                } else {

                    login = loginField.getText().toString();

                    password = passwordField.getText().toString();

                    User user = new User(login, password);

                    InsertTaskUser insertTaskUser = new InsertTaskUser();

                    insertTaskUser.execute(user);

                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (insertTaskUser.error != null) {
                        Snackbar.make(view, "Пользователь с таким логином уже есть", Snackbar.LENGTH_SHORT).show();
                    } else {

                        Snackbar.make(root, "Регистрация прошла успешно", Snackbar.LENGTH_SHORT).show();

                        saveSettings(user);

                        prefs.edit().putBoolean("firstrun", false).commit();

                        dialog.dismiss();
                    }
                }

            }
        });

    }

    private class InsertTaskUser extends AsyncTask<User, Void, Void> {

        public AlreadyExistLoginException error;

        @Override
        protected Void doInBackground(User... users) {

            UserDAO userDAO = new UserDAO(dbHelper);

            try {
                userDAO.signUp(users[0]);
            } catch (AlreadyExistLoginException e) {
                error = e;
            }

            synchronized (lock){
                lock.notify();
            }

            return null;
        }
    }

    public void saveSettings(User user) {
        SharedPreferences.Editor ed = getSharedPreferences("setting",MODE_PRIVATE).edit();
        ed.putString(SAVE_LOGIN, user.getLogin());
        ed.putString(SAVE_PASSWORD, user.getPassword());
        ed.putString(SAVE_USERID, user.getUserid());
        login = user.getLogin();
        password = user.getPassword();
        userid = user.getUserid();

        ed.commit();
    }

    public void loadSettings() {
        login = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_LOGIN, "");
        password = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_PASSWORD, "");
        userid = getSharedPreferences("setting",MODE_PRIVATE).getString(SAVE_USERID, "");
    }
}