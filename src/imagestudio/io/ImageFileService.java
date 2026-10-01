package imagestudio.io;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/* Pagbasa at pagsave */
public final class ImageFileService {

    public BufferedImage readImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("\"" + file.getName() + "\" is not an image Java can read.\n"
                    + "Supported formats are PNG, JPG, GIF and BMP.");
        }
        return normalise(image);
    }

    /* Gawing ARGB format */
    private BufferedImage normalise(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    public void saveImage(BufferedImage image, File target) throws IOException {
        String format = SaveFileNaming.extensionOf(target);
        if (!"png".equals(format) && !SaveFileNaming.isJpeg(format)) {
            throw new IOException("Choose a PNG or JPG filename.");
        }
        // Puting JPEG background
        BufferedImage output = SaveFileNaming.isJpeg(format) ? flattenOntoWhite(image) : image;
        Path destination = target.toPath().toAbsolutePath();
        Path temporary = Files.createTempFile(destination.getParent(), ".image-studio-", ".tmp");
        try {
            // Tapusin bago palitan
            try (var stream = Files.newOutputStream(temporary)) {
                if (!ImageIO.write(output, format, stream)) {
                    throw new IOException("No image writer is available for " + format + ".");
                }
            }
            try {
                Files.move(temporary, destination,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }


    /* Patungan ng puti */
    private BufferedImage flattenOntoWhite(BufferedImage src) {
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return out;
    }

}
