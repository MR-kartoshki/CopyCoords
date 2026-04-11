package com.example.copycoords;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class XaeroWaypointExporter {
    private static final String[] DEFAULT_FILE_NAMES = {"waypoints.txt", "mw$default_1.txt"};
    private static final String HEADER = "#\n"
            + "#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination\n"
            + "#\n";

    private XaeroWaypointExporter() {
    }

    static XaeroExportResult exportToCurrentTarget(Minecraft client, CopyCoordsDataStore.BookmarkEntry entry) throws IOException {
        Path xaeroRoot = FabricLoader.getInstance().getGameDir().resolve("XaeroWaypoints");
        List<String> candidates = XaeroTargetContextCompat.getCurrentProfileCandidates(client);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Couldn't determine the current Xaero world/server folder. Use an explicit target path.");
        }

        Path profileDir = null;
        for (String candidate : candidates) {
            Path candidateDir = xaeroRoot.resolve(candidate);
            if (Files.isDirectory(candidateDir)) {
                profileDir = candidateDir;
                break;
            }
        }

        if (profileDir == null) {
            profileDir = xaeroRoot.resolve(candidates.get(0));
        }

        XaeroWaypoint waypoint = XaeroWaypoint.fromBookmark(entry);
        Path dimensionDir = profileDir.resolve(waypoint.dimensionFolder);
        List<Path> files = writeWaypointFiles(dimensionDir, waypoint, null);
        return new XaeroExportResult(files, profileDir);
    }

    static XaeroExportResult exportToTarget(CopyCoordsDataStore.BookmarkEntry entry, String target) throws IOException {
        String trimmed = target == null ? "" : target.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Target path cannot be empty.");
        }

        Path rawPath = Path.of(trimmed);
        Path resolved = rawPath.isAbsolute() ? rawPath : FabricLoader.getInstance().getGameDir().resolve(rawPath);
        XaeroWaypoint waypoint = XaeroWaypoint.fromBookmark(entry);

        List<Path> files;
        Path reportTarget;
        if (looksLikeWaypointFile(resolved)) {
            Path parent = resolved.toAbsolutePath().getParent();
            if (parent == null) {
                throw new IllegalArgumentException("Target waypoint file must have a parent directory.");
            }
            files = writeWaypointFiles(parent, waypoint, resolved.getFileName().toString());
            reportTarget = resolved;
        } else if (looksLikeDimensionDirectory(resolved)) {
            files = writeWaypointFiles(resolved, waypoint, null);
            reportTarget = resolved;
        } else {
            Path dimensionDir = resolved.resolve(waypoint.dimensionFolder);
            files = writeWaypointFiles(dimensionDir, waypoint, null);
            reportTarget = resolved;
        }

        return new XaeroExportResult(files, reportTarget);
    }

    private static List<Path> writeWaypointFiles(Path dimensionDir, XaeroWaypoint waypoint, String explicitFileName)
            throws IOException {
        Files.createDirectories(dimensionDir);

        List<Path> writtenFiles = new ArrayList<>();
        if (explicitFileName != null) {
            Path file = dimensionDir.resolve(explicitFileName);
            writeWaypointFile(file, waypoint);
            writtenFiles.add(file);
            return writtenFiles;
        }

        for (String fileName : DEFAULT_FILE_NAMES) {
            Path file = dimensionDir.resolve(fileName);
            writeWaypointFile(file, waypoint);
            writtenFiles.add(file);
        }
        return writtenFiles;
    }

    private static void writeWaypointFile(Path file, XaeroWaypoint waypoint) throws IOException {
        List<String> existing = Files.exists(file) ? Files.readAllLines(file, StandardCharsets.UTF_8) : List.of();
        List<String> output = new ArrayList<>();
        boolean headerPresent = false;

        for (String line : existing) {
            if (line.startsWith("#")) {
                headerPresent = true;
                output.add(line);
                continue;
            }

            if (line.startsWith("waypoint:") && hasWaypointName(line, waypoint.name)) {
                continue;
            }

            if (!line.isBlank()) {
                output.add(line);
            }
        }

        if (!headerPresent) {
            output.clear();
            for (String headerLine : HEADER.split("\n")) {
                output.add(headerLine);
            }
        }

        output.add(waypoint.serialize());
        Files.writeString(file, String.join("\n", output) + "\n", StandardCharsets.UTF_8);
    }

    private static boolean hasWaypointName(String line, String expectedName) {
        int start = "waypoint:".length();
        int end = line.indexOf(':', start);
        return end > start && line.substring(start, end).equals(expectedName);
    }

    private static boolean looksLikeWaypointFile(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        return fileName.toString().toLowerCase(Locale.ROOT).endsWith(".txt");
    }

    private static boolean looksLikeDimensionDirectory(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }
        return fileName.toString().startsWith("dim%");
    }

    static final class XaeroExportResult {
        final List<Path> writtenFiles;
        final Path targetPath;

        XaeroExportResult(List<Path> writtenFiles, Path targetPath) {
            this.writtenFiles = writtenFiles;
            this.targetPath = targetPath;
        }
    }

    private static final class XaeroWaypoint {
        final String name;
        final String initials;
        final int x;
        final int y;
        final int z;
        final int color;
        final boolean disabled;
        final int type;
        final String set;
        final boolean rotateOnTp;
        final int tpYaw;
        final int visibilityType;
        final boolean destination;
        final String dimensionFolder;

        private XaeroWaypoint(String name, String initials, int x, int y, int z, String dimensionFolder) {
            this.name = name;
            this.initials = initials;
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = 11;
            this.disabled = false;
            this.type = 0;
            this.set = "gui.xaero_default";
            this.rotateOnTp = false;
            this.tpYaw = 0;
            this.visibilityType = 0;
            this.destination = false;
            this.dimensionFolder = dimensionFolder;
        }

        static XaeroWaypoint fromBookmark(CopyCoordsDataStore.BookmarkEntry entry) {
            if (entry == null) {
                throw new IllegalArgumentException("Bookmark entry is null.");
            }

            String dimensionId = entry.dimensionId;
            String dimensionFolder;
            if (dimensionId == null || dimensionId.equals("minecraft:overworld")) {
                dimensionFolder = "dim%0";
            } else if (dimensionId.equals("minecraft:the_nether")) {
                dimensionFolder = "dim%-1";
            } else if (dimensionId.equals("minecraft:the_end")) {
                dimensionFolder = "dim%1";
            } else {
                throw new IllegalArgumentException(
                        "Xaero export currently supports Overworld, Nether, and End bookmarks only.");
            }

            String sanitizedName = sanitizeName(entry.name);
            return new XaeroWaypoint(
                    sanitizedName,
                    buildInitials(sanitizedName),
                    (int) Math.floor(entry.x),
                    (int) Math.floor(entry.y),
                    (int) Math.floor(entry.z),
                    dimensionFolder
            );
        }

        String serialize() {
            return "waypoint:" + name
                    + ":" + initials
                    + ":" + x
                    + ":" + y
                    + ":" + z
                    + ":" + color
                    + ":" + disabled
                    + ":" + type
                    + ":" + set
                    + ":" + rotateOnTp
                    + ":" + tpYaw
                    + ":" + visibilityType
                    + ":" + destination;
        }

        private static String sanitizeName(String name) {
            String value = name == null ? "" : name.trim();
            if (value.isEmpty()) {
                value = "Waypoint";
            }
            return value.replace(':', '-')
                    .replace('\r', ' ')
                    .replace('\n', ' ')
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        private static String buildInitials(String name) {
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < name.length() && initials.length() < 2; i++) {
                char current = name.charAt(i);
                if (Character.isLetterOrDigit(current)) {
                    initials.append(Character.toUpperCase(current));
                }
            }
            if (initials.length() == 0) {
                initials.append('X');
            }
            return initials.toString();
        }
    }
}
