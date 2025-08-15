import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.json.simple.JSONObject;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    private JPanel root;

    public WeatherAppGui() {
        super("Weather App");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(480, 640);
        this.setLocationRelativeTo((Component) null);
        this.setResizable(false);

        root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(Color.decode("#0078d7"));
        this.setContentPane(root);


        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        topPanel.setOpaque(false);

        JTextField searchTextField = new JTextField();
        searchTextField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        searchTextField.setPreferredSize(new Dimension(10, 40));

        JButton searchButton = new JButton();
        ImageIcon searchIcon = this.loadImageIcon("src/assets/search.png", 36, 36);
        searchButton.setIcon(searchIcon);
        searchButton.setPreferredSize(new Dimension(48, 48));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topPanel.add(searchTextField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);


        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridy = 1;
        JLabel weatherConditionImage = new JLabel();
        weatherConditionImage.setHorizontalAlignment(JLabel.CENTER);
        weatherConditionImage.setIcon(this.loadImageIcon("src/assets/cloudy.png", 260, 150));
        centerPanel.add(weatherConditionImage, gbc);

        gbc.gridy = 2;
        JLabel temperatureText = new JLabel("--°C");
        temperatureText.setFont(new Font("Segoe UI", Font.BOLD, 56));
        temperatureText.setHorizontalAlignment(JLabel.CENTER);
        temperatureText.setForeground(Color.WHITE);
        centerPanel.add(temperatureText, gbc);

        gbc.gridy = 3;
        JLabel weatherConditionDesc = new JLabel("Condition");
        weatherConditionDesc.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        weatherConditionDesc.setHorizontalAlignment(JLabel.CENTER);
        weatherConditionDesc.setPreferredSize(new Dimension(420, 40));
        weatherConditionDesc.setForeground(Color.WHITE);
        centerPanel.add(weatherConditionDesc, gbc);

        root.add(centerPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setOpaque(false);

        JLabel humidityLabel = new JLabel("<html><b>Humidity:</b><br>--%</html>", JLabel.CENTER);
        humidityLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        humidityLabel.setForeground(Color.WHITE);

        JLabel windLabel = new JLabel("<html><b>Windspeed:</b><br>-- km/h</html>", JLabel.CENTER);
        windLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        windLabel.setForeground(Color.WHITE);

        bottomPanel.add(humidityLabel);
        bottomPanel.add(windLabel);
        root.add(bottomPanel, BorderLayout.SOUTH);


        searchButton.addActionListener((e) -> {
            String userInput = searchTextField.getText().trim();
            if (!userInput.isEmpty()) {
                this.weatherData = WeatherApp.getWeatherData(userInput);
                if (this.weatherData == null) {
                    JOptionPane.showMessageDialog(this, "Could not fetch weather for: " + userInput);
                } else {
                    String condition = (String) this.weatherData.get("weather_condition");
                    long humidity = (long) this.weatherData.get("humidity");
                    double temperature = Double.parseDouble(String.valueOf(this.weatherData.get("temperature")));

                    Color targetColor;
                    Color textColor;

                    if ("Rain".equals(condition)) {
                        targetColor = Color.decode("#36332F");
                        textColor = Color.WHITE;
                    } else if ("Cloudy".equals(condition) && humidity > 90) {
                        targetColor = Color.decode("#36332F");
                        textColor = Color.WHITE;
                    } else if (temperature < 0) {
                        targetColor = Color.WHITE;
                        textColor = Color.BLACK;
                    } else {
                        targetColor = Color.decode("#0078d7");
                        textColor = Color.WHITE;
                    }


                    animateBackground(root.getBackground(), targetColor);


                    setTextColor(textColor, temperatureText, weatherConditionDesc, humidityLabel, windLabel);


                    switch (condition) {
                        case "Clear" -> weatherConditionImage.setIcon(this.loadImageIcon("src/assets/clear.png", 260, 150));
                        case "Cloudy" -> weatherConditionImage.setIcon(this.loadImageIcon("src/assets/cloudy.png", 260, 150));
                        case "Rain" -> weatherConditionImage.setIcon(this.loadImageIcon("src/assets/rain.png", 260, 150));
                        case "Snow" -> weatherConditionImage.setIcon(this.loadImageIcon("src/assets/snow.png", 260, 150));
                        default -> weatherConditionImage.setIcon(this.loadImageIcon("src/assets/cloudy.png", 260, 150));
                    }


                    temperatureText.setText(temperature + "°C");
                    weatherConditionDesc.setText(condition);
                    humidityLabel.setText("<html><b>Humidity:</b><br>" + humidity + "%</html>");
                    windLabel.setText("<html><b>Windspeed:</b><br>" + String.valueOf(this.weatherData.get("windspeed")) + " km/h</html>");
                }
            }
        });
    }


    private void animateBackground(Color start, Color end) {
        Timer timer = new Timer(30, null);
        final int steps = 30;
        final int[] count = {0};

        timer.addActionListener(e -> {
            float ratio = (float) count[0] / steps;
            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
            root.setBackground(new Color(r, g, b));
            count[0]++;
            if (count[0] > steps) timer.stop();
        });
        timer.start();
    }


    private void setTextColor(Color color, JLabel... labels) {
        for (JLabel label : labels) {
            label.setForeground(color);
        }
    }

    private ImageIcon loadImageIcon(String resourcePath, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            if (image == null) {
                return null;
            } else {
                Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (IOException e) {
            System.out.println("Could not load image: " + resourcePath + " -> " + e.getMessage());
            return null;
        }
    }
}
