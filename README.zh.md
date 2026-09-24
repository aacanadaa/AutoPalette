# AutoPalette（客户端）

[English](README.md) · **中文** · [Français](README.fr.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/autopalette?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge](https://img.shields.io/curseforge/v/1577747?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![Minecraft](https://img.shields.io/modrinth/game-versions/autopalette?label=Minecraft)](https://modrinth.com/mod/autopalette/versions)
[![Downloads](https://img.shields.io/modrinth/dt/autopalette?label=Downloads&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1577747?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/suoim)

在 Minecraft 里对着画架坐了好几个小时，用染料一个像素一个像素点，只为画一张自定义图片？

AutoPalette 是一款轻量的客户端 Fabric 模组，适用于 Minecraft 1.21.x，可自动在服务端 ArtMap（或 DFArt）画架上绘制自定义图片。选择你的图片，查看自动生成的染料购物清单，坐下，让模组替你画！

---

## 功能

* **直观的游戏内 GUI**：按 H（可自定义）打开游戏内控制面板。浏览本地图片、调整设置，实时预览图像变化。
* **量化与抖动**：可选择直接最近色匹配，或高质量 Floyd-Steinberg 抖动，让地图上的画面极其清晰。
* **最大调色板限制**：将图片调色板限制为出现频率最高的无限、32、16 或 8 种颜色。GUI 预览会立即重新渲染以反映你的选择。
* **游戏内购物清单**：材料标签页精确计算你需要多少染料、煤和羽毛，甚至在每项旁渲染实时 16x16 Minecraft 物品图标。
* **智能像素跳过**：绘制前读取画布状态，自动跳过已与图像匹配的像素，节省大量染料和绘制时间。
* **煤与木炭可互换**：着色和基础黑色步骤中智能地将煤与木炭视为等价，优先使用背包里有的。
* **像素级对齐**：使用预序列化查找表（east.ser、west.ser 等）和原版 MapColor 数据库（mappings.json），保证 100% 边界对齐和颜色准确。

---

## 使用方法

1. **导入图片**：将 PNG 或 JPG 图片放入 `.minecraft/config/autopalette/images/` 目录。
2. **打开 GUI**：坐在带画布的 ArtMap 画架上，按 H 打开面板。
3. **设置**：选择图片、切换抖动、设置绘制延迟、选择颜色上限。
   ![游戏内 GUI](https://cdn.modrinth.com/data/cached_images/76bbe40e593a4f935f502cc590b1f84610ba0a36_0.webp)
4. **收集染料**：点击材料标签页查看购物清单，收集所需物品。
   ![材料清单 GUI](https://cdn.modrinth.com/data/cached_images/4bc3295dfda3c289cf2de34a39b12573389b8630.png)
5. **绘制**：点击开始绘制！模组会自动将工具/染料换到快捷栏、转动视角并绘制画布。

---

## 依赖与兼容性

* **平台**：Fabric
* **Minecraft 版本**：1.21.8（开箱即用支持 1.21.9 至 1.21.11）
* **依赖**：仅需 Fabric Loader 和 Fabric API
* **服务端兼容**：适用于任何运行 ArtMap 或 DFArt Bukkit/Spigot 插件的服务器

---

## 开发者设置与编译

如果你想从源码自行构建 AutoPalette：

### 1. 克隆仓库

```bash
git clone https://github.com/aacanadaa/AutoPalette.git
cd AutoPalette
```

### 2. 构建 .jar 文件

本项目使用 Gradle。根据你的操作系统运行相应命令：

```bash
# Windows:
gradlew build

# Mac/Linux:
./gradlew build
```

完成后，编译好的生产可用模组位于 `build/libs/` 目录。

---

## 许可证

本项目采用 [Apache License 2.0](LICENSE)。

## 贡献与支持

欢迎贡献、错误报告和 Pull Request。如果发现问题或有建议，请在上方 Issues 标签页提交 issue。

---

*免责声明：这是一个客户端工具模组。在公共多人网络使用本模组前，请先确认服务器关于自动化玩法的规则。*
