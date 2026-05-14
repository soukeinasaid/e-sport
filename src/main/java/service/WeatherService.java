package service;

import entity.Tournoi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class WeatherService {

    private static final String GEOCODE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getWeatherSummary(Tournoi tournoi) throws IOException, InterruptedException {
        if (tournoi == null || tournoi.getLieu() == null || tournoi.getLieu().isBlank()) {
            return "Météo indisponible : lieu du tournoi manquant.";
        }

        Location location = geocode(tournoi.getLieu());
        if (location == null) {
            return "Météo indisponible : lieu introuvable.";
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = tournoi.getDateDebut();
        LocalDate endDate = tournoi.getDateFin();

        if (!today.isAfter(endDate)) {
            long daysUntilStart = ChronoUnit.DAYS.between(today, startDate);
            if (daysUntilStart <= 16) {
                LocalDate targetDate = today.isBefore(startDate) ? startDate : today;
                String forecastSummary = getForecastSummary(location, targetDate);
                if (forecastSummary != null) {
                    return forecastSummary;
                }
            }
        }

        String currentSummary = getCurrentWeatherSummary(location);
        if (currentSummary != null) {
            return currentSummary;
        }

        return "Météo indisponible pour le moment.";
    }

    private String getForecastSummary(Location location, LocalDate targetDate) throws IOException, InterruptedException {
        String url = FORECAST_URL
                + "?latitude=" + location.latitude()
                + "&longitude=" + location.longitude()
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
                + "&timezone=auto"
                + "&start_date=" + targetDate
                + "&end_date=" + targetDate;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        JSONObject daily = json.optJSONObject("daily");

        if (daily == null) {
            return null;
        }

        JSONArray maxTemps = daily.optJSONArray("temperature_2m_max");
        JSONArray minTemps = daily.optJSONArray("temperature_2m_min");
        JSONArray codes = daily.optJSONArray("weather_code");

        if (maxTemps == null || minTemps == null || codes == null || maxTemps.isEmpty()) {
            return null;
        }

        double max = maxTemps.getDouble(0);
        double min = minTemps.getDouble(0);
        int weatherCode = codes.getInt(0);

        return String.format(
                "Prévision météo à %s pour le %s : %s, %.1f°C / %.1f°C",
                location.name(),
                targetDate,
                weatherCodeToFrench(weatherCode),
                min,
                max
        );
    }

    private String getCurrentWeatherSummary(Location location) throws IOException, InterruptedException {
        String url = FORECAST_URL
                + "?latitude=" + location.latitude()
                + "&longitude=" + location.longitude()
                + "&current=temperature_2m,weather_code"
                + "&timezone=auto";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        JSONObject current = json.optJSONObject("current");

        if (current == null) {
            return null;
        }

        double temperature = current.optDouble("temperature_2m", Double.NaN);
        int weatherCode = current.optInt("weather_code", Integer.MIN_VALUE);

        if (Double.isNaN(temperature) || weatherCode == Integer.MIN_VALUE) {
            return null;
        }

        return String.format(
                "Météo actuelle à %s : %s, %.1f°C",
                location.name(),
                weatherCodeToFrench(weatherCode),
                temperature
        );
    }

    private Location geocode(String place) throws IOException, InterruptedException {
        String url = GEOCODE_URL + "?name=" + URLEncoder.encode(place, StandardCharsets.UTF_8) + "&count=1&language=fr&format=json";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        JSONArray results = json.optJSONArray("results");

        if (results == null || results.isEmpty()) {
            return null;
        }

        JSONObject first = results.getJSONObject(0);
        return new Location(
                first.optString("name", place),
                first.getDouble("latitude"),
                first.getDouble("longitude")
        );
    }

    private String weatherCodeToFrench(int code) {
        return switch (code) {
            case 0 -> "ciel dégagé";
            case 1, 2, 3 -> "partiellement nuageux";
            case 45, 48 -> "brouillard";
            case 51, 53, 55 -> "bruine";
            case 56, 57 -> "bruine verglaçante";
            case 61, 63, 65 -> "pluie";
            case 66, 67 -> "pluie verglaçante";
            case 71, 73, 75, 77 -> "neige";
            case 80, 81, 82 -> "averses";
            case 85, 86 -> "averses de neige";
            case 95 -> "orage";
            case 96, 99 -> "orage avec grêle";
            default -> "conditions variables";
        };
    }

    private record Location(String name, double latitude, double longitude) {
    }
}
