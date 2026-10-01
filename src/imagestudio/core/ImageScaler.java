package imagestudio.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Paliitin ang preview
public final class ImageScaler {

    /* Pinaliit na cache */
    private BufferedImage cachedSource;
    private BufferedImage cached;
    private int cachedHalvings = -1;

    /* Sukat para preview */
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

    /* Linisin ang cache */
    public void clear() {
        cachedSource = null;
        cached = null;
        cachedHalvings = -1;
    }


    /* Bilang ng paghahati */
    public static int halvingsFor(int width, int height, int targetW, int targetH) {
        int halvings = 0;
        while (width > targetW * 2 && height > targetH * 2) {
            width /= 2;
            height /= 2;
            halvings++;
        }
        return halvings;
    }

    /* Hatiin ang sukat */
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
