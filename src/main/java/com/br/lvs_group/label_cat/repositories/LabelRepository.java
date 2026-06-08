package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByCodeRef(String codeRef);

    boolean existsByCodeRef(String codeRef);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Label l SET l.createdBy = null WHERE l.createdBy.id IN :ids")
    void nullifyCreatedBy(@Param("ids") List<Long> ids);
}
