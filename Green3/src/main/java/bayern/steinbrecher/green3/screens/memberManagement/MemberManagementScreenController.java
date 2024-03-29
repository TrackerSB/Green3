package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.dbConnector.AuthException;
import bayern.steinbrecher.dbConnector.ConnectionFailedException;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.SshConnection;
import bayern.steinbrecher.dbConnector.credentials.SshCredentials;
import bayern.steinbrecher.dbConnector.query.GenerationFailedException;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import bayern.steinbrecher.dbConnector.utility.DBSynchronizer;
import bayern.steinbrecher.dbConnector.utility.InvalidSyncTargetException;
import bayern.steinbrecher.dbConnector.utility.TableViewGenerator;
import bayern.steinbrecher.green3.data.Membership;
import bayern.steinbrecher.green3.data.Tables;
import bayern.steinbrecher.green3.elements.TableFilterList;
import bayern.steinbrecher.green3.features.FeatureRegistry;
import bayern.steinbrecher.javaUtility.CSVFormat;
import bayern.steinbrecher.javaUtility.IOUtility;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
    private Text filterStatus;
    @FXML
    private Button exportMembers;
    @FXML
    private ResourceBundle resources;
    private TableView<Membership> memberTable;

    @FXML
    private void initialize() {
        memberViewFilterList.visibleProperty()
                .bind(FeatureRegistry.MEMBER_MANAGEMENT_TABLE_FILTERS.enabledProperty());
        memberViewFilterList.managedProperty()
                .bind(memberViewFilterList.visibleProperty());

        exportMembers.visibleProperty()
                .bind(FeatureRegistry.MEMBER_MANAGEMENT_EXPORT.enabledProperty());
        exportMembers.managedProperty()
                .bind(exportMembers.visibleProperty());
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
                if (FeatureRegistry.MEMBER_MANAGEMENT_TABLE_FILTERS.isEnabled()) {
                    memberViewFilterList.activeFiltersProperty().add(
                            new TableFilterList.Filter<>(
                                    ms -> ms.leavingDate().isEmpty(), resources.getString("currentMembers")));
                }

                memberTable = TableViewGenerator.createTableView(optMemberTable.get());
                ObservableList<Membership> memberItems
                        = FXCollections.observableArrayList(dbConnection.getTableContent(Tables.MEMBERS));
                try {
                    new DBSynchronizer<>(Tables.MEMBERS, dbConnection).synchronize(memberItems);
                    memberTable.setEditable(true);
                } catch (InvalidSyncTargetException ex) {
                    LOGGER.log(Level.WARNING, "Could not setup synchronization with database. "
                            + "The table won't be editable", ex);
                    memberTable.setEditable(false);
                }

                var filterableItems = new FilteredList<>(memberItems);
                filterableItems.predicateProperty().bind(memberViewFilterList.filterProperty());
                var sortableFilterableItems = new SortedList<>(filterableItems);
                sortableFilterableItems.comparatorProperty()
                        .bind(memberTable.comparatorProperty());
                memberTable.setItems(sortableFilterableItems);

                VBox.setVgrow(memberTable, Priority.ALWAYS);

                Platform.runLater(() -> memberViewPlaceholder.getChildren().addAll(memberTable));
            }
        } catch (QueryFailedException | GenerationFailedException ex) {
            throw new CompletionException("Could not populate member view", ex);
        }
    }

    private void setupFilterStatus(@NonNull DBConnection dbConnection) {
        IntegerProperty numOverallMembers = new SimpleIntegerProperty(0);
        ChangeListener<DBConnection.Table<?, ?>> filterListTableChanged = (obs, previousTable, currentTable) -> {
            try {
                numOverallMembers.set(dbConnection.getTableContent(Tables.MEMBERS).size());
            } catch (GenerationFailedException | QueryFailedException ex) {
                LOGGER.log(Level.WARNING, "Could not determine number of table entries", ex);
                numOverallMembers.set(0);
            }
        };
        memberViewFilterList.tableProperty()
                .addListener(filterListTableChanged);
        filterListTableChanged.changed(null, null, memberViewFilterList.getTable()); // Initialize listener
        var numOverallMembersFormat = new MessageFormat(resources.getString("numMembers"));
        StringBinding numOverallMembersText = Bindings.createStringBinding(
                () -> numOverallMembersFormat.format(new Object[]{numOverallMembers.get()}),
                numOverallMembers);

        IntegerProperty numVisibleMembers = new SimpleIntegerProperty(memberTable.getItems().size());
        ListChangeListener<? super Membership> memberTableChangedListener
                = change -> numVisibleMembers.set(change.getList().size());
        memberTable.getItems()
                .addListener(memberTableChangedListener);
        var numVisibleMembersFormat = new MessageFormat(resources.getString("numMembersOf"));
        StringBinding numVisibleMembersText = Bindings.createStringBinding(
                () -> numVisibleMembersFormat.format(new Object[]{numVisibleMembers.get(), numOverallMembers.get()}),
                numVisibleMembers, numOverallMembers);

        StringBinding filterStatusText = new When(FeatureRegistry.MEMBER_MANAGEMENT_TABLE_FILTERS.enabledProperty())
                .then(numVisibleMembersText)
                .otherwise(numOverallMembersText);
        filterStatus.textProperty()
                .bind(filterStatusText);
    }

    @Override
    protected void afterScreenManagerIsSet() {
        CompletableFuture.runAsync(() -> getScreenManager().showOverlay(resources.getString("retrievingData")))
                .thenApply(v -> connectToMemberDB())
                .thenAccept(dbConnection -> {
                    setupMemberViewColumns(dbConnection);
                    setupFilterStatus(dbConnection);
                })
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        LOGGER.log(Level.SEVERE, "Could not set up member management screen", throwable);
                    }
                    // Ensure overlay is always closed
                    getScreenManager().hideOverlay();
                    return null;
                });
    }

    @FXML
    private void exportMembers() throws IOException {
        FileChooser savePathChooser = new FileChooser();
        savePathChooser.setInitialFileName(resources.getString("export") + ".csv");
        savePathChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(resources.getString("csvFiles"), "*.csv"));
        savePathChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(resources.getString("allFiles"), "*"));
        savePathChooser.setTitle(resources.getString("exportMembers"));
        File savePath = savePathChooser.showSaveDialog(null);// FIXME Specify owner
        if (savePath != null) {
            Collection<Iterable<String>> content = new ArrayList<>();

            // Add headers
            content.add(
                    memberTable.getColumns()
                            .stream()
                            .map(TableColumnBase::getText)
                            .toList());

            // Add currently shown members
            for (Membership membership : memberTable.getItems()) {
                Collection<String> row = new ArrayList<>();
                for (TableColumn<Membership, ?> column : memberTable.getColumns()) {
                    Object cellValue = column.getCellObservableValue(membership).getValue();
                    row.add(cellValue == null ? "" : cellValue.toString());
                }
                content.add(row);
            }

            IOUtility.writeCSV(savePath.toPath(), content, CSVFormat.EXCEL);
        }
    }
}
