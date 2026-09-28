package com.coworking.api.controller;

import com.coworking.api.domain.dto.SpaceRequest;
import com.coworking.api.domain.dto.SpaceResponse;
import com.coworking.api.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<SpaceResponse>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponse> getSpaceById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getSpaceById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpaceResponse> createSpace(@Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.createSpace(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpaceResponse> updateSpace(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.updateSpace(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }

}