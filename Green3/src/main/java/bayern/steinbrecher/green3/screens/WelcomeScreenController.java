package bayern.steinbrecher.green3.screens;

import bayern.steinbrecher.green3.elements.ImageButton;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.green3.features.WelcomeScreenFeature;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;
import lombok.NonNull;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class WelcomeScreenController extends ScreenController {
    @FXML
    private TilePane menu;
    @FXML
    private ResourceBundle resources;

    private @NonNull ImageButton generateMenuEntry(@NonNull WelcomeScreenFeature feature) {
        ImageButton menuEntry = new ImageButton();
        menuEntry.setText(resources.getString(feature.getDescriptionResourceKey()));
        menuEntry.setImageUrl(feature.getImageURL().toExternalForm());
        menuEntry.setOnAction(mevt -> feature.getAction().run());
        return menuEntry;
    }

    @FXML
    private void initialize() {
        FeatureRegistry.find(WelcomeScreenFeature.class)
                .stream()
                .map(this::generateMenuEntry)
                .forEach(entry -> menu.getChildren().add(entry));
    }
}
