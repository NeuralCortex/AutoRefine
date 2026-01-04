package com.nc.prop.controller;

import com.nc.prop.Globals;
import com.nc.prop.controller.tabs.FieldsController;
import com.nc.prop.controller.tabs.PropertiesController;
import com.nc.prop.tools.HelperFunctions;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *
 * @author Neural Cortex
 */
public class MainController implements Initializable {

    @FXML
    private BorderPane borderPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Label lbStatus;
    @FXML
    private HBox hboxStatus;
    @FXML
    private Label lbInfo;
    @FXML
    private Menu menuFile;
    @FXML
    private Menu menuHelp;
    @FXML
    private MenuItem miClose;
    @FXML
    private MenuItem miAbout;
    @FXML
    private MenuBar menuBar;

    private final Stage stage;

    public MainController(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle bundle) {
        borderPane.setPrefSize(Globals.WIDTH, Globals.HEIGHT);

        init(bundle);

        HelperFunctions.addTab(bundle, tabPane, Globals.FXML_PROPERTIES_PATH, new PropertiesController(this), bundle.getString("tab.props"));
        HelperFunctions.addTab(bundle, tabPane, Globals.FXML_FIELDS_PATH, new FieldsController(this), bundle.getString("tab.fields"));

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                Object obj = n.getContent().getUserData();
                if (obj instanceof PopulateInterface pi) {
                    pi.reset();
                }
            }
        });

        miClose.setOnAction(e -> {
            System.exit(0);
        });

        miAbout.setOnAction(e -> {
            showAboutDlg(bundle);
        });
    }

    private void showAboutDlg(ResourceBundle bundle) {
        HelperFunctions.showAlertDialog(bundle, Alert.AlertType.INFORMATION, bundle.getString("app.name") + "\n" + MessageFormat.format(bundle.getString("app.about"), LocalDate.now().getYear()));
    }

    private void init(ResourceBundle bundle) {
        hboxStatus.getStyleClass().add("blue");

        menuFile.setText(bundle.getString("menu.file"));
        menuHelp.setText(bundle.getString("menu.help"));

        miAbout.setText(bundle.getString("mi.about"));
        miClose.setText(bundle.getString("mi.close"));

        String programmer = bundle.getString("app.about");
        lbInfo.setText(MessageFormat.format(programmer, LocalDate.now().getYear()));
    }

    public Stage getStage() {
        return stage;
    }

    public Label getLbStatus() {
        return lbStatus;
    }

    public Label getLbInfo() {
        return lbInfo;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }
}
