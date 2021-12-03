package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.screenswitcher.Screen;
import lombok.NonNull;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class TileScreen extends Screen<TileScreenController> {
    @NonNull
    private final Feature rootFeature;

    protected TileScreen(@NonNull ResourceBundle resourceBundle,
                         @NonNull Feature rootFeature) {
        super(TileScreen.class.getResource("TileScreen.fxml"), resourceBundle);
        // FIXME How to force all sub features are TileScreenFeatures?
        this.rootFeature = rootFeature;
    }

    @Override
    protected void afterControllerIsInitialized(@NonNull TileScreenController controller) {
        super.afterControllerIsInitialized(controller);
        controller.populateWithTiles(rootFeature);
    }
}
