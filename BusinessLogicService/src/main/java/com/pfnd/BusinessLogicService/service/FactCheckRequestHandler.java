package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.messages.ScrapedContent;

public interface FactCheckRequestHandler {
    void sendToScraper(FactCheckCommand command);
    void sendToEvaluator(ScrapedContent content);
}
