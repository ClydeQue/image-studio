package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;

import javax.swing.AbstractButton;
import javax.swing.Action;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Magkasabay na controls
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

    /* Irehistro ang grayscale */
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

    /* Kailangan ng edits */
    void bindNeedsModification(AbstractButton control) {
        needsModification.add(control);
    }

    void bindActionNeedsImage(Action action) {
        actionsNeedingImage.add(action);
    }

    void bindActionNeedsModification(Action action) {
        actionsNeedingModification.add(action);
    }

    /* Hintayin ang render */
    void bindActionNeedsSavable(Action action) {
        actionsNeedingSavable.add(action);
    }

    /* I-update ang controls */
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
