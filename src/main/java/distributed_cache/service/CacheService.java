package distributed_cache.service;

import distributed_cache.cluster.ConsistentHashRouter;
import distributed_cache.core.LRUCache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

@Service
public class CacheService {
    @Value("${server.port}") private String port;
    @Value("${cluster.nodes}") private String nodes;
    private final LRUCache<String, String> cache = new LRUCache<>(3);
    private final ConsistentHashRouter router = new ConsistentHashRouter();
    private final RestTemplate rest = new RestTemplate();

    @PostConstruct
    public void setup() { router.init(Arrays.asList(nodes.split(","))); }

    public String get(String key) {
        String target = router.route(key);
        if (target.equals("localhost:" + port)) return cache.get(key);
        return rest.getForObject("http://" + target + "/api/cache/internal/" + key, String.class);
    }

    public void put(String key, String val, long ttl) {
        String target = router.route(key);
        if (target.equals("localhost:" + port)) cache.put(key, val, ttl);
        else rest.postForLocation("http://" + target + "/api/cache?key=" + key + "&value=" + val + "&ttl=" + ttl, null);
    }

    public String getLocal(String key) { return cache.get(key); }
}