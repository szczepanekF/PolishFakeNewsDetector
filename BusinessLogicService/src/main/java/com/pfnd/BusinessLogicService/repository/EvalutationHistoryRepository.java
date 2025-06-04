package com.pfnd.BusinessLogicService.repository;

import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalutationHistoryRepository extends JpaRepository<EvaluationHistoryRecord, Integer> {
    List<EvaluationHistoryRecord> findByUserId(int userId);
}
