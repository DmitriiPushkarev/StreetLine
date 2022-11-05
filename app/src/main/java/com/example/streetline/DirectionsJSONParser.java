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
            JSONArray arr1 = jObject.getJSONArray("routes");

            JSONArray arr = decodeGeometry(arr1.getJSONObject(0).getString("geometry"), false);

            decodeGeometry(arr1.getJSONObject(0).getString("geometry"), false);

            String location = "";

            for (int i = 0; i < arr.length(); i++)
            {
                location = arr.getString(i);

                System.out.println(arr.getString(i));

                int firstPoint = location.indexOf(',');

                double latitude = Double.parseDouble(location.substring(1,firstPoint));

                double longitude = Double.parseDouble(location.substring(firstPoint+1, location.length() - 1));

                Location objLocation = new Location(longitude, latitude);

                locations.add(objLocation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public List<Location> parseStringToLocations(String str){

        List<Location> locations = new ArrayList<>();

        try {

            JSONArray arr = new JSONArray(str);

            String location = "";

            for (int i = 0; i < arr.length(); i++)
            {
                location = arr.getString(i);

                int firstPoint = location.indexOf(',');

                double latitude = Double.parseDouble(location.substring(1,firstPoint));

                double longitude = Double.parseDouble(location.substring(firstPoint+1, location.length() - 1));

                Location objLocation = new Location(longitude, latitude);

                locations.add(objLocation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public static JSONArray decodeGeometry(String encodedGeometry, boolean inclElevation) {
        JSONArray geometry = new JSONArray();
        int len = encodedGeometry.length();
        int index = 0;
        int lat = 0;
        int lng = 0;
        int ele = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedGeometry.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedGeometry.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);


            if(inclElevation){
                result = 1;
                shift = 0;
                do {
                    b = encodedGeometry.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                ele += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            }

            JSONArray location = new JSONArray();
            try {
                location.put(lat / 1E5);
                location.put(lng / 1E5);
                if(inclElevation){
                    location.put((float) (ele / 100));
                }
                geometry.put(location);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return geometry;
    }
}
