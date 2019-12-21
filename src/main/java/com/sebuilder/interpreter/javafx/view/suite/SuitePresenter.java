package com.sebuilder.interpreter.javafx.view.suite;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseChains;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.view.data.DataSetView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SuitePresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TreeView<String> treeViewScriptName;

    @FXML
    public MenuItem openDataSource;

    @FXML
    void initialize() {
        assert this.treeViewScriptName != null : "fx:id=\"treeViewScriptName\" was not injected: check your FXML file 'scriptview.fxml'.";
        this.application.suiteProperty().addListener((observed, oldValue, newValue) -> this.showScriptView());
        this.application.displayTestCaseProperty().addListener((observed, oldValue, newValue) -> {
            if (this.application.getSuite().name().equals(newValue.name())) {
                this.treeViewScriptName.getSelectionModel().selectFirst();
            } else {
                this.findItem(newValue).ifPresent(it -> this.treeViewScriptName.getSelectionModel().select(it));
            }
            if (observed.getValue().runtimeDataSet().isLoadable() && observed.getValue().loadData().size() > 0) {
                openDataSource.setDisable(false);
            } else {
                openDataSource.setDisable(true);
            }
        });
        this.showScriptView();
    }

    @FXML
    public void handleOpenDataSource(ActionEvent actionEvent) {
        new DataSetView().showDataSet(this.application.getDisplayTestCaseDataSource(), this.treeViewScriptName.getScene().getWindow());
    }

    @FXML
    void handleScriptInsert(ActionEvent event) {
        this.application.insertScript();
        this.showScriptView();
    }

    @FXML
    void handleScriptAdd(ActionEvent event) {
        this.application.addScript();
        this.showScriptView();
    }

    @FXML
    void handleScriptCreateTemplate(ActionEvent event) {
        new TemplateView(treeViewScriptName.getScene().getWindow());
    }

    @FXML
    void handleScriptImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileChooser.setInitialDirectory(this.getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(treeViewScriptName.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            this.application.importScript(file);
        }
    }

    @FXML
    void handleScriptDelete(ActionEvent event) {
        this.application.removeScript();
        this.showScriptView();
    }

    @FXML
    void handleScriptSave(ActionEvent event) {
        if (Strings.isNullOrEmpty(this.application.getDisplayTestCase().path())) {
            this.saveTestCaseToNewFile();
        } else {
            this.application.saveTestCase();
        }
    }

    @FXML
    void handleScriptSaveAs(ActionEvent event) {
        this.saveTestCaseToNewFile();
    }

    public void showScriptView() {
        Suite suite = this.application.getSuite();
        TreeItem<String> root = new TreeItem<>(suite.name());
        root.setExpanded(true);
        this.treeViewScriptName.setRoot(root);
        this.refreshScriptView(suite, this.application.getDisplayTestCase().name());
    }

    private void refreshScriptView(Suite suite, String selectScriptName) {
        this.treeViewScriptName.getRoot().getChildren().clear();
        addChainToTreeView(selectScriptName, suite.getChains(), this.treeViewScriptName.getRoot());
        this.treeViewScriptName.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        this.application.selectScript(newValue.getValue());
                    }
                });
    }

    private void addChainToTreeView(String selectScriptName, TestCaseChains chains, TreeItem<String> parent) {
        for (TestCase testCase : chains) {
            String name = testCase.name();
            TreeItem<String> item = new TreeItem<>(name);
            parent.getChildren().add(item);
            if (name.equals(selectScriptName)) {
                this.treeViewScriptName.getSelectionModel().select(item);
            }
            addChainToTreeView(selectScriptName, testCase.getChains(), item);
        }
    }

    private Optional<TreeItem<String>> findItem(TestCase newValue) {
        return this.treeItems()
                .filter(it -> newValue.name().equals(it.getValue()))
                .findFirst();
    }

    private Stream<TreeItem<String>> treeItems() {
        List<TreeItem<String>> result = Lists.newArrayList();
        this.collectItems(this.treeViewScriptName.getRoot().getChildren(), result);
        return result.stream();
    }

    private void collectItems(List<TreeItem<String>> source, List<TreeItem<String>> result) {
        source.forEach(collectItems(result));
    }

    private Consumer<TreeItem<String>> collectItems(List<TreeItem<String>> result) {
        return it -> {
            result.add(it);
            if (it.getChildren().size() > 0) {
                collectItems(it.getChildren(), result);
            }
        };
    }

    private void saveTestCaseToNewFile() {
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save TestCase File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileSave.setInitialDirectory(this.getBaseDirectory());
        File file = fileSave.showSaveDialog(treeViewScriptName.getScene().getWindow());
        if (file != null) {
            this.application.saveTestCase(file);
        }
    }

    private File getBaseDirectory() {
        return Optional.ofNullable(this.application.getDisplayTestCase().relativePath()).orElse(Context.getBaseDirectory());
    }
}