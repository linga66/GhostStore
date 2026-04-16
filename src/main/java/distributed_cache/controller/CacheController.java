package distributed_cache.controller;

import distributed_cache.service.CacheService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {
    private final CacheService service;
    public CacheController(CacheService service) { this.service = service; }

    @PostMapping
    public void put(@RequestParam String key, @RequestParam String value, @RequestParam long ttl) {
        service.put(key, value, ttl);
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) { return service.get(key); }

    @GetMapping("/internal/{key}")
    public String getInternal(@PathVariable String key) { return service.getLocal(key); }
}