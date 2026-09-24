package imagestudio.io;

import java.io.File;
import java.util.Locale;

/**
 * ============================================================================
 *  SaveFileNaming
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Decides what a saved file is called and, from that, what format
 *            it is written in.
 *
 *            Pure string handling, deliberately kept apart from the dialogs in
 *            ImageFileService. Everything in that class needs a window and a
 *            user to exercise it; everything here can be checked by SelfTest
 *            in a fraction of a second, and these are the parts with the
 *            fiddly cases.
 *
 *  THE RULES, IN ORDER OF PRIORITY
 *            1. An extension the user typed wins, so somebody who types
 *               "photo.jpg" gets a JPEG whatever the dropdown says.
 *            2. Otherwise the format chosen in the dropdown decides, and its
 *               extension is appended.
 *            3. Only png, jpg and jpeg count as "typed an extension". An
 *               extension this program cannot write, like .gif, must not be
 *               mistaken for a format request, so it gains a real extension on
 *               the end rather than being honoured.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class SaveFileNaming {

    /** The formats this program can write. Anything else is not an extension. */
    private static final String[] WRITABLE = {"png", "jpg", "jpeg"};

    private SaveFileNaming() {
    }

    /**
     * Appends the chosen extension when the user did not type a usable one, so
     * "beach" becomes "beach.png" instead of an extensionless file the system
     * cannot open.
     *
     * @param file     the file the user chose or typed
     * @param fallback the extension to append, from the format dropdown
     * @return the file to actually write, always with a usable extension
     */
    public static File withExtension(File file, String fallback) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String extension : WRITABLE) {
            if (name.endsWith("." + extension)) {
                return file;                     // the user was explicit; respect it
            }
        }
        return new File(file.getParentFile(), file.getName() + "." + fallback);
    }

    /**
     * Swaps a usable extension for a different one, leaving any other kind of
     * name untouched apart from the new extension on the end.
     * <p>
     * Used to keep the filename in the save dialog honest: the box is
     * pre-filled with a suggestion ending in .png, so switching the format
     * dropdown to JPEG has to rewrite that suggestion. Without this the user
     * picks JPEG, the pre-filled .png is read as them having typed it, and
     * they silently get a PNG.
     *
     * @param file      the current filename
     * @param extension the extension it should end with
     */
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

    /**
     * The format to write, read back from the final filename.
     *
     * @return the lower-cased extension, or "png" when there is none
     */
    public static String extensionOf(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1) : "png";
    }

    /** JPEG answers to two extensions, and both have to be recognised. */
    public static boolean isJpeg(String format) {
        return "jpg".equals(format) || "jpeg".equals(format);
    }
}
