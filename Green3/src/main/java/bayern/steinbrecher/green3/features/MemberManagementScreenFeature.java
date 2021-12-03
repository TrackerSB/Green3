package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.memberManagement.MemberManagementScreen;
import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreenFeature extends Feature {
    MemberManagementScreenFeature(@NonNull FeatureDescription description, boolean enabled) {
        super(description, enabled);
    }

    @Override
    protected @NonNull Class<?> getAssociatedClass() {
        return MemberManagementScreen.class;
    }
}
