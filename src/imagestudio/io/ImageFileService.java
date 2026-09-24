package imagestudio.io;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ============================================================================
 *  ImageFileService
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Everything to do with files. Opening, saving, choosing formats,
 *            and turning the things that go wrong into sentences a person can
 *            act on.
 *
 *            Keeping this out of MainWindow matters: the window is already
 *            responsible for the layout and the wiring, and file handling is
 *            where most of the awkward cases live.
 *
 *  THE AWKWARD CASES, ALL HANDLED HERE
 *
 *    ImageIO.read returns null rather than throwing when it is handed a file
 *    it cannot decode, so a plain try/catch would let a .txt through as a
 *    silent null and crash later somewhere unrelated.
 *
 *    JPEG has no alpha channel, so ImageIO's JPEG writer cannot accept an
 *    image that has one. Handed a TYPE_INT_ARGB image it gives up quietly:
 *    write() returns false and no file appears at all. Older JDKs were worse
 *    and wrote a corrupted, pink-tinted file instead. Either way the image has
 *    to be flattened onto a background first, and a program that does not do
 *    this has a Save As JPEG button that simply does nothing.
 *
 *    A user who types "beach" into the save box means beach.png. Making them
 *    remember the extension is a small cruelty that is easy to remove.
 *
 *    A large photograph can exhaust the heap. OutOfMemoryError is an Error
 *    rather than an Exception, so it slips past catch(Exception) and takes the
 *    window down with it.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class ImageFileService {

    private static final String[] READABLE = {"png", "jpg", "jpeg", "gif", "bmp"};

    /** Remembered between dialogs so the user does not re-navigate every time. */
    private static File lastDirectory;

    private ImageFileService() {
    }

    /** An image together with the name of the file it came from. */
    public record Loaded(BufferedImage image, String fileName) {
    }

    /* ============================= IMPORT =================================== */

    /**
     * Asks the user for an image file and loads it.
     *
     * @return the loaded image, or null if the user cancelled or the file
     *         could not be read (in which case they have already been told why)
     */
    public static Loaded importImage(Component parent) {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Import Image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (PNG, JPG, GIF, BMP)", READABLE));

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastDirectory = chooser.getCurrentDirectory();
        return readImage(parent, chooser.getSelectedFile());
    }

    /**
     * Loads an image from a known file. Shared by the Import dialog and by
     * drag and drop, so both routes behave identically and report problems the
     * same way.
     *
     * @return the loaded image, or null after telling the user what went wrong
     */
    public static Loaded readImage(Component parent, File file) {
        try {
            BufferedImage raw = ImageIO.read(file);

            /* Null, not an exception, is how ImageIO says "I cannot decode
               this". Catching only exceptions would let a text file through. */
            if (raw == null) {
                error(parent, "Cannot open that file",
                        "\"" + file.getName() + "\" is not an image Java can read.\n\n"
                                + "Supported formats are PNG, JPG, GIF and BMP.");
                return null;
            }

            lastDirectory = file.getParentFile();
            return new Loaded(normalise(raw), file.getName());

        } catch (IOException ex) {
            error(parent, "Cannot open that file",
                    "\"" + file.getName() + "\" could not be read.\n\n" + ex.getMessage());
            return null;

        } catch (RuntimeException ex) {
            /* Decoders are not required to wrap everything they hit. A
               malformed file can surface as ArrayIndexOutOfBoundsException or
               IllegalArgumentException from deep inside a reader, and an
               unchecked exception let loose here escapes to the event dispatch
               thread, where the user sees nothing at all. Truncated files do
               come back as IIOException and are handled above; this is for the
               ones that do not. */
            error(parent, "Cannot open that file",
                    "\"" + file.getName() + "\" could not be decoded.\n\n"
                            + "The file may be damaged or incomplete.");
            return null;

        } catch (OutOfMemoryError ex) {
            /* An Error, not an Exception, so it would otherwise escape and kill
               the window. Caught here so the program survives a huge photo. */
            error(parent, "Image too large",
                    "\"" + file.getName() + "\" needs more memory than is available.\n\n"
                            + "Try a smaller image, or start the program with a larger\n"
                            + "heap, for example:  java -Xmx2g -cp classes imagestudio.ImageStudio");
            return null;
        }
    }

    /**
     * Redraws any incoming image as TYPE_INT_ARGB.
     * <p>
     * Images arrive in whatever colour model their format uses. A GIF is
     * indexed to a 256-colour palette, a JPEG has no alpha at all. Converting
     * once on the way in means every operation downstream works on the same
     * predictable layout, and a grey level computed by the program can never be
     * quietly re-quantised to the nearest palette entry on the way out.
     */
    private static BufferedImage normalise(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    /* ============================== SAVE =================================== */

    /**
     * Asks the user where to save, then writes the image there.
     *
     * @param image         the rendered image, exactly as shown in the preview
     * @param suggestedName a filename describing the recipe, offered as default
     * @return the file written, or null if the user cancelled or backed out of
     *         overwriting
     */
    public static File saveImage(Component parent, BufferedImage image, String suggestedName) {
        FileNameExtensionFilter png = new FileNameExtensionFilter("PNG image (*.png)", "png");
        FileNameExtensionFilter jpg = new FileNameExtensionFilter("JPEG image (*.jpg)", "jpg", "jpeg");

        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Save Image As");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(png);
        chooser.addChoosableFileFilter(jpg);
        chooser.setFileFilter(png);                       // PNG is lossless, so it is the default
        chooser.setSelectedFile(new File(suggestedName));

        /* Keep the filename in step with the format dropdown. The box starts
           pre-filled with a .png suggestion, so without this a user who picks
           JPEG and presses Save would have that pre-filled .png read back as a
           deliberate choice and would silently get a PNG. */
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, event -> {
            File current = chooser.getSelectedFile();
            if (current != null) {
                chooser.setSelectedFile(SaveFileNaming.withNewExtension(current,
                        chooser.getFileFilter() == jpg ? "jpg" : "png"));
            }
        });

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastDirectory = chooser.getCurrentDirectory();

        boolean wantsJpeg = chooser.getFileFilter() == jpg;
        File target = SaveFileNaming.withExtension(
                chooser.getSelectedFile(), wantsJpeg ? "jpg" : "png");
        String format = SaveFileNaming.extensionOf(target);

        if (target.exists() && !confirmOverwrite(parent, target)) {
            return null;
        }

        try {
            /* JPEG cannot store transparency, and ImageIO's writer will not
               accept an image that carries an alpha channel: it returns false
               and writes nothing. Flattening onto white first is what makes
               Save As JPEG work at all. Verified on this JDK. */
            BufferedImage toWrite = SaveFileNaming.isJpeg(format) ? flattenOntoWhite(image) : image;

            if (!ImageIO.write(toWrite, format, target)) {
                error(parent, "Cannot save that format",
                        "No writer is available for \"" + format + "\" files.\n\n"
                                + "Try saving as PNG instead.");
                return null;
            }
            return target;

        } catch (IOException ex) {
            error(parent, "Cannot save the image",
                    "\"" + target.getName() + "\" could not be written.\n\n" + ex.getMessage());
            return null;
        }
    }

    /* ---------------------- helper methods ---------------------------------- */

    /**
     * Composites the image over white so no transparency reaches the writer.
     * <p>
     * White is the right background here because these are photographs being
     * exported, and a transparent region flattened to black would read as real
     * image content rather than as empty space.
     */
    private static BufferedImage flattenOntoWhite(BufferedImage src) {
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    private static boolean confirmOverwrite(Component parent, File file) {
        int choice = JOptionPane.showConfirmDialog(parent,
                "\"" + file.getName() + "\" already exists.\nReplace it?",
                "File exists",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    /** One place for problem messages, so they all read the same way. */
    private static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
