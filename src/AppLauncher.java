//Developed By: Mijanur_Rahman_Oli,Muhammad_Kawser_Azim,Rayhan_Rahman,Seam_Islam

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AppLauncher {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            WeatherAppGui gui = new WeatherAppGui();
            gui.setVisible (true);
        });
    }
}
