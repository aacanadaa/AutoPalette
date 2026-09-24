# AutoPalette (Client-Side)

[English](README.md) · [中文](README.zh.md) · **Français** · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/autopalette?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge](https://img.shields.io/curseforge/v/1577747?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![Minecraft](https://img.shields.io/modrinth/game-versions/autopalette?label=Minecraft)](https://modrinth.com/mod/autopalette/versions)
[![Downloads](https://img.shields.io/modrinth/dt/autopalette?label=Downloads&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1577747?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/suoim)

Fatigué de rester des heures sur un chevalet, à cliquer pixel par pixel avec des teintures, juste pour peindre une image personnalisée dans Minecraft ?

AutoPalette est un mod Fabric client léger pour Minecraft 1.21.x qui automatise le dessin d'images personnalisées sur les chevalets ArtMap (ou DFArt) côté serveur. Sélectionnez votre image, consultez la liste de courses en teintures générée automatiquement, asseyez-vous, et laissez le mod peindre pour vous !

---

## Fonctionnalités

* **GUI en jeu intuitive** : Appuyez sur H (personnalisable) pour ouvrir un tableau de bord dans le jeu. Parcourez les images locales, réglez les options, et regardez l'aperçu changer en temps réel.
* **Quantisation et tramage** : Choisissez entre la correspondance de couleur la plus proche directe ou le tramage Floyd-Steinberg haute fidélité pour un rendu incroyablement net sur la carte.
* **Limiteur de palette max** : Limitez la palette de votre image aux couleurs les plus fréquentes : Illimité, 32, 16 ou 8. L'aperçu GUI se rééxécute instantanément.
* **Liste de courses en jeu** : L'onglet Matériaux calcule exactement combien de teintures, de charbon et de plumes il vous faut. Il affiche même les icônes d'objets Minecraft 16x16 en direct.
* **Saut de pixels intelligent** : Le mod lit l'état du canevas avant de dessiner et saute automatiquement les pixels déjà identiques à votre image, vous faisant économiser énormément de teintures et de temps.
* **Charbon et charbon de bois interchangeables** : Le mod traite intelligemment charbon et charbon de bois comme interchangeables pour l'ombrage et les étapes noires de base, utilisant ce qui est disponible dans votre inventaire.
* **Alignement pixel parfait** : Utilise des lookups pré-sérialisés (east.ser, west.ser, etc.) et une base de données MapColor vanilla (mappings.json) pour garantir un alignement de bord et une précision colorimétrique de 100 % sur le canevas.

---

## Comment l'utiliser

1. **Importer des images** : Placez vos fichiers PNG ou JPG dans le répertoire `.minecraft/config/autopalette/images/`.
2. **Ouvrir la GUI** : Asseyez-vous sur un chevalet ArtMap avec un canevas et appuyez sur H.
3. **Configurer** : Sélectionnez votre image, activez le tramage, définissez le délai de dessin et choisissez la limite de couleurs.
   ![GUI en jeu](https://cdn.modrinth.com/data/cached_images/76bbe40e593a4f935f502cc590b1f84610ba0a36_0.webp)
4. **Collecter les teintures** : Cliquez sur l'onglet Matériaux pour voir votre liste, et rassemblez les objets requis dans votre inventaire.
   ![GUI liste de matériaux](https://cdn.modrinth.com/data/cached_images/4bc3295dfda3c289cf2de34a39b12573389b8630.png)
5. **Dessiner** : Cliquez sur Démarrer le dessin ! Le mod échangera automatiquement outils/teintures vers votre hotbar, tournera votre caméra et peindra le canevas.

---

## Dépendances et compatibilité

* **Plateforme** : Fabric
* **Version Minecraft** : 1.21.8 (prend en charge les versions mineures 1.21.9 à 1.21.11 nativement).
* **Dépendances** : Fabric Loader et Fabric API uniquement.
* **Compatibilité serveur** : Fonctionne sur tout serveur avec les plugins ArtMap ou DFArt Bukkit/Spigot.

---

## Configuration développeur et compilation

Si vous voulez compiler AutoPalette depuis le code source :

### 1. Cloner le dépôt

```bash
git clone https://github.com/aacanadaa/AutoPalette.git
cd AutoPalette
```

### 2. Compiler le .jar

Ce projet utilise Gradle. Lancez la commande adaptée à votre système :

```bash
# Windows :
gradlew build

# Mac/Linux :
./gradlew build
```

Une fois terminé, le mod compilé se trouve dans le répertoire `build/libs/`.

---

## Licence

Ce projet est sous licence [Apache License 2.0](LICENSE).

## Contribuer et soutenir

Les contributions, rapports de bugs et pull requests sont bienvenus. Si vous rencontrez un bug ou avez une suggestion, ouvrez une issue dans l'onglet Issues ci-dessus.

---

*Avertissement : c'est un mod utilitaire côté client. Vérifiez les règles de votre serveur concernant l'automatisation avant de l'utiliser sur des réseaux multijoueurs publics.*
