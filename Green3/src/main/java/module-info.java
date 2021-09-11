module bayern.steinbrecher.Green3 {
    requires bayern.steinbrecher.DBConnector;
    requires bayern.steinbrecher.ScreenSwitcher;
    requires bayern.steinbrecher.SepaXMLGenerator;
    requires io.soabase.recordbuilder.core;
    requires java.compiler;
    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires static lombok;

    opens bayern.steinbrecher.green3.screens to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.about to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.featureSelection to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.memberManagement to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.profileSettings to javafx.fxml;
    opens bayern.steinbrecher.green3.screens.welcome to javafx.fxml;

    exports bayern.steinbrecher.green3 to javafx.graphics;
    exports bayern.steinbrecher.green3.features to javafx.graphics;
}