package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;

import javax.swing.AbstractButton;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  ControlBindings
 *  ---------------------------------------------------------------------------
 *  PURPOSE : One register of every control in the window, and one method that
 *            pushes the controller's state out to all of them.
 *
 *  WHY THIS EXISTS
 *            The project asks for a menu-driven interface AND buttons for each
 *            method, so every operation appears twice. Two copies of a control
 *            are two chances for them to disagree, which is exactly the kind
 *            of bug that makes an interface feel broken.
 *
 *            So the menu bar and the toolbar both register whatever they build
 *            here, grouped by what the control means rather than by what widget
 *            it is. sync() is then the single place that decides what is
 *            enabled and what is selected, and neither builder needs to know
 *            the other exists.
 *
 *            Everything is held as AbstractButton, the shared supertype of
 *            JToggleButton, JRadioButtonMenuItem and JCheckBoxMenuItem, so a
 *            menu item and a toolbar button are treated identically.
 *
 *  Author : Clyde
 * ============================================================================
 */
final class ControlBindings {

    private final Map<GrayscaleMethod, List<AbstractButton>> grayscale =
            new EnumMap<>(GrayscaleMethod.class);
    private final List<AbstractButton> flipHorizontal = new ArrayList<>();
    private final List<AbstractButton> flipVertical = new ArrayList<>();

    private final List<AbstractButton> needsImage = new ArrayList<>();
    private final List<AbstractButton> needsModification = new ArrayList<>();
    private final List<Action> actionsNeedingImage = new ArrayList<>();
    private final List<Action> actionsNeedingModification = new ArrayList<>();
    private final List<Action> actionsNeedingSavable = new ArrayList<>();

    /** Registers a control that selects one grayscale method. */
    void bindGrayscale(GrayscaleMethod method, AbstractButton control) {
        grayscale.computeIfAbsent(method, key -> new ArrayList<>()).add(control);
        needsImage.add(control);
    }

    void bindFlipHorizontal(AbstractButton control) {
        flipHorizontal.add(control);
        needsImage.add(control);
    }

    void bindFlipVertical(AbstractButton control) {
        flipVertical.add(control);
        needsImage.add(control);
    }

    /** A control that only makes sense once something has been applied. */
    void bindNeedsModification(AbstractButton control) {
        needsModification.add(control);
    }

    void bindActionNeedsImage(Action action) {
        actionsNeedingImage.add(action);
    }

    void bindActionNeedsModification(Action action) {
        actionsNeedingModification.add(action);
    }

    /** Save, which additionally has to wait for a render to finish. */
    void bindActionNeedsSavable(Action action) {
        actionsNeedingSavable.add(action);
    }

    /**
     * Pushes the controller's state out to every registered control.
     * <p>
     * Called after any change, which is what guarantees the menu, the toolbar
     * and the status bar can never disagree about what is applied. It also
     * disables everything with nothing to act on, so no control in this
     * program is ever clickable but inert.
     * <p>
     * Note that setSelected fires an ItemListener but never an ActionListener.
     * Every handler in this program is an ActionListener, so this method can
     * set controls freely without triggering the handlers that led here.
     */
    void sync(StudioController controller) {
        boolean hasImage = controller.hasImage();
        boolean modified = controller.isModified();

        needsImage.forEach(control -> control.setEnabled(hasImage));
        needsModification.forEach(control -> control.setEnabled(modified));
        actionsNeedingImage.forEach(action -> action.setEnabled(hasImage));
        actionsNeedingModification.forEach(action -> action.setEnabled(modified));
        actionsNeedingSavable.forEach(action -> action.setEnabled(controller.canSave()));

        GrayscaleMethod active = controller.grayscale();
        grayscale.forEach((method, controls) ->
                controls.forEach(control -> control.setSelected(method == active)));

        flipHorizontal.forEach(c -> c.setSelected(controller.isFlippedHorizontally()));
        flipVertical.forEach(c -> c.setSelected(controller.isFlippedVertically()));
    }
}
