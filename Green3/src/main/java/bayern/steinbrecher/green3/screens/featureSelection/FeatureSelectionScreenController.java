package bayern.steinbrecher.green3.screens.featureSelection;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class FeatureSelectionScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(FeatureSelectionScreenController.class.getName());
    @FXML
    private TreeView<FeatureTreeItem> featureTree;

    private TreeItem<FeatureTreeItem> generateTreeItem(Feature feature) {
        TreeItem<FeatureTreeItem> item = new TreeItem<>(new FeatureLeaf(feature));
        item.setExpanded(true);
        return item;
    }

    private TreeItem<FeatureTreeItem> generateTreeItem(Class<? extends Feature> featureType) {
        TreeItem<FeatureTreeItem> item = new TreeItem<>(new FeatureLevelNode(featureType));
        item.setExpanded(true);
        return item;
    }

    @FXML
    private void initialize() {
        featureTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(FeatureTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDescription());
                }
            }
        });

        TreeItem<FeatureTreeItem> root = generateTreeItem(Feature.class);
        featureTree.setRoot(root);

        Map<Class<? extends Feature>, TreeItem<FeatureTreeItem>> featureSubTrees = new HashMap<>();
        featureSubTrees.put(Feature.class, root);
        FeatureRegistry.findSub(Feature.class)
                .forEach(f -> {
                    Class<? extends Feature> currentFeatureClass = f.getClass();
                    TreeItem<FeatureTreeItem> currentFeatureItem = generateTreeItem(f);
                    while (!featureSubTrees.containsKey(currentFeatureClass)) {
                        TreeItem<FeatureTreeItem> newFeatureLevel = generateTreeItem(currentFeatureClass);
                        newFeatureLevel.getChildren()
                                .add(currentFeatureItem);
                        TreeItem<FeatureTreeItem> removedItem
                                = featureSubTrees.put(currentFeatureClass, newFeatureLevel);
                        assert removedItem == null : "Replaced already existing entry for feature class";

                        currentFeatureClass = (Class<? extends Feature>) currentFeatureClass.getSuperclass();
                        currentFeatureItem = newFeatureLevel;
                    }
                    featureSubTrees.get(currentFeatureClass)
                            .getChildren()
                            .add(currentFeatureItem);
                });
    }

    private static abstract class FeatureTreeItem {
        public abstract String getDescription();
    }

    private static class FeatureLevelNode extends FeatureTreeItem {
        private final Class<? extends Feature> featureType;

        public FeatureLevelNode(Class<? extends Feature> featureType) {
            this.featureType = featureType;
        }

        public Class<? extends Feature> getFeatureType() {
            return featureType;
        }

        @Override
        public String getDescription() {
            try {
                Method getFeatureSetDescription = featureType.getMethod("getFeatureSetDescription");
                return (String) getFeatureSetDescription.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, "Could not find feature set description", ex);
                return String.format("<Not provided for \"%s\">", featureType.getName());
            }
        }
    }

    private static class FeatureLeaf extends FeatureTreeItem {
        private final Feature feature;

        public FeatureLeaf(Feature feature) {
            this.feature = feature;
        }

        public Feature getFeature() {
            return feature;
        }

        @Override
        public String getDescription() {
            return feature.getDescription()
                    .name();
        }
    }
}
