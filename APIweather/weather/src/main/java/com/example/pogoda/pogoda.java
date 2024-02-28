package com.example.pogoda;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
public class pogoda extends Application {
    private static final String API_KEY = "7504628a7545287309c45b5dd200e071";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private TextField cityTextField;
    private TextArea resultTextArea;
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pogoda");
        Label headerLabel = new Label("Pogoda na dzis");
        headerLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        cityTextField = new TextField();
        cityTextField.setPromptText("Wprowadź nazwę miasta");
        Image ikona = new Image("https://cdn-icons-png.flaticon.com/512/954/954591.png");
        ImageView lupa = new ImageView(ikona);
        Button searchButton = new Button();
        searchButton.setGraphic(lupa);
        lupa.setFitHeight(15);
        lupa.setFitWidth(15);
        searchButton.setOnAction(e -> searchWeather());
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        Button closeButton = new Button("Zamknij");
        closeButton.setOnAction(e -> primaryStage.close());
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(headerLabel, cityTextField, searchButton, resultTextArea, closeButton);
        Scene scene = new Scene(layout, 600, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void searchWeather() {
        String cityName = cityTextField.getText();
        if (!cityName.isEmpty()) {
            String apiUrl = String.format("%s?q=%s&appid=%s&lang=pl&units=metric", API_URL, cityName, API_KEY);
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                try (InputStream responseStream = connection.getInputStream();
                     Scanner scanner = new Scanner(responseStream).useDelimiter("\\A")) {
                    String responseBody = scanner.hasNext() ? scanner.next() : "";
                    JSONObject json = new JSONObject(responseBody);
                    displayWeatherInfo(json);
                }
            } catch (IOException e) {
                resultTextArea.setText("Brak wyniku.");
            }
        } else {
            resultTextArea.setText("Wprowadź nazwę miasta");
        }
    }
    private void displayWeatherInfo(JSONObject json) {
        String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
        JSONObject mainSection = json.getJSONObject("main");
        double windSpeed = json.getJSONObject("wind").getDouble("speed");
        String windSpeedString = String.format("%.2f", windSpeed);
        String windDirection = getWindDirection(json.getJSONObject("wind").optDouble("deg"));
        String rainInfo = getRainSnowInfo(json, "rain");
        String snowInfo = getRainSnowInfo(json, "snow");
        int clouds = json.getJSONObject("clouds").getInt("all");
        String cityName = json.getString("name");
        String result = String.format("Opis: %s\nTemperatura: %.2f°C\nWilgotność: %d%%\nCiśnienie: %.2f hPa\nOdczuwalna: %.2f°C\nMin. temperatura: %.2f°C\nMax. temperatura: %.2f°C\nPrędkość wiatru: %s m/s\nKierunek wiatru: %s\nDeszcz: %s\nŚnieg: %s\nZachmurzenie: %d%%\nMiasto: %s",
                description, mainSection.getDouble("temp"), mainSection.getInt("humidity"),
                mainSection.getDouble("pressure"), mainSection.getDouble("feels_like"),
                mainSection.getDouble("temp_min"), mainSection.getDouble("temp_max"),
                windSpeedString, windDirection, rainInfo, snowInfo, clouds, cityName);
        resultTextArea.setText(result);
    }
    private String getWindDirection(double degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "E";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "SW";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }
    private String getRainSnowInfo(JSONObject json, String key) {
        if (json.optJSONObject(key) != null) {
            return json.getJSONObject(key).toString();
        } else {
            return "Brak danych";
        }
    }
}