package com.sebuilder.interpreter.step.getter;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.TestRun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Cmd extends AbstractGetter {

    @Override
    public String get(final TestRun ctx) {
        final String[] cmd = ctx.string("cmd").split(",");
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        // 標準エラー出力を標準出力にマージする
        pb.redirectErrorStream(true);
        try {
            final Process process = pb.start();
            final InputStream in = process.getInputStream();
            final BufferedReader br = new BufferedReader(new InputStreamReader(in, "MS932"));
            String stdout = "";
            while ((stdout = br.readLine()) != null) {
                // 不要なメッセージを表示しない
                if (Strings.isNullOrEmpty(stdout)) {
                    continue;
                }
                if (stdout.contains("echo off ")) {
                    continue;
                }
                if (stdout.contains("続行するには何かキーを押してください ")) {
                    continue;
                }
                ctx.log().info(stdout);
            }
            return String.valueOf(process.waitFor());
        } catch (final InterruptedException | IOException e) {
            ctx.log().error(e);
            return "false";
        }
    }

    @Override
    public String cmpParamName() {
        return "value";
    }
}
