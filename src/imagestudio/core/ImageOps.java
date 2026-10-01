package imagestudio.core;

import java.awt.image.BufferedImage;

// Pagproseso ng pixels
public final class ImageOps {

    private static final int BYTE_MASK = 0xFF;   // Huling walong bits
    private static final int A_SHIFT   = 24;
    private static final int R_SHIFT   = 16;
    private static final int G_SHIFT   = 8;

    /* Utility na class */
    private ImageOps() {
    }


    /* Gawing grayscale */
    public static BufferedImage toGrayscale(BufferedImage src, GrayscaleMethod method) {
        if (!method.isConversion()) {
            return src;                       // Walang kailangang baguhin
        }

        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];

            /* Kunin ang channels */
            int a = (px >>> A_SHIFT) & BYTE_MASK;
            int r = (px >>  R_SHIFT) & BYTE_MASK;
            int g = (px >>  G_SHIFT) & BYTE_MASK;
            int b =  px              & BYTE_MASK;

            /* Ilapat ang formula */
            int grey = method.toGray(r, g, b);

            /* Buuin ang pixel */
            out[i] = (a << A_SHIFT) | (grey << R_SHIFT) | (grey << G_SHIFT) | grey;
        }

        return imageFrom(out, w, h);
    }


    /* Pahalang na baliktad */
    public static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int y = 0; y < h; y++) {
            int row = y * w;                       // Simula ng hanay
            for (int x = 0; x < w; x++) {
                out[row + x] = pixels[row + (w - 1 - x)];
            }
        }

        return imageFrom(out, w, h);
    }

    /* Patayong baliktad */
    public static BufferedImage flipVertical(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = readPixels(src);
        int[] out = new int[pixels.length];

        for (int y = 0; y < h; y++) {
            /* Kopyahin ang hanay */
            System.arraycopy(pixels, (h - 1 - y) * w, out, y * w, w);
        }

        return imageFrom(out, w, h);
    }


    /* Basahin ang pixels */
    private static int[] readPixels(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        return img.getRGB(0, 0, w, h, new int[w * h], 0, w);
    }

    /* Buuin ang larawan */
    private static BufferedImage imageFrom(int[] pixels, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }
}
