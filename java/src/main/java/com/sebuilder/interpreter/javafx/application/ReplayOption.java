package com.sebuilder.interpreter.javafx.application;

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

public class ReplayOption {
    private final Map<String, Pair<Integer, InputData>> dataLoadSettings;
    private final boolean isAspectTakeOver;

    public ReplayOption(Map<String, Pair<Integer, InputData>> dataLoadSettings, boolean isAspectTakeOver) {
        this.dataLoadSettings = dataLoadSettings;
        this.isAspectTakeOver = isAspectTakeOver;
    }

    public InputData reduceShareInput(InputData defaultValue, DataSourceLoader[] shareDataSources) throws IOException {
        InputData result = defaultValue;
        for (DataSourceLoader loader : shareDataSources) {
            DataSourceLoader withShareInput = loader.shareInput(result);
            if (withShareInput.isLoadable()) {
                result = this.merge(result, withShareInput);
            }
        }
        return result;
    }

    public Iterable<DataSourceLoader> filterLoadableSource(InputData replayShareInput, DataSourceLoader[] displayTestCaseDataSources) throws IOException {
        List<DataSourceLoader> result = Lists.newArrayList();
        InputData shareInput = replayShareInput;
        for (DataSourceLoader loader : displayTestCaseDataSources) {
            DataSourceLoader withShareInput = loader.shareInput(shareInput);
            if (withShareInput.isLoadable()) {
                result.add(withShareInput);
                shareInput = this.merge(shareInput, withShareInput);
            }
        }
        return result;
    }

    public TestCaseBuilder apply(TestCaseBuilder target) {
        final TestCase targetBuild = target.isPreventContextAspect(!this.isAspectTakeOver).build();
        final Pair<Integer, InputData> runtimeInfo = this.dataLoadSettings.get(targetBuild.runtimeDataSet().name());
        return target.changeWhenConditionMatch(it -> runtimeInfo != null
                , it -> {
                    try {
                        return it.setOverrideTestDataSet(new Manual(), targetBuild.loadData()
                                .get(runtimeInfo.getKey() - 1)
                                .add(runtimeInfo.getValue())
                                .input());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private InputData merge(InputData shareInput, DataSourceLoader withShareInput) throws IOException {
        Pair<Integer, InputData> setting = this.getSetting(withShareInput.name());
        return shareInput
                .add(withShareInput.loadData().get(setting.getKey() - 1))
                .add(setting.getValue());
    }

    private Pair<Integer, InputData> getSetting(String dataSetName) {
        return Optional.ofNullable(this.dataLoadSettings.get(dataSetName))
                .orElse(new Pair<>(1, new InputData()));
    }

}
