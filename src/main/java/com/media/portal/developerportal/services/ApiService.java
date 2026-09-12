package com.media.portal.developerportal.services;

import com.media.portal.developerportal.controllers.BadApiRequestException;
import com.media.portal.developerportal.controllers.ResourceNotFoundException;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.repositories.ApiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional
public class ApiService {

    private static final Pattern API_BASEPATH_PATTERN =
            Pattern.compile("^/[a-z0-9-]+/v[0-9]+$");

    private final ApiRepository apiRepository;

    public ApiService(ApiRepository apiRepository) {
        this.apiRepository = apiRepository;
    }

    public Api createApi(Api api) {
        var name = api.getName();
        var basePath = api.getBasePath();
        if (name == null) {
            throw new BadApiRequestException("Missing API name");
        }
        if (name.length() < 3 || name.length() > 60) {
            throw new BadApiRequestException(String.format("API name should be between 3 and 30 characters, requested: %s", name.length()));
        }
        if (!API_BASEPATH_PATTERN.matcher(basePath).matches()) {
            throw new BadApiRequestException(String.format("API base path should be a name followed by version: %s, requested", basePath));
        }
        return apiRepository.save(api);
    }

    @Transactional
    public Api publishApi(Long id) {
        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.publish();
        return apiRepository.save(api);
    }

    @Transactional
    public Api deprecateApi(Long id) {
        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.deprecate();
        return apiRepository.save(api);
    }

    public Page<Api> listApis(String status, Pageable pageable) {
        return apiRepository.findAllByStatus(status, pageable);
    }
}
