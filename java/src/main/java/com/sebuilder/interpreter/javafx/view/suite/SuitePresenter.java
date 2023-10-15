package com.sebuilder.interpreter.javafx.view.suite;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseChains;
import com.sebuilder.interpreter.javafx.control.dragdrop.DragAndDropSortTreeViewCellFactory;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.HasFileChooser;
import com.sebuilder.interpreter.javafx.view.aspect.AspectView;
import com.sebuilder.interpreter.javafx.view.data.DataSetView;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SuitePresenter implements HasFileChooser {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TreeView<String> treeViewScriptName;
    @FXML
    public MenuItem openDataSource;

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.treeViewScriptName.setCellFactory(new DragAndDropSortTreeViewCellFactory<>() {
                private TestCase dragged;

                @Override
                protected void updateItemCallback(final TreeCell<String> treeCell, final String s, final boolean b) {
                    if (Strings.isNullOrEmpty(s) || b) {
                        treeCell.setText(null);
                    } else {
                        treeCell.setText(s);
                    }
                }

                @Override
                protected void removeDragItemFromPreviousParent(final TreeItem<String> droppedItemParent) {
                    super.removeDragItemFromPreviousParent(droppedItemParent);
                    this.dragged = SuitePresenter.this.application.findTestCase(droppedItemParent.getValue(), this.getDraggedItem().getValue());
                    SuitePresenter.this.application.removeScriptFromChain(droppedItemParent.getValue(), this.getDraggedItem().getValue());
                }

                @Override
                protected void addDropItemToNewParent(final TreeItem<String> droppedItemParent, final int i) {
                    super.addDropItemToNewParent(droppedItemParent, i);
                    SuitePresenter.this.application.addScript(droppedItemParent.getValue(), i, this.dragged);
                    this.dragged = null;
                }

            });
            this.application.suite().addListener((observed, oldValue, newValue) -> this.showScriptView());
            this.application.displayTestCase().addListener((observed, oldValue, newValue) -> {
                if (this.application.getSuite().name().equals(newValue.name())) {
                    this.treeViewScriptName.getSelectionModel().selectFirst();
                } else {
                    this.findItem(newValue).ifPresent(it -> this.treeViewScriptName.getSelectionModel().select(it));
                }
                try {
                    this.openDataSource.setDisable(!observed.getValue().runtimeDataSet().isLoadable() || observed.getValue().loadData().size() == 0);
                } catch (final IOException e) {
                    this.openDataSource.setDisable(true);
                }
            });
            this.showScriptView();
        });
    }

    @FXML
    public void handleOpenDataSource() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new DataSetView()
                .open(this.application.getDisplayTestCaseDataSource(), this.treeViewScriptName.getScene().getWindow()));
    }

    @FXML
    public void handleOpenAspect() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new AspectView()
                .open(this.treeViewScriptName.getScene().getWindow(), this.application.getDisplayTestCase()));
    }

    @FXML
    void handleScriptReplay() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                new InputView().open(this.treeViewScriptName.getScene().getWindow()));
    }

    @FXML
    void handleScriptInsert() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.insertScript();
            this.showScriptView();
        });
    }

    @FXML
    void handleScriptAdd() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.addScript();
            this.showScriptView();
        });
    }

    @FXML
    void handleScriptCreateTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new TemplateView()
                .open(this.treeViewScriptName.getScene().getWindow()));
    }

    @FXML
    void handleScriptImport() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.openDialog("Open Resource File", "json format (*.json)", "*.json");
            if (file != null) {
                this.application.importScript(file);
            }
        });
    }

    @FXML
    void handleScriptDelete() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.removeScript();
            this.showScriptView();
        });
    }

    @FXML
    void handleScriptSave() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            if (Strings.isNullOrEmpty(this.application.getDisplayTestCase().path())) {
                this.saveTestCaseToNewFile();
            } else {
                this.application.saveTestCase();
            }
        });
    }

    @FXML
    void handleScriptSaveAs() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(this::saveTestCaseToNewFile);
    }

    @Override
    public File getBaseDirectory() {
        return Optional.ofNullable(this.application.getDisplayTestCase().relativePath()).orElse(Context.getBaseDirectory());
    }

    @Override
    public Window currentWindow() {
        return this.treeViewScriptName.getScene().getWindow();
    }

    private void showScriptView() {
        final Suite suite = this.application.getSuite();
        final TreeItem<String> root = new TreeItem<>(suite.name());
        root.setExpanded(true);
        this.treeViewScriptName.setRoot(root);
        this.refreshScriptView(suite, this.application.getDisplayTestCase().name());
    }

    private void refreshScriptView(final Suite suite, final String selectScriptName) {
        this.treeViewScriptName.getRoot().getChildren().clear();
        this.addChainToTreeView(selectScriptName, suite.getChains(), this.treeViewScriptName.getRoot());
        this.treeViewScriptName.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        this.application.selectScript(newValue.getValue());
                    }
                });
    }

    private void addChainToTreeView(final String selectScriptName, final TestCaseChains chains, final TreeItem<String> parent) {
        chains.forEach(testCase -> {
            final String name = testCase.name();
            final TreeItem<String> item = new TreeItem<>(name);
            parent.getChildren().add(item);
            if (name.equals(selectScriptName)) {
                this.treeViewScriptName.getSelectionModel().select(item);
            }
            this.addChainToTreeView(selectScriptName, testCase.chains(), item);
        });
    }

    private Optional<TreeItem<String>> findItem(final TestCase newValue) {
        return this.treeItems()
                .filter(it -> newValue.name().equals(it.getValue()))
                .findFirst();
    }

    private Stream<TreeItem<String>> treeItems() {
        final List<TreeItem<String>> result = Lists.newArrayList();
        this.collectItems(this.treeViewScriptName.getRoot().getChildren(), result);
        return result.stream();
    }

    private void collectItems(final List<TreeItem<String>> source, final List<TreeItem<String>> result) {
        source.forEach(this.collectItems(result));
    }

    private Consumer<TreeItem<String>> collectItems(final List<TreeItem<String>> result) {
        return it -> {
            result.add(it);
            if (it.getChildren().size() > 0) {
                this.collectItems(it.getChildren(), result);
            }
        };
    }

    private void saveTestCaseToNewFile() {
        final File file = this.saveDialog("Save TestCase File", "json format (*.json)", "*.json");
        if (file != null) {
            this.application.saveTestCase(file);
        }
    }

}