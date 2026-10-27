# SeInterpreter-Java

Selenium Builder 形式のスクリプトを実行する Java 製インタプリタ。やりとりは日本語で行う。

- `java/` - Maven プロジェクト本体（`com.sebuilder.interpreter`）
  - `application/` - CLI（`SeInterpreter`）・REPL（`SeInterpreterREPL`）
  - `javafx/` - JavaFX 製 GUI（`javafx.application.SeInterpreterApplication`）
  - `step/`・`script/`・`pointcut/`・`aspect/`・`datasource/`・`report/` などがインタプリタ本体
- `test/suite/` - jar を使った E2E 用のテストスイート（CI の jlink 後のジョブで実行）
- `test/docker-compose.yml` - E2E 用の Selenium Grid

## ビルド・テスト
Java 25 が必要（JavaFX 27 が JDK 25 以上を要求するため）。`java/` で実行する。

- テスト: `mvn test`
- CI と同じビルド: `mvn package test allure:report -Pbuild`（`-Pbuild` は JavaFX を provided にして shade jar から外す）

## 依存関係の更新
- `selenium-java` は同梱される番号付きの `selenium-devtools-vNNN` を全て exclusion し、`selenium-devtools-latest` だけ残す。バージョンを上げたら `mvn dependency:tree -Dincludes=org.seleniumhq.selenium` で同梱バージョンを確認し、exclusion を差し替える。
- JavaFX・Java のバージョンを変えるときは `maven-compiler-plugin` の source/target と `.github/workflows/maven.yaml` の `java-version`（build・jlink の2か所）を揃える。

## Claude Code on the web 用セットアップ
`.claude/settings.json` の SessionStart フックが `.claude/start-maven-proxy.sh` を実行し、JDK 25 導入・`~/.mavenrc` 作成・（認証付きプロキシ環境なら）`.claude/maven-proxy.py` の起動を行う。ローカルでは使わない。web 環境のプロキシ方式や JDK 配布 URL が変わって Maven/Java が動かなくなったら、このスクリプトをメンテする（dbunitcli と同じスクリプト）。
