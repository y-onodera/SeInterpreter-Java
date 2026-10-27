# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Selenium Builder 形式（JSON）のスクリプトを実行する Java 製インタプリタ。CLI・REPL・JavaFX GUI を持つ。やりとりは日本語で行う。

## 実行環境（Claude Code on the web）
Java 25 が必要（JavaFX 27 の要件）。web 環境では `.claude/settings.json` の SessionStart フックが `.claude/start-maven-proxy.sh` を実行し、JDK 25 を `~/jdk-25` に導入して `~/.mavenrc` で Maven の `JAVA_HOME` に設定する（`CLAUDE_CODE_REMOTE=true` のときのみ。ローカルでは何もしない）。
- Maven は `mvn` をそのまま使えば JDK 25 で動く。PATH 上の `java` は 21 のことがあるので、jar を直接動かすときは `~/jdk-25/bin/java` を使う
- フック・`.claude/maven-proxy.py` は dbunitcli と同じもの。web 環境のプロキシ方式や JDK 配布 URL が変わって Maven/Java が動かなくなったらスクリプトをメンテする

## コマンド（`java/` で実行）
- テスト: `mvn test`
- 単一テスト: `mvn test -Dtest=TestRunTest`、ネストクラスは `mvn test -Dtest='TestRunTest$WithStepTest'`
- CI と同じビルド: `mvn package test allure:report -Pbuild`（`-Pbuild` は JavaFX を provided にし、`*-jar-with-dependencies.jar` に含めない。既定の `testrun` プロファイルは compile スコープ）
- GUI 起動: `mvn javafx:run@run`（デバッグ: `@debug`、8000 番で待ち受け）
- CLI 実行: `java -cp target/sebuilder-interpreter-2.0.0-SNAPSHOT-jar-with-dependencies.jar com.sebuilder.interpreter.application.SeInterpreter [--driver=... --datasource.directory=... --aspectFile=...] script.json`（オプションは `application/CommandLineArgument`）
- `process-classes` フェーズで `javafx.build.Css2Bin` が GUI の CSS を bss に変換する
- E2E: CI（`.github/workflows/maven.yaml`）が jlink した JRE と jar で `test/suite/testsuite.json` を実行する。Selenium Grid は `test/docker-compose.yml`

## アーキテクチャ（`com.sebuilder.interpreter`）
- `Context`（enum シングルトン）がブラウザ・データソース・出力先・既定 Aspect などのグローバル設定を保持し、CLI/GUI 双方が起動時に設定する
- スクリプト読込: `ScriptParser` の実装 `script.Sebuilder`（独自 JSON 形式）と `script.seleniumide.SeleniumIDE`（変換して読込）。`*Loader` が step・chain・import・aspect・pointcut・データソース設定をそれぞれ担う
- モデル: `Suite` → `TestCase`（`TestCaseChains` で入れ子・連鎖、`DataSource` によるデータ駆動）→ `Step`（`StepType` + 文字列/ロケータパラメータ）。各ビルダー経由で不変オブジェクトとして組み立てる
- ステップ型の解決: `step.StepTypeFactoryImpl` が名前からリフレクションで `step.type.<Name>` を引く。`assert`/`verify`/`waitFor`/`store`/`print`/`if`/`retry` 接頭辞付きは `step.getter.<Name>`（`Getter`）と `step` の同名ラッパー（`Assert` 等）の組み合わせ。新しいステップは命名規約どおりにクラスを置くだけで使える
- 実行: `TestRun` が 1 テストケースを実行し、`TestRunListener`（`report/` の JUnit/ExtentReports 形式）に結果を通知する。`InputData` が変数展開を担う
- AOP: `Aspect` は `Interceptor`（主に `ExtraStepExecutor`）の集合で、`Pointcut`（`pointcut/` の各フィルタ）に一致したステップの前後・失敗時に別テストケースを差し込む（既定は失敗時スクリーンショット）
- GUI: `javafx/` は afterburner.fx の MVP 構成（`view/*/XxxView` + `XxxPresenter` + fxml/css）。実行・デバッグは `javafx/model`（`SeInterpreterRunner`・`Debugger`）

## 依存関係の更新
- `selenium-java` は同梱される番号付き `selenium-devtools-vNNN` を全て exclusion し、`selenium-devtools-latest` だけ残す。更新時は `mvn dependency:tree -Dincludes=org.seleniumhq.selenium` で同梱バージョンを確認して exclusion を差し替える
- `commons-jexl3` は 3.6.1 に固定。3.7.0 以降はスクリプト内の `has('x')`/`get('x')`（`MapContext` のメソッド呼び出し）が解決できず `${...}` が展開されなくなる（`InputDataTest.evaluateContextFunction` で検知）
- JavaFX/Java のバージョンを変えるときは `maven-compiler-plugin` の source/target、`.github/workflows/maven.yaml` の `java-version`（build・jlink の 2 か所）、`.claude/start-maven-proxy.sh` の `REQUIRED_JAVA_VERSION` を揃える
