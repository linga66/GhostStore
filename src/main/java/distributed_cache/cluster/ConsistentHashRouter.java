package distributed_cache.cluster;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashRouter {
    private final TreeMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodes = 100;

    public void init(List<String> nodes) {
        ring.clear();
        for (String node : nodes) {
            for (int i = 0; i < virtualNodes; i++) {
                // Using Native Java MD5 Hashing
                ring.put(hash(node + ":" + i), node);
            }
        }
    }

    public String route(String key) {
        if (ring.isEmpty()) return null;
        long hash = hash(key);
        SortedMap<Long, String> tailMap = ring.tailMap(hash);
        // If no tail, wrap around to the first node on the ring
        long nodeHash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        return ring.get(nodeHash);
    }

    // Native Java Hashing Implementation (MD5)
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            // Convert first 8 bytes of MD5 to a long value
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xFF);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }
}