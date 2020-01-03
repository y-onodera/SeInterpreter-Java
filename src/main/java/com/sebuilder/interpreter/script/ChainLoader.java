package com.sebuilder.interpreter.script;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.ScriptFile;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestRunListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ChainLoader {
    private final Sebuilder sebuilder;
    private final ScriptFile suiteFile;
    private final JSONArray scriptArrays;
    private int index;
    private List<Integer> alreadyBreak;
    private TestRunListener testRunListener;

    public ChainLoader(Sebuilder sebuilder, ScriptFile suiteFile, JSONArray scriptArrays, TestRunListener testRunListener) {
        this.sebuilder = sebuilder;
        this.suiteFile = suiteFile;
        this.scriptArrays = scriptArrays;
        this.testRunListener = testRunListener;
        this.alreadyBreak = Lists.newArrayList();
    }

    public TestCase load() throws JSONException, IOException {
        TestCase result = this.next();
        while (this.hasNext()) {
            final TestCase next = this.next();
            result = result.map(it -> it.addChain(next));
        }
        return result.map(it -> it.isChainTakeOverLastRun(true));
    }

    protected boolean hasNext() {
        return this.index < this.scriptArrays.length();
    }

    protected TestCase next() throws JSONException, IOException {
        TestCase loaded = this.sebuilder.loadScript(this.scriptArrays.getJSONObject(this.index++), this.suiteFile.toFile(), this.testRunListener);
        if (loaded.isNestedChain()) {
            loaded = this.loadNestedChain(loaded);
        }
        return loaded;
    }

    protected TestCase loadNestedChain(TestCase loaded) throws JSONException, IOException {
        if (!this.hasNext()) {
            return loaded;
        }
        TestCase result = loaded;
        while (this.hasNext()) {
            JSONObject json = this.scriptArrays.getJSONObject(this.index);
            if (!this.alreadyBreak.contains(this.index) && this.sebuilder.isBreakNestedChain(json)) {
                this.alreadyBreak.add(this.index);
                break;
            } else {
                this.index++;
            }
            TestCase next = this.sebuilder.loadScript(json, this.suiteFile.toFile(), this.testRunListener);
            if (next.isNestedChain()) {
                next = this.loadNestedChain(next);
            }
            final TestCase nextCase = next;
            result = result.map(it -> it.addChain(nextCase));
        }
        return result.map(it -> it.isChainTakeOverLastRun(true));
    }
}
