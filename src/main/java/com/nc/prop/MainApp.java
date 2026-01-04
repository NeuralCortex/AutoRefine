package com.nc.prop;

import com.nc.prop.controller.MainController;
import java.io.File;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class MainApp extends Application {

    private static final Logger _log = LogManager.getLogger(MainApp.class);
    private final ResourceBundle bundle = ResourceBundle.getBundle(Globals.BUNDLE_PATH, Globals.DEFAULT_LOCALE);

    @Override
    public void start(Stage stage) throws Exception {
        initLogger(Globals.LOG4J2_CONFIG_PATH);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(Globals.FXML_MAIN_PATH), bundle);
        MainController mainController = new MainController(stage);
        loader.setController(mainController);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Globals.CSS_PATH);
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream(Globals.APP_LOGO_PATH)));

        stage.setTitle(bundle.getString("app.name") + " " + MainApp.class.getPackage().getImplementationVersion());
        stage.setScene(scene);
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
        
        _log.info(getClass().getName()+" successfully started.");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            _log.error(ex.getLocalizedMessage());
        }
        launch(args);
    }

    private void initLogger(String path) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(new File(path).toURI());
    }
}
