package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.net.URL;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public record FeatureDescription(@NonNull String name, URL image, boolean mandatory) {
}
