package bayern.steinbrecher.green3.screens.profileSettings;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureDescription;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class ProfileSettingsScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(ProfileSettingsScreenController.class.getName());
    @FXML
    private TreeView<FeatureTreeItem> featureTree;

    @FXML
    private void initialize() {
        featureTree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(FeatureTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    CheckBox itemBox = new CheckBox(item.getDescription());
                    itemBox.setGraphic(item.getImage());
                    itemBox.setAllowIndeterminate(true);
                    item.isEnabled().ifPresentOrElse(itemBox::setSelected, () -> itemBox.setIndeterminate(true));
                    itemBox.setDisable(item.isMandatory());
                    setGraphic(itemBox);
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

        public abstract Optional<Boolean> isEnabled();

        public abstract boolean isMandatory();

        public final TreeItem<FeatureTreeItem> generateTreeItem() {
            TreeItem<FeatureTreeItem> item = new TreeItem<>(this);
            item.setExpanded(true);
            return item;
        }
    }

    private static class FeatureLevelNode extends FeatureTreeItem {
        @NonNull
        private final Class<? extends Feature> featureType;

        public FeatureLevelNode(@NonNull Class<? extends Feature> featureType) {
            this.featureType = featureType;
        }

        @NonNull
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

        @Override
        public Optional<Boolean> isEnabled() {
            Collection<? extends Feature> subFeatures = FeatureRegistry.findSub(getFeatureType());
            Boolean enabledState;

            boolean allEnabled = subFeatures.stream().allMatch(Feature::isEnabled);
            if (allEnabled) {
                enabledState = true;
            } else {
                boolean noneEnabled = subFeatures.stream().noneMatch(Feature::isEnabled);
                enabledState = noneEnabled ? false : null;
            }

            return Optional.ofNullable(enabledState);
        }

        @Override
        public boolean isMandatory() {
            return FeatureRegistry.findSub(getFeatureType())
                    .stream()
                    .map(Feature::getDescription)
                    .allMatch(FeatureDescription::mandatory);
        }
    }

    private static class FeatureLeaf extends FeatureTreeItem {
        @NonNull
        private final Feature feature;

        public FeatureLeaf(@NonNull Feature feature) {
            this.feature = feature;
        }

        @NonNull
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

        @Override
        public Optional<Boolean> isEnabled() {
            return Optional.of(getFeature().isEnabled());
        }

        @Override
        public boolean isMandatory() {
            return getFeature()
                    .getDescription()
                    .mandatory();
        }
    }
}
