package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.messages.ScrapedContent;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactCheckRequestHandlerImpl implements FactCheckRequestHandler {
    @Override
    public void sendToScraper(FactCheckCommand command) {

    }

    @Override
    public void sendToEvaluator(ScrapedContent content) {

    }
}
