package imagestudio.ui;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Font;

/**
 * ============================================================================
 *  HelpDialogs
 *  ---------------------------------------------------------------------------
 *  PURPOSE : The Help menu's two panels, text and presentation together, kept
 *            away from the window class.
 *
 *            A window should be busy with layout and wiring, not carrying a
 *            hundred lines of explanation. Putting the wording here makes it
 *            easy to find and edit without scrolling past Swing code to reach
 *            it, and means both panels are presented the same way.
 *
 *  Author : Clyde
 * ============================================================================
 */
final class HelpDialogs {

    private HelpDialogs() {
    }

    /** The numbers quoted here are the real ones, checked by SelfTest. */
    private static final String FORMULAS = """
            Every method turns three colour channels into one grey level, then
            writes that same level back into R, G and B.

            AVERAGE
                gray = (R + G + B) / 3

                The plain mean. Simple and fast, but it treats the three
                channels as equally bright, which the eye does not.

            LUMINOSITY
                gray = 0.21 R + 0.72 G + 0.07 B

                Weighted for human vision. We see green most strongly and blue
                least, so a flat average makes greens look too dark and blues
                too light. Pure green becomes 184 here but only 85 by Average,
                and pure blue becomes 18 rather than 85.

                Computed as (21 R + 72 G + 7 B + 50) / 100 in whole numbers, so
                the result is exact on every machine and never needs clamping.

                Other weightings exist. ITU-R BT.601 uses 0.299 / 0.587 / 0.114
                and BT.709 uses 0.2126 / 0.7152 / 0.0722. This program uses the
                0.21 / 0.72 / 0.07 set.

            LIGHTNESS
                gray = (max(R, G, B) + min(R, G, B)) / 2

                The midpoint of the brightest and darkest channel. It ignores
                the middle channel entirely, so every fully saturated colour
                lands on the same grey, 127.
            """;

    private static final String ABOUT = """
            IMAGE STUDIO
            Grayscale conversion and image transformations

            Clyde - Ateneo de Zamboanga University

            WHAT IT DOES
                Three grayscale methods: Average, Luminosity, Lightness.
                Two transformations: flip horizontal and flip vertical.

            THE TWO FLIPS
                Flip Horizontal mirrors the image left to right, which is a
                reflection across its vertical axis.

                Flip Vertical mirrors the image top to bottom, which is a
                reflection across its horizontal axis.

                Both namings are given everywhere in this program because the
                two conventions disagree about which is which.

            HOW IT WORKS
                Nothing is ever done to the imported image. The program
                remembers which method is selected and which flips are on, then
                rebuilds the result from the original every time.

                That is why clicking a flip twice returns the image exactly to
                where it started, and why switching from Average to Luminosity
                gives the true Luminosity of the original colours rather than a
                second conversion applied on top of the first.

            HOW IT IS BUILT
                Java Swing, no required libraries. Every pixel operation is
                written by hand in core/ImageOps.java, and each one is checked
                against values worked out on paper by verify/SelfTest.java.
            """;

    static void showFormulas(Component owner) {
        show(owner, "Grayscale Formulas", FORMULAS);
    }

    static void showAbout(Component owner) {
        show(owner, "About Image Studio", ABOUT);
    }

    /** Monospaced, so the formulas line up the way they were written. */
    private static void show(Component owner, String title, String body) {
        JTextArea area = new JTextArea(body);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.SPACE_3));
        JOptionPane.showMessageDialog(owner, area, title, JOptionPane.PLAIN_MESSAGE);
    }
}
