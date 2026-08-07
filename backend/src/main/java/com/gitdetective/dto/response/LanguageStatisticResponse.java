package com.gitdetective.dto.response;

import java.math.BigDecimal;

public record LanguageStatisticResponse(
        String language, int fileCount, long lineCount, long byteCount, BigDecimal percentage) {}
