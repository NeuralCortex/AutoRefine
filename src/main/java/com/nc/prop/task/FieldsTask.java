package com.nc.prop.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.nc.prop.pojo.FieldsResult;
import com.nc.prop.tools.HelperFunctions;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neural Cortex
 */
public class FieldsTask extends Task<Void> {

    private static final Logger _log = LogManager.getLogger(FieldsTask.class);

    private final File dir;
    private final File file;

    private final Set<String> usedFields = new HashSet<>();
    private final Set<String> usedEnums = new HashSet<>();

    public interface FieldsTaskListener {

        public void getResult(FieldsResult result);
    }
    private FieldsTaskListener fieldsTaskListener;

    public FieldsTask(File dir, File file) {
        this.dir = dir;
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        AtomicInteger totalFiles = new AtomicInteger(0);
        AtomicInteger processedFiles = new AtomicInteger(0);

        JavaSymbolSolver javaSymbolSolver = HelperFunctions.getJavaSolver(dir);
        StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);

        countFiles(dir, totalFiles);
        updateProgress(0, totalFiles.get());

        processFile(dir, file, totalFiles, processedFiles);

        fieldsTaskListener.getResult(new FieldsResult(usedFields, usedEnums));

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

    private void processFile(File dir, File file, AtomicInteger totalFiles, AtomicInteger processedFiles) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                processFile(child, file, totalFiles, processedFiles);
            }
        } else {
            if (dir.getName().endsWith(".java") && !file.getAbsolutePath().equals(dir.getAbsolutePath())) {
                try {
                    CompilationUnit cu = StaticJavaParser.parse(dir);

                    LexicalPreservingPrinter.setup(cu);

                    cu.findAll(FieldAccessExpr.class).stream()
                            .filter(fae -> fae.getScope().isNameExpr())
                            .map(FieldAccessExpr::getNameAsString)
                            .forEach(usedFields::add);

                    cu.findAll(FieldAccessExpr.class).forEach(fae -> {
                        usedEnums.add(fae.getNameAsString());
                    });
                } catch (Exception ex) {
                    _log.error(ex.getLocalizedMessage());
                }
            }

        }
        int processed = processedFiles.incrementAndGet();
        updateProgress(processed, totalFiles.get());
        updateMessage(dir.getName());
    }

    public void setFieldsTaskListener(FieldsTaskListener fieldsTaskListener) {
        this.fieldsTaskListener = fieldsTaskListener;
    }
}
