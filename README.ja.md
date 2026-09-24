# AutoPalette（クライアントサイド）

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · **日本語** · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/autopalette?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge](https://img.shields.io/curseforge/v/1577747?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![Minecraft](https://img.shields.io/modrinth/game-versions/autopalette?label=Minecraft)](https://modrinth.com/mod/autopalette/versions)
[![Downloads](https://img.shields.io/modrinth/dt/autopalette?label=Downloads&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1577747?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/suoim)

Minecraft でエゼルに何時間も座って、染料で 1 ピクセルずつクリックしてカスタム画像を描くのに疲れていませんか？

AutoPalette は Minecraft 1.21.x 向けの軽量クライアントサイド Fabric モッドで、サーバーサイド ArtMap（または DFArt）のエゼル上にカスタム画像を自動で描画します。画像を選んで、自動生成された染料の買い物リストを確認して、座って、モッドに描かせましょう！

---

## 機能

* **直感的なゲーム内 GUI**：H キー（カスタマイズ可能）でゲーム内ダッシュボードを開きます。ローカル画像を閲覧し、設定を調整し、プレビューがリアルタイムに変化するのを確認できます。
* **量子化とディザリング**：直接の最近傍色マッチング、または高品質な Floyd-Steinberg ディザリングを選択でき、地図上で映像を非常に鮮明に見せます。
* **最大パレット制限**：画像のパレットを最も頻出する Unlimited / 32 / 16 / 8 色に制限できます。GUI プレビューは選択を反映して即座に再描画されます。
* **ゲーム内買い物リスト**：Materials タブが必要な染料・石炭・羽の数を正確に計算します。各項目の隣に 16x16 の Minecraft アイコンも表示します。
* **スマートなピクセルスキップ**：描画前にキャンバス状態を読み取り、画像と一致するピクセルを自動スキップし、大量の染料と描画時間を節約します。
* **石炭と木炭の相互互換性**：陰影とベースの黒ステップで石炭と木炭を柔軟に使い分け、インベントリにある方を使います。
* **ピクセル完璧なアライメント**：事前シリアライズのルックアップ（east.ser、west.ser など）とバニラ MapColor データベース（mappings.json）を使い、キャンバス上の境界整合と色精度を 100% 保証します。

---

## 使い方

1. **画像をインポート**：PNG または JPG を `.minecraft/config/autopalette/images/` に置きます。
2. **GUI を開く**：キャンバス付き ArtMap エゼルに座り、H キーでパネルを開きます。
3. **設定**：画像を選択、ディザリング切替、描画ディレイ設定、色数上限を選択。
   ![ゲーム内 GUI](https://cdn.modrinth.com/data/cached_images/76bbe40e593a4f935f502cc590b1f84610ba0a36_0.webp)
4. **染料を集める**：Materials タブで買い物リストを確認し、必要なアイテムを集めます。
   ![材料リスト GUI](https://cdn.modrinth.com/data/cached_images/4bc3295dfda3c289cf2de34a39b12573389b8630.png)
5. **描画**：Start Drawing をクリック！モッドが道具/染料をホットバーに自動交換し、カメラを回してキャンバスを描画します。

---

## 依存関係と互換性

* **プラットフォーム**：Fabric
* **Minecraft バージョン**：1.21.8（マイナーリリース 1.21.9〜1.21.11 はデフォルト対応）
* **依存**：Fabric Loader と Fabric API のみ
* **サーバー互換**：ArtMap または DFArt Bukkit/Spigot プラグインを動かす任意のサーバーで動作

---

## 開発者セットアップとビルド

ソースから AutoPalette をビルドしたい場合：

### 1. リポジトリをクローン

```bash
git clone https://github.com/aacanadaa/AutoPalette.git
cd AutoPalette
```

### 2. .jar をビルド

このプロジェクトは Gradle を使います。OS に合うコマンドを実行：

```bash
# Windows:
gradlew build

# Mac/Linux:
./gradlew build
```

完了後、コンパイル済みの本番用モッドは `build/libs/` にあります。

---

## ライセンス

このプロジェクトは [Apache License 2.0](LICENSE) の下でライセンスされています。

## コントリビューションとサポート

コントリビューション、バグ報告、Pull Request を歓迎します。バグや提案があれば、上の Issues タブから issue を開いてください。

---

*免責事項：これはクライアントサイドのユーティリティモッドです。公開マルチプレイヤーネットワークで使う前に、サーバーの自動化に関するルールを確認してください。*
