# Changelog

All notable changes to CopyCoords will be documented in this file.

## [1.14.0] - 2026-04-06

### Added

- Support for Minecraft `26.1` and `26.1.1`.


## [1.13.0] - 2026-03-21

### Added

- New config option: `Show instant send key unbound hint`.
  - Controls whether the chat hint is shown when the Instant Chat Send keybind is unbound.
  - Default value is `true`.
- New `/copycoords` utility commands:
  - `/copycoords status` to display instant chat related settings and instant-send keybind state.
  - `/copycoords hintunbound <true|false>` and `/copycoords config showInstantChatSendUnboundHint <true|false>` to change the hint option in-game.

### Changed

- Removed Fabric API version constraints from manifest dependencies.
  - `Fabric API` is still required, but no minimum version is forced.
- Localized instant-chat config option labels/tooltips in Mod Menu via language keys.
- Added missing translation keys for the new instant-send hint option across all shipped locales.

## [1.12.5] - 2026-03-20

### Added

- `/msgcoords` name suggestion

### Fixed

- Fixed the mod failing to start on Java 17 for Minecraft versions below 1.20.5.

## [1.12.4] - 2026-03-15

### Fixed

- Fixed coordinates always showing `.0` decimal suffix when using the copy-with-dimension keybind, the copy-converted keybind, and history commands. Block-position coordinates (which are always integers) are now formatted without decimal places.

- Fixed `/msg` coordinate command ignoring the configured coordinate format, custom template, and dimension display settings.

## [1.12.3] - 2026-03-14

### Fixed 

- Fixed name-mapping mismatch at runtime. This caused the instant send to chat keybind to not work.

## [1.12.2] - 2026-03-14

### Fixed

- Fixed a bug where the instant send to chat keybind would not work.

## [1.12.1] - 2026-03-13

### Added

- Support for Minecraft `1.19` through `1.19.4`

### Removed

- Telemetry and all related config/UI code

## [1.12.0] - 2026-03-02

### Added

- New history commands:
  - `/coordshistory remove <index>` to remove a single history entry
  - `/coordshistory menu <index>` to open quick actions for a specific entry
- `coordshistory list` entries now include clickable action chips:
  - `[copy]`, `[insert]`, `[remove]`, and `[menu]`
- History quick menu now includes a `[clear_all]` action for one-click full history clearing

### Changed

- History entries now support click-to-insert behavior for chat input (`[insert]` and suggest-command action)
- Bookmark commands now support both roots:
  - `/coordsbookmark`
  - `/coordbookmark`

## [1.11.0] - 2026-02-26

### Added

- Bookmark import/export command using `.json` files in the game root directory

### Changed

- Bookmark command root is now `/coordsbookmark` with `/coordbookmark` kept as an alias
