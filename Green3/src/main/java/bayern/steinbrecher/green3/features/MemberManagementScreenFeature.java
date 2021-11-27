package bayern.steinbrecher.green3.features;

import bayern.steinbrecher.green3.screens.memberManagement.MemberManagementScreen;
import lombok.NonNull;

import java.util.List;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreenFeature extends Feature {
    public static final MemberManagementScreenFeature TABLE_FILTERS = new MemberManagementScreenFeature(
            new FeatureDescription(
                    resources.getString("tableFilter"),
                    MemberManagementScreen.class.getResource("add.png"), false),
            true
    );
    static Iterable<MemberManagementScreenFeature> FEATURES = List.of(
            TABLE_FILTERS
    );

    private MemberManagementScreenFeature(@NonNull FeatureDescription description, boolean enabled) {
        super(description, enabled);
    }
}
