package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.DataSource;
import org.json.JSONObject;

import java.util.HashMap;

public class DataSourceConfigLoader {

    public DataSource getDataSource(final JSONObject o) {
        if (!o.has("data")) {
            return DataSource.NONE;
        }
        final JSONObject data = o.getJSONObject("data");
        return Context.getDataSourceFactory().getDataSource(data.getString("source"));
    }

    public HashMap<String, String> getDataSourceConfig(final JSONObject o) {
        if (!o.has("data")) {
            return new HashMap<>();
        }
        final JSONObject data = o.getJSONObject("data");
        final String sourceName = data.getString("source");
        final HashMap<String, String> config = new HashMap<>();
        if (data.has("configs") && data.getJSONObject("configs").has(sourceName)) {
            final JSONObject cfg = data.getJSONObject("configs").getJSONObject(sourceName);
            cfg.keySet().forEach(key -> config.put(key, cfg.getString(key)));
        }
        return config;
    }

}
