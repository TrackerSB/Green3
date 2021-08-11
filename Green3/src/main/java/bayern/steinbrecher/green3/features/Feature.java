package bayern.steinbrecher.green3.features;

import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    private final String id;
    private boolean enabled;

    protected Feature(@NonNull String id, boolean enabled) {
        this.id = id;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
