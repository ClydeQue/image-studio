package imagestudio;

import imagestudio.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * ============================================================================
 *  ImageStudio
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Starts the program. Chooses a look and feel, then builds the
 *            window on the event dispatch thread, which is the only thread
 *            Swing components may be touched from.
 *
 *  ABOUT THE OPTIONAL LOOK AND FEEL
 *            setLookAndFeel has an overload that takes a class NAME as a
 *            String and resolves it at run time. That single detail is what
 *            lets this program use FlatLaf for a modern look when the jar
 *            happens to be there, while still compiling and running perfectly
 *            when it is not. Nothing here links against FlatLaf, so:
 *
 *                javac -d classes src/imagestudio/*.java
 *
 *            succeeds on a bare JDK with an empty lib folder. Referring to the
 *            class directly instead would turn a missing jar into a build
 *            failure on somebody else's machine, which is not a trade worth
 *            making for a cosmetic gain.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class ImageStudio {

    /** Resolved by name at run time. Absent jar simply means the fallback runs. */
    private static final String FLATLAF = "com.formdev.flatlaf.FlatLightLaf";

    private ImageStudio() {
    }

    public static void main(String[] args) {
        /* Names the application in the macOS menu bar and dock. Ignored
           everywhere else, so it costs nothing to set unconditionally. */
        System.setProperty("apple.awt.application.name", "Image Studio");

        /* Deliberately NOT setting apple.laf.useScreenMenuBar. On macOS that
           moves the menu bar out of the window and up to the top of the
           screen, which looks native but hides the menus from any screenshot
           of the window. The menus are part of what this program is showing,
           so they stay where they can be seen. */

        applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);

            /* An optional file name on the command line opens straight away,
               so the program can be launched on a specific image:
                   java -cp classes imagestudio.ImageStudio test-images/color-chart.png */
            if (args.length > 0) {
                window.openFile(new java.io.File(args[0]));
            }
        });
    }

    /**
     * Tries the modern look first, then the platform's own, then gives up
     * quietly. Appearance is never worth failing to start over, so every step
     * here degrades instead of throwing.
     */
    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(FLATLAF);
            return;
        } catch (Exception flatLafNotAvailable) {
            /* Expected whenever lib/flatlaf.jar is not on the classpath. */
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception systemLookAndFeelUnavailable) {
            /* Keep whatever the default is. The program still works fully. */
        }
    }
}
