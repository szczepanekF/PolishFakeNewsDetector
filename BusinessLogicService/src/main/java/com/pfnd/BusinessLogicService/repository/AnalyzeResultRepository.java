package com.pfnd.BusinessLogicService.repository;

import com.pfnd.BusinessLogicService.model.postgresql.AnalyzeResultRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalyzeResultRepository extends JpaRepository<AnalyzeResultRecord, Integer> {
    List<AnalyzeResultRecord> findByHistoryRecord_Id(int historyId);

}
