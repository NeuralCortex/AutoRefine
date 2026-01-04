package com.nc.prop.controller.tabs;

import com.nc.prop.Globals;
import com.nc.prop.controller.MainController;
import com.nc.prop.controller.PopulateInterface;
import com.nc.prop.custom.CustomTreeCell;
import com.nc.prop.pojo.TreeNode;
import com.nc.prop.styler.JavaStyler;
import com.nc.prop.styler.PropertiesStyler;
import com.nc.prop.styler.UnknownStyler;
import com.nc.prop.task.PropertiesTask;
import com.nc.prop.tools.HelperFunctions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

/**
 *
 * @author Neural Cortex
 */
public class PropertiesController implements Initializable, PopulateInterface {

    @FXML
    private HBox hboxTop;
    @FXML
    private HBox hboxContent;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnCheck;
    @FXML
    private Button btnReset;
    @FXML
    private Label lbDir;
    @FXML
    private Label lbStatus;
    @FXML
    private TreeView<TreeNode> tree;
    @FXML
    private BorderPane borderPaneResult;
    @FXML
    private CheckBox cbUnused;
    @FXML
    private CheckBox cbSort;

    private static final Logger _log = LogManager.getLogger(PropertiesController.class);
    private final MainController mainController;

    private final CodeArea editor = new CodeArea();

    private HashMap<String, Set<Object>> map = new HashMap<>();
    private Set<String> usedKeys = new HashSet<>();

    public PropertiesController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        btnOpen.setText(bundle.getString("btn.open"));
        btnCheck.setText(bundle.getString("btn.check"));
        btnReset.setText(bundle.getString("btn.reset"));
        cbSort.setText(bundle.getString("cb.undo.sort"));
        cbUnused.setText(bundle.getString("cb.remove.marker"));

        hboxTop.getStyleClass().add("blue");
        hboxContent.getStyleClass().add("indigo");
        btnCheck.getStyleClass().add("btn-orange");

        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.getStylesheets().add(Globals.CSS_JAVA_PATH);
        editor.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .successionEnds(Duration.ofMillis(200)) // debounce 200ms
                .subscribe(change -> {
                    editor.setStyleSpans(0, switchStyler(editor.getText()));
                });
        borderPaneResult.setCenter(new StackPane(new VirtualizedScrollPane(editor)));

        tree.setCellFactory(tv -> new CustomTreeCell(Globals.TAB.PROPERTIES, bundle, editor, map));
        tree.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            handleTreeItemSelection(bundle, n);
        });

        btnOpen.setOnAction(e -> openDir());
        btnCheck.setOnAction(e -> check(bundle));
        btnReset.setOnAction(e -> reset());
        cbUnused.selectedProperty().addListener((ov, o, n) -> {
            handleOptions(bundle, !n, !cbSort.isSelected());
        });
        cbSort.selectedProperty().addListener((ov, o, n) -> {
            handleOptions(bundle, !cbUnused.isSelected(), !n);
        });
    }

    private void check(ResourceBundle bundle) {
        TreeItem<TreeNode> root = tree.getRoot();
        if (root != null) {
            File file = root.getValue().getFile();
            try {
                PropertiesTask propertiesTask = new PropertiesTask(file);
                HelperFunctions.showProgressDialog(mainController.getStage(), bundle, propertiesTask);
                propertiesTask.setPropertiesTaskListener(result -> {

                    map = result.getMap();
                    usedKeys = result.getUsedKeys();

                    Platform.runLater(() -> {
                        tree.setCellFactory(tv -> new CustomTreeCell(Globals.TAB.PROPERTIES, bundle, editor, map));
                        tree.refresh();

                        mainController.getLbStatus().setText(MessageFormat.format(bundle.getString("lb.total"), map.size(), usedKeys.size()));
                        TreeItem<TreeNode> node = tree.getSelectionModel().getSelectedItem();
                        if (node != null) {
                            if (map.containsKey(node.getValue().getFile().getAbsolutePath())) {
                                handleOptions(bundle, !cbUnused.isSelected(), !cbSort.isSelected());

                            }
                        }
                    });
                });
            } catch (Exception ex) {
                _log.error(ex.getLocalizedMessage());
            }
        } else {
            HelperFunctions.showAlertDialog(bundle, Alert.AlertType.ERROR, bundle.getString("msg.open.first"));
        }
    }

    private void openDir() {
        reset();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        String propDir = Globals.propman.getProperty(Globals.PATH_PROP_DIR, System.getProperty("user.dir"));
        directoryChooser.setInitialDirectory(new File(propDir));

        File file = directoryChooser.showDialog(mainController.getStage());
        if (file != null) {
            lbDir.setText(file.getAbsolutePath());

            TreeItem<TreeNode> rootItem = HelperFunctions.createLazyNode(new TreeNode(file));
            rootItem.setExpanded(true);

            tree.setRoot(rootItem);

            HelperFunctions.loadChildrenIfNeeded(rootItem);

            Globals.propman.setProperty(Globals.PATH_PROP_DIR, file.getAbsolutePath());
            Globals.propman.save();
        }
    }

    private void handleOptions(ResourceBundle bundle, boolean used, boolean sort) {
        TreeItem<TreeNode> node = tree.getSelectionModel().getSelectedItem();
        if (node != null && node.getValue().getContent() != null) {
            String modContent = modifyContent(bundle, node, used, sort);
            showInEditor(modContent);
        }
    }

    private void handleTreeItemSelection(ResourceBundle bundle, TreeItem<TreeNode> node) {
        if (node != null) {
            try {
                File file = node.getValue().getFile();
                if (file != null && file.isFile()) {
                    List<String> content = Files.readAllLines(file.toPath());
                    node.getValue().setContent(content);
                    String modContent = modifyContent(bundle, node, !cbUnused.isSelected(), !cbSort.isSelected());
                    showInEditor(modContent);
                }
            } catch (IOException ex) {
                _log.error(ex.getLocalizedMessage());
            }
        }

        editor.moveTo(0);
        editor.requestFollowCaret();
    }

    private String modifyContent(ResourceBundle bundle, TreeItem<TreeNode> node, boolean used, boolean sort) {
        List<String> content = new ArrayList<>(node.getValue().getContent());
        List<String> propLines = new ArrayList<>();
        List<String> unused = new ArrayList<>();
        int countUsed = 0;
        int countUnused = 0;
        if (map.containsKey(node.getValue().getFile().getAbsolutePath())) {
            Set<Object> propKeys = map.get(node.getValue().getFile().getAbsolutePath());

            for (Object key : propKeys) {

                if (!usedKeys.contains(key.toString())) {
                    int idx = findIndexOfKey(content, key.toString());
                    if (used) {
                        content.set(idx, Globals.UNUSED_FLAG_PROPS + content.get(idx));
                    } else {
                        content.remove(idx);
                    }
                    countUnused++;
                } else {
                    String line = findLineOfKey(content, key.toString());
                    propLines.add(line);
                    countUsed++;
                }
            }
        }

        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i);
            if (line.startsWith(Globals.UNUSED_FLAG_PROPS)) {
                unused.add(line);
            }
        }

        if (sort) {
            propLines.sort(String::compareTo);
        }
        content.removeAll(propLines);
        content.addAll(propLines);

        if (!used) {
            content.removeAll(unused);
        }

        if (node.getValue().getFile().getName().endsWith(".properties")) {
            lbStatus.setText(MessageFormat.format(bundle.getString("lb.prop.status"), countUsed, countUnused));
        } else {
            lbStatus.setText("");
        }
        return String.join("\n", content);
    }

    private String findLineOfKey(List<String> content, String key) {
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).matches("^" + Pattern.quote(key) + "\\s*=.*")) {
                return content.get(i);
            }
        }
        return null;
    }

    private int findIndexOfKey(List<String> content, String key) {
        int idx = -1;
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).matches("^" + Pattern.quote(key) + "\\s*=.*")) {
                return i;
            }
        }
        return idx;
    }

    private void showInEditor(String content) {
        editor.clear();
        editor.replaceText(content);
        editor.setStyleSpans(0, switchStyler(content));
    }

    private StyleSpans<Collection<String>> switchStyler(String content) {
        TreeItem<TreeNode> node = tree.getSelectionModel().getSelectedItem();
        if (node != null && node.getValue().getFile() != null) {
            File file = node.getValue().getFile();
            if (file.getName().endsWith(".properties")) {
                return PropertiesStyler.computeHighlighting(content);
            } else if (file.getName().endsWith(".java")) {
                return JavaStyler.computeHighlighting(content);
            }
        }
        return UnknownStyler.computeHighlighting(content);
    }

    @Override
    public void populate() {

    }

    @Override
    public void reset() {
        mainController.getLbStatus().setText("");
        lbDir.setText("");
        lbStatus.setText("");
        tree.setRoot(null);
        map.clear();
        editor.clear();
        cbUnused.setSelected(false);
        cbSort.setSelected(false);
    }

    @Override
    public void clear() {
    }
}
