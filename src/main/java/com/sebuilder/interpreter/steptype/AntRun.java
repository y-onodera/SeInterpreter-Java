package com.sebuilder.interpreter.steptype;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.TestRun;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

public class AntRun implements Getter {

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        File buildFile = new File(ctx.string("build.xml"));
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        ctx.vars().forEach((k, v) -> p.setProperty(k, v));
        String target = p.getDefaultTarget();
        if (ctx.containsKey("target") && !Strings.isNullOrEmpty(ctx.string("target"))) {
            target = ctx.string("target");
        }
        p.executeTarget(target);
        return Objects.toString(p.getProperties().get(ctx.string("resultProperty")), "");
    }

    @Override
    public String cmpParamName() {
        return "value";
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("build.xml")) {
            o.put("build.xml", "${_baseDir}/build.xml");
        }
        if (!o.has("target")) {
            o.put("target", "");
        }
        if (!o.has("resultProperty")) {
            o.put("resultProperty", "resultProperty");
        }
    }

}
