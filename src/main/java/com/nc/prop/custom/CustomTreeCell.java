package com.nc.prop.custom;

import com.nc.prop.Globals;
import com.nc.prop.pojo.TreeNode;
import com.nc.prop.tools.HelperFunctions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author Neural Cortex
 */
public class CustomTreeCell extends TreeCell<TreeNode> {

    private static final Logger _log = LogManager.getLogger(CustomTreeCell.class);
    private final Globals.TAB tab;
    private final CodeArea editor;
    private final HashMap<String, Set<Object>> map;

    public CustomTreeCell(Globals.TAB tab, ResourceBundle bundle, CodeArea editor, HashMap<String, Set<Object>> map) {
        this.tab = tab;
        this.editor = editor;
        this.map = map;

        init(bundle);
    }

    @Override
    protected void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);

        getStyleClass().remove("checked");
        getStyleClass().remove("unchecked");

        if (empty || node == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            if (tab == Globals.TAB.PROPERTIES) {
                if (map.containsKey(node.getFile().getAbsolutePath())) {
                    getStyleClass().add("checked");
                } else {
                    getStyleClass().add("unchecked");
                }
            }

            setText(node.getFile().getName());
            setGraphic(null);
        }
    }

    private void init(ResourceBundle bundle) {
        ContextMenu contextMenu = buildContextMenu(bundle);

        setOnContextMenuRequested(event -> {
            if (isEmpty() || getItem() == null) {
                event.consume();
                return;
            }

            if (!getItem().getFile().isDirectory()) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });
    }

    private ContextMenu buildContextMenu(ResourceBundle bundle) {
        MenuItem miSave = new MenuItem(bundle.getString("mi.save"));
        MenuItem miDel = new MenuItem(bundle.getString("mi.del"));

        miSave.setOnAction(e -> save(bundle));
        miDel.setOnAction(e -> delete(bundle));

        ContextMenu contextMenu = new ContextMenu(miSave, new SeparatorMenuItem(), miDel);
        return contextMenu;
    }

    private void save(ResourceBundle bundle) {
        Optional<ButtonType> res = HelperFunctions.showYesNoDialog(bundle, bundle.getString("msg.save") + " " + getItem().getFile().getName());
        if (res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.YES) {
            Path source = getItem().getFile().toPath();
            Path target = source.resolveSibling(getItem().getFile().getName() + ".bak");

            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(source, editor.getText());
            } catch (IOException ex) {
                _log.error(ex.getLocalizedMessage());
            }

            TreeItem<TreeNode> parent = getTreeItem().getParent();
            if (parent == null) {
                return;
            }

            boolean exists = parent.getChildren().stream()
                    .anyMatch(child -> {
                        File existingFile = child.getValue().getFile();
                        return existingFile != null && existingFile.equals(target.toFile());
                    });

            if (!exists) {
                parent.getChildren().add(new TreeItem<>(new TreeNode(target.toFile())));
            }
        }
    }

    private void delete(ResourceBundle bundle) {
        Optional<ButtonType> res = HelperFunctions.showYesNoDialog(bundle, bundle.getString("mi.del") + " " + getItem().getFile().getName());
        if (res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.YES) {
            TreeItem<TreeNode> parent = getTreeItem().getParent();
            if (parent == null) {
                return;
            }
            parent.getChildren().remove(getTreeItem());

            try {
                Files.deleteIfExists(getItem().getFile().toPath());
            } catch (IOException ex) {
                _log.error(ex.getLocalizedMessage());
            }
        }
    }
}
