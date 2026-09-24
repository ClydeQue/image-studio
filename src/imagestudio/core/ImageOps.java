package imagestudio.core;

import java.awt.image.BufferedImage;

/**
 * ============================================================================
 *  ImageOps
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Every pixel operation in the program, as pure static functions.
 *            Give one an image, get a new image back. Nothing here is mutated,
 *            nothing here knows that a GUI exists.
 *
 *            That last part is deliberate. This file imports no Swing, so the
 *            image maths can be tested on its own (see SelfTest) and the user
 *            interface stays a thin shell over code that is already proven.
 *
 *  PIXEL LAYOUT : A Java BufferedImage pixel is one 32-bit int packed as ARGB,
 *                 one byte per channel:
 *
 *                   bit 31 ................................ bit 0
 *                   [ AAAAAAAA ][ RRRRRRRR ][ GGGGGGGG ][ BBBBBBBB ]
 *                        A          R           G           B
 *
 *                 So taking a pixel apart is a shift and a mask, and putting it
 *                 back is a shift and an OR.
 *
 *  WHY BULK ARRAYS : getRGB(x, y) and setRGB(x, y, px) cost a method call per
 *                 pixel. On a 12 megapixel photo that is 24 million calls and
 *                 the window visibly stutters. Reading the whole raster into an
 *                 int[] once, working on the array, then writing it back once is
 *                 roughly 20x faster and the loop body reads exactly the same.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class ImageOps {

    /* ---- Bit masks and shift distances (named so the code reads clearly) --- */
    private static final int BYTE_MASK = 0xFF;   // 0000 0000 1111 1111 (low byte)
    private static final int A_SHIFT   = 24;
    private static final int R_SHIFT   = 16;
    private static final int G_SHIFT   = 8;

    /** Utility class: never instantiated. */
    private ImageOps() {
    }

    /* ======================= GRAYSCALE CONVERSION ========================== */

    /**
     * Converts an image to grey using the chosen method.
     * <p>
     * The alpha channel is carried through untouched, so a transparent PNG stays
     * transparent. The three colour channels are all set to the same grey level,
     * which is what makes a pixel grey in the first place.
     *
     * @param src    the source image, never modified
     * @param method which of the three formulas to apply
     * @return a new grey image, or {@code src} itself when the method is
     *         {@link GrayscaleMethod#NONE}, since that is a no-op by definition
     */
    public static BufferedImage toGrayscale(BufferedImage src, GrayscaleMethod method) {
        if (!method.isConversion()) {
            return src;                       // NONE: nothing to do, hand it straight back
        }

        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];

            /* --- UNPACK ------------------------------------------------------
               >>> on alpha so the sign bit is never dragged in, >> plus a mask
               on the colour bytes.                                            */
            int a = (px >>> A_SHIFT) & BYTE_MASK;
            int r = (px >>  R_SHIFT) & BYTE_MASK;
            int g = (px >>  G_SHIFT) & BYTE_MASK;
            int b =  px              & BYTE_MASK;

            /* --- CONVERT -----------------------------------------------------
               The formula itself lives on the enum constant, so this loop never
               needs to know which of the three is running.                    */
            int grey = method.toGray(r, g, b);

            /* --- REPACK ------------------------------------------------------
               Same grey into R, G and B; original alpha back on top.          */
            out[i] = (a << A_SHIFT) | (grey << R_SHIFT) | (grey << G_SHIFT) | grey;
        }

        return imageFrom(out, w, h);
    }

    /* ========================= TRANSFORMATIONS ============================= */

    /**
     * Flips the image horizontally, mirroring it left to right.
     * <p>
     * In axis terms this reflects the image across its <em>vertical</em> axis.
     * Both namings are given because the two conventions are easy to confuse:
     * "flip horizontal" describes the direction pixels travel, while "flip about
     * the vertical axis" describes the mirror line they travel across. They mean
     * the same picture.
     * <p>
     * Every row keeps its position; only the order within the row reverses, so
     * the pixel at column x is taken from column (width - 1 - x).
     *
     * @param src the source image, never modified
     * @return a new, mirrored image
     */
    public static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int y = 0; y < h; y++) {
            int row = y * w;                       // start index of this row
            for (int x = 0; x < w; x++) {
                out[row + x] = pixels[row + (w - 1 - x)];
            }
        }

        return imageFrom(out, w, h);
    }

    /**
     * Flips the image vertically, mirroring it top to bottom.
     * <p>
     * In axis terms this reflects the image across its <em>horizontal</em> axis.
     * <p>
     * Here whole rows swap places and the order inside a row never changes,
     * so each row can be moved in one arraycopy rather than pixel by pixel.
     *
     * @param src the source image, never modified
     * @return a new, mirrored image
     */
    public static BufferedImage flipVertical(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int y = 0; y < h; y++) {
            /* Row y of the output is row (h - 1 - y) of the input, intact. */
            System.arraycopy(pixels, (h - 1 - y) * w, out, y * w, w);
        }

        return imageFrom(out, w, h);
    }

    /* ---------------------- helper methods ---------------------------------- */

    /** Reads the whole raster into one int[] of packed ARGB pixels, row by row. */
    private static int[] readPixels(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        return img.getRGB(0, 0, w, h, new int[w * h], 0, w);
    }

    /** Wraps a finished int[] of packed ARGB pixels back into an image. */
    private static BufferedImage imageFrom(int[] pixels, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }
}
