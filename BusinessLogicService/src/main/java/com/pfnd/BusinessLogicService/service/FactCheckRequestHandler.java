package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;

public interface FactCheckRequestHandler {
    void requestEvaluation(FactCheckCommand request);
}
