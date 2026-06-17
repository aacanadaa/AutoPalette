
# AutoPalette (Client-Side)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform: Fabric](https://img.shields.io/badge/Platform-Fabric-orange.svg)](https://fabricmc.net/)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/autopalette?style=flat-square&color=242629)](https://modrinth.com/mod/autopalette)
[![GitHub Issues](https://img.shields.io/github/issues/aacanadaa/autopalette?style=flat-square&color=e6b800)](https://github.com/aacanadaa/autopalette/issues)

Tired of sitting at an easel for hours, clicking pixel-by-pixel with dyes, just to paint a custom image in Minecraft? 

AutoPalette is a lightweight, client-side Fabric mod for Minecraft 1.21.x that automates painting custom images on server-side ArtMap (or DFArt) easels. Simply select your image, check the automatically generated shopping list of dyes, sit down, and let the mod paint for you!

---

## Features

* **Intuitive In-Game GUI**: Press H (customizable) to open a control dashboard inside the game. Browse local images, tweak settings, and watch your image preview change in real-time.
* **Quantization and Dithering**: Choose between direct nearest-color matching or high-fidelity Floyd-Steinberg dithering to make pictures look incredibly crisp on the map.
* **Max Colors Palette Limiter**: Restrict your image's palette to the top Unlimited, 32, 16, or 8 most frequent colors. The GUI preview re-renders instantly to reflect your choice.
* **In-Game Shopping List**: The Materials tab calculates exactly how many dyes, coal, and feathers you need. It even renders live 16x16 Minecraft item icons next to each entry.
* **Smart Pixel Skipping**: The mod reads the canvas state before drawing and automatically skips painting any pixels that already match your image, saving you massive amounts of dye and drawing time.
* **Coal and Charcoal Interchangeability**: The mod intelligently treats coal and charcoal interchangeably for shading and base black steps, utilizing whatever is available in your inventory.
* **Pixel-Perfect Alignment**: Utilizes pre-serialized lookups (east.ser, west.ser, etc.) and a vanilla MapColor database (mappings.json) to guarantee 100% boundary alignment and color accuracy on the canvas.

---

## How to Use

1. **Import Images**: Put your image files (PNG or JPG) into the `.minecraft/config/autopalette/images/` directory.
2. **Open the GUI**: Sit on an ArtMap easel with a canvas, and press H to open the panel.
3. **Set Up**: Select your image, toggle dithering, set your drawing delay, and choose your color limit.
   ![GUI in game](https://cdn.modrinth.com/data/cached_images/76bbe40e593a4f935f502cc590b1f84610ba0a36_0.webp)
4. **Collect Dyes**: Click the Materials tab to check your shopping list, and gather the required items in your inventory.
   ![Material List GUI](https://cdn.modrinth.com/data/cached_images/4bc3295dfda3c289cf2de34a39b12573389b8630.png)
5. **Draw**: Click Start Drawing! The mod will automatically swap tools/dyes into your hotbar, turn your camera, and paint the canvas.

---

## Dependencies & Compatibility

* **Platform**: Fabric
* **Minecraft Version**: 1.21.8 (supports minor releases 1.21.9 through 1.21.11 out of the box).
* **Dependencies**: Fabric Loader and Fabric API only.
* **Server Compatibility**: Works on any server running the ArtMap or DFArt Bukkit/Spigot plugins.

---

## Developer Setup & Compilation

If you want to build AutoPalette from the source code yourself:

### 1. Clone the Repository
```bash
git clone [https://github.com/YOUR_GITHUB_USERNAME/autopalette.git](https://github.com/YOUR_GITHUB_USERNAME/autopalette.git)
cd autopalette

```

### 2. Build the .jar File

This project uses Gradle. Run the compiler command appropriate for your Operating System:

```bash
# On Windows:
gradlew build

# On Mac/Linux:
./gradlew build

```

Once completed, the compiled, production-ready mod will be located in the `build/libs/` directory.

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.

## Contributing & Support

Contributions, bug reports, and pull requests are welcome. If you encounter a bug or have a suggestion, please open an issue in the Issues tab above.

---

*Disclaimer: This is a client-side utility mod. Please check your server's rules regarding automation and automated gameplay elements before using this mod on public multiplayer networks.*

```


