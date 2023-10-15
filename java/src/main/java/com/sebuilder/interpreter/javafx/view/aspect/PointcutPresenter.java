package com.sebuilder.interpreter.javafx.view.aspect;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.StepSelectable;
import com.sebuilder.interpreter.javafx.view.step.StepPresenter;
import com.sebuilder.interpreter.pointcut.*;
import com.sebuilder.interpreter.step.Verify;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.tbee.javafx.scene.layout.fxml.MigPane;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PointcutPresenter implements StepSelectable {
    private static final String[] FILTER_TYPES = {
            ""
            , "type"
            , "locator"
            , "stringParam"
            , "skip"
            , "negated"
    };
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private MigPane pointcutGrid;
    @FXML
    private StepPresenter stepEditorController;

    private final List<String> selectableTypes = new ArrayList<>(Arrays.asList(FILTER_TYPES));

    private final List<PointCutValues> inputs = new ArrayList<>();

    private Consumer<Pointcut> applyPointcut;

    @FXML
    public void initialize() {
        this.refreshView("", 0);
    }

    void setApplyAction(final Stage dialog, final Consumer<Pointcut> applyAction) {
        this.applyPointcut = applyAction;
        this.stepEditorController.populate(dialog, s -> s.startsWith("verify"), key -> !key.equals("skip")
                , (interpreter, step) -> {
                    Pointcut result = this.createPointcut();
                    if (step != null) {
                        result = result.and(new VerifyFilter((Verify) step.type()
                                , step.stringParams()
                                , step.locatorParams()));
                    }
                    this.applyPointcut.accept(result);
                });
    }

    void refreshView(final Pointcut pointcut) {
        int row = 0;
        for (final Pointcut it : pointcut.getLeafCondition()) {
            final PointCutValues values;
            if (it instanceof TypeFilter type) {
                values = new TypeValues(type, row);
            } else if (it instanceof LocatorFilter locator) {
                values = new LocatorValues(locator, row);
            } else if (it instanceof SkipFilter skip) {
                values = new SkipValues(skip, row);
            } else if (it instanceof NegatedFilter negated) {
                values = new NegatedValues(negated, row);
            } else if (it instanceof StringParamFilter stringParam) {
                values = new StringParamValues(stringParam, row);
            } else {
                values = null;
            }
            if (values != null) {
                this.inputs.add(values);
                row = values.lastRow() + 1;
            }
        }
        this.refreshView("", row);
    }

    void refreshView(final String selected, final int rowIndex) {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.pointcutGrid.getChildren().clear();
            this.inputs.removeIf(it -> it.startRow() >= rowIndex);
            switch (selected) {
                case "type" -> this.inputs.add(new TypeValues(rowIndex));
                case "locator" -> this.inputs.add(new LocatorValues(rowIndex));
                case "stringParam" -> this.inputs.add(new StringParamValues(rowIndex));
                case "skip" -> this.inputs.add(new SkipValues(rowIndex));
                case "negated" -> this.inputs.add(new NegatedValues(rowIndex));
            }
            this.inputs.forEach(it -> {
                if (!"stringParam".equals(it.filterType())) {
                    this.selectableTypes.remove(it.filterType());
                }
            });
            IntStream.range(0, this.inputs.size()).forEach(index -> {
                final PointCutValues pointCutValues = this.inputs.get(index);
                this.addFilterNode(pointCutValues.filterType(), pointCutValues.startRow());
                pointCutValues.toInputNode().forEach(pair -> this.pointcutGrid.add(pair.getKey(), pair.getValue()));
            });
            if (this.inputs.size() == 0) {
                this.addFilterNode(selected, 0);
            } else {
                this.addFilterNode("", this.inputs.get(this.inputs.size() - 1).lastRow() + 1);
            }
            if (this.pointcutGrid.getScene() != null) {
                this.currentWindow().sizeToScene();
            }
        });
    }

    private void addFilterNode(final String selected, final int rowIndex) {
        final ComboBox<String> andTypeSelect;
        if (rowIndex == 0) {
            this.pointcutGrid.add(new Label("filter"), "cell 0 " + rowIndex);
            andTypeSelect = new ComboBox<>();
            andTypeSelect.getItems().setAll(FILTER_TYPES);
            andTypeSelect.setValue(selected);
        } else {
            this.pointcutGrid.add(new Label("and filter"), "cell 0 " + rowIndex);
            andTypeSelect = this.createAndTypeSelect(selected);
        }
        andTypeSelect.getSelectionModel().selectedItemProperty().addListener((options, oldVal, newVal) -> {
            this.selectableTypes.clear();
            this.selectableTypes.addAll(List.of(FILTER_TYPES));
            if (!Objects.equals(oldVal, newVal)) {
                this.refreshView(newVal, rowIndex);
            }
        });
        this.pointcutGrid.add(andTypeSelect, "cell 1 " + rowIndex);
    }

    private ComboBox<String> createAndTypeSelect(final String selected) {
        final ComboBox<String> result = new ComboBox<>();
        result.getItems().addAll(this.selectableTypes);
        result.getSelectionModel().select(selected);
        return result;
    }

    private Pointcut createPointcut() {
        return this.inputs.stream()
                .map(PointCutValues::toPointcut)
                .reduce(Pointcut.ANY, Pointcut::and);
    }

    private Window currentWindow() {
        return this.pointcutGrid.getScene().getWindow();
    }

    interface PointCutValues {
        List<Pair<Node, String>> toInputNode();

        Pointcut toPointcut();

        int startRow();

        int lastRow();

        String filterType();

    }

    static class TypeValues implements PointCutValues {

        private final ComboBox<String> method = new ComboBox<>();
        private final ComboBox<String> type = new ComboBox<>();
        private final List<Pair<Node, String>> result = new ArrayList<>();
        private final int lastRow;

        public TypeValues(final int rowIndex) {
            this.method.getItems().addAll(Pointcut.METHODS.keySet());
            this.type.getItems().addAll(STEP_TYPES);
            this.type.setEditable(true);
            this.result.add(new Pair<>(this.method, "grow,cell 2 " + rowIndex));
            this.result.add(new Pair<>(this.type, "wrap,span,grow,cell 3 " + rowIndex));
            this.lastRow = rowIndex;
        }

        public TypeValues(final TypeFilter type, final int row) {
            this(row);
            this.method.setValue(type.method());
            this.type.setValue(type.target());
        }

        @Override
        public List<Pair<Node, String>> toInputNode() {
            return this.result;
        }

        @Override
        public Pointcut toPointcut() {
            return new TypeFilter(this.type.getValue(), this.method.getValue());
        }

        @Override
        public int lastRow() {
            return this.lastRow;
        }

        @Override
        public int startRow() {
            return this.lastRow;
        }

        @Override
        public String filterType() {
            return "type";
        }
    }

    static class LocatorValues implements PointCutValues {

        private final TextField key = new TextField("locator");
        private final ComboBox<String> method = new ComboBox<>();
        private final ComboBox<String> type = new ComboBox<>();
        private final TextField value = new TextField();
        private final List<Pair<Node, String>> result = new ArrayList<>();
        private final int lastRow;

        public LocatorValues(final int row) {
            this.key.setPromptText("key");
            this.method.getItems().addAll(Pointcut.METHODS.keySet());
            this.type.getItems().addAll(Stream.of(Locator.Type.values()).map(Object::toString).toList());
            this.type.setEditable(true);
            this.type.setPromptText("locator type");
            this.value.setPromptText("value");
            this.result.add(new Pair<>(this.key, "grow,cell 2 " + row));
            this.result.add(new Pair<>(this.method, "grow,cell 3 " + row));
            this.result.add(new Pair<>(this.type, "wrap,span,grow,cell 4 " + row));
            this.lastRow = row + 1;
            this.result.add(new Pair<>(this.value, "wrap,grow,cell 4 " + this.lastRow));
        }

        public LocatorValues(final LocatorFilter locator, final int row) {
            this(row);
            this.key.setText(locator.key());
            this.method.setValue(locator.method());
            this.type.setValue(locator.target().type());
            this.value.setText(locator.target().value());
        }

        @Override
        public List<Pair<Node, String>> toInputNode() {
            return this.result;
        }

        @Override
        public Pointcut toPointcut() {
            return new LocatorFilter(this.key.getText()
                    , new Locator(this.type.getValue(), this.value.getText())
                    , this.method.getValue());
        }

        @Override
        public int lastRow() {
            return this.lastRow;
        }

        @Override
        public int startRow() {
            return this.lastRow - 1;
        }

        @Override
        public String filterType() {
            return "locator";
        }
    }

    static class SkipValues implements PointCutValues {

        private final CheckBox skip = new CheckBox();
        private final List<Pair<Node, String>> result = new ArrayList<>();
        private final int lastRow;

        public SkipValues(final int row) {
            this.result.add(new Pair<>(this.skip, "wrap,cell 2 " + row));
            this.lastRow = row;
        }

        public SkipValues(final SkipFilter skip, final int row) {
            this(row);
            this.skip.setSelected(skip.target());
        }

        @Override
        public List<Pair<Node, String>> toInputNode() {
            return this.result;
        }

        @Override
        public Pointcut toPointcut() {
            return new SkipFilter(this.skip.isSelected());
        }

        @Override
        public int lastRow() {
            return this.lastRow;
        }

        @Override
        public int startRow() {
            return this.lastRow;
        }

        @Override
        public String filterType() {
            return "skip";
        }
    }

    static class NegatedValues implements PointCutValues {

        private final CheckBox negated = new CheckBox();
        private final List<Pair<Node, String>> result = new ArrayList<>();
        private final int lastRow;

        public NegatedValues(final int row) {
            this.result.add(new Pair<>(this.negated, "wrap,cell 2 " + row));
            this.lastRow = row;
        }

        public NegatedValues(final NegatedFilter negated, final int row) {
            this(row);
            this.negated.setSelected(negated.target());
        }

        @Override
        public List<Pair<Node, String>> toInputNode() {
            return this.result;
        }

        @Override
        public Pointcut toPointcut() {
            return new NegatedFilter(this.negated.isSelected());
        }

        @Override
        public int lastRow() {
            return this.lastRow;
        }

        @Override
        public int startRow() {
            return this.lastRow;
        }

        @Override
        public String filterType() {
            return "negated";
        }
    }

    static class StringParamValues implements PointCutValues {

        private final TextField key = new TextField();
        private final ComboBox<String> method = new ComboBox<>();
        private final TextField value = new TextField();
        private final List<Pair<Node, String>> result = new ArrayList<>();
        private final int lastRow;

        public StringParamValues(final int row) {
            this.key.setPromptText("key");
            this.method.getItems().addAll(Pointcut.METHODS.keySet());
            this.value.setPromptText("value");
            this.result.add(new Pair<>(this.key, "grow,cell 2 " + row));
            this.result.add(new Pair<>(this.method, "grow,cell 3 " + row));
            this.result.add(new Pair<>(this.value, "wrap,grow,cell 4 " + row));
            this.lastRow = row;
        }

        public StringParamValues(final StringParamFilter stringParam, final int row) {
            this(row);
            this.key.setText(stringParam.key());
            this.method.setValue(stringParam.method());
            this.value.setText(stringParam.target());
        }

        @Override
        public List<Pair<Node, String>> toInputNode() {
            return this.result;
        }

        @Override
        public Pointcut toPointcut() {
            return new StringParamFilter(this.key.getText(), this.value.getText(), this.method.getValue());
        }

        @Override
        public int lastRow() {
            return this.lastRow;
        }

        @Override
        public int startRow() {
            return this.lastRow;
        }

        @Override
        public String filterType() {
            return "stringParam";
        }
    }
}
