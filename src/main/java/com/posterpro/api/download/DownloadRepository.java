package com.posterpro.api.download;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownloadRepository extends JpaRepository<Download, Long> {
    List<Download> findByUserIdOrderByCreatedAtDesc(Long userId);
}
