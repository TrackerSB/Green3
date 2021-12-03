package bayern.steinbrecher.green3.data;

import bayern.steinbrecher.green3.features.Feature;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class Profile {
    private static final Logger LOGGER = Logger.getLogger(Profile.class.getName());
    private static final Preferences APP_ROOT_PREF_NODE = Preferences.userRoot().node("/bayern/steinbrecher/green3");
    private static Profile CURRENT_PROFILE = null;
    private String name;
    private Preferences rootNode;
    private Preferences featuresNode;

    private Profile(@NonNull String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    private void setName(@NonNull String name) {
        this.name = name;
        rootNode = APP_ROOT_PREF_NODE.node(name);
        featuresNode = rootNode.node("features");
    }

    public static void loadFeatureSettings(@NonNull Preferences featureNode, @NonNull Feature feature)
            throws BackingStoreException {
        for (String subFeatureNodeName : featureNode.childrenNames()) {
            Optional<Feature> subFeature = feature.findSubFeature(subFeatureNodeName);
            if (subFeature.isPresent()) {
                Preferences subFeatureNode = featureNode.node(subFeatureNodeName);
                if (Arrays.asList(subFeatureNode.keys()).contains("enabled")) {
                    subFeature.get()
                            .setEnabled(subFeatureNode.getBoolean("enabled", false));
                } else {
                    LOGGER.log(Level.WARNING,
                            String.format("The feature preference node \"%s\" does not specify whether the feature "
                                    + "has to be enabled. Falling back to default.", featureNode.absolutePath()));
                }
                loadFeatureSettings(subFeatureNode, subFeature.get());
            } else {
                LOGGER.log(Level.WARNING,
                        String.format("Could not find sub feature of name \"%s\" (in preferences node \"%s\"). "
                                        + "Skipping feature preference.",
                                subFeatureNodeName, featureNode.absolutePath()));
            }
        }
    }

    public static void loadProfile(@NonNull String name) {
        if (CURRENT_PROFILE != null) {
            storeProfile();
        }
        CURRENT_PROFILE = new Profile(name);
        try {
            loadFeatureSettings(CURRENT_PROFILE.featuresNode, FeatureRegistry.ROOT);
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE,
                    "Could not load any feature preferences. Falling back to default feature preferences", ex);
        }
    }

    private static void storeFeatureSettings(@NonNull Preferences featureNode, @NonNull Feature feature) {
        featureNode.putBoolean("enabled", feature.isEnabled());
        for (Feature subFeature : feature.getDescription().subFeatures()) {
            Preferences subFeatureNode = featureNode.node(subFeature.getDescription().nameKey());
            storeFeatureSettings(subFeatureNode, subFeature);
        }
    }

    public static void storeProfile() {
        storeFeatureSettings(CURRENT_PROFILE.featuresNode, FeatureRegistry.ROOT);
    }
}
