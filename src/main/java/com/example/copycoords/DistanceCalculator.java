package com.example.copycoords;

public class DistanceCalculator {
    
    public static class DistanceResult {
        public final double horizontalDistance;  // Distance on X-Z plane
        public final double verticalDistance;    // Difference in Y
        public final double totalDistance;       // 3D Euclidean distance
        public final double bearing;             // Angle in degrees (0-360), 0 = North, 90 = East, 180 = South, 270 = West
        public final String direction;           // Cardinal direction (N, NE, E, SE, S, SW, W, NW)
        public final int blocksTravelledHorizontal; // Blocks to travel horizontally (Manhattan)
        
        public DistanceResult(double horizontalDistance, double verticalDistance, double totalDistance, 
                            double bearing, String direction, int blocksTravelledHorizontal) {
            this.horizontalDistance = horizontalDistance;
            this.verticalDistance = verticalDistance;
            this.totalDistance = totalDistance;
            this.bearing = bearing;
            this.direction = direction;
            this.blocksTravelledHorizontal = blocksTravelledHorizontal;
        }
    }
    
    public static DistanceResult calculate(int x1, int y1, int z1, int x2, int y2, int z2) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double verticalDistance = dy;
        double totalDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);


        double bearing = calculateBearing(dx, dz);

        String direction = getCardinalDirection(bearing);

        int blocksTravelledHorizontal = (int) (Math.abs(dx) + Math.abs(dz));
        
        return new DistanceResult(horizontalDistance, verticalDistance, totalDistance, bearing, 
                                direction, blocksTravelledHorizontal);
    }
    
    private static double calculateBearing(double dx, double dz) {


        double angleRadians = Math.atan2(dx, -dz);  // -dz because north is -Z
        double angleDegrees = Math.toDegrees(angleRadians);

        if (angleDegrees < 0) {
            angleDegrees += 360;
        }
        
        return angleDegrees;
    }
    
    public static String getCardinalDirection(double bearing) {

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", 
                             "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

        double offset = bearing + 11.25;
        if (offset >= 360) {
            offset -= 360;
        }
        
        int index = (int) (offset / 22.5);
        return directions[index % 16];
    }
    
    public static String formatResult(DistanceResult result, boolean showAll) {
        String format;
        if (showAll) {
            format = String.format("Distance: %.1f blocks (horizontal), %.1f blocks vertically, %.1f blocks total | " +
                    "Direction: %s (%.1f°) | Blocks to travel: %d",
                    result.horizontalDistance,
                    result.verticalDistance,
                    result.totalDistance,
                    result.direction,
                    result.bearing,
                    result.blocksTravelledHorizontal);
        } else {
            format = String.format("Distance: %.1f blocks | Direction: %s (%.1f°)",
                    result.horizontalDistance,
                    result.direction,
                    result.bearing);
        }
        return format;
    }
    
    public static int[] adjustForDimensionScale(int x1, int z1, int x2, int z2, boolean sourceIsNether) {


        if (sourceIsNether) {
            return new int[]{x1 * 8, z1 * 8, x2, z2};
        } else {
            return new int[]{x1, z1, x2 / 8, z2 / 8};
        }
    }
}

