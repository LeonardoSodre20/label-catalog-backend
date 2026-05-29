package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<Upload, Long> {
}
