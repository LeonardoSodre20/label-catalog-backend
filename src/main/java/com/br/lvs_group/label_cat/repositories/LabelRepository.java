package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByCodeRef(String codeRef);

    boolean existsByCodeRef(String codeRef);

    @Query("""
            SELECT l FROM Label l
            WHERE (:search IS NULL
                   OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(l.codeRef) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Label> findAllWithSearch(@Param("search") String search, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Label l SET l.createdBy = null WHERE l.createdBy.id IN :ids")
    void nullifyCreatedBy(@Param("ids") List<Long> ids);
}
