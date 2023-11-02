package com.sebuilder.interpreter.javafx.view.aspect;

import com.google.common.base.Charsets;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.aspect.ImportInterceptor;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.HasFileChooser;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import com.sebuilder.interpreter.javafx.view.filter.FilterTablePresenter;
import com.sebuilder.interpreter.javafx.view.step.StepTablePresenter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.util.Pair;
import org.tbee.javafx.scene.layout.fxml.MigPane;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class AspectPresenter implements HasFileChooser {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TreeView<String> scriptNames;
    @FXML
    private ComboBox<String> interceptorSelect;
    @FXML
    private TextArea textAreaJson;
    @FXML
    private TabPane tabPane;
    @FXML
    private FilterTablePresenter pointcutController;
    @FXML
    private StepTablePresenter beforeController;
    @FXML
    private StepTablePresenter afterController;
    @FXML
    private StepTablePresenter failureController;
    @FXML
    private MigPane buttonArea;
    private final List<Button> buttons = new ArrayList<>();
    private final ObjectProperty<Aspect> rootProperty = new SimpleObjectProperty<>();

    private Map<String, ImportInterceptor> imports = new HashMap<>();

    private String selectedTree;

    private final ObjectProperty<Aspect> currentProperty = new SimpleObjectProperty<>();

    private final Map<String, ExtraStepExecutor> interceptors = new LinkedHashMap<>();

    private ExtraStepExecutor selectedInterceptor;

    private Consumer<Aspect> commitOnclick = (aspect) -> {
    };

    public void setRootProperty(final Pair<String, Aspect> target) {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final TreeItem<String> root = new TreeItem<>(target.getKey());
            root.setExpanded(true);
            this.scriptNames.setRoot(root);
            this.rootProperty.set(target.getValue());
        });
    }

    public void setOnClickCommit(final Consumer<Aspect> onClickCommit) {
        this.commitOnclick = onClickCommit;
    }

    @Override
    public Window currentWindow() {
        return this.scriptNames.getScene().getWindow();
    }

    @Override
    public File getBaseDirectory() {
        return this.application.getCurrentRootDir();
    }

    @FXML
    void initialize() {
        this.buttons.addAll(this.buttonArea.getChildren().stream().map(it -> (Button) it).toList());
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.pointcutController.setDefaultValue(Pointcut.ANY);
            this.pointcutController.addListener((observed, oldValue, newValue) ->
                    this.convertSelected(builder -> builder.setPointcut(newValue).build()));
            this.beforeController.addListener((observed, oldValue, newValue) ->
                    this.convertSelected(builder -> builder.replaceBefore(newValue).build()));
            this.afterController.addListener((observed, oldValue, newValue) ->
                    this.convertSelected(builder -> builder.replaceAfter(newValue).build()));
            this.failureController.addListener((observed, oldValue, newValue) ->
                    this.convertSelected(builder -> builder.replaceFailure(newValue).build()));
            this.rootProperty.addListener((observed, oldValue, newValue) -> {
                this.refreshTreeView();
                this.scriptNames.getSelectionModel().select(0);
            });
            this.currentProperty.addListener((observed, oldValue, newValue) -> {
                if (this.isRootSelect()) {
                    this.rootProperty.set(newValue);
                }
            });
        });
    }

    @FXML
    void handleImport() {
        final File toImport = this.openDialog("choose import aspect", "select aspect file", "*.json");
        if (toImport != null) {
            this.rootProperty.set(this.rootProperty.get().builder()
                    .add(new ImportInterceptor(this.getBaseDirectory().toPath()
                            .relativize(toImport.toPath())
                            .toString().replace("\\", "/")
                            , ""
                            , (path) -> Context.getScriptParser().loadAspect(toImport)))
                    .build());
        }
    }

    @FXML
    void handleRemove() {
        if (!this.isRootSelect()) {
            this.rootProperty.set(this.rootProperty.get().remove(this.imports.get(this.selectedTree)));
        }
    }

    @FXML
    void selectInterceptor() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String selectedItem = this.interceptorSelect.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                this.selectInterceptor(this.interceptors.get(selectedItem));
            }
        });
    }

    @FXML
    void removeInterceptor() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.replaceInterceptors(current -> current.remove(this.selectedInterceptor));
            this.refreshTab();
        });
    }

    @FXML
    void copyInterceptor() {
        this.setNameAnd(response -> {
            final ExtraStepExecutor named = this.selectedInterceptor.builder().setDisplayName(response).build();
            this.replaceInterceptors(current -> current.builder().add(named).build());
            this.selectInterceptor(named);
            this.refreshTab();
        });
    }

    @FXML
    void commit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.applyChangeToCurrent();
            this.commitOnclick.accept(this.rootProperty.get());
            SuccessDialog.show("commit succeed");
        });
    }

    @FXML
    void save() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.applyChangeToCurrent();
            final File target = new File(this.application.getCurrentRootDir(), this.selectedTree);
            Files.writeString(target.toPath(), this.textAreaJson.getText(), Charsets.UTF_8);
            SuccessDialog.show("save succeed");
        });
    }

    @FXML
    void saveAs() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.applyChangeToCurrent();
            final File target = this.saveDialog("Save Aspect File", "json format (*.json)", "*.json");
            Files.writeString(target.toPath(), this.textAreaJson.getText(), Charsets.UTF_8);
            SuccessDialog.show("save succeed");
        });
    }

    private void refreshTreeView() {
        this.replaceCurrent(this.rootProperty.get());
        final TreeItem<String> root = this.scriptNames.getRoot();
        root.getChildren().clear();
        this.imports = this.rootProperty.get().getStream().filter(it -> it instanceof ImportInterceptor)
                .map(it -> (ImportInterceptor) it)
                .collect(Collectors.toMap(ImportInterceptor::path, it -> it));
        this.imports.forEach((key, value) -> {
            final TreeItem<String> item = new TreeItem<>(key);
            root.getChildren().add(item);
            if (key.equals(this.selectedTree)) {
                this.scriptNames.getSelectionModel().select(item);
            }
        });
        this.scriptNames.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.getValue().equals(this.selectedTree)) {
                        this.buttonArea.getChildren().clear();
                        if (newValue.getValue().equals(root.getValue())) {
                            this.buttonArea.add(this.buttons.get(0));
                            this.buttonArea.add(this.buttons.get(2));
                            this.selectTree(root.getValue(), this.rootProperty.get());
                        } else {
                            this.buttonArea.add(this.buttons.get(1));
                            this.buttonArea.add(this.buttons.get(2));
                            this.selectTree(newValue.getValue()
                                    , this.imports.get(newValue.getValue()).toAspect().materialize(new InputData()));
                        }
                    }
                });
    }

    private void selectTree(final String name, final Aspect selected) {
        this.selectedTree = name;
        this.replaceCurrent(selected);
        this.refreshInterceptors(selected);
    }

    private void replaceCurrent(final Aspect newValue) {
        this.currentProperty.set(newValue);
        this.textAreaJson.clear();
        this.textAreaJson.setText(Context.toString(this.currentProperty.get()));
    }

    private void applyChangeToCurrent() {
        if ("plain text(json)".equals(this.tabPane.getSelectionModel().getSelectedItem().getText())) {
            this.replaceCurrent(Context.getScriptParser()
                    .loadAspect(this.textAreaJson.getText(), this.application.getCurrentRootDir()));
            this.refreshTab();
        } else {
            if (this.selectedInterceptor.hasStep()) {
                if (this.selectedInterceptor.pointcut() == Pointcut.NONE) {
                    this.convertSelected(builder -> builder.setPointcut(Pointcut.ANY).build());
                }
                if (!this.selectedInterceptor.hasDisplayName()) {
                    this.setNameAnd(response -> {
                        this.convertSelected(builder -> builder.setDisplayName(response).build());
                        this.refreshTab();
                    });
                }
            } else {
                this.removeInterceptor();
            }
        }
    }

    private void refreshTab() {
        this.selectTree(this.selectedTree, this.currentProperty.get());
    }

    private void refreshInterceptors(final Aspect aspect) {
        this.interceptors.clear();
        this.interceptorSelect.getItems().clear();
        int interceptorCount = 0;
        for (final Interceptor it : aspect.interceptors()) {
            if (it instanceof ExtraStepExecutor executor) {
                if (executor.hasDisplayName()) {
                    this.interceptors.put(executor.displayName(), executor);
                } else {
                    this.interceptors.put(String.format("has no displayName#%s", interceptorCount++), executor);
                }
            }
        }
        if (this.interceptors.size() == 0) {
            this.interceptors.put("", new ExtraStepExecutor.Builder().build());
        }
        this.interceptorSelect.getItems().setAll(this.interceptors.keySet());
        this.interceptorSelect.getSelectionModel().select(0);
        this.selectInterceptor(this.interceptors.get(this.interceptorSelect.getSelectionModel().getSelectedItem()));
    }

    private void setNameAnd(final Consumer<String> stringConsumer) {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(this.interceptorSelect.getScene().getWindow());
            dialog.setTitle("new interceptor name");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .disableProperty()
                    .bind(dialog.getEditor().textProperty().isEmpty());
            dialog.showAndWait().ifPresent(stringConsumer);
        });
    }

    private void convertSelected(final Function<ExtraStepExecutor.Builder, ExtraStepExecutor> function) {
        final ExtraStepExecutor replaced = function.apply(this.selectedInterceptor.builder());
        if (this.currentProperty.get().getStream().filter(this.selectedInterceptor::equals).findAny().isEmpty()) {
            this.replaceInterceptors(current -> current.builder().add(replaced).build());
        } else {
            this.replaceInterceptors(current -> current.replace(this.selectedInterceptor, replaced));
        }
        this.selectInterceptor(replaced);
    }

    private void replaceInterceptors(final UnaryOperator<Aspect> function) {
        this.replaceCurrent(function.apply(this.currentProperty.get()));
    }

    private void selectInterceptor(final ExtraStepExecutor toSelect) {
        this.selectedInterceptor = toSelect;
        this.pointcutController.setTarget(this.selectedInterceptor.pointcut());
        this.beforeController.setTestCase(this.selectedInterceptor.beforeStep());
        this.afterController.setTestCase(this.selectedInterceptor.afterStep());
        this.failureController.setTestCase(this.selectedInterceptor.failureStep());
    }

    private boolean isRootSelect() {
        return Optional.ofNullable(this.scriptNames.getSelectionModel().getSelectedItem())
                .filter(it -> it.equals(this.scriptNames.getRoot()))
                .isPresent();
    }

}
