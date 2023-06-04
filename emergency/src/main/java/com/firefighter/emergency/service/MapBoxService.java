package com.firefighter.emergency.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.firefighter.emergency.dto.Coord;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class MapBoxService {
    private static final String ACCESS_TOKEN = "pk.eyJ1IjoibWF0Z3J2IiwiYSI6ImNsaWh0bHZqdjBlOGUzZHBpcml4bWIwbW8ifQ.Q82YD2XdwAO_fN5B3yApDg";

    private OkHttpClient client = new OkHttpClient();

    public double getDistance(Coord coord1, Coord coord2) {
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving/" + coord1.getLon() + "," + coord1.getLat() +
                ";" + coord2.getLon() + "," + coord2.getLat() + "?access_token=" + ACCESS_TOKEN;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            // Parse response body to get distance
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
            JsonArray routes = jsonObject.getAsJsonArray("routes");
            if (routes.size() > 0) {
                JsonObject route = routes.get(0).getAsJsonObject();
                double distance = route.get("distance").getAsDouble();
                return distance;
            } else {
                throw new RuntimeException("No route found between the two points");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Coord> getRoute(Coord coord1, Coord coord2) {
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving/" + coord1.getLon() + "," + coord1.getLat() +
                ";" + coord2.getLon() + "," + coord2.getLat() + "?access_token=" + ACCESS_TOKEN + "&geometries=geojson";
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            // Parse response body to get route
            JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
            JsonArray routes = jsonObject.getAsJsonArray("routes");
            if (routes.size() > 0) {
                JsonObject route = routes.get(0).getAsJsonObject();
                JsonObject geometry = route.getAsJsonObject("geometry");
                JsonArray coordinates = geometry.getAsJsonArray("coordinates");
                List<Coord> routeCoords = new ArrayList<>();
                for (JsonElement coordinate : coordinates) {
                    JsonArray coordArray = coordinate.getAsJsonArray();
                    Coord coord = new Coord();
                    coord.setLon(coordArray.get(0).getAsDouble());
                    coord.setLat(coordArray.get(1).getAsDouble());
                    routeCoords.add(coord);
                }
                return routeCoords;
            } else {
                throw new RuntimeException("No route found between the two points");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
