package com.pfnd.BusinessLogicService.model.messages;

import java.util.List;
import java.util.UUID;

public record ScrapedContent(UUID requestId, String email, String originalText, String language, List<String> sources) {
}
