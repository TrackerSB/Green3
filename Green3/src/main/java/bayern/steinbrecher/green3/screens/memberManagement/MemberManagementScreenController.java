package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.ConnectionFailedException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.green3.data.Membership;
import bayern.steinbrecher.green3.data.Tables;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.NonNull;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(MemberManagementScreenController.class.getName());
    @FXML
    private TableView<Membership> memberView;

    private DBConnection connectToMemberDB() {
        try {
            return new SshConnection(
                    SupportedDBMS.MY_SQL,
                    "",
                    3306,
                    "",
                    "",
                    22,
                    StandardCharsets.UTF_8,
                    new SshCredentials(
                            "",
                            "",
                            "",
                            ""
                    )
            );
        } catch (ConnectionFailedException | AuthException | UnknownHostException ex) {
            throw new CompletionException("Could not request member data", ex);
        }
    }

    private void setupMemberViewColumns(@NonNull DBConnection dbConnection) {
        try {
            @SuppressWarnings("Convert2MethodRef")
            Collection<TableColumn<Membership, ?>> columns = dbConnection.getAllColumns(Tables.MEMBERS)
                    .stream()
                    .sorted(Comparator.comparingInt(DBConnection.Column::getIndex))
                    .map(DBConnection.Column::getName)
                    .map(n -> new TableColumn<Membership, String>(n))
                    .collect(Collectors.toList());
            Platform.runLater(() -> memberView.getColumns().setAll(columns));
        } catch (QueryFailedException ex) {
            throw new CompletionException("Could not set columns to member view", ex);
        }
    }

    @Override
    protected void afterScreenManagerIsSet() {
        CompletableFuture.runAsync(() -> getScreenManager().showOverlay("Retrieving data..."))
                .thenApply(v -> connectToMemberDB())
                .thenAccept(this::setupMemberViewColumns)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        LOGGER.log(Level.SEVERE, "Could not set up member view", throwable);
                    }
                    // Ensure overlay is always closed
                    getScreenManager().hideOverlay();
                    return null;
                });
    }
}
