package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.QueryCondition;
import bayern.steinbrecher.dbConnector.query.SupportedDBMS;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterList extends Control implements Reportable {
    private final CheckableControlBase<TableFilterList> ccBase = new CheckableControlBase<>(this);
    private final ListProperty<QueryCondition<?>> activeFilters
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ReadOnlyBooleanProperty noActiveFilters = activeFilters.emptyProperty();
    private final ObjectProperty<DBConnection.Table<?, ?>> table = new SimpleObjectProperty<>(null);
    private final ObjectProperty<SupportedDBMS> dbms = new SimpleObjectProperty<>(SupportedDBMS.MY_SQL);

    public TableFilterList() {
        getStyleClass()
                .add("table-filter-list");

        tableProperty()
                .addListener((obs, previousScheme, currentScheme) -> activeFiltersProperty().clear());
        dbmsProperty()
                .addListener((obs, previousDbms, currentDbms) -> activeFiltersProperty().clear());
    }

    public ListProperty<QueryCondition<?>> activeFiltersProperty() {
        return activeFilters;
    }

    public ObservableList<QueryCondition<?>> getActiveFilters() {
        return activeFiltersProperty().get();
    }

    public void setActiveFilters(@NonNull ObservableList<QueryCondition<?>> activeFilters) {
        activeFiltersProperty().set(activeFilters);
    }

    public ReadOnlyBooleanProperty noActiveFiltersProperty() {
        return noActiveFilters;
    }

    public boolean isNoActiveFilters() {
        return noActiveFiltersProperty().get();
    }

    public ObjectProperty<DBConnection.Table<?, ?>> tableProperty() {
        return table;
    }

    public DBConnection.Table<?, ?> getTable() {
        return tableProperty().get();
    }

    public void setTable(@NonNull DBConnection.Table<?, ?> table) {
        tableProperty().set(table);
    }

    public ObjectProperty<SupportedDBMS> dbmsProperty() {
        return dbms;
    }

    public SupportedDBMS getDbms() {
        return dbmsProperty().get();
    }

    public void setDbms(@NonNull SupportedDBMS dbms) {
        dbmsProperty().set(dbms);
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
