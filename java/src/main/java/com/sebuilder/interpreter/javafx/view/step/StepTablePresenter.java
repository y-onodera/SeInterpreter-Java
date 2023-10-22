package com.sebuilder.interpreter.javafx.view.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.javafx.control.dragdrop.DragAndDropTableViewRowFactory;
import com.sebuilder.interpreter.javafx.model.Action;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
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
    protected ErrorDialog errorDialog;
    @FXML
    protected TableView<StepDefine> stepTable;
    @FXML
    protected TableColumn<StepDefine, StepNo> stepNo;
    @FXML
    protected TableColumn<StepDefine, String> scriptBody;

    protected final ObjectProperty<TestCase> testCase = new SimpleObjectProperty<>(new TestCaseBuilder().build());

    @FXML
    public void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.stepNo.setCellValueFactory(body -> body.getValue().noProperty());
            this.stepNo.setCellFactory(cell -> new StepNoCell());
            this.scriptBody.setCellValueFactory(body -> body.getValue().scriptProperty());
            this.stepTable.setRowFactory(this.getRowFactory());
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
    public void handleStepInsert() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.initStepEditDialog(Action.INSERT));
    }

    @FXML
    public void handleStepAdd() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.initStepEditDialog(Action.ADD));
    }

    @FXML
    public void handleStepEdit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepView stepView = this.initStepEditDialog(Action.EDIT);
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            stepView.refresh(this.testCase.get()
                    .steps()
                    .get(item.index())
            );
        });
    }

    @FXML
    public void handleStepDelete() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final int stepIndex = this.stepTable.getSelectionModel().getSelectedItem().index();
            this.replaceTestCase(this.testCase.get().removeStep(stepIndex));
        });
    }

    protected DragAndDropTableViewRowFactory<StepDefine> getRowFactory() {
        return new DragAndDropTableViewRowFactory<>() {
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
        };
    }

    protected void replaceTestCase(final TestCase newCase) {
        this.setTestCase(newCase);
    }

    protected void moveStep(final int from, final int to) {
        final Step step = this.testCase.get().steps().get(from);
        if (to > from) {
            this.replaceTestCase(this.testCase.get().addStep(to, step)
                    .removeStep(from));
        } else {
            this.replaceTestCase(this.testCase.get().insertStep(to, step)
                    .removeStep(from + 1));
        }
    }

    protected void refreshTable() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.stepTable.getItems().clear();
            this.stepTable.getItems().setAll(new ArrayList<>());
            int no = 1;
            for (final Step step : this.testCase.get().steps()) {
                final StepDefine row = this.getStepDefine(no++, step);
                this.stepTable.getItems().add(row);
            }
            this.stepTable.refresh();
        });
    }

    protected StepDefine getStepDefine(final int no, final Step step) {
        return new StepDefine(new StepNo(no), step.toPrettyString(), "");
    }

    private StepView initStepEditDialog(final Action action) {
        final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
        final int no = item != null ? item.index() : 0;
        final StepView stepView = new StepView();
        stepView.open(this.currentWindow(), step -> {
            if (step != null) {
                if (action == Action.EDIT) {
                    this.replaceTestCase(this.testCase.get().replaceSteps(no, step));
                } else if (action == Action.INSERT) {
                    this.replaceTestCase(this.testCase.get().insertStep(no, step));
                } else {
                    this.replaceTestCase(this.testCase.get().addStep(no, step));
                }
            }
        });
        return stepView;
    }

    private Window currentWindow() {
        return this.stepTable.getScene().getWindow();
    }
}
