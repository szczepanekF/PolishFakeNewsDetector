package com.pfnd.BusinessLogicService.model.messages;

public record FactCheckCommand(int historyId, String text, int userId) {

}