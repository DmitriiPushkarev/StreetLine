package com.example.streetline;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anupamchugh on 27/11/15.
 */

public class DirectionsJSONParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<Location> parse(JSONObject jObject){

        List<Location> locations = new ArrayList<>();

        try {
            JSONArray arr = jObject.getJSONArray("waypoints");

            String location = "";

            for (int i = 0; i < arr.length(); i++)
            {
                location = arr.getJSONObject(i).getString("location");

                int firstPoint = location.indexOf(',');

                double longitude = Double.parseDouble(location.substring(1,firstPoint));

                double latitude = Double.parseDouble(location.substring(firstPoint+1, location.length() - 1));

                System.out.println(longitude + " " + latitude);

                Location objLocation = new Location(longitude, latitude);

                locations.add(objLocation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }
}
