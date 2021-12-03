package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.ConnectionFailedException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.GenerationFailedException;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.green3.data.Membership;
import bayern.steinbrecher.green3.data.Tables;
import bayern.steinbrecher.green3.elements.TableFilterList;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.NonNull;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class MemberManagementScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(MemberManagementScreenController.class.getName());
    @FXML
    private Pane memberViewPlaceholder;
    @FXML
    private TableFilterList<Membership> memberViewFilterList;
    @FXML
    private ResourceBundle resources;

    @FXML
    private void initialize() {
        memberViewFilterList.visibleProperty()
                .bind(FeatureRegistry.MEMBER_MANAGEMENT_TABLE_FILTERS.enabledProperty());
        memberViewFilterList.managedProperty()
                .bind(memberViewFilterList.visibleProperty());
    }

    @NonNull
    private DBConnection connectToMemberDB() {
        try {
            return new SshConnection(
                    SupportedDBMS.MY_SQL,
                    "<dbHost>",
                    3306,
                    "<dbName>",
                    "<sshHost>",
                    22,
                    StandardCharsets.UTF_8,
                    new SshCredentials(
                            "<dbUsername>",
                            "<dbPassword>",
                            "<sshUsername>",
                            "<sshPassword>"
                    )
            );
        } catch (ConnectionFailedException | AuthException | UnknownHostException ex) {
            throw new CompletionException("Could not request member data", ex);
        }
    }

    private void setupMemberViewColumns(@NonNull DBConnection dbConnection) {
        try {
            Optional<DBConnection.Table<Set<Membership>, Membership>> optMemberTable
                    = dbConnection.getTable(Tables.MEMBERS);
            if (optMemberTable.isPresent()) {
                memberViewFilterList.setTable(optMemberTable.get());

                TableView<Membership> memberTable = optMemberTable.get().createTableView();
                var filterableItems = new FilteredList<>(
                        FXCollections.observableArrayList(dbConnection.getTableContent(Tables.MEMBERS)));
                filterableItems.predicateProperty()
                        .bind(memberViewFilterList.filterProperty());
                memberTable.setItems(filterableItems);
                VBox.setVgrow(memberTable, Priority.ALWAYS);

                Platform.runLater(() -> memberViewPlaceholder.getChildren().addAll(memberTable));
            }
        } catch (QueryFailedException | GenerationFailedException ex) {
            throw new CompletionException("Could not populate member view", ex);
        }
    }

    @Override
    protected void afterScreenManagerIsSet() {
        CompletableFuture.runAsync(() -> getScreenManager().showOverlay(resources.getString("retrievingData")))
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
