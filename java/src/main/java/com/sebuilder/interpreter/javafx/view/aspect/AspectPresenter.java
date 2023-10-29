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
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
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
    private final ObjectProperty<Aspect> rootProperty = new SimpleObjectProperty<>();

    private Map<String, ImportInterceptor> imports = new HashMap<>();

    private String treeViewSelect;

    private final ObjectProperty<Aspect> currentProperty = new SimpleObjectProperty<>();

    private final Map<String, ExtraStepExecutor> interceptors = new LinkedHashMap<>();

    private ExtraStepExecutor currentSelected;

    private Runnable commitOnclick = () -> {
    };

    public void setRootProperty(final TestCase testCase) {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.commitOnclick = () -> this.application.replaceDisplayCase(testCase
                    .builder()
                    .setAspect(this.rootProperty.get())
                    .build());
            final TreeItem<String> root = new TreeItem<>(testCase.name());
            root.setExpanded(true);
            this.scriptNames.setRoot(root);
            this.rootProperty.set(testCase.aspect());
        });
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
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.pointcutController.setDefaultValue(Pointcut.ANY);
            this.pointcutController.addListener((final ObservableValue<? extends Pointcut> observed, final Pointcut oldValue, final Pointcut newValue) -> {
                final ExtraStepExecutor replaced = this.getCurrentSelected().builder().setPointcut(newValue).build();
                if (this.currentProperty.get().getStream().findAny().isEmpty()) {
                    this.replaceTarget(this.currentProperty.get().builder().add(replaced).build());
                } else {
                    this.replaceTarget(this.currentProperty.get().replace(this.currentSelected, replaced));
                }
                this.setCurrentSelected(replaced);
            });
            this.beforeController.addListener((final ObservableValue<? extends TestCase> observed, final TestCase oldValue, final TestCase newValue) -> {
                final ExtraStepExecutor replaced = this.getCurrentSelected().builder().replaceBefore(newValue).build();
                if (this.currentProperty.get().getStream().findAny().isEmpty()) {
                    this.replaceTarget(this.currentProperty.get().builder().add(replaced).build());
                } else {
                    this.replaceTarget(this.currentProperty.get().replace(this.currentSelected, replaced));
                }
                this.setCurrentSelected(replaced);
            });
            this.afterController.addListener((final ObservableValue<? extends TestCase> observed, final TestCase oldValue, final TestCase newValue) -> {
                final ExtraStepExecutor replaced = this.getCurrentSelected().builder().replaceAfter(newValue).build();
                if (this.currentProperty.get().getStream().findAny().isEmpty()) {
                    this.replaceTarget(this.currentProperty.get().builder().add(replaced).build());
                } else {
                    this.replaceTarget(this.currentProperty.get().replace(this.currentSelected, replaced));
                }
                this.setCurrentSelected(replaced);
            });
            this.failureController.addListener((final ObservableValue<? extends TestCase> observed, final TestCase oldValue, final TestCase newValue) -> {
                final ExtraStepExecutor replaced = this.getCurrentSelected().builder().replaceFailure(newValue).build();
                if (this.currentProperty.get().getStream().findAny().isEmpty()) {
                    this.replaceTarget(this.currentProperty.get().builder().add(replaced).build());
                } else {
                    this.replaceTarget(this.currentProperty.get().replace(this.currentSelected, replaced));
                }
                this.setCurrentSelected(replaced);
            });
            this.rootProperty.addListener((observed, oldValue, newValue) -> {
                this.resetTreeView();
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
            this.rootProperty.set(this.rootProperty.get().remove(this.imports.get(this.treeViewSelect)));
        }
    }

    @FXML
    void selectInterceptor() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String selectedItem = this.interceptorSelect.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                this.setCurrentSelected(this.interceptors.get(selectedItem));
            }
        });
    }

    @FXML
    void removeInterceptor() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.replaceTarget(this.currentProperty.get().remove(this.currentSelected));
            this.resetTab(this.treeViewSelect, this.currentProperty.get());
        });
    }

    @FXML
    void copyInterceptor() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(this.interceptorSelect.getScene().getWindow());
            dialog.setTitle("new interceptor name");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .disableProperty()
                    .bind(dialog.getEditor().textProperty().isEmpty());
            dialog.showAndWait().ifPresent(response -> {
                if (!this.interceptorSelect.getSelectionModel().getSelectedItem().isEmpty()) {
                    this.replaceTarget(this.currentProperty.get().builder()
                            .add(this.currentSelected.builder().setDisplayName(response).build())
                            .build());
                } else {
                    this.replaceTarget(this.currentProperty.get()
                            .replace(this.currentSelected, this.currentSelected.builder().setDisplayName(response).build())
                    );
                }
                this.resetTab(this.treeViewSelect, this.currentProperty.get());
            });
        });
    }

    @FXML
    void jsonCommit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            if (this.isRootSelect()
                    && Set.of("pointcut", "before", "after", "failure").contains(this.tabPane.getSelectionModel().getSelectedItem().getText())) {
                if (this.currentSelected.hasStep()) {
                    if (this.currentSelected.pointcut() == Pointcut.NONE) {
                        this.replaceTarget(this.currentProperty.get()
                                .replace(this.currentSelected, this.currentSelected.builder().setPointcut(Pointcut.ANY).build()));
                    }
                    if (!this.currentSelected.hasDisplayName()) {
                        this.copyInterceptor();
                    }
                } else {
                    this.replaceTarget(new Aspect());
                }
            } else {
                this.replaceTarget(Context.getScriptParser()
                        .loadAspect(this.textAreaJson.getText(), this.application.getCurrentRootDir()));
                this.resetTab(this.treeViewSelect, this.currentProperty.get());
            }
            if (this.isRootSelect()) {
                this.commitOnclick.run();
            } else {
                this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
                    final File target = new File(this.application.getCurrentRootDir(), this.treeViewSelect);
                    Files.writeString(target.toPath(), this.textAreaJson.getText(), Charsets.UTF_8);
                });
            }
            SuccessDialog.show("commit succeed");
        });
    }

    private void resetTreeView() {
        this.replaceTarget(this.rootProperty.get());
        final TreeItem<String> root = this.scriptNames.getRoot();
        root.getChildren().clear();
        this.imports = this.rootProperty.get().getStream().filter(it -> it instanceof ImportInterceptor)
                .map(it -> (ImportInterceptor) it)
                .collect(Collectors.toMap(ImportInterceptor::path, it -> it));
        this.imports.forEach((key, value) -> {
            final TreeItem<String> item = new TreeItem<>(key);
            root.getChildren().add(item);
            if (key.equals(this.treeViewSelect)) {
                this.scriptNames.getSelectionModel().select(item);
            }
        });
        this.scriptNames.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.getValue().equals(this.treeViewSelect)) {
                        if (newValue.getValue().equals(root.getValue())) {
                            this.resetTab(root.getValue(), this.rootProperty.get());
                        } else {
                            this.resetTab(newValue.getValue()
                                    , this.imports.get(newValue.getValue()).toAspect().materialize(new InputData()));
                        }
                    }
                });
    }

    private void resetTab(final String name, final Aspect selected) {
        this.treeViewSelect = name;
        this.resetInterceptorSelect(selected);
        this.replaceTarget(selected);
    }

    private void resetInterceptorSelect(final Aspect aspect) {
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
        this.setCurrentSelected(this.interceptors.get(this.interceptorSelect.getSelectionModel().getSelectedItem()));
    }

    private ExtraStepExecutor getCurrentSelected() {
        return this.currentSelected;
    }

    private void setCurrentSelected(final ExtraStepExecutor currentSelected1) {
        this.currentSelected = currentSelected1;
        this.pointcutController.setTarget(this.currentSelected.pointcut());
        this.beforeController.setTestCase(this.currentSelected.beforeStep());
        this.afterController.setTestCase(this.currentSelected.afterStep());
        this.failureController.setTestCase(this.currentSelected.failureStep());
    }

    private void replaceTarget(final Aspect newValue) {
        this.currentProperty.set(newValue);
        this.textAreaJson.clear();
        this.textAreaJson.setText(Context.toString(this.currentProperty.get()));
    }

    private boolean isRootSelect() {
        return Optional.ofNullable(this.scriptNames.getSelectionModel().getSelectedItem())
                .filter(it -> it.equals(this.scriptNames.getRoot()))
                .isPresent();
    }

}
