module bayern.steinbrecher.Green3 {
    requires javafx.graphics;
    requires bayern.steinbrecher.ScreenSwitcher;

    requires static lombok;

    exports bayern.steinbrecher.green3 to javafx.graphics;
    exports bayern.steinbrecher.green3.screens to javafx.fxml;
}