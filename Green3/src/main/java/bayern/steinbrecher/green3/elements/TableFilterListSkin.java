package bayern.steinbrecher.green3.elements;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class TableFilterListSkin extends SkinBase<TableFilterList> {

    public TableFilterListSkin(@NonNull TableFilterList control) {
        super(control);

        Text filterDescription = new Text(ControlResources.RESOURCES.getString("filters"));

        Node activeFilterContainer = createActiveFilterContainer(control);

        Node filterInputField = createFilterInputField();
        HBox.setHgrow(filterInputField, Priority.ALWAYS);

        getChildren()
                .add(new HBox(filterDescription, activeFilterContainer, filterInputField));
    }

    @NonNull
    private static Node createActiveFilterContainer(@NonNull TableFilterList control) {
        DisposableBadge noFilterBadge = new DisposableBadge(ControlResources.RESOURCES.getString("none"), false);
        HBox activeFilterContainer = new HBox();

        ChangeListener<Boolean> activeFiltersEmptyListener = (obs, wasEmpty, isEmpty) -> {
            if (isEmpty) {
                if (!activeFilterContainer.getChildren().contains(noFilterBadge)) {
                    activeFilterContainer.getChildren()
                            .add(noFilterBadge);
                }
            } else {
                activeFilterContainer.getChildren()
                        .remove(noFilterBadge);
            }
        };
        control.activeFiltersProperty()
                .emptyProperty()
                .addListener(activeFiltersEmptyListener);
        activeFiltersEmptyListener.changed(null, null, control.isNoActiveFilters());

        return activeFilterContainer;
    }

    @NonNull
    private static Node createFilterInputField() {
        CheckedTextField filterInputField = new CheckedTextField();
        filterInputField.checkedProperty()
                .bind(filterInputField.emptyProperty().not());
        filterInputField.setOnAction(aevt -> {
            if (filterInputField.isValid()) {
                System.out.println("Entered: " + filterInputField.textProperty().get());
                filterInputField.textProperty().set("");
            } else {
                System.out.println("Current input is not valid");
            }
        });
        ReadOnlyBooleanProperty filterInputValid = new SimpleBooleanProperty(false); // FIXME Implement check
        filterInputField.addReport(new ReportEntry("invalidFilter", ReportType.ERROR, filterInputValid.not()));

        return filterInputField;
    }
}
