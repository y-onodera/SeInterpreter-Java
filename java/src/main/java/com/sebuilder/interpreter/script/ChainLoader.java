package com.sebuilder.interpreter.script;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.ScriptFile;
import com.sebuilder.interpreter.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ChainLoader {
    private final Sebuilder sebuilder;
    private final ScriptFile suiteFile;
    private final JSONArray scriptArrays;
    private int index;
    private final List<Integer> alreadyBreak;
    private final OverrideSettingLoader overrideSettingLoader;

    public ChainLoader(final Sebuilder sebuilder, final OverrideSettingLoader overrideSettingLoader, final ScriptFile suiteFile, final JSONArray scriptArrays) {
        this.sebuilder = sebuilder;
        this.overrideSettingLoader = overrideSettingLoader;
        this.suiteFile = suiteFile;
        this.scriptArrays = scriptArrays;
        this.alreadyBreak = Lists.newArrayList();
    }

    public TestCase load() {
        TestCase result = this.next();
        while (this.hasNext()) {
            result = result.map(it -> it.addChain(this.next()));
        }
        return result.map(it -> it.isChainTakeOverLastRun(true));
    }

    protected boolean hasNext() {
        return this.index < this.scriptArrays.length();
    }

    protected TestCase next() {
        TestCase loaded = this.sebuilder.loadScript(this.scriptArrays.getJSONObject(this.index++), this.suiteFile.toFile());
        if (loaded.nestedChain()) {
            loaded = this.loadNestedChain(loaded);
        }
        return loaded;
    }

    protected TestCase loadNestedChain(final TestCase loaded) {
        if (!this.hasNext()) {
            return loaded;
        }
        TestCase result = loaded;
        while (this.hasNext()) {
            final JSONObject json = this.scriptArrays.getJSONObject(this.index);
            if (!this.alreadyBreak.contains(this.index) && this.overrideSettingLoader.isBreakNestedChain(json)) {
                this.alreadyBreak.add(this.index);
                break;
            } else {
                this.index++;
            }
            TestCase next = this.sebuilder.loadScript(json, this.suiteFile.toFile());
            if (next.nestedChain()) {
                next = this.loadNestedChain(next);
            }
            final TestCase nextCase = next;
            result = result.map(it -> it.addChain(nextCase));
        }
        return result.map(it -> it.isChainTakeOverLastRun(true));
    }
}
