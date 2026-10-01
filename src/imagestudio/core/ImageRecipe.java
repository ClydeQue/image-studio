package imagestudio.core;

import java.awt.image.BufferedImage;
import java.util.Objects;

// Nakapirming mga edits
public record ImageRecipe(GrayscaleMethod grayscale, boolean flipHorizontal, boolean flipVertical) {

    public ImageRecipe {
        Objects.requireNonNull(grayscale, "grayscale");
    }

    public BufferedImage applyTo(BufferedImage original) {
        BufferedImage result = ImageOps.toGrayscale(original, grayscale);
        if (flipHorizontal) {
            result = ImageOps.flipHorizontal(result);
        }
        if (flipVertical) {
            result = ImageOps.flipVertical(result);
        }
        return result;
    }
}
