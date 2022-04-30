package com.sebuilder.interpreter.step.getter;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.Utils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.util.Objects;

public class AntRun extends AbstractGetter {

    @Override
    public String get(TestRun ctx) {
        File buildFile = Utils.findFile(ctx.getRelativePath(), ctx.string("build.xml"));
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        ctx.vars()
                .entrySet()
                .forEach(entry -> p.setProperty(entry.getKey(), ctx.bindRuntimeVariables(entry.getValue())));
        Context.getEnvironmentProperties()
                .entrySet()
                .forEach(entry -> p.setProperty("env." + entry.getKey(), ctx.bindRuntimeVariables(entry.getValue().toString())));
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
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("build.xml")) {
            o.put("build.xml", "${_baseDir}/build.xml");
        }
        if (!o.containsStringParam("target")) {
            o.put("target", "");
        }
        if (!o.containsStringParam("resultProperty")) {
            o.put("resultProperty", "resultProperty");
        }
        return super.addDefaultParam(o);
    }
}
