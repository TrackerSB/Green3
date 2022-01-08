package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.CheckedDatePicker;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.QueryOperator;
import bayern.steinbrecher.dbConnector.scheme.ColumnPattern;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.When;
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
import javafx.scene.control.CheckBox;
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
import java.time.LocalDate;
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
            QueryOperator.LIKE, ControlResources.RESOURCES.getString("sameAs").toLowerCase(Locale.ROOT),
            // LocalDate operators
            QueryOperator.IS_BEFORE_DATE, ControlResources.RESOURCES.getString("isBeforeDate").toLowerCase(Locale.ROOT),
            QueryOperator.IS_AT_DATE, ControlResources.RESOURCES.getString("isAtDate").toLowerCase(Locale.ROOT),
            QueryOperator.IS_AFTER_DATE, ControlResources.RESOURCES.getString("isAfterDate").toLowerCase(Locale.ROOT)
    );
    private final MapProperty<TableFilterList.Filter<I>, DisposableBadge> visibleBadges
            = new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final BooleanProperty valueValid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Boolean> nullValue = new SimpleObjectProperty<>(null);
    private final BooleanBinding currentFilterInputValid;
    private final ObjectProperty<Optional<TableFilterList.Filter<I>>> currentFilterInput;

    public TableFilterListSkin(@NonNull TableFilterList<I> control) {
        super(control);

        Text filterDescription = new Text(ControlResources.RESOURCES.getString("filters"));

        Node activeFilterContainer = createActiveFilterContainer(control);

        CheckedComboBox<DBConnection.Column<I, ?>> columnSelection = createColumnSelection(control.tableProperty());

        CheckBox nullValueSelector = createNullValueSelector(columnSelection);

        CheckedComboBox<QueryOperator<?>> operatorSelection
                = createOperatorSelection(columnSelection, nullValueSelector);

        Node valueContainer = createValueContainer(columnSelection, nullValueSelector);

        Node clearFilter = createClearFilterButton(columnSelection);
        HBox.setHgrow(clearFilter, Priority.ALWAYS);

        currentFilterInputValid = columnSelection.validProperty()
                .and(operatorSelection.validProperty())
                .and(valueValid);
        currentFilterInput = trackCurrentFilterInput(columnSelection, operatorSelection, nullValueSelector);
        currentFilterInput.addListener((obs, previousUnconfirmedFilter, currentUnconfirmedFilter) -> {
            previousUnconfirmedFilter.ifPresent(filter -> control.activeFiltersProperty().remove(filter));
            currentUnconfirmedFilter.ifPresent(filter -> control.activeFiltersProperty().add(filter));
        });

        Node addFilter = createAddFilterButton(control, columnSelection);

        getChildren()
                .add(new HBox(filterDescription, activeFilterContainer, addFilter, columnSelection, nullValueSelector,
                        operatorSelection, valueContainer, clearFilter));
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
            LOGGER.log(Level.FINE, "Cannot remove badge for filter which was not associated");
        }
    }

    @NonNull
    private Pane createActiveFilterContainer(@NonNull TableFilterList<I> control) {
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
        var noFilterBadge = new DisposableBadge(ControlResources.RESOURCES.getString("none"), false);
        visibleBadges.addListener(
                (MapChangeListener<? super TableFilterList.Filter<I>, ? super DisposableBadge>) change -> {
                    /* NOTE 2021-12-30: It is required to treat removals before additions in order to handle
                     * replacements correctly.
                     */
                    if (change.wasRemoved()) {
                        Platform.runLater(() -> activeFilterContainer.getChildren().remove(change.getValueRemoved()));
                        if (activeFilterContainer.getChildren().isEmpty()) {
                            addBadge(null, noFilterBadge);
                        }
                    }
                    if (change.wasAdded()) {
                        Platform.runLater(() -> activeFilterContainer.getChildren().add(change.getValueAdded()));
                        if (change.getKey() != null && visibleBadges.containsKey(null)) {
                            removeBadge(null);
                        }
                    }
                });
        visibleBadges.put(null, noFilterBadge);

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
                            Platform.runLater(() -> setText(item.name()));
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
                    columnSelection.getItems().sort((cA, cB) -> cA.name().compareToIgnoreCase(cB.name()));
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
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection, @NonNull CheckBox nullValueSelector) {
        CheckedComboBox<QueryOperator<?>> operatorSelection = new CheckedComboBox<>();
        operatorSelection.setPlaceholder(new Text(ControlResources.RESOURCES.getString("unsupported")));
        operatorSelection.setEditable(false);

        BooleanExpression operatorRequired = nullValueSelector.indeterminateProperty();
        operatorSelection.disableProperty()
                .bind(operatorRequired.not());
        operatorSelection.checkedProperty()
                .bind(operatorRequired);

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
                    boolean operatorTypeChanged;
                    if (previouslySelected == null || currentlySelected == null) {
                        operatorTypeChanged = true;
                    } else {
                        Class<?> previousColumnType = previouslySelected.columnType();
                        Class<?> currentColumnType = currentlySelected.columnType();
                        operatorTypeChanged = !previousColumnType.equals(currentColumnType);
                    }

                    if (operatorTypeChanged) {
                        operatorSelection.getSelectionModel().clearSelection();
                        operatorSelection.getItems().clear();

                        if (currentlySelected != null) {
                            Class<?> currentColumnType = currentlySelected.columnType();
                            if (Boolean.class.isAssignableFrom(currentColumnType)) {
                                operatorSelection.getItems().addAll(QueryOperator.BOOLEAN_OPERATORS);
                            } else if (String.class.isAssignableFrom(currentColumnType)) {
                                operatorSelection.getItems().addAll(QueryOperator.STRING_OPERATORS);
                            } else if (LocalDate.class.isAssignableFrom(currentColumnType)) {
                                operatorSelection.getItems().addAll(QueryOperator.LOCALDATE_OPERATORS);
                            } else {
                                LOGGER.log(Level.WARNING,
                                        String.format("The type %s of the selected column %s is unsupported",
                                                currentColumnType.getName(), currentlySelected.name()));
                            }
                        }
                    }
                };
        columnSelection.valueProperty()
                .addListener(selectedItemChanged);
        selectedItemChanged.changed(null, null, columnSelection.getValue());

        return operatorSelection;
    }

    @NonNull
    private CheckBox createNullValueSelector(@NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection) {
        var nullValueSelector = new CheckBox();
        nullValueSelector.setAllowIndeterminate(true);
        nullValueSelector.setIndeterminate(true);

        ObjectProperty<Boolean> selected = new SimpleObjectProperty<>();
        selected.bind(nullValueSelector.selectedProperty());
        nullValue.bind(
                new When(nullValueSelector.indeterminateProperty())
                        .then((Boolean) null)
                        .otherwise(selected));

        columnSelection.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, previousColumn, currentColumn) -> {
                    nullValueSelector.setIndeterminate(true);
                    nullValueSelector.setDisable(!currentColumn.nullable());
                });

        return nullValueSelector;
    }

    @NonNull
    private Node createValueContainer(
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection, @NonNull CheckBox nullValueSelector) {
        Pane valueContainer = new HBox();

        BooleanExpression valueRequired = nullValueSelector.indeterminateProperty();
        valueContainer.disableProperty()
                .bind(valueRequired.not());

        ChangeListener<DBConnection.Column<?, ?>> selectedItemChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    valueContainer.getChildren().clear();
                    if (currentlySelected != null) {
                        Class<?> columnType = currentlySelected.columnType();
                        if (Boolean.class.isAssignableFrom(columnType)) {
                            valueValid.unbind();
                            valueValid.set(true);
                            value.unbind();
                            value.set(null);
                        } else if (String.class.isAssignableFrom(columnType)) {
                            var inputField = new CheckedTextField();
                            inputField.checkedProperty()
                                    .bind(valueRequired);
                            valueContainer.getChildren()
                                    .add(inputField);
                            valueValid.bind(inputField.validProperty());
                            value.bind(inputField.textProperty());
                        } else if (LocalDate.class.isAssignableFrom(columnType)) {
                            var datePicker = new CheckedDatePicker();
                            datePicker.checkedProperty()
                                    .bind(valueRequired);
                            valueContainer.getChildren()
                                    .add(datePicker);
                            valueValid.bind(datePicker.validProperty());
                            value.bind(datePicker.valueProperty());
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The type %s of the selected column %s is unsupported",
                                            columnType.getName(), currentlySelected.name()));
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
    private Node createClearFilterButton(@NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection) {
        var clearFilter = new Button("X");
        clearFilter.setOnAction(aevt -> columnSelection.getSelectionModel().clearSelection());
        return clearFilter;
    }

    @NonNull
    private Optional<Predicate<I>> createFilterEvaluator(
            @NonNull QueryOperator<?> operator, @NonNull Function<I, ?> itemFieldGetter,
            @NonNull Supplier<?> valueGetter) {
        // Boolean operators
        if (operator == QueryOperator.IS_FALSE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && !((Boolean) fieldValue);
            });
        }
        if (operator == QueryOperator.IS_TRUE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && (Boolean) fieldValue;
            });
        }

        // String operators
        if (operator == QueryOperator.CONTAINS) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && ((String) fieldValue)
                        .toLowerCase(Locale.ROOT)
                        // FIXME What happens if value is null?
                        .contains(((String) valueGetter.get()).toLowerCase(Locale.ROOT));
            });
        }
        if (operator == QueryOperator.LIKE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && ((String) fieldValue)
                        .toLowerCase(Locale.ROOT)
                        // FIXME What happens if value is null?
                        .equalsIgnoreCase(((String) valueGetter.get()).toLowerCase(Locale.ROOT));
            });
        }

        // LocalDate operator
        if (operator == QueryOperator.IS_BEFORE_DATE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && ((LocalDate) fieldValue).isBefore((LocalDate) valueGetter.get());
            });
        }
        if (operator == QueryOperator.IS_AT_DATE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && fieldValue.equals(valueGetter.get());
            });
        }
        if (operator == QueryOperator.IS_AFTER_DATE) {
            return Optional.of(item -> {
                Object fieldValue = itemFieldGetter.apply(item);
                return fieldValue != null
                        && ((LocalDate) fieldValue).isAfter((LocalDate) valueGetter.get());
            });
        }

        // Otherwise
        LOGGER.log(Level.WARNING, String.format("Cannot handle query operator \"%s\"", operator.getOperatorSymbol()));
        return Optional.empty();
    }

    @NonNull
    private Optional<TableFilterList.Filter<I>> createFilter(
            @NonNull DBConnection.Column<I, ?> column, QueryOperator<?> operator) {
        Optional<? extends ColumnPattern<?, I>> columnPattern = column.pattern();
        if (columnPattern.isEmpty()) {
            LOGGER.log(Level.WARNING,
                    String.format("Cannot create filter for column %s since it is not supported by the scheme",
                            column.name()));
            return Optional.empty();
        }

        Class<?> columnType = column.columnType();
        Function<I, ?> itemFieldGetter = item -> {
            try {
                return columnType.cast(columnPattern.get().getValue(item, column.name()));
            } catch (ClassCastException ex) {
                /* NOTE 2022-01-01: Since the order, in which listeners are executed, is not reliable it may happen,
                 * that, when the type of the selected column changes, the operators are not updated yet and thus try to
                 * cast a field of the item to a wrong type.
                 * However, this is not a problem, since after updating the operators another revalidation of the filter
                 * occurs.
                 */
                return null;
            }
        };
        Supplier<?> valueGetter = () -> {
            assert valueValid.get() : "Tried to read user specified value for being used in a "
                    + "filter although the user specified value is invalid";

            try {
                return columnType.cast(value.get());
            } catch (ClassCastException ex) {
                /* NOTE 2022-01-01: Since the order, in which listeners are executed, is not reliable it may happen,
                 * that, when the type of the selected column changes, the operators are not updated yet and thus try to
                 * cast a field of the item to a wrong type.
                 * However, this is not a problem, since after updating the operators another revalidation of the filter
                 * occurs.
                 */
                return null;
            }
        };

        // If checking column for (not) having a null value
        if (nullValue.get() != null) {
            if (nullValue.get()) {
                return Optional.of(new TableFilterList.Filter<>(item -> itemFieldGetter.apply(item) == null, "")); // FIXME Add description
            }
            return Optional.of(new TableFilterList.Filter<>(item -> itemFieldGetter.apply(item) != null, "")); // FIXME Add description
        }

        // Otherwise, compare columns using the selected operator against a given value
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
                    String description = String.format("%s %s %s", column.name(),
                            operatorRepresentation, valueRepresentation);
                    return new TableFilterList.Filter<>(fe, description);
                });
    }

    @NonNull
    private ObjectProperty<Optional<TableFilterList.Filter<I>>> trackCurrentFilterInput(
            @NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection,
            @NonNull CheckedComboBox<QueryOperator<?>> operatorSelection,
            @NonNull CheckBox nullValueSelector) {
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
        nullValueSelector.indeterminateProperty().addListener(obs -> updateCurrentFilter.run());
        nullValueSelector.selectedProperty().addListener(obs -> updateCurrentFilter.run());
        value.addListener(obs -> updateCurrentFilter.run());

        return currentFilterInput;
    }

    @NonNull
    private Node createAddFilterButton(@NonNull TableFilterList<I> control,
                                       CheckedComboBox<DBConnection.Column<I, ?>> columnSelection) {
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
            columnSelection.getSelectionModel().clearSelection();
            control.activeFiltersProperty()
                    .add(filterToConfirm);
        });

        return addFilter;
    }
}
