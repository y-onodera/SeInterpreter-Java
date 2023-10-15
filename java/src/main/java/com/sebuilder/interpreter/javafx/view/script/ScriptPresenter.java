package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.control.dragdrop.DragAndDropTableViewRowFactory;
import com.sebuilder.interpreter.javafx.model.BreakPoint;
import com.sebuilder.interpreter.javafx.model.Result;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.model.ViewType;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import com.sebuilder.interpreter.javafx.view.step.StepDefine;
import com.sebuilder.interpreter.javafx.view.step.StepNo;
import com.sebuilder.interpreter.javafx.view.step.StepTablePresenter;
import com.sebuilder.interpreter.javafx.view.step.StepView;
import com.sebuilder.interpreter.pointcut.VerifyFilter;
import com.sebuilder.interpreter.step.Verify;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScriptPresenter extends StepTablePresenter {

    @Inject
    private SeInterpreter application;

    final List<Integer> hasBreakPoint = new ArrayList<>();

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        this.application.displayTestCase().addListener((observed, oldValue, newValue) -> {
            if (this.application.scriptViewType().get() == ViewType.TABLE) {
                this.setTestCase(this.application.getDisplayTestCase());
            }
        });
        this.application.scriptViewType().addListener((final ObservableValue<? extends ViewType> observed, final ViewType oldValue, final ViewType newValue) -> {
            if (newValue == ViewType.TABLE) {
                this.setTestCase(this.application.getDisplayTestCase());
            }
        });
        this.application.replayStatus().addListener((observed, oldVar, newVar) -> {
            if (newVar != null) {
                this.handleStepResult(newVar.getKey(), newVar.getValue());
            }
        });
        this.setTestCase(this.application.getDisplayTestCase());
    }

    @FXML
    void handleRunStep() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            final InputView inputView = new InputView();
            inputView.setOnclickReplayStart((it) ->
                    this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                            this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) == 0, false)
                    )
            );
            inputView.open(this.currentWindow());
        });
    }

    @FXML
    void handleRunFromHere() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            final InputView inputView = new InputView();
            inputView.setOnclickReplayStart((it) ->
                    this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                            this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) <= 0, true)
                    )
            );
            inputView.open(this.currentWindow());
        });
    }

    @FXML
    void handleRunToHere() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            final InputView inputView = new InputView();
            inputView.setOnclickReplayStart((it) ->
                    this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                            this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) >= 0, false)
                    )
            );
            inputView.open(this.currentWindow());
        });
    }

    @FXML
    void handleAddBreakPoint() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            new StepView(s -> s.startsWith("verify"), key -> !key.equals("skip"))
                    .open(this.currentWindow(), (application, step) -> {
                        if (step != null) {
                            final VerifyFilter pointcut = new VerifyFilter((Verify) step.type(), step.stringParams(), step.locatorParams());
                            application.addBreakPoint(item.index(), pointcut);
                        } else {
                            application.addBreakPoint(item.index(), Pointcut.ANY);
                        }
                    });
        });
    }

    @FXML
    void handleRemoveBreakPoint() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final StepDefine item = this.stepTable.getSelectionModel().getSelectedItem();
            this.application.removeBreakPoint(item.index());
        });
    }

    @Override
    protected DragAndDropTableViewRowFactory<StepDefine> getRowFactory() {
        return new DragAndDropTableViewRowFactory<>() {
            @Override
            protected void updateItemCallback(final TableRow<StepDefine> tableRow, final StepDefine stepDefine, final boolean empty, final int notEmptyValCount) {
                for (final Result result : Result.values()) {
                    tableRow.getStyleClass().remove(result.toString().toLowerCase());
                }
                if (!empty && !tableRow.isEmpty()) {
                    if (notEmptyValCount == 1) {
                        tableRow.getItem().runningResultProperty().addListener((final ObservableValue<? extends String> observed, final String oldValue, final String newValue) -> {
                            for (final Result result : Result.values()) {
                                tableRow.getStyleClass().remove(result.toString().toLowerCase());
                            }
                            if (!Strings.isNullOrEmpty(newValue)) {
                                tableRow.getStyleClass().add(newValue.toLowerCase());
                            }
                        });
                        tableRow.setOnMouseClicked(ev -> {
                            if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                ScriptPresenter.this.handleStepEdit();
                            }
                        });
                    }
                    final String currentStyle = tableRow.getItem().runningResultProperty().get();
                    if (!Strings.isNullOrEmpty(currentStyle)) {
                        tableRow.getStyleClass().add(currentStyle.toLowerCase());
                    }
                }
            }

            @Override
            protected void move(final int draggedIndex, final int dropIndex) {
                ScriptPresenter.this.moveStep(draggedIndex, dropIndex);
            }
        };
    }

    @Override
    protected void replaceTestCase(final TestCase newCase) {
        this.application.replaceDisplayCase(newCase);
    }

    @Override
    protected void refreshTable() {
        this.hasBreakPoint.clear();
        final Optional<BreakPoint> breakPoint = BreakPoint.findFrom(this.testCase.get().aspect());
        breakPoint.ifPresent(it -> this.hasBreakPoint.addAll(it.targetStepIndex()));
        super.refreshTable();
    }

    @Override
    protected StepDefine getStepDefine(final int no, final Step step) {
        if (this.hasBreakPoint.contains(no - 1)) {
            return new StepDefine(new StepNo(no).withBreakPoint(), step.toPrettyString(), "");
        }
        return new StepDefine(new StepNo(no), step.toPrettyString(), "");
    }

    private void handleStepResult(final int stepIndex, final Result result) {
        final List<StepDefine> bodies = this.stepTable.getItems();
        bodies.stream()
                .filter(item -> item.index() == stepIndex)
                .findFirst()
                .ifPresent(target -> target.setRunningResult(result.name()));
    }

    private Window currentWindow() {
        return this.stepTable.getScene().getWindow();
    }

}
