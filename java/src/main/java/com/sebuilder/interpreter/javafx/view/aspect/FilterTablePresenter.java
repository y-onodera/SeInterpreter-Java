package com.sebuilder.interpreter.javafx.view.aspect;

import com.sebuilder.interpreter.Exportable;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.model.steps.Action;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.step.StepView;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

public class FilterTablePresenter {
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TableView<FilterDefine> filterTable;
    @FXML
    private TableColumn<FilterDefine, Number> no;
    @FXML
    private TableColumn<FilterDefine, Combinator> combinator;
    @FXML
    private TableColumn<FilterDefine, String> filter;

    private final ObjectProperty<Pointcut> target = new SimpleObjectProperty<>(Pointcut.NONE);

    @FXML
    public void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.no.setCellValueFactory(body -> body.getValue().noProperty());
            this.combinator.setCellValueFactory(body -> body.getValue().combinatorProperty());
            this.filter.setCellValueFactory(body -> body.getValue().scriptProperty());
            this.filterTable.setRowFactory((tableView) -> new TableRow<>() {
                int notEmptyValCount = 0;

                @Override
                protected void updateItem(final FilterDefine tableRow, final boolean empty) {
                    if (!empty) {
                        this.notEmptyValCount++;
                    }
                    if (!empty && !this.isEmpty()) {
                        if (this.notEmptyValCount == 1) {
                            this.setOnMouseClicked(ev -> {
                                if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                    FilterTablePresenter.this.handleEdit();
                                }
                            });
                        }
                    }
                }
            });
        });
    }

    @FXML
    public void handleEdit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final FilterDefine item = this.filterTable.getSelectionModel().getSelectedItem();
        });
    }

    @FXML
    public void handleDelete() {
    }

    @FXML
    public void handleAdd() {
    }

    public void setTarget(final Pointcut pointcut) {
        this.target.set(pointcut);
        this.refreshTable();
    }

    private void refreshTable() {
        this.filterTable.getItems().clear();
        this.filterTable.getItems().setAll(new ArrayList<>());
        for (final FilterDefine row : this.parseFilter()) {
            this.filterTable.getItems().add(row);
        }
        this.filterTable.refresh();
    }

    private Collection<FilterDefine> parseFilter() {
        return this.parseFilter(this.target.get(), 1);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no) {
        return this.parseFilter(pointcut, no, null, false, false);
    }

    private Collection<FilterDefine> parseNewFilter(final Pointcut pointcut, final int no) {
        return this.parseFilter(pointcut, no, null, true, false);
    }

    private Collection<FilterDefine> parseCombineFilter(final Pointcut pointcut, final int no, final Combinator combinator) {
        return this.parseFilter(pointcut, no, combinator, true, true);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no, final Combinator combinator, final boolean nested, final boolean append) {
        final ArrayList<FilterDefine> results = new ArrayList<>();
        if (pointcut instanceof Pointcut.Or or) {
            if (combinator == Combinator.AND) {
                results.add(new FilterDefine(no, append ? combinator : null, "("));
                results.addAll(this.parseNewFilter(or.origin(), no));
                results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
                results.add(new FilterDefine(no, null, ")"));
            } else if (combinator == Combinator.OR || append) {
                results.addAll(this.parseCombineFilter(or.origin(), no, Combinator.OR));
                results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
            } else {
                if (nested) {
                    results.addAll(this.parseNewFilter(or.origin(), no));
                    results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
                } else {
                    results.addAll(this.parseFilter(or.origin(), no));
                    results.addAll(this.parseFilter(or.other(), no + 1));
                }
            }
            return results;
        } else if (pointcut instanceof Pointcut.And and) {
            if (and.origin() instanceof Pointcut.Or) {
                results.addAll(this.parseFilter(and.origin(), no, Combinator.AND, true, false));
            } else {
                results.addAll(this.parseNewFilter(and.origin(), no));
            }
            results.addAll(this.parseCombineFilter(and.other(), no, Combinator.AND));
            return results;
        } else if (pointcut instanceof Exportable result) {
            results.add(new FilterDefine(no, append ? combinator : null, result.toPrettyString()));
        }
        return results;
    }

    private StepView initStepEditDialog(final Action action) {
        final StepView stepView = new StepView();
        stepView.open(this.currentWindow(), (application, step) -> {
            if (step != null) {
                if (action == Action.EDIT) {
                } else if (action == Action.INSERT) {
                } else {
                }
            }
        });
        return stepView;
    }

    private Window currentWindow() {
        return this.filterTable.getScene().getWindow();
    }

    private static class FilterDefine {
        private final IntegerProperty no;

        private final ObjectProperty<Combinator> combinator;

        private final StringProperty script;

        public FilterDefine(final Integer no, final Combinator combinator, final String script) {
            this.no = new SimpleIntegerProperty(no);
            this.combinator = new SimpleObjectProperty<>(combinator);
            this.script = new SimpleStringProperty(script);
        }

        public IntegerProperty noProperty() {
            return this.no;
        }

        public ObjectProperty<Combinator> combinatorProperty() {
            return this.combinator;
        }

        public String getScript() {
            return this.script.get();
        }

        public StringProperty scriptProperty() {
            return this.script;
        }
    }

    private enum Combinator {
        OR, AND
    }

}
