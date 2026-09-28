package com.coworking.api.repository;

import com.coworking.api.domain.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    boolean existsByName(String name);
    Optional<Space> findByName(String name);

}
