package com.sebuilder.interpreter.javafx.view.aspect;

import com.google.common.base.Charsets;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.aspect.ImportInterceptor;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class AspectPresenter {

    @Inject
    private SeInterpreterApplication application;
    @FXML
    private TreeView<String> scriptNames;
    @FXML
    private ComboBox<String> interceptorSelect;
    @FXML
    private TextArea textAreaJson;
    @FXML
    private TabPane tabPane;
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

    @FXML
    void initialize() {
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
        this.currentProperty.addListener((observed, oldValue, newValue) -> {
            if (Optional.ofNullable(this.scriptNames.getSelectionModel().getSelectedItem())
                    .filter(it -> it.equals(this.scriptNames.getRoot()))
                    .isPresent()) {
                this.rootProperty.set(newValue);
            }
        });
    }

    public void setRootProperty(final TestCase testCase) {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.commitOnclick = () -> {
                this.application.replaceDisplayCase(testCase.builder()
                        .setAspect(this.rootProperty.get())
                        .build());
            };
            this.rootProperty.set(testCase.aspect());
            this.resetTreeView(testCase.name(), testCase.aspect());
            this.scriptNames.getSelectionModel().select(0);
        });
    }

    @FXML
    void selectInterceptor() {
        final String selectedItem = this.interceptorSelect.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            this.setCurrentSelected(this.interceptors.get(selectedItem));
        }
    }

    @FXML
    void removeInterceptor() {
        this.replaceTarget(this.currentProperty.get().remove(this.currentSelected));
        this.resetTab(this.treeViewSelect, this.currentProperty.get());
    }

    @FXML
    void copyInterceptor() {
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
    }

    @FXML
    void jsonCommit() {
        if (Set.of("before", "after", "failure").contains(this.tabPane.getSelectionModel().getSelectedItem().getText())
                && !this.currentSelected.hasDisplayName()) {
            if (this.currentSelected.beforeStep().steps().size() > 0
                    || this.currentSelected.afterStep().steps().size() > 0
                    || this.currentSelected.failureStep().steps().size() > 0) {
                this.copyInterceptor();
            }
        } else {
            this.replaceTarget(Context.getScriptParser().loadAspect(this.textAreaJson.getText(), new File(this.treeViewSelect)));
            this.resetTab(this.treeViewSelect, this.currentProperty.get());
        }
        if (Optional.ofNullable(this.scriptNames.getSelectionModel().getSelectedItem())
                .filter(it -> it.equals(this.scriptNames.getRoot()))
                .isPresent()) {
            this.commitOnclick.run();
        } else {
            this.application.executeAndLoggingCaseWhenThrowException(() -> {
                final File target = new File(new File(this.application.getSuite().path()).getParentFile()
                        , this.treeViewSelect);
                Files.writeString(target.toPath(), this.textAreaJson.getText(), Charsets.UTF_8);
            });
        }
        SuccessDialog.show("commit succeed");
    }

    private void resetTreeView(final String target, final Aspect aspect) {
        this.replaceTarget(aspect);
        final TreeItem<String> root = new TreeItem<>(target);
        root.setExpanded(true);
        this.scriptNames.setRoot(root);
        this.scriptNames.getRoot().getChildren().clear();
        this.imports = aspect.getStream().filter(it -> it instanceof ImportInterceptor)
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
        this.beforeController.setTestCase(this.currentSelected.beforeStep());
        this.afterController.setTestCase(this.currentSelected.afterStep());
        this.failureController.setTestCase(this.currentSelected.failureStep());
    }

    private void replaceTarget(final Aspect newValue) {
        this.currentProperty.set(newValue);
        this.textAreaJson.clear();
        this.textAreaJson.setText(Context.getTestCaseConverter().toString(this.currentProperty.get()));
    }

}
