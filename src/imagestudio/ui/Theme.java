package imagestudio.ui;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * ============================================================================
 *  Theme
 *  ---------------------------------------------------------------------------
 *  PURPOSE : The design system in one file. Every colour, spacing step and
 *            corner radius used anywhere in the interface is named here, so no
 *            other file contains a raw colour literal or a stray pixel number.
 *
 *  THE IDEA : light chrome around a dark canvas. The photograph should be the
 *            brightest thing on screen, because it is the thing being looked
 *            at. Photo tools are built this way for a reason.
 *
 *  SPACING  : a 4 / 8 / 16 / 24 scale. Using four fixed steps instead of
 *            arbitrary numbers is what makes a layout look deliberate rather
 *            than assembled.
 *
 *  FONTS    : derived from whatever the platform look and feel is already
 *            using, never hardcoded to a family name. Hardcoding "Segoe UI" or
 *            "Helvetica" means the program looks wrong on the other operating
 *            system, so only the size and weight are adjusted here.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class Theme {

    /* ---------------------------- chrome ----------------------------------- */

    /** Window background behind the toolbar and status bar. */
    public static final Color BG_CHROME = new Color(0xF7, 0xF8, 0xFA);
    /** Raised surfaces such as the toolbar. */
    public static final Color SURFACE = Color.WHITE;
    /** Hairline separators. */
    public static final Color BORDER = new Color(0xE2, 0xE5, 0xEA);
    /** Primary text. */
    public static final Color TEXT = new Color(0x1B, 0x1F, 0x24);
    /** Secondary text: dimensions, hints, the zoom readout. */
    public static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);
    /** The single accent colour. One is enough. */
    public static final Color ACCENT = new Color(0x2F, 0x6F, 0xED);

    /* ---------------------------- canvas ----------------------------------- */

    /** The dark area the image floats on. */
    public static final Color BG_CANVAS = new Color(0x2B, 0x2F, 0x36);
    /** Transparency checkerboard, light square. */
    public static final Color CHECKER_A = new Color(0x3A, 0x3F, 0x47);
    /** Transparency checkerboard, dark square. */
    public static final Color CHECKER_B = new Color(0x33, 0x38, 0x3F);
    /** Text drawn on the dark canvas, such as the empty state. */
    public static final Color CANVAS_TEXT = new Color(0x8A, 0x91, 0x9C);
    /** The dashed outline of the empty state. */
    public static final Color CANVAS_HINT = new Color(0x4A, 0x50, 0x5A);

    /* ---------------------------- metrics ---------------------------------- */

    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 16;
    public static final int SPACE_4 = 24;

    /** Corner radius for the image card and the empty state. */
    public static final int RADIUS = 10;
    /** Side of one transparency checkerboard square, in pixels. */
    public static final int CHECKER_SIZE = 12;
    /** Breathing room between the image and the edge of the canvas. */
    public static final int CANVAS_PADDING = 32;

    private Theme() {
    }

    /* ----------------------------- fonts ----------------------------------- */

    /** The look and feel's own label font, so the program looks native. */
    private static Font base() {
        Font f = UIManager.getFont("Label.font");
        return (f != null) ? f : new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    /** Slightly smaller, for the status bar and secondary readouts. */
    public static Font small() {
        return base().deriveFont(base().getSize2D() - 1f);
    }

    /** Bold, for the filename in the status bar and the empty-state heading. */
    public static Font emphasis() {
        return base().deriveFont(Font.BOLD);
    }

    /** Bold and larger, for the empty-state heading only. */
    public static Font heading() {
        return base().deriveFont(Font.BOLD, base().getSize2D() + 3f);
    }
}
