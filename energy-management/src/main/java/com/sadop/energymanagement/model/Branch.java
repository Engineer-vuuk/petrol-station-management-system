package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String branchName;

}
