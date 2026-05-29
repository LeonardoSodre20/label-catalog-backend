package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByCodeRef(String codeRef);

    boolean existsByCodeRef(String codeRef);
}
