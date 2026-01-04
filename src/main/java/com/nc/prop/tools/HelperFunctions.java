package com.nc.prop.tools;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.nc.prop.Globals;
import com.nc.prop.pojo.TreeNode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class HelperFunctions {

    public static void centerWindow(Window window) {
        window.addEventHandler(WindowEvent.WINDOW_SHOWN, (WindowEvent event) -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((screenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((screenBounds.getHeight() - window.getHeight()) / 2);
        });
    }

    public Node loadFxml(ResourceBundle bundle, String path, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path), bundle);
            loader.setController(controller);
            Node node = loader.load();
            return node;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Tab addTab(ResourceBundle bundle, TabPane tabPane, String path, Object controller, String tabName) {
        long start = System.currentTimeMillis();
        Tab tab = new Tab(tabName);
        tabPane.getTabs().add(tab);
        HelperFunctions helperFunctions = new HelperFunctions();
        Node node = helperFunctions.loadFxml(bundle, path, controller);
        node.setUserData(controller);
        tab.setContent(node);
        long end = System.currentTimeMillis();
        System.out.println("Loadtime (" + controller.toString() + ") in ms: " + (end - start));
        return tab;
    }

    public static void showAlertDialog(ResourceBundle bundle, Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(type.name());
        alert.setHeaderText(null);//cleaner look
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Globals.CSS_PATH);

        Stage stage = (Stage) dialogPane.getScene().getWindow();
        try {
            stage.getIcons().add(new Image(HelperFunctions.class.getResourceAsStream(Globals.APP_LOGO_PATH)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        HelperFunctions.centerWindow(stage);

        styleDialogButtons(alert);

        alert.showAndWait();
    }

    public static Optional<ButtonType> showYesNoDialog(ResourceBundle bundle, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Alert.AlertType.CONFIRMATION.name());
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().clear();

        ButtonType yesButtonType = new ButtonType(
                bundle.getString("btn.yes"),
                ButtonBar.ButtonData.YES
        );
        ButtonType noButtonType = new ButtonType(
                bundle.getString("btn.no"),
                ButtonBar.ButtonData.NO
        );

        dialogPane.getButtonTypes().addAll(yesButtonType, noButtonType);
        dialogPane.getStylesheets().add(Globals.CSS_PATH);

        Stage stage = (Stage) dialogPane.getScene().getWindow();
        try {
            stage.getIcons().add(new Image(HelperFunctions.class.getResourceAsStream(Globals.APP_LOGO_PATH)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        HelperFunctions.centerWindow(stage);

        styleDialogButtons(alert);

        return alert.showAndWait();
    }

    public static void styleDialogButtons(Dialog<?> dialog) {
        dialog.setOnShown(e
                -> Platform.runLater(()
                        -> styleButtons(dialog)
                )
        );
    }

    private static void styleButtons(Dialog<?> dialog) {
        for (ButtonType type : dialog.getDialogPane().getButtonTypes()) {
            Button btn = (Button) dialog.getDialogPane().lookupButton(type);
            if (type == ButtonType.CANCEL) {
                btn.getStyleClass().add("btn-indigo");
            } else {
                btn.getStyleClass().add("btn-blue");
            }
        }
    }

    public static TreeItem<TreeNode> createLazyNode(TreeNode node) {
        TreeItem<TreeNode> item = new TreeItem<>(node);

        if (node.getFile().isDirectory()) {
            item.getChildren().add(new TreeItem<>(null));

            item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && item.getChildren().size() == 1
                        && item.getChildren().get(0).getValue() == null) {
                    loadChildrenIfNeeded(item);
                }
            });
        }

        return item;
    }

    public static void loadChildrenIfNeeded(TreeItem<TreeNode> parentItem) {
        TreeNode parentFile = parentItem.getValue();
        if (parentFile == null || !parentFile.getFile().isDirectory()) {
            return;
        }

        parentItem.getChildren().clear();

        File[] files = parentFile.getFile().listFiles();
        if (files == null) {
            return;
        }

        Arrays.stream(files)
                .filter(f -> !f.isHidden())
                .sorted((a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) {
                        return -1;
                    }
                    if (!a.isDirectory() && b.isDirectory()) {
                        return 1;
                    }
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .forEach(child -> {
                    TreeItem<TreeNode> childItem = createLazyNode(new TreeNode(child));
                    parentItem.getChildren().add(childItem);
                });
    }

    public static void showProgressDialog(Window owner, ResourceBundle bundle, Task<?> task) {
        Alert progressDialog = new Alert(Alert.AlertType.INFORMATION);
        progressDialog.initOwner(owner);
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        progressDialog.setTitle(bundle.getString("dlg.progress.title"));
        progressDialog.setHeaderText(bundle.getString("dlg.progress.header"));

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        HBox hBox = new HBox();
        hBox.getChildren().add(progressBar);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        Label label = new Label();
        label.textProperty().bind(task.messageProperty());
        vBox.getChildren().addAll(label, hBox);

        progressDialog.getDialogPane().getButtonTypes().clear();

        ButtonType cancelButtonType = new ButtonType(
                bundle.getString("btn.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        progressDialog.getDialogPane().getButtonTypes().addAll(cancelButtonType);
        Button cancelBtn = (Button) progressDialog.getDialogPane().lookupButton(cancelButtonType);
        cancelBtn.getStyleClass().add("btn-blue");
        cancelBtn.setOnAction(e -> {
            if (task.cancel()) {
                System.out.println("Task cancellation requested");
            } else {
                System.out.println("Task already completed or cancelled");
            }
            progressDialog.close();
        });

        progressDialog.getDialogPane().getStylesheets().add(Globals.CSS_PATH);

        progressDialog.getDialogPane().setPrefWidth(400);
        progressDialog.getDialogPane().setContent(vBox);

        // Bind progress
        progressBar.progressProperty().bind(task.progressProperty());

        // Hide dialog on completion or error
        task.setOnSucceeded(e -> {
            progressDialog.close();

        });
        task.setOnFailed(e -> {
            progressDialog.close();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Analysis Failed");
            error.setContentText(task.getException().getMessage());
            error.showAndWait();
        });
        task.setOnCancelled(e -> progressDialog.close());

        // Show dialog and start task in background
        progressDialog.show();

        new Thread(task).start();

    }

    public static JavaSymbolSolver getJavaSolver(File root) {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new JavaParserTypeSolver(root));
        typeSolver.add(new ReflectionTypeSolver(false));

        return new JavaSymbolSolver(typeSolver);
    }
}
