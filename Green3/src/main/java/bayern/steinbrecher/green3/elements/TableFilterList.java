package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterList extends Control implements Reportable {
    private final CheckableControlBase<TableFilterList> ccBase = new CheckableControlBase<>(this);
    private final ListProperty<Object> activeFilters = new SimpleListProperty<>();
    private final ReadOnlyBooleanProperty noActiveFilters = activeFilters.emptyProperty();
    private final ObjectProperty<TableScheme<?, ?>> tableScheme = new SimpleObjectProperty<>(null);

    public TableFilterList() {
        getStyleClass()
                .add("table-filter-list");

        tableSchemeProperty()
                .addListener((obs, previousScheme, currentScheme) -> activeFiltersProperty().clear());
    }

    public ListProperty<Object> activeFiltersProperty() {
        return activeFilters;
    }

    public ObservableList<Object> getActiveFilters() {
        return activeFiltersProperty().get();
    }

    public void setActiveFilters(ObservableList<Object> activeFilters) {
        activeFiltersProperty().set(activeFilters);
    }

    public ReadOnlyBooleanProperty noActiveFiltersProperty() {
        return noActiveFilters;
    }

    public boolean isNoActiveFilters() {
        return noActiveFiltersProperty().get();
    }

    public ObjectProperty<TableScheme<?, ?>> tableSchemeProperty() {
        return tableScheme;
    }

    public TableScheme<?, ?> getTableScheme() {
        return tableSchemeProperty().get();
    }

    public void setTableScheme(TableScheme<?, ?> tableScheme) {
        tableSchemeProperty().set(tableScheme);
    }

    @Override
    protected Skin<TableFilterList> createDefaultSkin() {
        return new TableFilterListSkin(this);
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
}
