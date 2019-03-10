package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.util.Map;

public class ScriptChain {

    private final Map<Script, Script> chains = Maps.newHashMap();

    public ScriptChain() {
        this(Maps.newHashMap());
    }

    public ScriptChain(Map<Script, Script> chains) {
        this.chains.putAll(chains);
    }

    public int size() {
        return this.chains.size();
    }

    public boolean containsKey(Script s) {
        return this.chains.containsKey(s);
    }

    public boolean containsValue(Script script) {
        return this.chains.containsValue(script);
    }

    public Script get(Script scriptFrom) {
        return this.chains.get(scriptFrom);
    }

    public ScriptChain copy() {
        return new ScriptChain(this.chains);
    }

    public ScriptChain replace(Script oldScript, Script newScript) {
        Map<Script, Script> newChain = Maps.newHashMap(this.chains);
        if (newChain.containsKey(oldScript) || newChain.containsValue(oldScript)) {
            for (Map.Entry<Script, Script> entry : this.chains.entrySet()) {
                if (entry.getKey() == oldScript) {
                    newChain.remove(oldScript);
                    newChain.put(newScript, entry.getValue());
                } else if (entry.getValue() == oldScript) {
                    newChain.put(entry.getKey(), newScript);
                }
            }
        }
        return new ScriptChain(newChain);
    }

    public ScriptChain add(Script chainFrom, Script to) {
        Map<Script, Script> newChain = Maps.newHashMap(this.chains);
        newChain.put(chainFrom, to);
        return new ScriptChain(newChain);
    }

}
