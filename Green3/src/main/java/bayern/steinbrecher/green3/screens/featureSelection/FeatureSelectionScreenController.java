package bayern.steinbrecher.green3.screens.featureSelection;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
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

    @FXML
    private void initialize() {
        featureTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(FeatureTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDescription());
                    setGraphic(item.getImage());
                }
            }
        });

        TreeItem<FeatureTreeItem> root = new FeatureLevelNode(Feature.class).generateTreeItem();
        featureTree.setRoot(root);

        Map<Class<? extends Feature>, TreeItem<FeatureTreeItem>> featureSubTrees = new HashMap<>();
        featureSubTrees.put(Feature.class, root);
        FeatureRegistry.findSub(Feature.class)
                .forEach(f -> {
                    Class<? extends Feature> currentFeatureClass = f.getClass();
                    TreeItem<FeatureTreeItem> currentFeatureItem = new FeatureLeaf(f).generateTreeItem();
                    while (!featureSubTrees.containsKey(currentFeatureClass)) {
                        TreeItem<FeatureTreeItem> newFeatureLevel
                                = new FeatureLevelNode(currentFeatureClass).generateTreeItem();
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

        protected abstract URL getImageURL();

        public final ImageView getImage() {
            if (getImageURL() == null) {
                return null;
            }
            return new ImageView(new Image(getImageURL().toExternalForm(), 30d, 30d, true, true));
        }

        public final TreeItem<FeatureTreeItem> generateTreeItem() {
            TreeItem<FeatureTreeItem> item = new TreeItem<>(this);
            item.setGraphic(getImage());
            item.setExpanded(true);
            return item;
        }
    }

    private static class FeatureLevelNode extends FeatureTreeItem {
        private final Class<? extends Feature> featureType;

        public FeatureLevelNode(@NonNull Class<? extends Feature> featureType) {
            this.featureType = featureType;
        }

        public Class<? extends Feature> getFeatureType() {
            return featureType;
        }

        @Override
        public String getDescription() {
            try {
                Method getFeatureSetDescription = getFeatureType().getMethod("getFeatureSetDescription");
                return (String) getFeatureSetDescription.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, "Could not find feature set description", ex);
                return String.format("<Not provided for \"%s\">", getFeatureType().getName());
            }
        }

        @Override
        public URL getImageURL() {
            try {
                Method getFeatureSetImageURL = getFeatureType().getMethod("getFeatureSetImageURL");
                return (URL) getFeatureSetImageURL.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, "Could not find feature set icon", ex);
                return null;
            }
        }
    }

    private static class FeatureLeaf extends FeatureTreeItem {
        private final Feature feature;

        public FeatureLeaf(@NonNull Feature feature) {
            this.feature = feature;
        }

        public Feature getFeature() {
            return feature;
        }

        @Override
        public String getDescription() {
            return getFeature()
                    .getDescription()
                    .name();
        }

        @Override
        public URL getImageURL() {
            return getFeature()
                    .getDescription()
                    .image();
        }
    }
}
