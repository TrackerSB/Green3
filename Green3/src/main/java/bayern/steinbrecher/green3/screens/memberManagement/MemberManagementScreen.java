package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.screenswitcher.Screen;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreen extends Screen<MemberManagementScreenController> {
    public MemberManagementScreen() {
        super(
                MemberManagementScreen.class.getResource("MemberManagementScreen.fxml"),
                ResourceBundle.getBundle("bayern.steinbrecher.green3.screens.memberManagement.MemberManagement")
        );
    }
}
