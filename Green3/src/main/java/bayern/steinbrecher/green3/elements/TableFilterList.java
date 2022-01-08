package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.dbConnector.DBConnection;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import lombok.NonNull;

import java.util.function.Predicate;

/**
 * @param <I> The type of the elements represented by the table to filter.
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterList<I> extends Control implements Reportable {
    private final CheckableControlBase<TableFilterList<?>> ccBase = new CheckableControlBase<>(this);
    private final ListProperty<Filter<I>> activeFilters
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ReadOnlyObjectWrapper<Predicate<I>> filter
            = new ReadOnlyObjectWrapper<>(null);
    private final ReadOnlyBooleanProperty noActiveFilters = activeFilters.emptyProperty();
    private final ObjectProperty<DBConnection.Table<?, I>> table = new SimpleObjectProperty<>(null);

    public TableFilterList() {
        tableProperty()
                .addListener((obs, previousScheme, currentScheme) -> activeFiltersProperty().clear());
        activeFiltersProperty()
                .addListener((obs, previouslyActiveFilters, currentActiveFilters) -> {
                    filter.set(
                            currentActiveFilters.stream()
                                    .map(Filter::predicate)
                                    .reduce(Predicate::and)
                                    .orElse(null)
                    );
                });
    }

    public ListProperty<Filter<I>> activeFiltersProperty() {
        return activeFilters;
    }

    public ObservableList<Filter<I>> getActiveFilters() {
        return activeFiltersProperty().get();
    }

    public void setActiveFilters(@NonNull ObservableList<Filter<I>> activeFilters) {
        activeFiltersProperty().set(activeFilters);
    }

    public ReadOnlyObjectProperty<? extends Predicate<I>> filterProperty() {
        return filter.getReadOnlyProperty();
    }

    public Predicate<I> getFilter() {
        return filterProperty().get();
    }

    public ReadOnlyBooleanProperty noActiveFiltersProperty() {
        return noActiveFilters;
    }

    public boolean isNoActiveFilters() {
        return noActiveFiltersProperty().get();
    }

    public ObjectProperty<DBConnection.Table<?, I>> tableProperty() {
        return table;
    }

    public DBConnection.Table<?, ?> getTable() {
        return tableProperty().get();
    }

    public void setTable(@NonNull DBConnection.Table<?, I> table) {
        tableProperty().set(table);
    }

    @Override
    protected Skin<TableFilterList<I>> createDefaultSkin() {
        return new TableFilterListSkin<>(this);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return ccBase.validProperty();
    }

    @Override
    public boolean addValidityConstraint(ObservableBooleanValue constraint) {
        return ccBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return ccBase.getReports();
    }

    @Override
    public boolean addReport(ReportEntry report) {
        return ccBase.addReport(report);
    }

    public record Filter<I>(
            Predicate<I> predicate,
            String description
    ) {
    }
}
