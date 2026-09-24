package imagestudio.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * ============================================================================
 *  ImageScaler
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Shrinks an image for display without the artefacts a single
 *            scaling step would produce, and remembers the last result so a
 *            window being dragged does not redo the work on every frame.
 *
 *  WHY NOT JUST ONE drawImage CALL
 *            Bilinear interpolation reads a 2x2 neighbourhood per output
 *            pixel. Going from 4000 pixels wide to 800 in one step means each
 *            output pixel is decided by 4 of the 5 source pixels it covers,
 *            and the rest are never read at all. Fine detail turns into
 *            aliasing and crawling edges. Halving repeatedly reads every pixel
 *            on the way down, so nothing is skipped.
 *
 *  ASPECT RATIO
 *            Both sides are always halved together, never clamped
 *            independently, so the proportions stay exact no matter how many
 *            steps are taken. SelfTest checks this.
 *
 *  THE CACHE
 *            Keyed on the NUMBER OF HALVINGS, not on the requested size. The
 *            requested size changes on every frame while a window is being
 *            dragged, so a size-keyed cache would miss every single time and
 *            rebuild the whole pyramid on the event dispatch thread. The
 *            halving count only changes when the target crosses a power of
 *            two, so this version actually hits.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class ImageScaler {

    /* ---- Remembered pyramid, so a resize does not rebuild it every frame --- */
    private BufferedImage cachedSource;
    private BufferedImage cached;
    private int cachedHalvings = -1;

    /**
     * Returns a copy of the image reduced to within 2x of the requested size,
     * ready for a final scaling step by the caller's drawImage.
     * <p>
     * The last 2x is deliberately left undone. On a high-DPI display the
     * Graphics2D carries a device transform, and letting drawImage perform the
     * final step is what allows it to render at full device resolution instead
     * of at the smaller logical size.
     *
     * @param source  the image to reduce, never modified
     * @param targetW the width it is about to be drawn at, in device pixels
     * @param targetH the height it is about to be drawn at, in device pixels
     * @return the reduced image, or {@code source} itself when it is already
     *         small enough that one interpolation step is accurate
     */
    public BufferedImage scaledFor(BufferedImage source, int targetW, int targetH) {
        int halvings = halvingsFor(source.getWidth(), source.getHeight(), targetW, targetH);
        if (halvings == 0) {
            return source;
        }
        if (cached != null && cachedSource == source && cachedHalvings == halvings) {
            return cached;
        }

        cached = halve(source, halvings);
        cachedSource = source;
        cachedHalvings = halvings;
        return cached;
    }

    /** Forgets the cached pyramid. Called when the displayed image changes. */
    public void clear() {
        cachedSource = null;
        cached = null;
        cachedHalvings = -1;
    }

    /* ---------------------- the pure algorithm ------------------------------ */

    /**
     * How many times the image can be halved while both sides stay more than
     * twice the target. Stopping at 2x leaves one accurate interpolation step
     * for the caller.
     *
     * @return the number of halvings, zero when the image is already small enough
     */
    public static int halvingsFor(int width, int height, int targetW, int targetH) {
        int halvings = 0;
        while (width > targetW * 2 && height > targetH * 2) {
            width /= 2;
            height /= 2;
            halvings++;
        }
        return halvings;
    }

    /**
     * Halves the image the given number of times, both sides together.
     *
     * @param source the image to reduce, never modified
     * @param times  how many halvings to perform; zero returns the source
     * @return the reduced image
     */
    public static BufferedImage halve(BufferedImage source, int times) {
        BufferedImage current = source;
        for (int step = 0; step < times; step++) {
            int w = Math.max(1, current.getWidth() / 2);
            int h = Math.max(1, current.getHeight() / 2);

            BufferedImage next = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = next.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(current, 0, 0, w, h, null);
            g.dispose();
            current = next;
        }
        return current;
    }
}
