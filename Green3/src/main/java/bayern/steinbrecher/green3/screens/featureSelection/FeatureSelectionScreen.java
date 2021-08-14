package bayern.steinbrecher.green3.screens.featureSelection;

import bayern.steinbrecher.screenswitcher.Screen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class FeatureSelectionScreen extends Screen<FeatureSelectionScreenController> {
    public FeatureSelectionScreen() {
        super(
                FeatureSelectionScreen.class.getResource("FeatureSelectionScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.featureSelection.FeatureSelectionScreen")
        );
    }
}
