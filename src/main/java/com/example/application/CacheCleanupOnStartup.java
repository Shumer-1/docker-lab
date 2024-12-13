package com.example.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CacheCleanupOnStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("Clearing all caches after startup...");
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });
    }
}
