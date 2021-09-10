package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.ConnectionFailedException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.GenerationFailedException;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.dbConnector.scheme.ColumnParser;
import bayern.steinbrecher.green3.data.Membership;
import bayern.steinbrecher.green3.data.Tables;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import lombok.NonNull;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
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
    private BorderPane memberView;

    private DBConnection connectToMemberDB() {
        try {
            return new SshConnection(
                    SupportedDBMS.MY_SQL,
                    "rdbms.strato.de",
                    3306,
                    "DB4401428",
                    "ssh.strato.de",
                    22,
                    StandardCharsets.UTF_8,
                    new SshCredentials(
                            "U4401428",
                            "2cqhbunuMp6peXq",
                            "traunviertler-traunwalchen.de",
                            "wXjziib5FNcJ6aG"
                    )
            );
        } catch (ConnectionFailedException | AuthException | UnknownHostException ex) {
            throw new CompletionException("Could not request member data", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <C> ColumnParser<C> getParser(DBConnection.Column<?, C> column) {
        if (Boolean.class.isAssignableFrom(column.getColumnType())) {
            return (ColumnParser<C>) ColumnParser.BOOLEAN_COLUMN_PARSER;
        }
        if (Double.class.isAssignableFrom(column.getColumnType())) {
            return (ColumnParser<C>) ColumnParser.DOUBLE_COLUMN_PARSER;
        }
        if (Integer.class.isAssignableFrom(column.getColumnType())) {
            return (ColumnParser<C>) ColumnParser.INTEGER_COLUMN_PARSER;
        }
        if (LocalDate.class.isAssignableFrom(column.getColumnType())) {
            return (ColumnParser<C>) ColumnParser.LOCALDATE_COLUMN_PARSER;
        }
        if (String.class.isAssignableFrom(column.getColumnType())) {
            return (ColumnParser<C>) ColumnParser.STRING_COLUMN_PARSER;
        }

        LOGGER.log(Level.WARNING, String.format(
                "There is no appropriate column parser available for type %s. Assume it's String",
                column.getColumnType().getName()));
        return (ColumnParser<C>) ColumnParser.STRING_COLUMN_PARSER;
    }

    private void setupMemberViewColumns(@NonNull DBConnection dbConnection) {
        try {
            Optional<DBConnection.Table<Set<Membership>, Membership>> optMemberTable
                    = dbConnection.getTable(Tables.MEMBERS);
            if (optMemberTable.isPresent()) {
                TableView<Membership> memberTable = optMemberTable.get().createTableView();
                memberTable.setItems(FXCollections.observableArrayList(dbConnection.getTableContent(Tables.MEMBERS)));
                Platform.runLater(() -> memberView.setCenter(memberTable));
            }
        } catch (QueryFailedException | GenerationFailedException ex) {
            throw new CompletionException("Could not populate member view", ex);
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
