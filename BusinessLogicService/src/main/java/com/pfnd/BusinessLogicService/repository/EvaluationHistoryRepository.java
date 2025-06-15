package com.pfnd.BusinessLogicService.repository;

import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationHistoryRepository extends JpaRepository<EvaluationHistoryRecord, Integer> {
    List<EvaluationHistoryRecord> findByUserIdOrderByCreatedAtDesc(int userId);
}
