package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.TypeOfLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeOfLabelRepository extends JpaRepository<TypeOfLabel, Long> {

    Optional<TypeOfLabel> findByName(String name);

    boolean existsByName(String name);
}
