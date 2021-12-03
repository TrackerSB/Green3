package bayern.steinbrecher.green3.screens.profileSettings;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;

import java.net.URL;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class ProfileSettingsScreenController extends ScreenController {
    @FXML
    private TreeView<Feature> featureTree;

    @FXML
    private void initialize() {
        featureTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(Feature item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    CheckBox itemBox = new CheckBox(item.getDescription().getName());
                    URL imageURL = item.getImageURL();
                    if (imageURL != null) {
                        itemBox.setGraphic(new ImageView(
                                new Image(imageURL.toExternalForm(), 30d, 30d, true, true)));
                    }
                    itemBox.setAllowIndeterminate(false);
                    itemBox.setDisable(item.getDescription().mandatory());
                    itemBox.selectedProperty()
                            .bindBidirectional(item.enabledProperty());
                    setGraphic(itemBox);
                }
            }
        });

        TreeItem<Feature> root = generateTreeItem(FeatureRegistry.ROOT);
        featureTree.setRoot(root);
    }

    @NonNull
    private TreeItem<Feature> generateTreeItem(@NonNull Feature feature) {
        TreeItem<Feature> treeItem = new TreeItem<>(feature);
        treeItem.getChildren().addAll(
                feature.getDescription()
                        .subFeatures()
                        .stream()
                        .map(this::generateTreeItem)
                        .toList());
        treeItem.setExpanded(true);
        return treeItem;
    }
}
