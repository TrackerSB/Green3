package bayern.steinbrecher.green3.features;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.NonNull;

import java.net.URL;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    private final FeatureDescription description;
    private final BooleanProperty enabled = new SimpleBooleanProperty(true) {
        @Override
        public boolean get() {
            boolean anySubFeatureEnabled = getDescription()
                    .subFeatures()
                    .stream()
                    .anyMatch(Feature::isEnabled);
            return getDescription().mandatory() || super.get() || anySubFeatureEnabled;
        }

        @Override
        public void set(boolean newValue) {
            super.set(newValue);
            getDescription()
                    .subFeatures()
                    .forEach(f -> f.setEnabled(newValue));
        }
    };

    protected Feature(@NonNull FeatureDescription description, boolean enabled) {
        this.description = description;
        setEnabled(enabled);
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

    @NonNull
    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabledProperty().get();
    }

    public void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }
}
