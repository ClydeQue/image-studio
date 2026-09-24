package imagestudio.core;

import java.awt.image.BufferedImage;

/**
 * ============================================================================
 *  ImageDocument
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Holds the imported image and the short recipe of what the user has
 *            asked to be done to it. This is the whole state of the program.
 *
 *  THE NON-DESTRUCTIVE IDEA:
 *            The imported image is never written to. Instead the document
 *            remembers three small things,
 *
 *                grayscale : NONE | AVERAGE | LUMINOSITY | LIGHTNESS
 *                flipH     : true / false
 *                flipV     : true / false
 *
 *            and render() rebuilds the result from the original every time:
 *
 *                original -> grayscale -> flipH? -> flipV? -> result
 *
 *            That one decision buys a surprising amount:
 *
 *              - Switching Average to Luminosity recomputes from the original
 *                colour pixels. A destructive program would be running the
 *                second formula over pixels that are already grey, which gives
 *                a different and wrong answer.
 *              - Every control is its own undo. Clicking a flip twice returns
 *                the image to where it started, exactly, with no undo stack and
 *                no stored copies.
 *              - Comparing against the original is free, because the original
 *                is still sitting right there.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class ImageDocument {

    private final BufferedImage original;
    private final String sourceName;

    /* ---- The recipe. These three fields are the entire mutable state. ------ */
    private GrayscaleMethod grayscale = GrayscaleMethod.NONE;
    private boolean flipH;
    private boolean flipV;

    /**
     * @param original   the imported image, kept untouched for the whole session
     * @param sourceName the file name it came from, used for the window title
     *                   and for suggesting a name when saving
     */
    public ImageDocument(BufferedImage original, String sourceName) {
        this.original = original;
        this.sourceName = sourceName;
    }

    /* =========================== RENDERING ================================= */

    /**
     * Rebuilds the output image from the original by applying the recipe in
     * order: grayscale first, then the flips.
     * <p>
     * Order matters for readability but not for the result here, since a
     * per-pixel colour change and a rearrangement of pixel positions are
     * independent of each other. Grayscale runs first because that is the order
     * the controls are laid out in, and matching the two keeps the code honest.
     *
     * @return a newly rendered image, or the original itself if the recipe is
     *         empty (nothing has been selected yet)
     */
    public BufferedImage render() {
        BufferedImage result = ImageOps.toGrayscale(original, grayscale);
        if (flipH) {
            result = ImageOps.flipHorizontal(result);
        }
        if (flipV) {
            result = ImageOps.flipVertical(result);
        }
        return result;
    }

    /** Clears the recipe so the next render returns the untouched original. */
    public void reset() {
        grayscale = GrayscaleMethod.NONE;
        flipH = false;
        flipV = false;
    }

    /** @return true when anything at all has been applied. Drives Save and Reset. */
    public boolean isModified() {
        return grayscale.isConversion() || flipH || flipV;
    }

    /* ============================ RECIPE =================================== */

    public GrayscaleMethod grayscale() {
        return grayscale;
    }

    public void setGrayscale(GrayscaleMethod method) {
        this.grayscale = method;
    }

    public boolean isFlippedHorizontally() {
        return flipH;
    }

    public boolean isFlippedVertically() {
        return flipV;
    }

    /** Toggles, rather than sets, so the same control also undoes the flip. */
    public void toggleFlipHorizontal() {
        flipH = !flipH;
    }

    /** Toggles, rather than sets, so the same control also undoes the flip. */
    public void toggleFlipVertical() {
        flipV = !flipV;
    }

    /* ========================== DESCRIPTION ================================ */

    public BufferedImage original() {
        return original;
    }

    public String sourceName() {
        return sourceName;
    }

    public int width() {
        return original.getWidth();
    }

    public int height() {
        return original.getHeight();
    }

    /**
     * Plain-English description of the current recipe for the status bar, for
     * example "Luminosity + flip H". Keeping this next to the state it
     * describes means the status bar can never drift out of step with reality.
     */
    public String recipeSummary() {
        if (!isModified()) {
            return "Original, unmodified";
        }
        StringBuilder sb = new StringBuilder();
        if (grayscale.isConversion()) {
            sb.append(grayscale.label());
        }
        if (flipH) {
            sb.append(sb.isEmpty() ? "" : " + ").append("flip H");
        }
        if (flipV) {
            sb.append(sb.isEmpty() ? "" : " + ").append("flip V");
        }
        return sb.toString();
    }

    /**
     * Suggests a save filename that says what was done, for example
     * {@code beach_luminosity_flipH.png}. Saves the user from naming files
     * themselves and makes a folder of exports self-describing.
     *
     * @param extension the file extension to use, without the dot
     */
    public String suggestedFileName(String extension) {
        String base = sourceName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);       // drop the original extension
        }

        StringBuilder sb = new StringBuilder(base);
        if (grayscale.isConversion()) {
            sb.append('_').append(grayscale.fileNameTag());
        }
        if (flipH) {
            sb.append("_flipH");
        }
        if (flipV) {
            sb.append("_flipV");
        }
        return sb.append('.').append(extension).toString();
    }
}
