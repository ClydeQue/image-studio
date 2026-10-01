package imagestudio.ui;

import imagestudio.io.SaveFileNaming;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;

// Pagpili ng file
final class ImageFileDialogs {

    private final Component owner;
    private File lastDirectory;

    ImageFileDialogs(Component owner) {
        this.owner = owner;
    }

    File chooseImportFile() {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Import Image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (PNG, JPG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));

        if (chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastDirectory = chooser.getCurrentDirectory();
        return chooser.getSelectedFile();
    }

    void rememberDirectory(File file) {
        lastDirectory = file.getAbsoluteFile().getParentFile();
    }

    File chooseSaveFile(String suggestedName) {
        FileNameExtensionFilter png = new FileNameExtensionFilter("PNG image (*.png)", "png");
        FileNameExtensionFilter jpg = new FileNameExtensionFilter("JPEG image (*.jpg)", "jpg", "jpeg");
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Save Image As");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(png);
        chooser.addChoosableFileFilter(jpg);
        chooser.setFileFilter(png);
        chooser.setSelectedFile(new File(suggestedName));

        // Palitan ang extension
        chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, event -> {
            File current = chooser.getSelectedFile();
            if (current != null) {
                chooser.setSelectedFile(SaveFileNaming.withNewExtension(current,
                        chooser.getFileFilter() == jpg ? "jpg" : "png"));
            }
        });

        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        lastDirectory = chooser.getCurrentDirectory();
        File target = SaveFileNaming.withExtension(chooser.getSelectedFile(),
                chooser.getFileFilter() == jpg ? "jpg" : "png");
        if (target.exists() && !confirmOverwrite(target)) {
            return null;
        }
        return target;
    }

    void showError(String title, String message) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmOverwrite(File file) {
        int choice = JOptionPane.showConfirmDialog(owner,
                "\"" + file.getName() + "\" already exists.\nReplace it?",
                "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }
}
