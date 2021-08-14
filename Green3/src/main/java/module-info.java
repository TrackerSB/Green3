module bayern.steinbrecher.Green3 {
    requires bayern.steinbrecher.ScreenSwitcher;
    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires static lombok;

    opens bayern.steinbrecher.green3.screens to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.about to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.welcome to javafx.fxml;

    exports bayern.steinbrecher.green3 to javafx.graphics;
    exports bayern.steinbrecher.green3.features to javafx.graphics;
}