package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.TypeOfLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TypeOfLabelRepository extends JpaRepository<TypeOfLabel, Long> {

    Optional<TypeOfLabel> findByName(String name);

    boolean existsByName(String name);

    @Query("""
            SELECT t FROM TypeOfLabel t
            WHERE (:search IS NULL
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<TypeOfLabel> findAllWithSearch(@Param("search") String search, Pageable pageable);
}
