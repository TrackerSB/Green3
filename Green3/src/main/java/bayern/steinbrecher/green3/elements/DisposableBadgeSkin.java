package bayern.steinbrecher.green3.elements;

import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class DisposableBadgeSkin extends SkinBase<DisposableBadge> {

    public DisposableBadgeSkin(@NonNull DisposableBadge control) {
        super(control);

        Text content = new Text();
        content.textProperty()
                .bind(control.textProperty());

        Button closeButton = new Button();
        closeButton.disableProperty()
                .bind(control.disposableProperty().not());
        closeButton.onActionProperty()
                .bind(control.onCloseProperty());

        var container = new HBox(content, closeButton);
        getChildren().add(container);

        // Setup styles
        control.getStyleClass().add("disposable-badge");
    }
}
