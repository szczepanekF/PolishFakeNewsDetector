package com.pfnd.BusinessLogicService.model.messages;

import java.util.UUID;

public record FactCheckCommand(UUID id, String text, String email) {

}