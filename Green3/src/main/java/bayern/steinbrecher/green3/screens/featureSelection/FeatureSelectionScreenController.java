package bayern.steinbrecher.green3.screens.featureSelection;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class FeatureSelectionScreenController extends ScreenController {
    @FXML
    private TreeView<Feature> featureTree;

    @FXML
    private void initialize() {
        featureTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(Feature item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDescription().name());
                }
            }
        });

        TreeItem<Feature> root = new TreeItem<>();
        root.setExpanded(true);
        featureTree.setRoot(root);

        Map<Class<? extends Feature>, TreeItem<Feature>> featureSubTrees = new HashMap<>();
        featureSubTrees.put(Feature.class, root);
        FeatureRegistry.findSub(Feature.class)
                .forEach(f -> {
                    Class<? extends Feature> currentFeatureClass = f.getClass();
                    TreeItem<Feature> currentFeatureItem = new TreeItem<>(f);
                    while (!featureSubTrees.containsKey(currentFeatureClass)) {
                        TreeItem<Feature> newFeatureLevel = new TreeItem<>();
                        newFeatureLevel.setExpanded(true);
                        newFeatureLevel.getChildren()
                                .add(currentFeatureItem);
                        TreeItem<Feature> removedItem = featureSubTrees.put(currentFeatureClass, newFeatureLevel);
                        assert removedItem == null : "Replaced already existing entry for feature class";

                        currentFeatureClass = (Class<? extends Feature>) currentFeatureClass.getSuperclass();
                        currentFeatureItem = newFeatureLevel;
                    }
                    featureSubTrees.get(currentFeatureClass)
                            .getChildren()
                            .add(currentFeatureItem);
                });
    }
}
