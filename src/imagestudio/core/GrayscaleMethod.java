package imagestudio.core;

/**
 * ============================================================================
 *  GrayscaleMethod
 *  ---------------------------------------------------------------------------
 *  PURPOSE : The three grayscale conversion methods required by the project,
 *            plus NONE for "show the imported image unchanged".
 *
 *            Each constant knows its own formula, so adding or auditing a
 *            method means touching exactly one place. The UI builds its whole
 *            selector by looping over values(), which is what keeps the menu
 *            and the toolbar permanently in agreement about what is selected.
 *
 *  WHY INTEGER ARITHMETIC EVERYWHERE:
 *            The obvious way to write Luminosity is with floats:
 *
 *                Math.round(0.21f * r + 0.72f * g + 0.07f * b)
 *
 *            For the pixel (200, 100, 50) that lands on 117.50000149, which
 *            rounds to 118 only because of how floats happen to be stored.
 *            Sitting on a .5 boundary at the mercy of float noise is not worth
 *            it, so the weights are scaled by 100 and +50 does the rounding.
 *            The result is exact, identical on every machine, and faster.
 *
 *  WHY THERE IS NO CLAMPING:
 *            All three formulas are provably bounded by 0..255 for any 0..255
 *            input, so there is no Math.min(255, ...) noise anywhere in this
 *            program. Proof, at the maximum input of (255, 255, 255):
 *
 *                Average    : (255 + 255 + 255) / 3            = 255
 *                Luminosity : (255*21 + 255*72 + 255*7 + 50)/100
 *                             = (25500 + 50) / 100             = 255
 *                Lightness  : (255 + 255) / 2                  = 255
 *
 *            SelfTest asserts this, so the claim is checked and not just
 *            asserted in a comment.
 *
 *  Author : Clyde
 * ============================================================================
 */
public enum GrayscaleMethod {

    /**
     * No conversion. The imported image is shown in its original colour.
     * This is a real user-facing choice, which is why it lives in the enum
     * rather than being represented by a null somewhere.
     */
    NONE("Original Colour", "Show the imported image in full colour", '0') {
        @Override
        public boolean isConversion() {
            return false;
        }

        @Override
        public int toGray(int r, int g, int b) {
            throw new UnsupportedOperationException(
                    "NONE performs no conversion. Guard the call with isConversion().");
        }
    },

    /** Average method: the plain mean of the three channels. */
    AVERAGE("Average", "(R + G + B) / 3", '1') {
        @Override
        public int toGray(int r, int g, int b) {
            /* Integer division truncates, which is the standard textbook form. */
            return (r + g + b) / 3;
        }
    },

    /**
     * Luminosity method: weighted for human eye sensitivity. We perceive green
     * most strongly and blue least, so a flat average makes greens look too
     * dark and blues too light. This weighting fixes that.
     */
    LUMINOSITY("Luminosity", "0.21 R + 0.72 G + 0.07 B", '2') {
        @Override
        public int toGray(int r, int g, int b) {
            /* Weights x100 so this stays in integers; +50 rounds to nearest. */
            return (21 * r + 72 * g + 7 * b + 50) / 100;
        }
    },

    /** Lightness method: the midpoint of the brightest and darkest channel. */
    LIGHTNESS("Lightness", "(max(R,G,B) + min(R,G,B)) / 2", '3') {
        @Override
        public int toGray(int r, int g, int b) {
            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));
            return (max + min) / 2;
        }
    };

    private final String label;
    private final String formula;
    private final char accelerator;

    GrayscaleMethod(String label, String formula, char accelerator) {
        this.label = label;
        this.formula = formula;
        this.accelerator = accelerator;
    }

    /**
     * Converts one pixel's colour channels to a single grey level.
     *
     * @param r red   channel, 0..255
     * @param g green channel, 0..255
     * @param b blue  channel, 0..255
     * @return the grey level, 0..255, needing no clamping
     * @throws UnsupportedOperationException if called on {@link #NONE}
     */
    public abstract int toGray(int r, int g, int b);

    /** @return false only for {@link #NONE}, which callers must skip. */
    public boolean isConversion() {
        return true;
    }

    /** Short name shown on the button and the menu item. */
    public String label() {
        return label;
    }

    /** The formula itself, used as the tooltip so the maths is always visible. */
    public String formula() {
        return formula;
    }

    /** Number key that selects this method, shown as the menu accelerator. */
    public char accelerator() {
        return accelerator;
    }

    /** Lower-case token used when building a suggested save filename. */
    public String fileNameTag() {
        return name().toLowerCase();
    }
}
