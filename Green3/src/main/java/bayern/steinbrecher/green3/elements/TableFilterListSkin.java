package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.query.QueryOperator;
import bayern.steinbrecher.dbConnector.scheme.ColumnPattern;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final Map<TableFilterList.Filter<I>, DisposableBadge> visibleBadges = new HashMap<>();
    private final BooleanProperty valueValid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(null);
    private final BooleanBinding unconfirmedFilterValid;
    private final ObjectProperty<Optional<TableFilterList.Filter<I>>> unconfirmedFilter
            = new SimpleObjectProperty<>(Optional.empty());

    public TableFilterListSkin(@NonNull TableFilterList<I> control) {
        super(control);

        Text filterDescription = new Text(ControlResources.RESOURCES.getString("filters"));

        Node activeFilterContainer = createActiveFilterContainer(control);

        CheckedComboBox<DBConnection.Column<I, ?>> columnSelection = createColumnSelection(control.tableProperty());

        CheckedComboBox<QueryOperator<?>> operatorSelection = createOperatorSelection(columnSelection);

        Node valueContainer = createValueContainer(columnSelection);
        HBox.setHgrow(valueContainer, Priority.ALWAYS);

        unconfirmedFilterValid = columnSelection.validProperty()
                .and(operatorSelection.validProperty())
                .and(valueValid);
        setupFilterPreview(columnSelection, operatorSelection);
        unconfirmedFilter.addListener((obs, previousUnconfirmedFilter, currentUnconfirmedFilter) -> {
            if (previousUnconfirmedFilter.isPresent()
                    && visibleBadges.containsKey(previousUnconfirmedFilter.get())
                    && !visibleBadges.get(previousUnconfirmedFilter.get()).isDisposable()) {
                // Remove the previous unconfirmed filter only if it was not confirmed
                control.activeFiltersProperty()
                        .remove(previousUnconfirmedFilter.get());
            }

            if (currentUnconfirmedFilter.isEmpty()) {
                columnSelection.getSelectionModel()
                        .clearSelection();
            }

            // Add a filter to the elements' property if the current user input provides a filter
            if (unconfirmedFilterValid.get()) {
                control.activeFiltersProperty()
                        .add(currentUnconfirmedFilter.orElseThrow());
            }
        });

        Node addFilter = createAddFilterButton();

        getChildren()
                .add(new HBox(filterDescription, activeFilterContainer, addFilter, columnSelection, operatorSelection,
                        valueContainer));
    }

    @NonNull
    private Pane createActiveFilterContainer(@NonNull TableFilterList<I> control) {
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
                .addListener((ListChangeListener<? super TableFilterList.Filter<I>>) change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (TableFilterList.Filter<I> addedCondition : change.getAddedSubList()) {
                                boolean addedUnconfirmedFilter = unconfirmedFilterValid.get()
                                        && addedCondition.equals(unconfirmedFilter.get().get());
                                var conditionBadge = new DisposableBadge(
                                        addedCondition.description(), !addedUnconfirmedFilter);
                                conditionBadge.setOnClose(aevt -> control.getActiveFilters().remove(addedCondition));
                                visibleBadges.put(addedCondition, conditionBadge);
                                activeFilterContainer.getChildren()
                                        .add(conditionBadge);
                            }
                        }

                        if (change.wasRemoved()) {
                            for (TableFilterList.Filter<I> removedCondition : change.getRemoved()) {
                                DisposableBadge removedBadge = visibleBadges.remove(removedCondition);
                                if (removedBadge == null) {
                                    LOGGER.log(Level.WARNING, "Could not remove disposable badge since there is no "
                                            + "badge associated with the predicate to remove");
                                } else {
                                    activeFilterContainer.getChildren()
                                            .remove(removedBadge);
                                    boolean removedUnconfirmedFilter = unconfirmedFilterValid.get()
                                            && removedCondition.equals(unconfirmedFilter.get().get())
                                            && !visibleBadges.get(unconfirmedFilter.get().get()).isDisposable();
                                    if (removedUnconfirmedFilter) {
                                        unconfirmedFilter.set(Optional.empty());
                                    }
                                }
                            }
                        }
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
                            setText(item.getName());
                        }
                    }
                };
            }
        };
        columnSelection.setButtonCell(cellFactory.call(null));
        columnSelection.setCellFactory(cellFactory);

        ChangeListener<DBConnection.Table<?, I>> tableListener = (obs, previousTable, currentTable) -> {
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

    private void setupFilterPreview(@NonNull CheckedComboBox<DBConnection.Column<I, ?>> columnSelection,
                                    @NonNull CheckedComboBox<QueryOperator<?>> operatorSelection) {
        InvalidationListener filterPreviewChangedListener = obs -> {
            TableFilterList.Filter<I> newFilter = null;
            if (unconfirmedFilterValid.get()) {
                DBConnection.Column<I, ?> selectedColumn = columnSelection.getSelectionModel().getSelectedItem();
                String columnName = selectedColumn.getName();
                Optional<? extends ColumnPattern<?, I>> columnPattern = selectedColumn.getPattern();
                if (columnPattern.isPresent()) {
                    QueryOperator<?> selectedOperator = operatorSelection.getSelectionModel().getSelectedItem();
                    Class<?> operatorType = selectedOperator.getArgumentConverter().runtimeGenericTypeProvider;
                    Function<I, ?> itemFieldGetter
                            = item -> operatorType.cast(columnPattern.get().getValue(item, columnName));

                    Supplier<?> valueGetter = () -> {
                        if (valueValid.get()) {
                            return operatorType.cast(value.get());
                        }
                        throw new IllegalStateException("Tried to read user specified value for being used in a "
                                + "filter although the user specified value is invalid");
                    };

                    Function<I, Boolean> operator;

                    // Boolean operators
                    if (selectedOperator == QueryOperator.IS_FALSE) {
                        operator = item -> !((Boolean) itemFieldGetter.apply(item));
                    } else if (selectedOperator == QueryOperator.IS_TRUE) {
                        operator = item -> (Boolean) itemFieldGetter.apply(item);
                    }
                    // String operators
                    else if (selectedOperator == QueryOperator.CONTAINS) {
                        operator = item -> ((String) itemFieldGetter.apply(item))
                                .toLowerCase(Locale.ROOT)
                                .contains(((String) valueGetter.get()).toLowerCase(Locale.ROOT));
                    } else if (selectedOperator == QueryOperator.LIKE) {
                        operator = item -> ((String) itemFieldGetter.apply(item))
                                .equalsIgnoreCase((String) valueGetter.get());
                    }
                    // Otherwise
                    else {
                        LOGGER.log(Level.WARNING,
                                String.format("Cannot handle query operator \"%s\"",
                                        selectedOperator.getOperatorSymbol()));
                        operator = null;
                    }

                    if (operator != null) {
                        String operatorRepresentation = PRETTY_OPERATOR_NAME.getOrDefault(
                                selectedOperator, selectedOperator.getOperatorSymbol());
                        String valueRepresentation = ((Supplier<String>) () -> {
                            if (valueValid.get()) {
                                if (value.get() == null) {
                                    return "\"null\"";
                                }
                                return "\"" + value.get().toString() + "\"";
                            }
                            return "";
                        }).get();
                        String description = String.format("%s %s %s", columnName,
                                operatorRepresentation, valueRepresentation);
                        newFilter = new TableFilterList.Filter<>(operator::apply, description);
                    }
                } else {
                    LOGGER.log(Level.WARNING,
                            String.format("Cannot create filter for column %s since it is not supported by the scheme",
                                    columnName));
                }
            }
            unconfirmedFilter.set(Optional.ofNullable(newFilter));
        };

        unconfirmedFilterValid.addListener(filterPreviewChangedListener);
        columnSelection.getSelectionModel().selectedItemProperty().addListener(filterPreviewChangedListener);
        operatorSelection.getSelectionModel().selectedItemProperty().addListener(filterPreviewChangedListener);
        value.addListener(filterPreviewChangedListener);
    }

    @NonNull
    private Node createAddFilterButton() {
        Button addFilter = new Button();

        URL graphicsResource = TableFilterListSkin.class.getResource("add.png");
        if (graphicsResource == null) {
            LOGGER.log(Level.WARNING, "Could not find icon for add button");
            addFilter.setText("+");
        } else {
            addFilter.setGraphic(new ImageView(graphicsResource.toExternalForm()));
        }

        BooleanBinding isNewFilterPresent = Bindings.createBooleanBinding(
                () -> unconfirmedFilter.get() != null && unconfirmedFilter.get().isPresent(),
                unconfirmedFilter);

        addFilter.disableProperty()
                .bind(isNewFilterPresent.not());

        addFilter.setOnAction(aevt -> {
            if (isNewFilterPresent.get()) {
                TableFilterList.Filter<I> filterToBeConfirmed = unconfirmedFilter.get().orElseThrow();
                visibleBadges.get(filterToBeConfirmed).setDisposable(true);
                unconfirmedFilter.set(Optional.empty());
            }
        });

        return addFilter;
    }
}
