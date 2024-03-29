package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.green3.elements.ImageButton;
import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.TileScreenFeature;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;
import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class TileScreenController extends ScreenController {
    @FXML
    private TilePane menu;

    @NonNull
    private ImageButton generateMenuEntry(@NonNull TileScreenFeature feature) {
        ImageButton menuEntry = new ImageButton();
        menuEntry.setText(feature.getDescription().getName());
        menuEntry.setImageUrl(feature.getImageURL().toExternalForm());
        menuEntry.setOnAction(mevt -> feature.getAction().accept(getScreenManager()));
        return menuEntry;
    }

    /**
     * This method is to be called by {@link TileScreen} only.
     * FIXME Avoid calling this method manually by providing the {@code featureSet} parameter to the controller
     * directly.
     */
    void populateWithTiles(@NonNull Feature rootFeature) {
        rootFeature.getDescription()
                .subFeatures()
                .stream()
                .filter(f -> f instanceof TileScreenFeature)
                .map(f -> (TileScreenFeature) f)
                .map(this::generateMenuEntry)
                .forEach(entry -> menu.getChildren().add(entry));
    }
}
