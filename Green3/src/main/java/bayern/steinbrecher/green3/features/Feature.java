package bayern.steinbrecher.green3.features;

import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    private final String id;
    private final FeatureDescription description;
    private boolean enabled;

    protected Feature(@NonNull String id, @NonNull FeatureDescription description, boolean enabled) {
        this.id = id;
        this.description = description;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
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
