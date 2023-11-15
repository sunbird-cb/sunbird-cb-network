package org.sunbird.cb.hubservices.cache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.cb.hubservices.cache.service.RedisCacheService;
import org.sunbird.cb.hubservices.model.SBApiResponse;

@RestController
public class RedisCacheController {

    @Autowired
    RedisCacheService redisCacheService;


    @DeleteMapping("/redis")
    public ResponseEntity<?> deleteCache() throws Exception {
        SBApiResponse response = redisCacheService.deleteCache();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis")
    public ResponseEntity<?> getKeys() throws Exception {
        SBApiResponse response = redisCacheService.getKeys();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/redis/values")
    public ResponseEntity<?> getKeysAndValues() throws Exception {
        SBApiResponse response = redisCacheService.getKeysAndValues();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

}