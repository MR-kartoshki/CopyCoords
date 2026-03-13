# Changelog

All notable changes to CopyCoords will be documented in this file.

## [1.12.1] - 2026-03-13

### Added

- Support for Minecraft `1.19` through `1.19.4`

### Removed

- Telemetry and all related config/UI code

### Changed

- Updated README for the expanded version matrix and current feature set

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
