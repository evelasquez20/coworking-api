package com.coworking.api.service.impl;

import com.coworking.api.domain.dto.SpaceRequest;
import com.coworking.api.domain.dto.SpaceResponse;
import com.coworking.api.domain.entity.Space;
import com.coworking.api.exception.BusinessException;
import com.coworking.api.exception.ErrorCode;
import com.coworking.api.repository.SpaceRepository;
import com.coworking.api.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;

    @Override
    @Transactional
    public SpaceResponse createSpace(SpaceRequest request) {
        log.info("Iniciando creación de nuevo espacio con nombre: '{}'", request.name());

        if (spaceRepository.existsByName(request.name())) {
            log.warn("Error al crear espacio: ya existe un espacio con el nombre '{}'", request.name());
            throw new BusinessException(ErrorCode.SPACE_NAME_DUPLICATED);
        }

        Space space = Space.builder()
                .name(request.name())
                .type(request.type())
                .capacity(request.capacity())
                .location(request.location())
                .hourlyRate(request.hourlyRate())
                .build();

        Space savedSpace = spaceRepository.save(space);
        log.info("Creación de espacio completada exitosamente con ID: {}", savedSpace.getId());
        return mapToResponse(savedSpace);
    }

    @Override
    @Transactional
    public SpaceResponse updateSpace(Long id, SpaceRequest request) {
        log.info("Iniciando actualización del espacio con ID: {}", id);

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Error al actualizar: No se encontró el espacio con ID: {}", id);
                    return new BusinessException(ErrorCode.SPACE_NOT_FOUND);
                });

        space.setName(request.name());
        space.setType(request.type());
        space.setCapacity(request.capacity());
        space.setLocation(request.location());
        space.setHourlyRate(request.hourlyRate());

        Space updatedSpace = spaceRepository.save(space);
        log.info("Actualización del espacio con ID: {} completada exitosamente", updatedSpace.getId());
        return mapToResponse(updatedSpace);
    }

    @Override
    @Transactional(readOnly = true)
    public SpaceResponse getSpaceById(Long id) {
        log.info("Iniciando búsqueda de espacio con ID: {}", id);

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Búsqueda fallida: No se encontró el espacio con ID: {}", id);
                    return new BusinessException(ErrorCode.SPACE_NOT_FOUND);
                });

        log.info("Búsqueda del espacio con ID: {} finalizada con éxito", id);
        return mapToResponse(space);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceResponse> getAllSpaces() {
        log.info("Iniciando consulta global de todos los espacios");

        List<SpaceResponse> spaces = spaceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Consulta de espacios finalizada con éxito. Se recuperaron {} espacios", spaces.size());
        return spaces;
    }

    @Override
    @Transactional
    public void deleteSpace(Long id) {
        log.info("Iniciando proceso de eliminación para el espacio con ID: {}", id);

        if (!spaceRepository.existsById(id)) {
            log.warn("Error al eliminar: No existe el espacio con ID: {}", id);
            throw new BusinessException(ErrorCode.SPACE_NOT_FOUND);
        }

        spaceRepository.deleteById(id);
        log.info("Eliminación del espacio con ID: {} completada exitosamente", id);
    }

    private SpaceResponse mapToResponse(Space space) {
        return new SpaceResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                space.getCapacity(),
                space.getLocation(),
                space.getHourlyRate()
        );
    }

}