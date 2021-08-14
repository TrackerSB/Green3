package bayern.steinbrecher.green3.features;

import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public abstract class Feature {
    private final String id;
    private final String name;
    private final boolean mandatory;
    private boolean enabled;

    protected Feature(@NonNull String id, @NonNull String name, boolean mandatory, boolean enabled) {
        this.id = id;
        this.name = name;
        this.mandatory = mandatory;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isEnabled() {
        return isMandatory() || enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
