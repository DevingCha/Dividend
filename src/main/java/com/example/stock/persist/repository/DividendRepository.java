package com.example.stock.persist.repository;

import com.example.stock.persist.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    List<DividendEntity> findAllByCompanyId(long companyId);
    boolean existsByCompanyIdAndDate(long companyId, LocalDateTime date);

    @Transactional
    void deleteAllByCompanyId(Long companyId);
}
