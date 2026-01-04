package com.nc.prop.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.nc.prop.pojo.PropertiesResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neural Cortex
 */
public class PropertiesTask extends Task<Void> {

    private static final Logger _log = LogManager.getLogger(PropertiesTask.class);

    private final File file;
    private final HashMap<String, Set<Object>> map = new HashMap<>();
    private final Set<String> usedKeys = new HashSet<>();

    public interface PropertiesTaskListener {

        public void getResult(PropertiesResult result);
    }
    private PropertiesTaskListener propertiesTaskListener;

    public PropertiesTask(File file) {
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        AtomicInteger totalFiles = new AtomicInteger(0);
        AtomicInteger processedFiles = new AtomicInteger(0);

        countFiles(file, totalFiles);
        updateProgress(0, totalFiles.get());

        // process recursively
        processFile(file, totalFiles, processedFiles);

        propertiesTaskListener.getResult(new PropertiesResult(map, usedKeys));

        return null;
    }

    private void countFiles(File dir, AtomicInteger counter) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                countFiles(child, counter);
            }
        }
        counter.incrementAndGet();
    }

    private void processFile(File file, AtomicInteger totalFiles, AtomicInteger processedFiles) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                processFile(child, totalFiles, processedFiles);
            }
        } else {
            if (file.getName().endsWith(".java")) {
                try {
                    CompilationUnit cu = StaticJavaParser.parse(file);
                    LexicalPreservingPrinter.setup(cu);
                    cu.findAll(MethodCallExpr.class).stream()
                            .filter(m -> m.getNameAsString().equals("getString"))
                            .forEach(call -> {
                                if (call.getArgument(0).isStringLiteralExpr()) {
                                    StringLiteralExpr arg = call.getArgument(0).asStringLiteralExpr();
                                    String content = arg.getValue();
                                    usedKeys.add(content);
                                }
                            });
                } catch (Exception ex) {
                    _log.error(ex.getLocalizedMessage());
                }
            } else if (file.getName().endsWith(".properties")) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Properties props = new Properties();
                    props.load(fis);
                    map.put(file.getAbsolutePath(), props.keySet());
                } catch (Exception ex) {
                    _log.error(ex.getLocalizedMessage());
                }
            }
        }
        int processed = processedFiles.incrementAndGet();
        updateProgress(processed, totalFiles.get());
        updateMessage(file.getName());
    }

    public void setPropertiesTaskListener(PropertiesTaskListener propertiesTaskListener) {
        this.propertiesTaskListener = propertiesTaskListener;
    }
}
