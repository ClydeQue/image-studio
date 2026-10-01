/*
 * Owner and author: Kenneth Clyde A. Que (ClydeQue).
 * I own this repository and wrote the Image Studio source code.
 */
package imagestudio;

import imagestudio.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Simula ng app
public final class ImageStudio {

    /* Opsyonal na tema */
    private static final String FLATLAF = "com.formdev.flatlaf.FlatLightLaf";

    private ImageStudio() {
    }

    public static void main(String[] args) {
        /* Pangalan ng app */
        System.setProperty("apple.awt.application.name", "Image Studio");

        /* Menu sa window */

        applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);

            /* Buksan ang file */
            if (args.length > 0) {
                window.openFile(new java.io.File(args[0]));
            }
        });
    }

    /* Piliin ang tema */
    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(FLATLAF);
            return;
        } catch (Exception flatLafNotAvailable) {
            /* Walang dagdag tema */
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception systemLookAndFeelUnavailable) {
            /* Gamitin ang default */
        }
    }
}
