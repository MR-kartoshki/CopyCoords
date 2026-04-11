# CopyCoords Copilot Instructions

## Build, test, and validation commands

Use the Gradle wrapper from the repository root. On Windows use `.\gradlew.bat ...`; on Unix-like systems use `./gradlew ...`.

- Full build: `.\gradlew.bat build`
- Full version-matrix build: `.\gradlew.bat buildAllVersions`
- Build one Minecraft target: `.\gradlew.bat :<mc-version>:build`
- Run one Minecraft target in dev: `.\gradlew.bat :<mc-version>:runClient`
- Test task: `.\gradlew.bat test`
- Write the current changelog section used for releases: `.\gradlew.bat writeCurrentChangelog`

There is currently no `src/test` tree, so there is no meaningful single-test command to run. For targeted validation, use a single-version Gradle task such as `:<mc-version>:build` or `:<mc-version>:runClient`.

No dedicated lint task is defined in this repository.

## High-level architecture

This is a Stonecutter multi-version Fabric mod. Shared implementation lives under `src/main/java` and `src/main/resources`, while API-specific compatibility overrides live under `versions/<mc-version>/src/main/...`. Stonecutter creates one subproject per Minecraft version directory, and the shared build logic in `build.gradle` selects dependency versions, Java toolchain level, artifact naming, and publishing metadata from the active Minecraft version.

The main entrypoint is `CopyCoords`, which initializes config and persisted data, registers keybinds, and registers the client-side Brigadier commands. Core user-facing behavior is split across:

- `CopyCoords`: command registration, coordinate formatting, chat output, map-link generation, and history/bookmark command flows
- `CopyCoordsBind`: keybind registration and key-triggered command equivalents
- `CopyCoordsConfig`: persisted user settings in `config/copycoords/copycoords.json`
- `CopyCoordsDataStore`: persisted history and bookmarks in `config/copycoords/copycoords-data.json`
- `CopyCoordsModMenuIntegration`: Cloth Config / Mod Menu screen wiring
- `ChatCoordinateParser`: shared regex-based detection of coordinate triples in incoming chat text
- `ChatReceiveCompat`: compat layer for client-side incoming chat/game message hooks that feeds shared parsing logic
- `XaeroWaypointExporter`: shared file-based export of CopyCoords bookmarks into Xaero waypoint files
- `XaeroTargetContextCompat`: best-effort current world/server target resolution for Xaero auto-export

Compatibility shims such as `ChatSendCompat`, `PlayerLevelCompat`, `ChatScreenOpener`, `ChatEventFactory`, and some `CopyCoordsBind` variants are intentionally version-specific. Shared logic should stay in root sources unless a Minecraft/Fabric API change forces an override.

Release automation assumes one built jar per Minecraft version under `versions/<mc-version>/build/libs`. `buildAllVersions` and the GitHub release workflow iterate the Stonecutter target list and publish each version separately.

## Key conventions

- Prefer explicit per-version override classes over reflection or large runtime version branches. When a class needs an override, keep the same package and relative path as the shared root class.
- The `versions/` directories are the source of truth for supported Minecraft targets. Stonecutter settings and aggregate tasks should follow that directory set rather than a separate hardcoded version list.
- Root and version-specific source sets are merged with version sources first; root Java files are excluded at compile time when a version-specific file with the same relative path exists.
- `ext.gameVersions` in `build.gradle` is the dependency matrix. Adding or changing a Minecraft target usually means updating that map, version-specific source/resource overrides, and any release/build task logic that depends on the supported version set.
- Use official Mojang mappings only. For obfuscated targets through `1.21.11`, keep `net.fabricmc.fabric-loom-remap` with `loom.officialMojangMappings()`; `26.x` stays on `net.fabricmc.fabric-loom` without a `mappings` dependency because those targets are already unobfuscated. Never introduce Yarn mappings or mapping-verification helper tasks.
- Java toolchain selection is version-driven: 1.19 through 1.20.4 use Java 17, most later versions use Java 21, and `26.x` targets use Java 25.
- The build forces the exact Fabric API version from the per-version matrix via `resolutionStrategy.force(...)`. Keep dependency changes aligned with the selected Minecraft version instead of letting transitive dependencies float.
- When Fabric or Minecraft API names drift across versions, prefer small version-specific compat classes such as `ClientCommandCompat` and `PlayerMessageCompat` instead of forking large shared classes like `CopyCoords`.
- Incoming chat coordinate detection should stay split between shared parsing/presentation code and a narrow receive-hook compat class; if a target family breaks the receive callback API, add a `versions/<mc-version>/.../ChatReceiveCompat.java` override rather than branching detection logic inside `CopyCoords`.
- Xaero integration should stay file-based (`XaeroWaypoints/.../*.txt`) rather than depending on Xaero runtime classes; if automatic current world/server target detection breaks on a version family, isolate that in `XaeroTargetContextCompat` overrides instead of changing the exporter format logic.
- Config and data loaders migrate from legacy flat files (`config/copycoords.json`, `config/copycoords-data.json`) into the scoped `config/copycoords/` directory. Preserve that migration path when changing persistence.
- Bookmark lookup is case-insensitive and normalized with trimmed lowercase keys, while the original bookmark display name is preserved in stored entries.
- Artifact names include the Minecraft version suffix (`copycoords+<mc-version>`), and release publishing expects that per-version packaging shape.
- Root aggregate and release-helper tasks such as `buildAllVersions` and `writeCurrentChangelog` belong in `stonecutter.gradle`; putting them in the shared `build.gradle` makes them fan out per Stonecutter subproject.
- When Copilot makes code changes in this repository, update `.github/copilot-instructions.md` if the changes affect build/test commands, architecture, workflow, or conventions that future sessions should know.
