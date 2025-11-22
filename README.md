# Fabric Tab Mod (server-side) — Minimal

## Requirements
- Java 17
- Gradle (wrapper included)
- Fabric Loader & Fabric API on the server (server installs in server/mods/)

## Build
Run in project root:

On Linux / macOS:
```
./gradlew build
```
On Windows:
```
gradlew.bat build
```

The built mod JAR will be in `build/libs/` named like `fabric-tab-mod-1.0.0.jar`.

## Install
1. Put the JAR into your server `mods/` folder.
2. Ensure Fabric Loader + Fabric API are installed on the server.
3. Start the server. The mod will read `whitelist.json` in the server root and create/update `tabmod_config.json` if not present.

## Notes
- This is a server-side-only implementation that uses the player list header/footer and periodic server ticks to display whitelist stats.
- Adding fully custom fake player entries visible in the vanilla TAB for offline players would require packets that emulate player entries (possible, but more invasive). If you want that, I can extend the mod to send player list addition/removal packets to create "fake players" (works with all vanilla clients).