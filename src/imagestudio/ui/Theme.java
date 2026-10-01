package imagestudio.ui;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

// Kulay at spacing
public final class Theme {


    /* Background ng window */
    public static final Color BG_CHROME = new Color(0xF7, 0xF8, 0xFA);
    /* Kulay ng surface */
    public static final Color SURFACE = Color.WHITE;
    /* Mga separator */
    public static final Color BORDER = new Color(0xE2, 0xE5, 0xEA);
    /* Pangunahing teksto */
    public static final Color TEXT = new Color(0x1B, 0x1F, 0x24);
    /* Pangalawang teksto */
    public static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);
    /* Pangunahing accent */
    public static final Color ACCENT = new Color(0x2F, 0x6F, 0xED);
    /* Teksto sa accent */
    public static final Color ON_ACCENT = Color.WHITE;
    /* Outline ng button */
    public static final Color BUTTON_OUTLINE = new Color(0xB8, 0xBE, 0xC8);


    /* Madilim na canvas */
    public static final Color BG_CANVAS = new Color(0x2B, 0x2F, 0x36);
    /* Maliwanag na checker */
    public static final Color CHECKER_A = new Color(0x3A, 0x3F, 0x47);
    /* Madilim na checker */
    public static final Color CHECKER_B = new Color(0x33, 0x38, 0x3F);
    /* Teksto sa canvas */
    public static final Color CANVAS_TEXT = new Color(0x8A, 0x91, 0x9C);
    /* Outline ng canvas */
    public static final Color CANVAS_HINT = new Color(0x4A, 0x50, 0x5A);


    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 16;
    public static final int SPACE_4 = 24;

    /* Padding ng button */
    public static final int BUTTON_PAD_Y = 5;
    public static final int BUTTON_PAD_X = 8;

    /* Kurbada ng card */
    public static final int RADIUS = 10;
    /* Sukat ng checker */
    public static final int CHECKER_SIZE = 12;
    /* Espasyo sa gilid */
    public static final int CANVAS_PADDING = 32;

    private Theme() {
    }


    /* Font ng platform */
    private static Font base() {
        Font f = UIManager.getFont("Label.font");
        return (f != null) ? f : new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    /* Maliit na teksto */
    public static Font small() {
        return base().deriveFont(base().getSize2D() - 1f);
    }

    /* Makapal na teksto */
    public static Font emphasis() {
        return base().deriveFont(Font.BOLD);
    }

    /* Malaking pamagat */
    public static Font heading() {
        return base().deriveFont(Font.BOLD, base().getSize2D() + 3f);
    }
}
