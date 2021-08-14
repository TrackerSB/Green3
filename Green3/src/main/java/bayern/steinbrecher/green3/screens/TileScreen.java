package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.green3.features.TileScreenFeature;
import bayern.steinbrecher.screenswitcher.Screen;
import lombok.NonNull;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class TileScreen extends Screen<TileScreenController> {
    private final Class<? extends TileScreenFeature> featureSet;

    protected TileScreen(@NonNull ResourceBundle resourceBundle,
                         @NonNull Class<? extends TileScreenFeature> featureSet) {
        super(TileScreen.class.getResource("TileScreen.fxml"), resourceBundle);
        this.featureSet = featureSet;
    }

    @Override
    protected void afterControllerIsInitialized(@NonNull TileScreenController controller) {
        super.afterControllerIsInitialized(controller);
        controller.populateWithTiles(featureSet);
    }
}
