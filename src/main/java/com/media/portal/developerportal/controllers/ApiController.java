package com.media.portal.developerportal.controllers;


import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.repositories.ApiRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/apis/v1")
public class ApiController {

    private static final Pattern API_NAME_PATTERN =
            Pattern.compile("^/[a-z0-9-]+/v[0-9]+$");

    private final ApiRepository apiRepository;
    public ApiController(ApiRepository apiRepository) {
        this.apiRepository = apiRepository;
    }

    @PostMapping
    public ResponseEntity<Api> createApi(@Valid @RequestBody Api api) {

        var name = api.getName();
        var basePath = api.getBasePath();
        if (name.length() < 3 || name.length() > 30) {
            throw new BadApiRequestException(String.format("API name should be between 3 and 30 characters, requested: %s", name.length()));
        }
        if (!API_NAME_PATTERN.matcher(basePath).matches()) {
            throw new BadApiRequestException(String.format("API name should name followed by version: %s, requested", name));
        }
        var savedApi = apiRepository.save(api);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApi);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Api> publishApi(@PathVariable Long id) {

        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.publish();
        apiRepository.save(api);
        return ResponseEntity.status(HttpStatus.OK).body(api);
    }

    @PostMapping("/{id}/deprecate")
    public ResponseEntity<Api> deprecateApi(@PathVariable Long id) {

        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.deprecate();
        apiRepository.save(api);
        return ResponseEntity.status(HttpStatus.OK).body(api);
    }

    @GetMapping
    public ResponseEntity<Page<Api>> listApis(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<Api> apis = apiRepository.findAll(status, pageable);
        return ResponseEntity.ok(apis);
    }

}

