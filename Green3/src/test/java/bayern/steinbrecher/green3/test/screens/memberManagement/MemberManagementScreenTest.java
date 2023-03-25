package bayern.steinbrecher.green3.test.screens.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.DatabaseNotFoundException;
import bayern.steinbrecher.dbConnector.credentials.SimpleCredentials;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.green3.Main;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.NodeQuery;

import java.util.ArrayList;
import java.util.List;

import static org.testfx.util.NodeQueryUtils.hasText;

public class MemberManagementScreenTest extends ApplicationTest {
    private static final String DB_NAME = "TestDB";
    private static final String DB_USERNAME = "user";
    private static final String DB_PASSWORD = "password";
    private static final SimpleCredentials DB_SIMPLE_CREDENTIALS = new SimpleCredentials(DB_USERNAME, DB_PASSWORD);

    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 0; // 0 == automatically detect free port
    private static DB MARIADB;
    private static final List<DBConnection> CONNECTIONS = new ArrayList<>();

    @Override
    public void start(Stage stage) throws ScreenSwitchFailedException {
        new Main().start(stage);

        NodeQuery memberManagementTile = lookup(hasText("Mitgliederverwaltung"));
        clickOn((Node) memberManagementTile.query(), MouseButton.PRIMARY);
    }

    @BeforeAll
    static void setupDatabases()
            throws ManagedProcessException, AuthException, DatabaseNotFoundException, QueryFailedException {
        DBConfigurationBuilder configuration = DBConfigurationBuilder.newBuilder().setPort(DB_PORT);
        MARIADB = DB.newEmbeddedDB(configuration.build());
        // MARIADB.start();
        // MARIADB.createDB(DB_NAME, DB_USERNAME, DB_PASSWORD);

        // SimpleConnection connection
        //         = new SimpleConnection(SupportedDBMS.MARIADB, DB_HOST, DB_PORT, DB_NAME, DB_SIMPLE_CREDENTIALS, false);
        // CONNECTIONS.add(connection);
        // connection.createTableIfNotExists(Tables.MEMBERS);
    }

    @AfterAll
    static void stopDatabases() throws ManagedProcessException {
        // CONNECTIONS.forEach(DBConnection::close);
        // if(MARIADB != null) {
        //     MARIADB.stop();
        // }
    }

    @Test
    public void doTheTest() {
    }
}
