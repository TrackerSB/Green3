package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.QueryOperator;
import bayern.steinbrecher.dbConnector.scheme.ColumnPattern;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @param <I> The type of the elements represented by the table to filter.
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterListSkin<I> extends SkinBase<TableFilterList<I>> {
    private static final Logger LOGGER = Logger.getLogger(TableFilterListSkin.class.getName());
    private static final Map<QueryOperator<?>, String> PRETTY_OPERATOR_NAME = Map.of(
            // Boolean operators
            QueryOperator.IS_TRUE, ControlResources.RESOURCES.getString("yes").toLowerCase(Locale.ROOT),
            QueryOperator.IS_FALSE, ControlResources.RESOURCES.getString("no").toLowerCase(Locale.ROOT),
            // String operators
            QueryOperator.CONTAINS, ControlResources.RESOURCES.getString("contains").toLowerCase(Locale.ROOT),
            QueryOperator.LIKE, ControlResources.RESOURCES.getString("sameAs").toLowerCase(Locale.ROOT)
    );
    private final MapProperty<TableFilterList.Filter<I>, DisposableBadge> visibleBadges
            = new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final BooleanProperty valueValid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(null);
    private final BooleanBinding currentFilterInputValid;
    private final ObjectProperty<Optional<TableFilterList.Filter<I>>> currentFilterInput;

    public TableFilterListSkin(@NonNull TableFilterList<I> control) {
        super(control);

        Text filterDescription = new Text(ControlResources.RESOURCES.getString("filters"));

        Node activeFilterContainer = createActiveFilterContainer(control);

        CheckedComboBox<DBConnection.Column<I, ?>> columnSelection = createColumnSelection(control.tableProperty());

        CheckedComboBox<QueryOperator<?>> operatorSelection = createOperatorSelection(columnSelection);

        Node valueContainer = createValueContainer(columnSelection);
        HBox.setHgrow(valueContainer, Priority.ALWAYS);

        currentFilterInputValid = columnSelection.validProperty()
                .and(operatorSelection.validProperty())
                .and(valueValid);
        currentFilterInput = trackCurrentFilterInput(columnSelection, operatorSelection);
        currentFilterInput.addListener((obs, previousUnconfirmedFilter, currentUnconfirmedFilter) -> {
            previousUnconfirmedFilter.ifPresent(filter -> control.activeFiltersProperty().remove(filter));

            if (currentUnconfirmedFilter.isPresent()) {
                control.activeFiltersProperty().add(currentUnconfirmedFilter.get());
            } else {
                columnSelection.getSelectionModel().clearSelection();
            }
        });

        Node addFilter = createAddFilterButton(control);

        getChildren()
                .add(new HBox(filterDescription, activeFilterContainer, addFilter, columnSelection, operatorSelection,
                        valueContainer));
    }

    private void addBadge(TableFilterList.Filter<I> associatedFilter, @NonNull DisposableBadge badge) {
        if (visibleBadges.containsKey(associatedFilter)) {
            LOGGER.log(Level.INFO, "The badge for the given filter gets replaced");
        } else if (visibleBadges.containsValue(badge)) {
            LOGGER.log(Level.WARNING, "The given badge is already associated to a filter criterion");
        }

        // Do not add a badge for the unconfirmed filter
        if (currentFilterInput == null
                || currentFilterInput.get().isEmpty()
                || currentFilterInput.get().get() != associatedFilter) {
            visibleBadges.put(associatedFilter, badge);
        }
    }

    private void removeBadge(TableFilterList.Filter<I> associatedFilter) {
        DisposableBadge removedBadge = visibleBadges.remove(associatedFilter);
        if (removedBadge == null) {
            LOGGER.log(Level.WARNING, "Cannot remove badge for filter which was not associated");
        }
    }

    @NonNull
    private Pane createActiveFilterContainer(@NonNull TableFilterList<I> control) {
        ChangeListener<Boolean> noActiveFiltersListener = (obs, wereNoFilters, areNoFilters) -> {
            if (areNoFilters) {
                addBadge(null, new DisposableBadge(ControlResources.RESOURCES.getString("none"), false));
            } else {
                removeBadge(null);
            }
        };
        control.noActiveFiltersProperty()
                .addListener(noActiveFiltersListener);
        noActiveFiltersListener.changed(null, null, control.isNoActiveFilters());

        control.activeFiltersProperty()
                .addListener((ListChangeListener<? super TableFilterList.Filter<I>>) change -> {
                    while (change.next()) {
                        /* NOTE 2021-12-30: It is required to treat removals before additions in order to handle
                         * replacements correctly.
                         */
                        if (change.wasRemoved()) {
                            for (TableFilterList.Filter<I> removedCondition : change.getRemoved()) {
                                removeBadge(removedCondition);
                            }
                        }

                        if (change.wasAdded()) {
                            for (TableFilterList.Filter<I> addedCondition : change.getAddedSubList()) {
                                var conditionBadge = new DisposableBadge(addedCondition.description(), true);
                                conditionBadge.setOnClose(aevt -> control.getActiveFilters().remove(addedCondition));
                                addBadge(addedCondition, conditionBadge);
                            }
                        }
                    }
                });

        HBox activeFilterContainer = new HBox();
        visibleBadges.addListener(
                (MapChangeListener<? super TableFilterList.Filter<I>, ? super DisposableBadge>) change -> {
                    /* NOTE 2021-12-30: It is required to treat removals before additions in order to handle
                     * replacements correctly.
                     */
                    if (change.wasRemoved()) {
                        activeFilterContainer.getChildren()
                                .remove(change.getValueRemoved());
                    }
                    if (change.wasAdded()) {
                        activeFilterContainer.getChildren()
                                .add(change.getValueAdded());
                    }
                });

        return activeFilterContainer;
    }

    @NonNull
    private static <I> CheckedComboBox<DBConnection.Column<I, ?>> createColumnSelection(
            @NonNull ObjectProperty<DBConnection.Table<?, I>> tableProperty) {
        CheckedComboBox<DBConnection.Column<I, ?>> columnSelection = new CheckedComboBox<>();
        columnSelection.setPlaceholder(new Text(ControlResources.RESOURCES.getString("unsupported")));
        columnSelection.setEditable(false);

        var cellFactory = new Callback<ListView<DBConnection.Column<I, ?>>, ListCell<DBConnection.Column<I, ?>>>() {
            @NonNull
            @Override
            public ListCell<DBConnection.Column<I, ?>> call(ListView<DBConnection.Column<I, ?>> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(DBConnection.Column<I, ?> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            Platform.runLater(() -> setText(item.getName()));
                        }
                    }
                };
            }
        };
        columnSelection.setButtonCell(cellFactory.call(null));
        columnSelection.setCellFactory(cellFactory);

        ChangeListener<DBConnection.Table<?, I>> tableListener = (obs, previousTable, currentTable) -> {
            columnSelection.getSelectionModel().clearSelection();
            columnSelection.getItems().clear();

            if (currentTable != null) {
                try {
                    columnSelection.getItems().setAll(currentTable.getColumns());
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
    private static <I> CheckedComboBox<QueryOperator<?>> createOperatorSelection(
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection) {
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
                        } else {
                            LOGGER.log(Level.INFO, "Encountered empty or null item");
                            setText("");
                        }
                    }
                };
            }
        };
        operatorSelection.setButtonCell(cellFactory.call(null));
        operatorSelection.setCellFactory(cellFactory);

        ChangeListener<DBConnection.Column<?, ?>> selectedItemChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    operatorSelection.getSelectionModel().clearSelection();
                    operatorSelection.getItems().clear();

                    if (currentlySelected != null) {
                        Class<?> columnType = currentlySelected.getColumnType();
                        if (Boolean.class.isAssignableFrom(columnType)) {
                            operatorSelection.getItems().addAll(QueryOperator.BOOLEAN_OPERATORS);
                        } else if (String.class.isAssignableFrom(columnType)) {
                            operatorSelection.getItems().addAll(QueryOperator.STRING_OPERATORS);
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The type %s of the selected column %s is unsupported",
                                            columnType.getName(), currentlySelected.getName()));
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
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection) {
        Pane valueContainer = new HBox();
        ChangeListener<DBConnection.Column<?, ?>> selectedItemChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    valueContainer.getChildren().clear();
                    if (currentlySelected != null) {
                        Class<?> columnType = currentlySelected.getColumnType();
                        if (Boolean.class.isAssignableFrom(columnType)) {
                            valueValid.unbind();
                            valueValid.set(true);
                            value.unbind();
                            value.set(null);
                        } else if (String.class.isAssignableFrom(columnType)) {
                            var inputField = new CheckedTextField();
                            valueContainer.getChildren()
                                    .add(inputField);
                            valueValid.bind(inputField.validProperty());
                            value.bind(inputField.textProperty());
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The type %s of the selected column %s is unsupported",
                                            columnType.getName(), currentlySelected.getName()));
                            valueValid.unbind();
                            valueValid.set(false);
                            value.unbind();
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
    private Optional<Predicate<I>> createFilterEvaluator(
            @NonNull QueryOperator<?> operator, @NonNull Function<I, ?> itemFieldGetter,
            @NonNull Supplier<?> valueGetter) {
        // Boolean operators
        if (operator == QueryOperator.IS_FALSE) {
            return Optional.of(item -> !((Boolean) itemFieldGetter.apply(item)));
        }
        if (operator == QueryOperator.IS_TRUE) {
            return Optional.of(item -> (Boolean) itemFieldGetter.apply(item));
        }

        // String operators
        if (operator == QueryOperator.CONTAINS) {
            return Optional.of(item -> ((String) itemFieldGetter.apply(item))
                    .toLowerCase(Locale.ROOT)
                    .contains(((String) valueGetter.get()).toLowerCase(Locale.ROOT)));
        }
        if (operator == QueryOperator.LIKE) {
            return Optional.of(item -> ((String) itemFieldGetter.apply(item))
                    .equalsIgnoreCase((String) valueGetter.get()));
        }

        // Otherwise
        LOGGER.log(Level.WARNING, String.format("Cannot handle query operator \"%s\"", operator.getOperatorSymbol()));
        return Optional.empty();
    }

    @NonNull
    private Optional<TableFilterList.Filter<I>> createFilter(
            @NonNull DBConnection.Column<I, ?> column, @NonNull QueryOperator<?> operator) {
        Optional<? extends ColumnPattern<?, I>> columnPattern = column.getPattern();
        if (columnPattern.isEmpty()) {
            LOGGER.log(Level.WARNING,
                    String.format("Cannot create filter for column %s since it is not supported by the scheme",
                            column.getName()));
            return Optional.empty();
        }

        Class<?> operatorType = operator.getArgumentConverter().runtimeGenericTypeProvider;
        Function<I, ?> itemFieldGetter
                = item -> operatorType.cast(columnPattern.get().getValue(item, column.getName()));
        Supplier<?> valueGetter = () -> {
            assert valueValid.get() : "Tried to read user specified value for being used in a "
                    + "filter although the user specified value is invalid";
            return operatorType.cast(value.get());
        };

        return createFilterEvaluator(operator, itemFieldGetter, valueGetter)
                .map(fe -> {
                    String operatorRepresentation = PRETTY_OPERATOR_NAME.getOrDefault(operator, operator.getOperatorSymbol());
                    String valueRepresentation = ((Supplier<String>) () -> {
                        if (valueValid.get()) {
                            if (value.get() == null) {
                                return "\"null\"";
                            }
                            return "\"" + value.get().toString() + "\"";
                        }
                        return "";
                    }).get();
                    String description = String.format("%s %s %s", column.getName(),
                            operatorRepresentation, valueRepresentation);
                    return new TableFilterList.Filter<>(fe, description);
                });
    }

    @NonNull
    private ObjectProperty<Optional<TableFilterList.Filter<I>>> trackCurrentFilterInput(
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection,
            @NonNull CheckedComboBox<QueryOperator<?>> operatorSelection) {
        ObjectProperty<Optional<TableFilterList.Filter<I>>> currentFilterInput
                = new SimpleObjectProperty<>(Optional.empty());

        Runnable updateCurrentFilter = () -> {
            if (currentFilterInputValid.get()) {
                currentFilterInput.set(createFilter(
                        columnSelection.getSelectionModel().getSelectedItem(),
                        operatorSelection.getSelectionModel().getSelectedItem()
                ));
            } else {
                currentFilterInput.set(Optional.empty());
            }
        };
        currentFilterInputValid.addListener(obs -> updateCurrentFilter.run());
        columnSelection.getSelectionModel().selectedItemProperty().addListener(obs -> updateCurrentFilter.run());
        operatorSelection.getSelectionModel().selectedItemProperty().addListener(obs -> updateCurrentFilter.run());
        value.addListener(obs -> updateCurrentFilter.run());

        return currentFilterInput;
    }

    @NonNull
    private Node createAddFilterButton(@NonNull TableFilterList<I> control) {
        Button addFilter = new Button();

        URL graphicsResource = TableFilterListSkin.class.getResource("add.png");
        if (graphicsResource == null) {
            LOGGER.log(Level.WARNING, "Could not find icon for add button");
            addFilter.setText("+");
        } else {
            addFilter.setGraphic(new ImageView(graphicsResource.toExternalForm()));
        }

        addFilter.disableProperty()
                .bind(currentFilterInputValid.not());
        addFilter.setOnAction(aevt -> {
            assert currentFilterInputValid.get();
            assert currentFilterInput != null;
            assert currentFilterInput.get().isPresent();
            TableFilterList.Filter<I> filterToConfirm = currentFilterInput.get().get();
            currentFilterInput.set(Optional.empty());
            control.activeFiltersProperty()
                    .add(filterToConfirm);
        });

        return addFilter;
    }
}
