package com.media.portal.developerportal.controllers;


import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.services.ApiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/apis")
public class ApiController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping
    public ResponseEntity<Long> createApi(@RequestBody Api api) {
        var savedApi = apiService.createApi(api);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApi.getId());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishApi(@PathVariable Long id) {
        var api = apiService.publishApi(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/deprecate")
    public ResponseEntity<Void> deprecateApi(@PathVariable Long id) {
        var api = apiService.deprecateApi(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<Page<Api>> listApis(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<Api> apis = apiService.listApis(status, pageable);
        return ResponseEntity.ok(apis);
    }

}
