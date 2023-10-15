package com.sebuilder.interpreter.javafx.view.aspect;

import com.sebuilder.interpreter.Exportable;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.model.Action;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PointcutTablePresenter {
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TableView<PointcutDefine> pointcutTable;
    @FXML
    private TableColumn<PointcutDefine, Number> no;
    @FXML
    private TableColumn<PointcutDefine, Combinator> combinator;
    @FXML
    private TableColumn<PointcutDefine, String> filter;

    private final ObjectProperty<Pointcut> target = new SimpleObjectProperty<>(Pointcut.NONE);

    public void setTarget(final Pointcut pointcut) {
        this.target.set(pointcut);
        this.refreshTable();
    }

    public void addListener(final ChangeListener<Pointcut> listener) {
        this.target.addListener(listener);
    }

    @FXML
    public void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.no.setCellValueFactory(body -> body.getValue().noProperty());
            this.combinator.setCellValueFactory(body -> body.getValue().combinatorProperty());
            this.filter.setCellValueFactory(body -> body.getValue().scriptProperty());
            this.pointcutTable.setRowFactory((tableView) -> new TableRow<>() {
                int notEmptyValCount = 0;

                @Override
                protected void updateItem(final PointcutDefine tableRow, final boolean empty) {
                    if (!empty) {
                        this.notEmptyValCount++;
                    }
                    if (!empty && !this.isEmpty()) {
                        if (this.notEmptyValCount == 1) {
                            this.setOnMouseClicked(ev -> {
                                if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                    PointcutTablePresenter.this.handleEdit();
                                }
                            });
                        }
                    }
                }
            });
        });
    }

    @FXML
    public void handleAdd() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.initStepEditDialog(Action.ADD));
    }

    @FXML
    public void handleEdit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final PointcutView pointcutView = this.initStepEditDialog(Action.EDIT);
            final PointcutDefine item = this.pointcutTable.getSelectionModel().getSelectedItem();
            final Pointcut pointcut = this.target.get().toListTopLevelCondition().get(item.index());
            pointcutView.refresh(pointcut);
        });
    }

    @FXML
    public void handleDelete() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final int pointcutIndex = this.pointcutTable.getSelectionModel().getSelectedItem().index();
            final List<Pointcut> conditions = this.target.get().toListTopLevelCondition();
            conditions.remove(pointcutIndex);
            this.setTarget(conditions.stream().reduce(Pointcut.NONE, Pointcut::or));
        });
    }

    private void refreshTable() {
        this.pointcutTable.getItems().clear();
        this.pointcutTable.getItems().setAll(new ArrayList<>());
        for (final PointcutDefine row : this.parseFilter()) {
            this.pointcutTable.getItems().add(row);
        }
        this.pointcutTable.refresh();
    }

    private Collection<PointcutDefine> parseFilter() {
        final List<Pointcut> results = this.target.get().toListTopLevelCondition();
        return IntStream.rangeClosed(1, results.size())
                .mapToObj(no -> this.parseFilter(results.get(no - 1), no, null, false))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<PointcutDefine> parseNewFilter(final Pointcut pointcut, final int no) {
        return this.parseFilter(pointcut, no, null, false);
    }

    private Collection<PointcutDefine> parseCombineFilter(final Pointcut pointcut, final int no, final Combinator combinator) {
        return this.parseFilter(pointcut, no, combinator, true);
    }

    private Collection<PointcutDefine> parseFilter(final Pointcut pointcut, final int no, final Combinator combinator, final boolean append) {
        final ArrayList<PointcutDefine> results = new ArrayList<>();
        if (pointcut instanceof Pointcut.Or or) {
            if (combinator == Combinator.AND) {
                results.add(new PointcutDefine(no, append ? combinator : null, "("));
                results.addAll(this.parseNewFilter(or.origin(), no));
                results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
                results.add(new PointcutDefine(no, null, ")"));
            } else if (combinator == Combinator.OR || append) {
                results.addAll(this.parseCombineFilter(or.origin(), no, Combinator.OR));
                results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
            } else {
                results.addAll(this.parseNewFilter(or.origin(), no));
                results.addAll(this.parseCombineFilter(or.other(), no, Combinator.OR));
            }
            return results;
        } else if (pointcut instanceof Pointcut.And and) {
            if (and.origin() instanceof Pointcut.Or) {
                results.addAll(this.parseFilter(and.origin(), no, Combinator.AND, false));
            } else {
                results.addAll(this.parseNewFilter(and.origin(), no));
            }
            results.addAll(this.parseCombineFilter(and.other(), no, Combinator.AND));
            return results;
        } else if (pointcut instanceof Exportable result) {
            results.add(new PointcutDefine(no, append ? combinator : null, result.toPrettyString()));
        }
        return results;
    }

    private PointcutView initStepEditDialog(final Action action) {
        final PointcutView pointcutView = new PointcutView();
        pointcutView.open(this.currentWindow(), it -> {
            if (action == Action.ADD) {
                this.setTarget(this.target.get().or(it));
            } else {
                final Pointcut current = this.target.get();
                final PointcutDefine item = this.pointcutTable.getSelectionModel().getSelectedItem();
                final Pointcut before = current.toListTopLevelCondition().get(item.index());
                this.setTarget(current.toListTopLevelCondition()
                        .stream()
                        .map(pointcut -> pointcut == before ? it : pointcut)
                        .reduce(Pointcut.NONE, Pointcut::or));
            }
        });
        return pointcutView;
    }

    private Window currentWindow() {
        return this.pointcutTable.getScene().getWindow();
    }

    private static class PointcutDefine {
        private final IntegerProperty no;

        private final ObjectProperty<Combinator> combinator;

        private final StringProperty script;

        public PointcutDefine(final Integer no, final Combinator combinator, final String script) {
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

        public int no() {
            return this.no.get();
        }

        public int index() {
            return this.no() - 1;
        }
    }

    private enum Combinator {
        OR, AND
    }

}
