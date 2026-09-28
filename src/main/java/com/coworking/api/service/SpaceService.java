package com.coworking.api.service;

import com.coworking.api.domain.dto.SpaceRequest;
import com.coworking.api.domain.dto.SpaceResponse;

import java.util.List;

public interface SpaceService {

    SpaceResponse createSpace(SpaceRequest request);
    SpaceResponse updateSpace(Long id, SpaceRequest request);
    SpaceResponse getSpaceById(Long id);
    List<SpaceResponse> getAllSpaces();
    void deleteSpace(Long id);

}
