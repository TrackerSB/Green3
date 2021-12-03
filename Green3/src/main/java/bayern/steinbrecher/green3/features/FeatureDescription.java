package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.util.Collection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public record FeatureDescription(
        @NonNull String nameKey,
        String imageFileName,
        boolean mandatory,
        @NonNull Collection<Feature> subFeatures) { // FIXME How to force sub features are subtypes of parent feature?
    private static final Logger LOGGER = Logger.getLogger(FeatureDescription.class.getName());
    private static final ResourceBundle resources
            = ResourceBundle.getBundle("bayern.steinbrecher.green3.features.Features");

    public FeatureDescription {
        boolean containsMandatorySubFeatures = subFeatures.stream()
                .map(Feature::getDescription)
                .anyMatch(FeatureDescription::mandatory);
        assert !containsMandatorySubFeatures || mandatory
                : "If any sub features is mandatory the parent features has to be mandatory as well";
    }

    @NonNull
    public String getName() {
        try {
            return resources.getString(nameKey());
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.WARNING, "Could not get name of feature", ex);
            return nameKey();
        }
    }
}
