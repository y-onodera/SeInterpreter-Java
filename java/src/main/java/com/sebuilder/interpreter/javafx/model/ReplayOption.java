package com.sebuilder.interpreter.javafx.model;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.datasource.Manual;
import javafx.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ReplayOption(
        Map<String, Pair<Integer, InputData>> dataLoadSettings,
        boolean isAspectTakeOver) {

    public InputData reduceShareInput(final InputData defaultValue, final DataSourceLoader[] shareDataSources) throws IOException {
        InputData result = defaultValue;
        for (final DataSourceLoader loader : shareDataSources) {
            final DataSourceLoader withShareInput = loader.shareInput(result);
            if (withShareInput.isLoadable()) {
                result = this.merge(result, withShareInput);
            }
        }
        return result;
    }

    public Iterable<DataSourceLoader> filterLoadableSource(final InputData replayShareInput, final DataSourceLoader[] displayTestCaseDataSources) throws IOException {
        final List<DataSourceLoader> result = Lists.newArrayList();
        InputData shareInput = replayShareInput;
        for (final DataSourceLoader loader : displayTestCaseDataSources) {
            final DataSourceLoader withShareInput = loader.shareInput(shareInput);
            if (withShareInput.isLoadable()) {
                result.add(withShareInput);
                shareInput = this.merge(shareInput, withShareInput);
            }
        }
        return result;
    }

    public TestCaseBuilder apply(final TestCaseBuilder target) {
        final TestCase targetBuild = target.isPreventContextAspect(!this.isAspectTakeOver).build();
        final Pair<Integer, InputData> runtimeInfo = this.dataLoadSettings.get(targetBuild.runtimeDataSet().name());
        return target.mapWhen(it -> runtimeInfo != null
                , it -> {
                    try {
                        return it.setOverrideTestDataSet(new Manual(), targetBuild.loadData()
                                .get(runtimeInfo.getKey() - 1)
                                .add(runtimeInfo.getValue())
                                .input());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private InputData merge(final InputData shareInput, final DataSourceLoader withShareInput) throws IOException {
        final Pair<Integer, InputData> setting = this.getSetting(withShareInput.name());
        return shareInput
                .add(withShareInput.loadData().get(setting.getKey() - 1))
                .add(setting.getValue());
    }

    private Pair<Integer, InputData> getSetting(final String dataSetName) {
        return Optional.ofNullable(this.dataLoadSettings.get(dataSetName))
                .orElse(new Pair<>(1, new InputData()));
    }

}
