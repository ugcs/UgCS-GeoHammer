package com.ugcs.geohammer.map.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ugcs.geohammer.model.LatLon;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MacrostratGeologyService {

    private static final String API_URL = "https://macrostrat.org/api/v2/geologic_units/map?lat=%s&lng=%s";

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public List<GeologyUnit> identify(LatLon location) {
        try {
            String latitude = URLEncoder.encode(Double.toString(location.getLatDgr()), StandardCharsets.UTF_8);
            String longitude = URLEncoder.encode(Double.toString(location.getLonDgr()), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL.formatted(latitude, longitude)))
                    .timeout(Duration.ofSeconds(10)).header("User-Agent", "GeoHammer geology identify").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Macrostrat returned HTTP " + response.statusCode());
            }
            return parse(response.body());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to query Macrostrat geology", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Macrostrat geology query was interrupted", e);
        }
    }

    private static List<GeologyUnit> parse(String response) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonObject success = root.getAsJsonObject("success");
        if (success == null || !success.has("data")) {
            throw new IllegalArgumentException("Macrostrat returned no geology data");
        }
        JsonArray data = success.getAsJsonArray("data");
        List<GeologyUnit> units = new ArrayList<>();
        for (JsonElement element : data) {
            JsonObject unit = element.getAsJsonObject();
            units.add(new GeologyUnit(text(unit, "name"), text(unit, "best_int_name"), text(unit, "lith"),
                    number(unit, "t_age"), number(unit, "b_age"), text(unit, "descrip")));
        }
        return units;
    }

    private static String text(JsonObject object, String property) {
        JsonElement value = object.get(property);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static Double number(JsonObject object, String property) {
        JsonElement value = object.get(property);
        return value == null || value.isJsonNull() ? null : value.getAsDouble();
    }

    public record GeologyUnit(String name, String interval, String lithology, Double topAge, Double bottomAge,
                              String description) {
    }
}
