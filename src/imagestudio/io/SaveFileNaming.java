package imagestudio.io;

import java.io.File;
import java.util.Locale;

// Ayusin ang filename
public final class SaveFileNaming {

    /* Suportadong mga format */
    private static final String[] WRITABLE = {"png", "jpg", "jpeg"};

    private SaveFileNaming() {
    }

    /* Dagdagan ang extension */
    public static File withExtension(File file, String fallback) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String extension : WRITABLE) {
            if (name.endsWith("." + extension)) {
                return file;                     // Panatilihin ang extension
            }
        }
        return new File(file.getParentFile(), file.getName() + "." + fallback);
    }

    /* Palitan ang extension */
    public static File withNewExtension(File file, String extension) {
        String name = file.getName();
        String lower = name.toLowerCase(Locale.ROOT);
        for (String known : WRITABLE) {
            if (lower.endsWith("." + known)) {
                name = name.substring(0, name.length() - known.length() - 1);
                break;
            }
        }
        return new File(file.getParentFile(), name + "." + extension);
    }

    /* Basahin ang extension */
    public static String extensionOf(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1) : "png";
    }

    /* Mga JPEG extension */
    public static boolean isJpeg(String format) {
        return "jpg".equals(format) || "jpeg".equals(format);
    }
}
