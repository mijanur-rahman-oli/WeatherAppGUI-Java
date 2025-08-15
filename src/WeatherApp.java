import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherApp {
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);
        if (locationData != null && !locationData.isEmpty()) {
            JSONObject location = (JSONObject) locationData.get(0);
            double latitude = (Double) location.get("latitude");
            double longitude = (Double) location.get("longitude");
            String timezone = ZoneId.systemDefault().toString();


            String urlString = "https://api.open-meteo.com/v1/forecast?latitude="
                    + latitude + "&longitude=" + longitude
                    + "&current_weather=true"
                    + "&hourly=relativehumidity_2m"
                    + "&timezone=" + timezone;

            try {
                HttpURLConnection conn = fetchApiResponse(urlString);
                if (conn != null && conn.getResponseCode() == 200) {
                    StringBuilder resultJson = new StringBuilder();
                    Scanner scanner = new Scanner(conn.getInputStream());
                    while (scanner.hasNext()) {
                        resultJson.append(scanner.nextLine());
                    }
                    scanner.close();
                    conn.disconnect();

                    JSONParser parser = new JSONParser();
                    JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

                    JSONObject currentWeather = (JSONObject) resultJsonObj.get("current_weather");
                    if (currentWeather == null) {
                        return null;
                    } else {
                        JSONObject weatherData = new JSONObject();
                        weatherData.put("temperature", currentWeather.get("temperature"));
                        weatherData.put("windspeed", currentWeather.get("windspeed"));


                        String currentTime = (String) currentWeather.get("time");
                        JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
                        JSONArray humidityArray = (JSONArray) hourly.get("relativehumidity_2m");
                        JSONArray timeArray = (JSONArray) hourly.get("time");

                        long humidity = 60L;
                        for (int i = 0; i < timeArray.size(); i++) {
                            if (timeArray.get(i).equals(currentTime)) {
                                humidity = ((Number) humidityArray.get(i)).longValue();
                                break;
                            }
                        }
                        weatherData.put("humidity", humidity);

                        long weatherCode = ((Number) currentWeather.get("weathercode")).longValue();
                        weatherData.put("weather_condition", convertWeatherCode(weatherCode));
                        return weatherData;
                    }
                } else {
                    System.out.println("Error: Could not connect to API");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Location not found.");
            return null;
        }
    }

    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name="
                + locationName + "&count=1&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);
            if (conn != null && conn.getResponseCode() == 200) {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect();

                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(resultJson.toString());
                return (JSONArray) resultsJsonObj.get("results");
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertWeatherCode(long code) {
        if (code == 0L) {
            return "Clear";
        } else if (code <= 3L) {
            return "Cloudy";
        } else if ((code < 51L || code > 67L) && (code < 80L || code > 99L)) {
            return (code >= 71L && code <= 77L) ? "Snow" : "Unknown";
        } else {
            return "Rain";
        }
    }
}
