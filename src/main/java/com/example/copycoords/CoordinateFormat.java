package com.example.copycoords;

public enum CoordinateFormat {
    SPACE_SEPARATED("space", "100 64 200"),
    BRACKET_COMMA("bracket", "[100, 64, 200]"),
    XYZ_LABEL("xyz", "X:100 Y:64 Z:200");

    private final String id;
    private final String example;

    CoordinateFormat(String id, String example) {
        this.id = id;
        this.example = example;
    }

    public String getId() {
        return id;
    }

    public String getExample() {
        return example;
    }

    public String format(int x, int y, int z) {
        return switch (this) {
            case SPACE_SEPARATED -> x + " " + y + " " + z;
            case BRACKET_COMMA -> "[" + x + ", " + y + ", " + z + "]";
            case XYZ_LABEL -> "X:" + x + " Y:" + y + " Z:" + z;
        };
    }

    public static CoordinateFormat fromId(String id) {
        for (CoordinateFormat format : values()) {
            if (format.id.equalsIgnoreCase(id)) {
                return format;
            }
        }
        return SPACE_SEPARATED; // Default fallback
    }
}

