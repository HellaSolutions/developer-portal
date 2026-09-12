package com.media.portal.developerportal.services;

import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.ApiStatus;
import com.media.portal.developerportal.repositories.ApiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiService {

    private final ApiRepository apiRepository;

    public ApiService(ApiRepository apiRepository) {
        this.apiRepository = apiRepository;
    }

    public Api createApi(Api api) {
        return apiRepository.save(api);
    }

    @Transactional
    public void publishApi(Long id) {
        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.publish();
    }

    @Transactional
    public void deprecateApi(Long id) {
        var api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("API with id %s not found", id)));
        api.deprecate();
        apiRepository.save(api);
    }

    public Page<Api> listApis(String status, Pageable pageable) {
        if (status != null) {
            return apiRepository.findAllByStatus(ApiStatus.valueOf(status), pageable);
        }
        return apiRepository.findAll(pageable);
    }
}
