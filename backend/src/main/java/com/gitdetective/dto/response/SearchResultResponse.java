package com.gitdetective.dto.response;

import java.util.List;

public record SearchResultResponse(
        String query,
        List<SearchHit> files,
        List<SearchHit> folders,
        List<SearchHit> classes,
        List<SearchHit> packages,
        List<SearchHit> commits,
        List<SearchHit> branches,
        List<SearchHit> tags) {

    public record SearchHit(String type, String id, String label, String secondary) {}
}
