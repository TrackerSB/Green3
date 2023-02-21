package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.green3.Main;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.NodeQuery;

import static org.testfx.util.NodeQueryUtils.hasText;

public class MemberManagementScreenTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws ScreenSwitchFailedException {
        new Main().start(stage);

        NodeQuery memberManagementTile = lookup(hasText("Mitgliederverwaltung"));
        clickOn((Node) memberManagementTile.query(), MouseButton.PRIMARY);
    }

    @Test
    public void doTheTest() {
    }
}
