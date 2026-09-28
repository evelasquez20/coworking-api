package com.coworking.api.domain.entity;

import com.coworking.api.domain.enums.SpaceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "spaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String location;

    @Column(name = "hourly_rate", nullable = false)
    private BigDecimal hourlyRate;
    
}
