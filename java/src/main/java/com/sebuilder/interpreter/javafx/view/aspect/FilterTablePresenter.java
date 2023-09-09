package com.sebuilder.interpreter.javafx.view.aspect;

import com.sebuilder.interpreter.Exportable;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.control.dragdrop.DragAndDropTableViewRowFactory;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

public class FilterTablePresenter {
    @Inject
    private SeInterpreterApplication application;
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
    void initialize() {
        this.no.setCellValueFactory(body -> body.getValue().levelProperty());
        this.combinator.setCellValueFactory(body -> body.getValue().combinatorProperty());
        this.filter.setCellValueFactory(body -> body.getValue().scriptProperty());
        this.filterTable.setRowFactory(new DragAndDropTableViewRowFactory<>() {
            @Override
            protected void updateItemCallback(final TableRow<FilterDefine> tableRow, final FilterDefine stepDefine, final boolean empty, final int notEmptyValCount) {
                if (!empty && !tableRow.isEmpty()) {
                    if (notEmptyValCount == 1) {
                        tableRow.setOnMouseClicked(ev -> {
                            if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                FilterTablePresenter.this.handleStepEdit();
                            }
                        });
                    }
                }
            }

            @Override
            protected void move(final int draggedIndex, final int dropIndex) {
                FilterTablePresenter.this.moveStep(draggedIndex, dropIndex);
            }

        });
    }

    private void handleStepEdit() {
    }

    private void moveStep(final int draggedIndex, final int dropIndex) {
    }

    private void refreshTable() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.filterTable.getItems().clear();
            this.filterTable.getItems().setAll(new ArrayList<>());
            for (final FilterDefine row : this.parseFilter()) {
                this.filterTable.getItems().add(row);
            }
            this.filterTable.refresh();
        });
    }

    public void setTarget(final Pointcut pointcut) {
        this.target.set(pointcut);
        this.refreshTable();
    }

    private Collection<FilterDefine> parseFilter() {
        return this.parseFilter(this.target.get(), 1);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no) {
        return this.parseFilter(pointcut, no, null, false, false);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no, final boolean append) {
        return this.parseFilter(pointcut, no, null, true, append);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no, final Combinator combinator) {
        return this.parseFilter(pointcut, no, combinator, true, true);
    }

    private Collection<FilterDefine> parseFilter(final Pointcut pointcut, final int no, final Combinator combinator, final boolean nested, final boolean append) {
        final ArrayList<FilterDefine> results = new ArrayList<>();
        if (pointcut instanceof Pointcut.Or or) {
            if (combinator == Combinator.AND) {
                results.add(new FilterDefine(no, append ? combinator : null, "("));
                results.addAll(this.parseFilter(or.origin(), no, false));
                results.addAll(this.parseFilter(or.other(), no, Combinator.OR));
                results.add(new FilterDefine(no, null, ")"));
            } else if (combinator == Combinator.OR || append) {
                results.addAll(this.parseFilter(or.origin(), no, Combinator.OR));
                results.addAll(this.parseFilter(or.other(), no, Combinator.OR));
            } else {
                if (nested) {
                    results.addAll(this.parseFilter(or.origin(), no, false));
                    results.addAll(this.parseFilter(or.other(), no, Combinator.OR));
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
                results.addAll(this.parseFilter(and.origin(), no, false));
            }
            results.addAll(this.parseFilter(and.other(), no, Combinator.AND));
            return results;
        } else if (pointcut instanceof Exportable result) {
            results.add(new FilterDefine(no, append ? combinator : null, result.toPrettyString()));
        }
        return results;
    }

    private static class FilterDefine {
        private final IntegerProperty level;

        private final ObjectProperty<Combinator> combinator;

        private final StringProperty script;

        public FilterDefine(final Integer level, final Combinator combinator, final String script) {
            this.level = new SimpleIntegerProperty(level);
            this.combinator = new SimpleObjectProperty<>(combinator);
            this.script = new SimpleStringProperty(script);
        }

        public int getLevel() {
            return this.level.get();
        }

        public IntegerProperty levelProperty() {
            return this.level;
        }

        public Combinator getCombinator() {
            return this.combinator.get();
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
