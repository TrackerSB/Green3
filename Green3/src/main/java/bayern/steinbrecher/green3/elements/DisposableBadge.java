package bayern.steinbrecher.green3.elements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class DisposableBadge extends Control {
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty disposable = new SimpleBooleanProperty();
    private final ObjectProperty<EventHandler<ActionEvent>> onClose = new SimpleObjectProperty<>();

    public DisposableBadge() {
        this("");
    }

    public DisposableBadge(String text) {
        this(text, true);
    }

    public DisposableBadge(String text, boolean disposable) {
        getStyleClass().add("disposable-badge");
        setText(text);
        setDisposable(disposable);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DisposableBadgeSkin(this);
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return textProperty().get();
    }

    public void setText(String text) {
        textProperty().set(text);
    }

    public BooleanProperty disposableProperty() {
        return disposable;
    }

    public boolean isDisposable() {
        return disposableProperty().get();
    }

    public void setDisposable(boolean disposable) {
        disposableProperty().set(disposable);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onCloseProperty() {
        return onClose;
    }

    public EventHandler<ActionEvent> getOnClose() {
        return onCloseProperty().get();
    }

    public void setOnClose(EventHandler<ActionEvent> onClose) {
        onCloseProperty().set(onClose);
    }
}
