package imagestudio.verify;

import imagestudio.core.GrayscaleMethod;
import imagestudio.core.ImageDocument;
import imagestudio.core.ImageOps;
import imagestudio.core.ImageScaler;
import imagestudio.io.SaveFileNaming;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Suriin ang outputs
public final class SelfTest {

    private static int passed;
    private static int failed;

    private SelfTest() {
    }

    public static void main(String[] args) throws IOException {
        /* Walang bubuksang window */
        System.setProperty("java.awt.headless", "true");

        System.out.println("==================================================");
        System.out.println(" IMAGE STUDIO : SELF TEST");
        System.out.println("==================================================");

        checkGrayscaleFormulas();
        checkBoundsAndNeutrality();
        checkFlips();
        checkImageLevelBehaviour();
        checkSaveFileNaming();
        checkScaling();

        System.out.println();
        System.out.printf(" RESULT : %d passed, %d failed%n", passed, failed);

        writeProofImages();

        System.out.println();
        if (failed > 0) {
            System.out.println(" SELF TEST FAILED");
            System.exit(1);
        }
        System.out.println(" ALL CHECKS PASSED");
    }


    /* Manwal na resulta */
    private static void checkGrayscaleFormulas() {
        section("THE THREE FORMULAS, against values worked out by hand");

        check("Average    of (200,100,50)", 116, GrayscaleMethod.AVERAGE.toGray(200, 100, 50));
        check("Luminosity of (200,100,50)", 118, GrayscaleMethod.LUMINOSITY.toGray(200, 100, 50));
        check("Lightness  of (200,100,50)", 125, GrayscaleMethod.LIGHTNESS.toGray(200, 100, 50));

        /* Purong pulang pixel */
        check("Average    of (255,0,0)", 85, GrayscaleMethod.AVERAGE.toGray(255, 0, 0));
        check("Luminosity of (255,0,0)", 54, GrayscaleMethod.LUMINOSITY.toGray(255, 0, 0));
        check("Lightness  of (255,0,0)", 127, GrayscaleMethod.LIGHTNESS.toGray(255, 0, 0));

        /* Purong berdeng pixel */
        check("Average    of (0,255,0)", 85, GrayscaleMethod.AVERAGE.toGray(0, 255, 0));
        check("Luminosity of (0,255,0)", 184, GrayscaleMethod.LUMINOSITY.toGray(0, 255, 0));
        check("Lightness  of (0,255,0)", 127, GrayscaleMethod.LIGHTNESS.toGray(0, 255, 0));

        /* Purong asul pixel */
        check("Average    of (0,0,255)", 85, GrayscaleMethod.AVERAGE.toGray(0, 0, 255));
        check("Luminosity of (0,0,255)", 18, GrayscaleMethod.LUMINOSITY.toGray(0, 0, 255));
        check("Lightness  of (0,0,255)", 127, GrayscaleMethod.LIGHTNESS.toGray(0, 0, 255));
    }

    /* Saklaw ng grayscale */
    private static void checkBoundsAndNeutrality() {
        section("BOUNDS AND NEUTRALITY, the reason there is no clamping code");

        for (GrayscaleMethod m : GrayscaleMethod.values()) {
            if (!m.isConversion()) {
                continue;
            }
            check(pad(m.label()) + " maps black to 0",    0,   m.toGray(0, 0, 0));
            check(pad(m.label()) + " maps white to 255",  255, m.toGray(255, 255, 255));
            check(pad(m.label()) + " leaves grey 128",    128, m.toGray(128, 128, 128));
        }

        check("NONE reports itself as no conversion", !GrayscaleMethod.NONE.isConversion(),
                "isConversion() should be false");
    }


    private static void checkFlips() {
        section("FLIPS");

        /* Iba-ibang kulay */
        BufferedImage src = gridImage();

        BufferedImage h = ImageOps.flipHorizontal(src);
        BufferedImage v = ImageOps.flipVertical(src);

        /* Baliktarin bawat hanay */
        check("flipH moves (0,0) to (2,0)", src.getRGB(0, 0), h.getRGB(2, 0));
        check("flipH keeps row 0 in row 0", src.getRGB(1, 0), h.getRGB(1, 0));
        check("flipH moves (0,1) to (2,1)", src.getRGB(0, 1), h.getRGB(2, 1));

        /* Baliktarin mga hanay */
        check("flipV moves (0,0) to (0,1)", src.getRGB(0, 0), v.getRGB(0, 1));
        check("flipV keeps column 1 in column 1", src.getRGB(1, 0), v.getRGB(1, 1));

        /* Dobleng flip */
        check("flipH twice returns the original",
                identical(src, ImageOps.flipHorizontal(h)), "involution failed");
        check("flipV twice returns the original",
                identical(src, ImageOps.flipVertical(v)), "involution failed");

        /* Parehong flip */
        BufferedImage both = ImageOps.flipVertical(ImageOps.flipHorizontal(src));
        check("flipH then flipV is a 180 degree rotation",
                identical(both, rotate180(src)), "combination failed");

        /* Magkahiwalay na flips */
        check("flip order does not matter",
                identical(both, ImageOps.flipHorizontal(ImageOps.flipVertical(src))),
                "H then V differed from V then H");

        check("flipH preserves size",
                h.getWidth() == src.getWidth() && h.getHeight() == src.getHeight(),
                "dimensions changed");
    }


    private static void checkImageLevelBehaviour() {
        section("WHOLE-IMAGE BEHAVIOUR");

        BufferedImage src = gridImage();

        for (GrayscaleMethod m : GrayscaleMethod.values()) {
            if (!m.isConversion()) {
                continue;
            }
            BufferedImage grey = ImageOps.toGrayscale(src, m);
            check(pad(m.label()) + " output is truly grey (R == G == B)",
                    allChannelsEqual(grey), "a pixel had unequal channels");
            check(pad(m.label()) + " preserves the alpha channel",
                    alphaPreserved(src, grey), "alpha changed");
        }

        check("NONE returns the image untouched",
                identical(src, ImageOps.toGrayscale(src, GrayscaleMethod.NONE)),
                "NONE altered the image");

        /* Original ang base */
        ImageDocument doc = new ImageDocument(src, "grid.png");
        doc.setGrayscale(GrayscaleMethod.AVERAGE);
        doc.render();
        doc.setGrayscale(GrayscaleMethod.LUMINOSITY);
        check("switching method recomputes from the original, not from grey",
                identical(doc.render(), ImageOps.toGrayscale(src, GrayscaleMethod.LUMINOSITY)),
                "grayscale was applied on top of grayscale");

        doc.toggleFlipHorizontal();
        doc.toggleFlipHorizontal();
        check("toggling a flip twice clears it",
                !doc.isFlippedHorizontally(), "flip stayed on");

        doc.reset();
        check("reset returns the untouched original",
                identical(src, doc.render()), "reset did not restore the original");
        check("reset clears the modified flag", !doc.isModified(), "still modified");

        doc.setGrayscale(GrayscaleMethod.LUMINOSITY);
        doc.toggleFlipHorizontal();
        check("suggested filename describes the recipe",
                "grid_luminosity_flipH.png".equals(doc.suggestedFileName("png")),
                "got " + doc.suggestedFileName("png"));
    }


    /* Suriin ang filename */
    private static void checkSaveFileNaming() {
        section("SAVE FILE NAMING");

        check("a typed name with no extension gains one",
                "beach.png".equals(SaveFileNaming.withExtension(new File("beach"), "png").getName()),
                "got " + SaveFileNaming.withExtension(new File("beach"), "png").getName());

        check("an extension the user typed is left alone",
                "beach.jpg".equals(SaveFileNaming.withExtension(new File("beach.jpg"), "png").getName()),
                "the typed extension was overridden");

        check("extension matching ignores case",
                "beach.PNG".equals(SaveFileNaming.withExtension(new File("beach.PNG"), "jpg").getName()),
                "a capitalised extension was not recognised");

        /* Dagdagan tamang extension */
        check("a dotted name still gains a real extension",
                "my.photo.png".equals(SaveFileNaming.withExtension(new File("my.photo"), "png").getName()),
                "got " + SaveFileNaming.withExtension(new File("my.photo"), "png").getName());

        /* Format na suportado */
        check("an unwritable extension does not become the format",
                "photo.gif.png".equals(SaveFileNaming.withExtension(new File("photo.gif"), "png").getName()),
                "got " + SaveFileNaming.withExtension(new File("photo.gif"), "png").getName());

        check("format is read back from the name, lower-cased",
                "jpg".equals(SaveFileNaming.extensionOf(new File("beach.JPG"))),
                "got " + SaveFileNaming.extensionOf(new File("beach.JPG")));

        check("a name with no extension falls back to png",
                "png".equals(SaveFileNaming.extensionOf(new File("beach"))),
                "got " + SaveFileNaming.extensionOf(new File("beach")));

        check("jpg is recognised as JPEG", SaveFileNaming.isJpeg("jpg"), "not recognised");
        check("jpeg is recognised as JPEG", SaveFileNaming.isJpeg("jpeg"), "not recognised");
        check("png is not JPEG", !SaveFileNaming.isJpeg("png"), "wrongly treated as JPEG");
    }


    /* Suriin ang sukat */
    private static void checkScaling() {
        section("SCALING");

        /* Dalawang paghahati */
        check("halvings for 4000x3000 into 800x600", 2,
                ImageScaler.halvingsFor(4000, 3000, 800, 600));

        check("an image already small enough is not halved", 0,
                ImageScaler.halvingsFor(800, 600, 800, 600));

        check("an image smaller than the target is not halved", 0,
                ImageScaler.halvingsFor(100, 80, 800, 600));

        BufferedImage wide = new BufferedImage(3200, 1200, BufferedImage.TYPE_INT_ARGB);
        BufferedImage reduced = ImageScaler.halve(wide, 2);

        check("halving twice divides each side by four",
                reduced.getWidth() == 800 && reduced.getHeight() == 300,
                "got " + reduced.getWidth() + "x" + reduced.getHeight());

        check("halving preserves the aspect ratio exactly",
                (double) wide.getWidth() / wide.getHeight()
                        == (double) reduced.getWidth() / reduced.getHeight(),
                "the picture would be stretched");

        check("halving zero times returns the source untouched",
                ImageScaler.halve(wide, 0) == wide, "a needless copy was made");

        /* Panatilihin isang pixel */
        BufferedImage tiny = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        BufferedImage halvedTiny = ImageScaler.halve(tiny, 3);
        check("halving never produces a zero-sized image",
                halvedTiny.getWidth() >= 1 && halvedTiny.getHeight() >= 1,
                "got " + halvedTiny.getWidth() + "x" + halvedTiny.getHeight());
    }


    /* Gumawa ng samples */
    private static void writeProofImages() throws IOException {
        section("PROOF IMAGES");

        new File("test-images").mkdirs();
        new File("outputs").mkdirs();

        BufferedImage chart = colourChart();
        save(chart, "test-images/color-chart.png");

        save(chart, "outputs/00_source_color.png");
        save(ImageOps.toGrayscale(chart, GrayscaleMethod.AVERAGE),    "outputs/01_grayscale_average.png");
        save(ImageOps.toGrayscale(chart, GrayscaleMethod.LUMINOSITY), "outputs/02_grayscale_luminosity.png");
        save(ImageOps.toGrayscale(chart, GrayscaleMethod.LIGHTNESS),  "outputs/03_grayscale_lightness.png");
        save(ImageOps.flipHorizontal(chart),                          "outputs/04_flip_horizontal.png");
        save(ImageOps.flipVertical(chart),                            "outputs/05_flip_vertical.png");
        save(ImageOps.flipVertical(ImageOps.flipHorizontal(chart)),   "outputs/06_flip_both_180.png");
    }


    /* Mga sample pixel */
    private static BufferedImage gridImage() {
        int[] colours = {
                0xFFFF0000, 0xFF00FF00, 0xFF0000FF,   // Unang hanay
                0x80FFFF00, 0xC000FFFF, 0x40FF00FF    // Pangalawang hanay
        };
        BufferedImage img = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 3, 2, colours, 0, 3);
        return img;
    }

    /* Chart ng kulay */
    private static BufferedImage colourChart() {
        final int cols = 6, swatch = 120, rows = 2, ramp = 70, labelBand = 26;
        final int w = cols * swatch;
        final int h = rows * swatch + ramp;

        Color[] saturated = {
                new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
                new Color(0, 255, 255), new Color(255, 0, 255), new Color(255, 255, 0)
        };
        Color[] muted = {
                new Color(200, 100, 50),  new Color(60, 120, 70),  new Color(230, 190, 160),
                new Color(40, 70, 130),   new Color(150, 150, 150), new Color(90, 40, 110)
        };

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        for (int i = 0; i < cols; i++) {
            paintSwatch(g, saturated[i], i * swatch, 0, swatch, swatch, labelBand);
            paintSwatch(g, muted[i], i * swatch, swatch, swatch, swatch, labelBand);
        }

        /* Black-white na gradient */
        for (int x = 0; x < w; x++) {
            g.setColor(new Color(x * 255 / (w - 1), x * 255 / (w - 1), x * 255 / (w - 1)));
            g.fillRect(x, rows * swatch, 1, ramp);
        }

        g.dispose();
        return img;
    }

    /* Kulayan ang swatch */
    private static void paintSwatch(Graphics2D g, Color c, int x, int y, int w, int h, int band) {
        g.setColor(c);
        g.fillRect(x, y, w, h);

        /* Malinaw na label */
        int grey = GrayscaleMethod.LUMINOSITY.toGray(c.getRed(), c.getGreen(), c.getBlue());
        g.setColor(grey > 140 ? Color.BLACK : Color.WHITE);
        g.drawString(String.format("R%d G%d B%d", c.getRed(), c.getGreen(), c.getBlue()),
                x + 8, y + h - band + 4);
    }


    private static void check(String name, int expected, int actual) {
        boolean ok = expected == actual;
        record(ok, name, ok ? "" : String.format("expected %d, got %d", expected, actual));
    }

    private static void check(String name, boolean condition, String detail) {
        record(condition, name, condition ? "" : detail);
    }

    private static void record(boolean ok, String name, String detail) {
        if (ok) {
            passed++;
            System.out.printf("   PASS  %s%n", name);
        } else {
            failed++;
            System.out.printf("   FAIL  %s  (%s)%n", name, detail);
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(" " + title);
    }

    private static String pad(String s) {
        return String.format("%-10s", s);
    }

    /* Ihambing bawat pixel */
    private static boolean identical(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* Kalahating ikot */
    private static BufferedImage rotate180(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, src.getRGB(w - 1 - x, h - 1 - y));
            }
        }
        return out;
    }

    private static boolean allChannelsEqual(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int px = img.getRGB(x, y);
                int r = (px >> 16) & 0xFF, g = (px >> 8) & 0xFF, b = px & 0xFF;
                if (r != g || g != b) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean alphaPreserved(BufferedImage src, BufferedImage out) {
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                if ((src.getRGB(x, y) >>> 24) != (out.getRGB(x, y) >>> 24)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void save(BufferedImage img, String path) throws IOException {
        ImageIO.write(img, "png", new File(path));
        System.out.println("   saved -> " + path);
    }
}
