package com.nc.prop.pojo;

import java.io.File;
import java.util.List;

/**
 *
 * @author Neural Cortex
 */
public class TreeNode {

    private final File file;
    private List<String> content;
    private boolean checked = false;

    public TreeNode(File file) {
        this.file = file;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    

    public File getFile() {
        return file;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
