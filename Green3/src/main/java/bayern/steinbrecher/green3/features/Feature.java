package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    protected static final ResourceBundle resources
            = ResourceBundle.getBundle("bayern.steinbrecher.green3.features.Features");
    private final FeatureDescription description;
    private boolean enabled;

    protected Feature(@NonNull FeatureDescription description, boolean enabled) {
        this.description = description;
        this.enabled = enabled;
    }

    public FeatureDescription getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return getDescription().mandatory() || enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
