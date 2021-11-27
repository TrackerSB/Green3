package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.util.List;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreenFeature extends Feature {
    static Iterable<MemberManagementScreenFeature> FEATURES = List.of();

    private MemberManagementScreenFeature(@NonNull FeatureDescription description, boolean enabled) {
        super(description, enabled);
    }
}
