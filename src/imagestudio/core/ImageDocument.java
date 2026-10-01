package imagestudio.core;

import java.awt.image.BufferedImage;
import java.util.Objects;

// Larawan at edits
public final class ImageDocument {

    private final BufferedImage original;
    private final String sourceName;

    private GrayscaleMethod grayscale = GrayscaleMethod.NONE;
    private boolean flipH;
    private boolean flipV;

    /* Kunin ang original */
    public ImageDocument(BufferedImage original, String sourceName) {
        this.original = Objects.requireNonNull(original, "original");
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName");
    }


    /* Ilapat ang edits */
    public BufferedImage render() {
        return recipe().applyTo(original);
    }

    // Kopyahin ang edits
    public ImageRecipe recipe() {
        return new ImageRecipe(grayscale, flipH, flipV);
    }

    /* Ibalik ang original */
    public void reset() {
        grayscale = GrayscaleMethod.NONE;
        flipH = false;
        flipV = false;
    }

    /* May binagong edits */
    public boolean isModified() {
        return grayscale.isConversion() || flipH || flipV;
    }


    public GrayscaleMethod grayscale() {
        return grayscale;
    }

    public void setGrayscale(GrayscaleMethod method) {
        this.grayscale = Objects.requireNonNull(method, "method");
    }

    public boolean isFlippedHorizontally() {
        return flipH;
    }

    public boolean isFlippedVertically() {
        return flipV;
    }

    /* Baliktarin ang estado */
    public void toggleFlipHorizontal() {
        flipH = !flipH;
    }

    /* Baliktarin ang estado */
    public void toggleFlipVertical() {
        flipV = !flipV;
    }


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

    /* Buod ng edits */
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

    /* Mungkahing filename */
    public String suggestedFileName(String extension) {
        String base = sourceName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);       // Alisin ang extension
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
