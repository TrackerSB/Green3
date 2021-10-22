package bayern.steinbrecher.green3.features;

import lombok.NonNull;

import java.util.List;

public class MemberManagementScreenFeature extends Feature {
    static Iterable<MemberManagementScreenFeature> FEATURES = List.of();

    private MemberManagementScreenFeature(@NonNull String id, @NonNull FeatureDescription description, boolean enabled) {
        super(id, description, enabled);
    }
}
