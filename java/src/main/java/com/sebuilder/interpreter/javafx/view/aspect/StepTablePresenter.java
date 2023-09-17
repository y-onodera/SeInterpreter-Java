package com.sebuilder.interpreter.javafx.view.aspect;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.javafx.control.dragdrop.DragAndDropTableViewRowFactory;
import com.sebuilder.interpreter.javafx.model.steps.Action;
import com.sebuilder.interpreter.javafx.model.steps.StepDefine;
import com.sebuilder.interpreter.javafx.model.steps.StepNo;
import com.sebuilder.interpreter.javafx.model.steps.StepNoCell;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.step.StepView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.ArrayList;

public class StepTablePresenter {
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TableView<StepDefine> stepTable;
    @FXML
    private TableColumn<StepDefine, StepNo> stepNo;
    @FXML
    private TableColumn<StepDefine, String> scriptBody;

    private final ObjectProperty<TestCase> testCase = new SimpleObjectProperty<>(new TestCaseBuilder().build());

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.stepNo.setCellValueFactory(body -> body.getValue().noProperty());
            this.stepNo.setCellFactory(cell -> new StepNoCell());
            this.scriptBody.setCellValueFactory(body -> body.getValue().scriptProperty());
            this.stepTable.setRowFactory(new DragAndDropTableViewRowFactory<>() {
                @Override
                protected void updateItemCallback(final TableRow<StepDefine> tableRow, final StepDefine stepDefine, final boolean empty, final int notEmptyValCount) {
                    if (!empty && !tableRow.isEmpty()) {
                        if (notEmptyValCount == 1) {
                            tableRow.setOnMouseClicked(ev -> {
                                if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                    StepTablePresenter.this.handleStepEdit();
                                }
                            });
                        }
                    }
                }

                @Override
                protected void move(final int draggedIndex, final int dropIndex) {
                    StepTablePresenter.this.moveStep(draggedIndex, dropIndex);
                }
            });
        });
    }

    public void setTestCase(final TestCase testCase) {
        this.testCase.set(testCase);
        this.refreshTable();
    }

    public void addListener(final ChangeListener<TestCase> listener) {
        this.testCase.addListener(listener);
    }

    @FXML
    void handleStepDelete() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final int stepIndex = this.stepTable.getSelectionModel().getSelectedItem().index();
            this.setTestCase(this.testCase.get().removeStep(stepIndex));
        });
    }

    @FXML
    void handleStepInsert() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.initStepEditDialog(Action.INSERT));
    }

    @FXML
    void handleStepAdd() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.initStepEditDialog(Action.ADD));
    }

    @FXML
    void handleStepEdit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepView stepView = this.initStepEditDialog(Action.EDIT);
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            stepView.refresh(this.testCase.get()
                    .steps()
                    .get(item.index())
            );
        });
    }

    private void moveStep(final int from, final int to) {
        final Step step = this.testCase.get().steps().get(from);
        if (to > from) {
            this.setTestCase(this.testCase.get().addStep(to, step)
                    .removeStep(from));
        } else {
            this.setTestCase(this.testCase.get().insertStep(to, step)
                    .removeStep(from + 1));
        }
    }

    private void refreshTable() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.stepTable.getItems().clear();
            this.stepTable.getItems().setAll(new ArrayList<>());
            int no = 1;
            for (final Step step : this.testCase.get().steps()) {
                final StepDefine row = new StepDefine(new StepNo(no++), step.toPrettyString(), "");
                this.stepTable.getItems().add(row);
            }
            this.stepTable.refresh();
        });
    }

    private StepView initStepEditDialog(final Action action) {
        final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
        final int no = item != null ? item.index() : 0;
        final StepView stepView = new StepView();
        stepView.open(this.currentWindow(), (application, step) -> {
            if (step != null) {
                if (action == Action.EDIT) {
                    this.setTestCase(this.testCase.get().replaceSteps(no, step));
                } else if (action == Action.INSERT) {
                    this.setTestCase(this.testCase.get().insertStep(no, step));
                } else {
                    this.setTestCase(this.testCase.get().addStep(no, step));
                }
            }
        });
        return stepView;
    }

    private Window currentWindow() {
        return this.stepTable.getScene().getWindow();
    }
}
