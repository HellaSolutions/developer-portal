package com.media.portal.developerportal.controllers;


import com.media.portal.developerportal.controllers.dto.CreateApiRequest;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.services.ApiService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Long> createApi(@Valid @RequestBody CreateApiRequest request) {
        var api = new Api();
        api.setName(request.name());
        api.setBasePath(request.basePath());
        api.setOwnerTeam(request.ownerTeam());
        api.setOpenApiSpec(request.openApiSpec());

        var savedApi = apiService.createApi(api);
        var id = savedApi.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", String.format("/v1/apis/%s", id)).body(id);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishApi(@PathVariable Long id) {
        apiService.publishApi(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/deprecate")
    public ResponseEntity<Void> deprecateApi(@PathVariable Long id) {
        apiService.deprecateApi(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<Page<Api>> listApis(
            @RequestParam(required = false) ApiStatus status,
            Pageable pageable) {
        Page<Api> apis = apiService.listApis(status, pageable);
        return ResponseEntity.ok(apis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Api> getApi(@PathVariable Long id) {
        Api api = apiService.getApi(id);
        return ResponseEntity.ok(api);
    }
}
