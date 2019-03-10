package com.sebuilder.interpreter.steptype;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecCmd implements StepType {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        String[] cmd = ctx.string("cmd").split(",");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        // 標準エラー出力を標準出力にマージする
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "MS932"));
            String stdout = "";
            while ((stdout = br.readLine()) != null) {
                // 不要なメッセージを表示しない
                if (Strings.isNullOrEmpty(stdout))
                    continue;
                if (stdout.contains("echo off "))
                    continue;
                if (stdout.contains("続行するには何かキーを押してください "))
                    continue;
                ctx.log().info(stdout);
            }
            return process.waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            ctx.log().error(e);
            return false;
        }
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("cmd")) {
            o.put("cmd", "");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
