# AutoPalette (클라이언트 사이드)

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · **한국어**

[![Modrinth](https://img.shields.io/modrinth/v/autopalette?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge](https://img.shields.io/curseforge/v/1577747?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![Minecraft](https://img.shields.io/modrinth/game-versions/autopalette?label=Minecraft)](https://modrinth.com/mod/autopalette/versions)
[![Downloads](https://img.shields.io/modrinth/dt/autopalette?label=Downloads&logo=modrinth)](https://modrinth.com/mod/autopalette)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1577747?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/artmap-autopalette)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/suoim)

Minecraft에서 화가에 몇 시간씩 앉아 염료로 픽셀 하나씩 클릭하며 커스텀 이미지를 그리는 데 지치셨나요?

AutoPalette는 Minecraft 1.21.x용 경량 클라이언트 사이드 Fabric 모드로, 서버 사이드 ArtMap(또는 DFArt) 캔버스에 커스텀 이미지를 자동으로 그립니다. 이미지를 선택하고, 자동 생성된 염료 쇼핑 목록을 확인하고, 앉아서 모드가 대신 그리게 하세요!

---

## 기능

* **직관적인 인게임 GUI**: H 키(사용자 지정 가능)로 인게임 대시보드를 엽니다. 로컬 이미지를瀏覽하고, 설정을 조절하고, 미리보기가 실시간으로 바뀌는 것을 확인하세요.
* **양자화 및 디더링**: 직접 최근색 매칭 또는 고품질 Floyd-Steinberg 디더링 중에서 선택해 지도 위 화질을 선명하게 만듭니다.
* **최대 팔레트 제한**: 이미지 팔레트를 가장 빈번한 Unlimited, 32, 16, 8색으로 제한합니다. GUI 미리보기가 선택을 반영해 즉시 다시 그려집니다.
* **인게임 쇼핑 목록**: Materials 탭이 필요한 염료, 석탄, 깃털 수를 정확히 계산합니다. 각 항목旁边에 실시간 16x16 Minecraft 아이템 아이콘도 표시합니다.
* **스마트 픽셀 건너뛰기**: 그리기 전에 캔버스 상태를 읽어 이미지와 일치하는 픽셀을 자동으로 건너뛰어, 염료와 그리기 시간을 크게 절약합니다.
* **석탄과 목탄 상호 호환**: 음영과 기본 검정 단계에서 석탄과 목탄을 지능적으로 구분 없이 사용해, 인벤토리에 있는 쪽을 씁니다.
* **픽셀 완벽 정렬**: 사전 직렬화 룩업(east.ser, west.ser 등)과 바닐라 MapColor 데이터베이스(mappings.json)로 캔버스 경계 정렬과 색 정확도를 100% 보장합니다.

---

## 사용법

1. **이미지 가져오기**: PNG 또는 JPG 파일을 `.minecraft/config/autopalette/images/` 디렉터리에 넣습니다.
2. **GUI 열기**: 캔버스가 있는 ArtMap 화가에 앉고 H 키로 패널을 엽니다.
3. **설정**: 이미지 선택, 디더링 토글, 그리기 지연 설정, 색상 제한 선택.
   ![인게임 GUI](https://cdn.modrinth.com/data/cached_images/76bbe40e593a4f935f502cc590b1f84610ba0a36_0.webp)
4. **염료 모으기**: Materials 탭에서 쇼핑 목록을 확인하고 필요한 아이템을 모읍니다.
   ![재료 목록 GUI](https://cdn.modrinth.com/data/cached_images/4bc3295dfda3c289cf2de34a39b12573389b8630.png)
5. **그리기**: Start Drawing 클릭! 모드가 도구/염료를 핫바로 자동 교체하고, 카메라를 돌려 캔버스를 그립니다.

---

## 의존성 및 호환성

* **플랫폼**: Fabric
* **Minecraft 버전**: 1.21.8 (1.21.9~1.21.11 마이너 릴리스 기본 지원)
* **의존성**: Fabric Loader와 Fabric API만
* **서버 호환**: ArtMap 또는 DFArt Bukkit/Spigot 플러그인이 있는 모든 서버에서 작동

---

## 개발자 설정 및 빌드

소스에서 AutoPalette를 직접 빌드하고 싶다면:

### 1. 저장소 클론

```bash
git clone https://github.com/aacanadaa/AutoPalette.git
cd AutoPalette
```

### 2. .jar 빌드

이 프로젝트는 Gradle을 사용합니다. OS에 맞는 명령을 실행하세요:

```bash
# Windows:
gradlew build

# Mac/Linux:
./gradlew build
```

완료되면 빌드된 프로덕션 모드는 `build/libs/`에 있습니다.

---

## 라이선스

이 프로젝트는 [Apache License 2.0](LICENSE)에 따라 라이선스됩니다.

## 기여 및 지원

기여, 버그 리포트, Pull Request를 환영합니다. 버그를 발견하거나 제안이 있으면 위 Issues 탭에서 issue를 열어 주세요.

---

*면책: 이것은 클라이언트 사이드 유틸리티 모드입니다. 공개 멀티플레이어 네트워크에서 사용하기 전에 서버의 자동화 규칙을 확인하세요.*
