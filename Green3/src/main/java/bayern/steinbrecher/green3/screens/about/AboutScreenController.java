package bayern.steinbrecher.green3.screens.about;

import bayern.steinbrecher.green3.BuildConfig;
import bayern.steinbrecher.screenswitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public class AboutScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(AboutScreenController.class.getName());

    // List of pairs of resource key for entry and value of entry
    private static final String BUILD_TIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .format(Instant.ofEpochMilli(BuildConfig.BUILD_TIME)
                    .atZone(ZoneId.systemDefault()));
    private static final List<Pair<String, String>> BUILD_INFO_ENTRIES = List.of(
            new Pair<>("buildTime", BUILD_TIME),
            new Pair<>("featureSet", "Ultimate")
    );

    // Map from author names to resource keys of their roles
    private static final List<Pair<String, Collection<String>>> AUTHOR_INFO_ENTRIES = List.of(
            new Pair<>("Stefan \"TrackerSB\" Huber", List.of("founder", "mainDeveloper")),
            new Pair<>("\"Smashicons\"", List.of("iconDesigner")),
            new Pair<>("\"Freepik\"", List.of("iconDesigner")),
            new Pair<>("\"Dimitry Miroliubov\"", List.of("iconDesigner"))
    );

    // Map license names to license paths
    private static final Path LICENSES_ROOT_DIR = Paths.get("licenses");
    private static final Map<String, Path> LICENSES = Map.of(
            "Flaticon", LICENSES_ROOT_DIR.resolve("flaticon.pdf"),
            "Essential Collection", LICENSES_ROOT_DIR.resolve("148705-essential-collection.pdf"),
            "Computer", LICENSES_ROOT_DIR.resolve("3076315-computer.pdf"),
            "Business and Management", LICENSES_ROOT_DIR.resolve("292082-business-and-management.pdf"),
            "Clipboard", LICENSES_ROOT_DIR.resolve("340058-clipboard.pdf")
    );

    @FXML
    private Text appName;
    @FXML
    private Text appVersion;
    @FXML
    private GridPane authorInfo;
    @FXML
    private GridPane buildInfo;
    @FXML
    private VBox licensesInfo;
    @FXML
    private ResourceBundle resources;

    @FXML
    private void initialize() {
        appName.setText(BuildConfig.APP_NAME);
        appVersion.setText(String.format("%s (\"%s\")", BuildConfig.APP_VERSION, BuildConfig.APP_VERSION_NICKNAME));

        int currentRow = 0;
        for (final Pair<String, Collection<String>> entry : AUTHOR_INFO_ENTRIES) {
            authorInfo.add(new Text(entry.getKey()), 0, currentRow);
            final String rolesList = entry.getValue()
                    .stream()
                    .map(resources::getString)
                    .collect(Collectors.joining(", "));
            authorInfo.add(new Text(rolesList), 1, currentRow);
            currentRow++;
        }

        currentRow = 0;
        for (final Pair<String, String> entry : BUILD_INFO_ENTRIES) {
            buildInfo.add(new Text(resources.getString(entry.getKey())), 0, currentRow);
            buildInfo.add(new Text(entry.getValue()), 1, currentRow);
            currentRow++;
        }

        LICENSES.forEach((name, path) -> {
            final Hyperlink licenseLink = new Hyperlink(name);
            licenseLink.setOnAction(aevt -> {
                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not show license to user", ex);
                }
            });
            licensesInfo.getChildren()
                    .add(licenseLink);
        });
    }
}
