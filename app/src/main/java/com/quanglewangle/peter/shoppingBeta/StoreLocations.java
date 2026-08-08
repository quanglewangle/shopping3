package com.quanglewangle.peter.shoppingBeta;

/**
 * Matches a GPS fix against a small fixed list of known shops, so purchase
 * history can be tagged with a store name without depending on a third-party
 * reverse-geocoding service.
 */
public class StoreLocations {

    private static class Store {
        final String name;
        final double lat;
        final double lon;
        final double radiusMeters;

        Store(String name, double lat, double lon, double radiusMeters) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.radiusMeters = radiusMeters;
        }
    }

    // Radii are wide (1.5km) to tolerate ACCESS_COARSE_LOCATION's blurring, which can put
    // a fix over a kilometre from the true position. Fine for telling these three apart
    // (they're 3km+ from each other) but wouldn't distinguish two shops in the same retail park.
    private static final Store[] STORES = {
            new Store("Home", 50.151427, -5.659950, 1500),
            new Store("Co-op, St Just", 50.124121, -5.679850, 1500),
            new Store("Sainsbury's, Penzance", 50.128435, -5.519067, 1500),
    };

    /** Returns the name of the closest known store within its radius, or null if none match. */
    public static String nearestStore(double lat, double lon) {
        Store best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Store s : STORES) {
            double d = distanceMeters(lat, lon, s.lat, s.lon);
            if (d <= s.radiusMeters && d < bestDistance) {
                best = s;
                bestDistance = d;
            }
        }
        return best != null ? best.name : null;
    }

    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadius = 6371000;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
