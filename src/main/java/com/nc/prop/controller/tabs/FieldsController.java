package com.nc.prop.controller.tabs;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.nc.prop.Globals;
import com.nc.prop.controller.MainController;
import com.nc.prop.controller.PopulateInterface;
import com.nc.prop.custom.CustomTreeCell;
import com.nc.prop.pojo.FieldsResult;
import com.nc.prop.pojo.TreeNode;
import com.nc.prop.styler.JavaStyler;
import com.nc.prop.styler.PropertiesStyler;
import com.nc.prop.styler.UnknownStyler;
import com.nc.prop.task.FieldsTask;
import com.nc.prop.tools.HelperFunctions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
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
public class FieldsController implements Initializable, PopulateInterface {

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

    private static final Logger _log = LogManager.getLogger(FieldsController.class);
    private final MainController mainController;

    private final CodeArea editor = new CodeArea();

    private FieldsResult fieldsResult = null;
    private int countFields = 0;
    private int countEnums = 0;
    private int countFieldsUnused = 0;
    private int countEnumsUnused = 0;

    public FieldsController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        btnOpen.setText(bundle.getString("btn.open"));
        btnCheck.setText(bundle.getString("btn.check"));
        btnReset.setText(bundle.getString("btn.reset"));
        cbUnused.setText(bundle.getString("cb.remove.marker"));

        hboxTop.getStyleClass().add("blue");
        hboxContent.getStyleClass().add("indigo");
        btnCheck.getStyleClass().add("btn-orange");

        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.getStylesheets().add(Globals.CSS_JAVA_PATH);
        editor.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .successionEnds(Duration.ofMillis(10))
                .subscribe(change -> {
                    editor.setStyleSpans(0, switchStyler(editor.getText()));
                });
        borderPaneResult.setCenter(new StackPane(new VirtualizedScrollPane(editor)));

        tree.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            handleTreeItemSelection(bundle, n);
        });
        tree.setCellFactory(tv -> new CustomTreeCell(Globals.TAB.FIELDS, bundle, editor, null));

        btnOpen.setOnAction(e -> openDir());
        btnCheck.setOnAction(e -> check(bundle));
        btnReset.setOnAction(e -> reset());
        cbUnused.selectedProperty().addListener((ov, o, n) -> {
            handleOptions(bundle, !n);
        });
    }

    private void check(ResourceBundle bundle) {
        resetStats();
        TreeItem<TreeNode> root = tree.getRoot();
        if (root != null) {
            File file = root.getValue().getFile();
            try {
                TreeItem<TreeNode> sel = tree.getSelectionModel().getSelectedItem();
                if (sel != null && !sel.getValue().getFile().isDirectory()) {
                    FieldsTask fieldsTask = new FieldsTask(file, sel.getValue().getFile());
                    HelperFunctions.showProgressDialog(mainController.getStage(), bundle, fieldsTask);
                    fieldsTask.setFieldsTaskListener(result -> {
                        fieldsResult = result;

                        Platform.runLater(() -> {
                            mainController.getLbStatus().setText(MessageFormat.format(bundle.getString("lb.fields.total"), result.getUsedFields().size(), result.getUsedEnums().size()));
                            TreeItem<TreeNode> node = tree.getSelectionModel().getSelectedItem();
                            if (node != null) {
                                handleOptions(bundle, !cbUnused.isSelected());
                            }
                        });
                    });
                } else {
                    HelperFunctions.showAlertDialog(bundle, Alert.AlertType.ERROR, bundle.getString("msg.sel.file"));
                }
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
        String propDir = Globals.propman.getProperty(Globals.PATH_FIELD_DIR, System.getProperty("user.dir"));
        directoryChooser.setInitialDirectory(new File(propDir));

        File file = directoryChooser.showDialog(mainController.getStage());
        if (file != null) {
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
            combinedTypeSolver.add(new ReflectionTypeSolver(false));
            combinedTypeSolver.add(new JavaParserTypeSolver(file));
            StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

            lbDir.setText(file.getAbsolutePath());

            TreeItem<TreeNode> rootItem = HelperFunctions.createLazyNode(new TreeNode(file));
            rootItem.setExpanded(true);

            tree.setRoot(rootItem);

            HelperFunctions.loadChildrenIfNeeded(rootItem);

            Globals.propman.setProperty(Globals.PATH_FIELD_DIR, file.getAbsolutePath());
            Globals.propman.save();
        }
    }

    private void handleOptions(ResourceBundle bundle, boolean used) {

        TreeItem<TreeNode> node = tree.getSelectionModel().getSelectedItem();
        if (node != null && node.getValue().getContent() != null) {
            String modContent = "";
            if (fieldsResult != null) {
                modContent = modifyContent(bundle, node, !cbUnused.isSelected());
            } else {
                modContent = String.join("\n", node.getValue().getContent());
            }
            showInEditor(modContent);
        }
    }

    private void handleTreeItemSelection(ResourceBundle bundle, TreeItem<TreeNode> node) {
        resetStats();

        if (node != null) {
            try {
                File file = node.getValue().getFile();
                if (file != null && file.isFile()) {
                    List<String> content = Files.readAllLines(file.toPath());
                    node.getValue().setContent(content);
                    String modContent = "";
                    if (fieldsResult != null) {
                        modContent = modifyContent(bundle, node, !cbUnused.isSelected());
                    } else {
                        modContent = String.join("\n", content);
                    }

                    showInEditor(modContent);
                }
            } catch (IOException ex) {
                _log.error(ex.getLocalizedMessage());
            }
        }

        editor.moveTo(0);
        editor.requestFollowCaret();
    }

    private String modifyContent(ResourceBundle bundle, TreeItem<TreeNode> node, boolean used) {
        List<String> content = new ArrayList<>(node.getValue().getContent());

        int enumsTotal = 0;
        countFields = 0;
        countFieldsUnused = 0;
        countEnums = 0;
        countEnumsUnused = 0;

        try {
            CompilationUnit cu = StaticJavaParser.parse(node.getValue().getFile());
            LexicalPreservingPrinter.setup(cu);

            Set<String> onlyInFile = new HashSet<>();
            cu.findAll(VariableDeclarator.class).forEach(var -> {
                var.getInitializer().ifPresent(init -> {
                    init.findAll(NameExpr.class).forEach(nameExpr -> {
                        try {
                            ResolvedValueDeclaration resolved = nameExpr.resolve();
                            if (resolved.isField() && resolved.asField().isStatic()) {
                                String fieldName = resolved.getName();
                                onlyInFile.add(nameExpr.toString());
                            }
                        } catch (Exception ignored) {
                        }
                    });
                });
            });

            cu.findAll(FieldDeclaration.class).stream()
                    .filter(field -> field.isPublic() && field.isStatic())
                    .forEach(fieldDecl -> {
                        fieldDecl.getRange().ifPresent(range -> {
                            int startLine = range.begin.line - 1;
                            int endLine = range.end.line - 1;

                            for (int lineIdx = endLine; lineIdx >= startLine; lineIdx--) {
                                if (lineIdx >= content.size()) {
                                    continue;
                                }

                                String originalLine = content.get(lineIdx);

                                // Avoid double-commenting
                                if (originalLine.trim().startsWith(Globals.UNUSED_FLAG_FIELDS)) {
                                    continue;
                                }

                                String commentedLine = Globals.UNUSED_FLAG_FIELDS + originalLine;

                                if (fieldDecl.resolve().isField()) {
                                    String name = fieldDecl.resolve().asField().getName();

                                    if (!fieldsResult.getUsedFields().contains(name) && !onlyInFile.contains(name)) {
                                        content.set(lineIdx, commentedLine);
                                        countFieldsUnused++;
                                    } else {
                                        countFields++;
                                    }
                                }
                            }
                        });
                    });

            enumsTotal = cu.findAll(EnumDeclaration.class).size();

            cu.findAll(EnumDeclaration.class).stream()
                    .forEach(e -> {
                        e.getRange().ifPresent(range -> {
                            int startLine = range.begin.line - 1;
                            int endLine = range.end.line - 1;

                            for (int lineIdx = endLine; lineIdx >= startLine; lineIdx--) {
                                if (lineIdx >= content.size()) {
                                    continue;
                                }

                                String originalLine = content.get(lineIdx);

                                // Avoid double-commenting
                                if (originalLine.trim().startsWith(Globals.UNUSED_FLAG_FIELDS)) {
                                    continue;
                                }

                                String commentedLine = Globals.UNUSED_FLAG_FIELDS + originalLine;

                                if (!fieldsResult.getUsedEnums().contains(e.getNameAsString())) {
                                    content.set(lineIdx, commentedLine);
                                }
                            }
                        });

                        if (!fieldsResult.getUsedEnums().contains(e.getNameAsString())) {
                            countEnums++;
                        } else {
                            countEnumsUnused++;
                        }
                    });

        } catch (Exception ex) {
            _log.error(ex.getLocalizedMessage());
        }

        for (int i = content.size() - 1; i >= 0; i--) {
            String line = content.get(i);
            if (!used && line.startsWith(Globals.UNUSED_FLAG_FIELDS)) {
                content.remove(i);
            }
        }

        if (node.getValue().getFile().getName().endsWith(".java")) {
            lbStatus.setText(MessageFormat.format(bundle.getString("lb.fields.stat"), countFields, countFieldsUnused, enumsTotal - countEnums, enumsTotal - countEnumsUnused));
        } else {
            lbStatus.setText("");
        }
        return String.join("\n", content);
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

    private void resetStats() {
        countFields = 0;
        countFieldsUnused = 0;
        countEnums = 0;
        countEnumsUnused = 0;
        fieldsResult = null;
        lbStatus.setText("");
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
        fieldsResult = null;
        editor.clear();
        cbUnused.setSelected(false);
    }

    @Override
    public void clear() {
    }
}
