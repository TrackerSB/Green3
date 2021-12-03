package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.net.URL;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    private final FeatureDescription description;
    private boolean enabled;

    protected Feature(@NonNull FeatureDescription description, boolean enabled) {
        this.description = description;
        this.enabled = enabled;
    }

    public FeatureDescription getDescription() {
        return description;
    }

    @NonNull
    protected abstract Class<?> getAssociatedClass();

    public URL getImageURL() {
        if (getDescription().imageFileName() == null
                || getDescription().imageFileName().isBlank()) {
            return null;
        }
        return getAssociatedClass()
                .getResource(getDescription().imageFileName());
    }

    public Optional<Boolean> isEnabled() {
        if (getDescription().subFeatures().isEmpty()) {
            return Optional.of(enabled);
        }
        boolean allSubFeaturesEnabled = getDescription()
                .subFeatures()
                .stream()
                .map(Feature::isEnabled)
                .allMatch(e -> e.orElse(false));
        if (allSubFeaturesEnabled) {
            return Optional.of(true);
        }
        boolean allSubFeaturesDisabled = getDescription()
                .subFeatures()
                .stream()
                .map(Feature::isEnabled)
                .noneMatch(e -> e.orElse(true));
        if (allSubFeaturesDisabled) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getDescription()
                .subFeatures()
                .forEach(f -> f.setEnabled(enabled));
    }
}
