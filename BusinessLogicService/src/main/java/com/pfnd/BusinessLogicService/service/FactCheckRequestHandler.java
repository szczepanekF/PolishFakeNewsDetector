package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;

import java.util.Optional;

public interface FactCheckRequestHandler {
    void requestEvaluation(FactCheckCommand request);
    Optional<FactCheckResultDto> getInterimResult(long historyId);
}
