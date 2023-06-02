package com.firefighter.emergency.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.firefighter.emergency.dto.Coord;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class MapBoxService {
    private static final String ACCESS_TOKEN = "pk.eyJ1IjoibWFhYXRncnYiLCJhIjoiY2xpMXQwZzFhMDUwcDNzcWpuaG92ZGRtayJ9.blQzt4ZlhFsr4HLThIj6ow";

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
}
