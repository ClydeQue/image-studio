package imagestudio.core;

import java.util.Locale;

// Mga grayscale formula
public enum GrayscaleMethod {

    /* Orihinal na kulay */
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

    /* Average ng channels */
    AVERAGE("Average", "(R + G + B) / 3", '1') {
        @Override
        public int toGray(int r, int g, int b) {
            /* Tanggal ang decimal */
            return (r + g + b) / 3;
        }
    },

    /* Bigat ng kulay */
    LUMINOSITY("Luminosity", "0.21 R + 0.72 G + 0.07 B", '2') {
        @Override
        public int toGray(int r, int g, int b) {
            /* Round ang resulta */
            return (21 * r + 72 * g + 7 * b + 50) / 100;
        }
    },

    /* Gitnang liwanag */
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

    /* Kunin ang grayscale */
    public abstract int toGray(int r, int g, int b);

    /* Walang conversion */
    public boolean isConversion() {
        return true;
    }

    /* Pangalan ng method */
    public String label() {
        return label;
    }

    /* Formula sa tooltip */
    public String formula() {
        return formula;
    }

    /* Shortcut ng method */
    public char accelerator() {
        return accelerator;
    }

    /* Tag ng filename */
    public String fileNameTag() {
        return name().toLowerCase(Locale.ROOT);
    }
}
