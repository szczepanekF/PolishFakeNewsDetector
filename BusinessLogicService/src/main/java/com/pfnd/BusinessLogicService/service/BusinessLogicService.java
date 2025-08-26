package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.dto.*;

import java.util.List;

public interface BusinessLogicService {
    EvaluationHistoryDto initiateFactCheck(FactCheckRequestDto request, int userId);

    FactCheckProgressDto getEvaluationStatus(long id);

    // TODO change below method to return a list AnalyzeResult
    FactCheckProgressDto getEvaluationResult(int id);

    // TODO change below method to return a list of EvaluationHistoryDto
    List<FactCheckProgressDto> getUserHistory(int userId);
}
