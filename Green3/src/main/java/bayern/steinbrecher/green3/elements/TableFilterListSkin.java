package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.QueryCondition;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.QueryGenerator;
import bayern.steinbrecher.dbConnector.query.QueryOperator;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Callback;
import lombok.NonNull;

import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterListSkin extends SkinBase<TableFilterList> {
    private static final Logger LOGGER = Logger.getLogger(TableFilterListSkin.class.getName());
    private static final Map<QueryOperator<?>, String> PRETTY_OPERATOR_NAME = Map.of(
            // Boolean operators
            QueryOperator.IS_TRUE, ControlResources.RESOURCES.getString("yes"),
            QueryOperator.IS_FALSE, ControlResources.RESOURCES.getString("no"),
            // String operators
            QueryOperator.CONTAINS, ControlResources.RESOURCES.getString("contains"),
            QueryOperator.LIKE, ControlResources.RESOURCES.getString("sameAs")
    );
    private final BooleanProperty valueValid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(null);

    public TableFilterListSkin(@NonNull TableFilterList control) {
        super(control);

        Text filterDescription = new Text(ControlResources.RESOURCES.getString("filters"));

        Node activeFilterContainer = createActiveFilterContainer(control);

        CheckedComboBox<DBConnection.Column<?, ?>> columnSelection = createColumnSelection(control.tableProperty());

        CheckedComboBox<QueryOperator<?>> operatorSelection = createOperatorSelection(columnSelection);

        Node valueContainer = createValueContainer(columnSelection);
        HBox.setHgrow(valueContainer, Priority.ALWAYS);

        Node addFilter = createAddFilterButton(columnSelection, operatorSelection, control);

        getChildren()
                .add(new HBox(filterDescription, activeFilterContainer, addFilter, columnSelection, operatorSelection,
                        valueContainer));
    }

    @NonNull
    private static Pane createActiveFilterContainer(@NonNull TableFilterList control) {
        DisposableBadge noFilterBadge = new DisposableBadge(ControlResources.RESOURCES.getString("none"), false);
        HBox activeFilterContainer = new HBox();

        ChangeListener<Boolean> noActiveFiltersListener = (obs, wereNoFilters, areNoFilters) -> {
            if (areNoFilters) {
                if (!activeFilterContainer.getChildren().contains(noFilterBadge)) {
                    activeFilterContainer.getChildren()
                            .add(noFilterBadge);
                }
            } else {
                activeFilterContainer.getChildren()
                        .remove(noFilterBadge);
            }
        };
        control.noActiveFiltersProperty()
                .addListener(noActiveFiltersListener);
        noActiveFiltersListener.changed(null, null, control.isNoActiveFilters());

        control.activeFiltersProperty()
                .addListener((ListChangeListener<? super QueryCondition<?>>) change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (QueryCondition<?> addedCondition : change.getAddedSubList()) {
                                DisposableBadge conditionBadge
                                        = new DisposableBadge(addedCondition.getSqlExpression(), true);
                                activeFilterContainer.getChildren()
                                        .add(conditionBadge);
                            }
                        }
                    }
                });

        return activeFilterContainer;
    }

    @NonNull
    private static CheckedComboBox<DBConnection.Column<?, ?>> createColumnSelection(
            @NonNull ObjectProperty<DBConnection.Table<?, ?>> tableProperty) {
        CheckedComboBox<DBConnection.Column<?, ?>> columnSelection = new CheckedComboBox<>();
        columnSelection.setPlaceholder(new Text(ControlResources.RESOURCES.getString("unsupported")));
        columnSelection.setEditable(false);

        var cellFactory = new Callback<ListView<DBConnection.Column<?, ?>>, ListCell<DBConnection.Column<?, ?>>>() {
            @NonNull
            @Override
            public ListCell<DBConnection.Column<?, ?>> call(ListView<DBConnection.Column<?, ?>> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(DBConnection.Column<?, ?> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        }
                    }
                };
            }
        };
        columnSelection.setButtonCell(cellFactory.call(null));
        columnSelection.setCellFactory(cellFactory);

        ChangeListener<DBConnection.Table<?, ?>> tableListener = (obs, previousTable, currentTable) -> {
            columnSelection.getItems().clear();
            if (currentTable != null) {
                try {
                    columnSelection.getItems().addAll(currentTable.getColumns());
                    columnSelection.getItems().sort((cA, cB) -> cA.getName().compareToIgnoreCase(cB.getName()));
                } catch (QueryFailedException ex) {
                    LOGGER.log(Level.WARNING, "Could not populate column selection", ex);
                }
            }
        };
        tableProperty.addListener(tableListener);
        tableListener.changed(null, null, tableProperty.get());

        return columnSelection;
    }

    @NonNull
    private static CheckedComboBox<QueryOperator<?>> createOperatorSelection(
            @NonNull CheckedComboBox<DBConnection.Column<?, ?>> columnSelection) {
        CheckedComboBox<QueryOperator<?>> operatorSelection = new CheckedComboBox<>();
        operatorSelection.setPlaceholder(new Text(ControlResources.RESOURCES.getString("unsupported")));
        operatorSelection.setEditable(false);

        var cellFactory = new Callback<ListView<QueryOperator<?>>, ListCell<QueryOperator<?>>>() {
            @NonNull
            @Override
            public ListCell<QueryOperator<?>> call(ListView<QueryOperator<?>> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(QueryOperator<?> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(PRETTY_OPERATOR_NAME.getOrDefault(item, item.getOperatorSymbol()));
                        }
                    }
                };
            }
        };
        operatorSelection.setButtonCell(cellFactory.call(null));
        operatorSelection.setCellFactory(cellFactory);

        ChangeListener<DBConnection.Column<?, ?>> selectedItemChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    operatorSelection.getItems().clear();

                    if (currentlySelected != null) {
                        Class<?> columnType = currentlySelected.getColumnType();
                        if (Boolean.class.isAssignableFrom(columnType)) {
                            operatorSelection.getItems().addAll(QueryOperator.BOOLEAN_OPERATORS);
                        } else if (String.class.isAssignableFrom(columnType)) {
                            operatorSelection.getItems().addAll(QueryOperator.STRING_OPERATORS);
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The type of the selected column %s is unsupported",
                                            columnType.getName()));
                        }
                    }
                };
        columnSelection.valueProperty()
                .addListener(selectedItemChanged);
        selectedItemChanged.changed(null, null, columnSelection.getValue());

        return operatorSelection;
    }

    @NonNull
    private Node createValueContainer(
            @NonNull CheckedComboBox<DBConnection.Column<?, ?>> columnSelection) {
        Pane valueContainer = new HBox();
        ChangeListener<DBConnection.Column<?, ?>> selectedItemChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    valueContainer.getChildren().clear();
                    if (currentlySelected != null) {
                        Class<?> columnType = currentlySelected.getColumnType();
                        if (Boolean.class.isAssignableFrom(columnType)) {
                            valueValid.unbind();
                            valueValid.set(true);
                            value.set(null);
                        } else if (String.class.isAssignableFrom(columnType)) {
                            var inputField = new CheckedTextField();
                            valueContainer.getChildren()
                                    .add(inputField);
                            valueValid.bind(inputField.validProperty());
                            value.bind(inputField.textProperty());
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The type of the selected column %s is unsupported",
                                            columnType.getName()));
                            valueValid.unbind();
                            valueValid.set(false);
                            value.set(null);
                        }
                    }
                };
        columnSelection.valueProperty()
                .addListener(selectedItemChanged);
        selectedItemChanged.changed(null, null, columnSelection.getValue());

        return valueContainer;
    }

    @NonNull
    private Node createAddFilterButton(@NonNull CheckedComboBox<DBConnection.Column<?, ?>> columnSelection,
                                       @NonNull CheckedComboBox<QueryOperator<?>> operatorSelection,
                                       @NonNull TableFilterList control) {
        Button addFilter = new Button();

        URL graphicsResource = TableFilterListSkin.class.getResource("add.png");
        if (graphicsResource == null) {
            LOGGER.log(Level.WARNING, "Could not find icon for add button");
            addFilter.setText("+");
        } else {
            addFilter.setGraphic(new ImageView(graphicsResource.toExternalForm()));
        }

        BooleanBinding currentFilterValid = columnSelection.validProperty()
                .and(operatorSelection.validProperty())
                .and(valueValid);
        addFilter.disableProperty()
                .bind(currentFilterValid.not());

        addFilter.setOnAction(aevt -> {
            if (currentFilterValid.get()) {
                QueryOperator<?> selectedOperator = operatorSelection.getSelectionModel().getSelectedItem();
                QueryGenerator queryGenerator = control.getDbms().getQueryGenerator();
                DBConnection.Column<?, ?> selectedColumn = columnSelection.getSelectionModel().getSelectedItem();
                Object[] arguments;
                if (selectedOperator instanceof QueryOperator.BinaryQueryOperator) {
                    arguments = new Object[]{selectedColumn, value.get()};
                } else if (selectedOperator instanceof QueryOperator.PrefixQueryOperator) {
                    arguments = new Object[]{selectedColumn};
                } else {
                    LOGGER.log(Level.SEVERE, "Cannot create filter since the type of query operator is not supported");
                    arguments = null;
                }
                if (arguments != null) {
                    QueryCondition<?> queryCondition = selectedOperator.generateCondition(queryGenerator, arguments);
                    control.getActiveFilters()
                            .add(queryCondition);
                    columnSelection.getSelectionModel().clearSelection();
                }
            }
        });

        return addFilter;
    }
}
