package com.sebuilder.interpreter.steptype;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Cmd implements Getter {
    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
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
            return String.valueOf(process.waitFor());
        } catch (InterruptedException | IOException e) {
            ctx.log().error(e);
            return "false";
        }
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return "value";
    }
}
