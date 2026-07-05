package com.ajkumarray.margdarshak.models.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagedResponse<T> {

    @JsonProperty("content")
    @Schema(description = "content: list of records for the current page")
    private List<T> content;

    @JsonProperty("totalElements")
    @Schema(description = "totalElements: total number of records across all pages")
    private long totalElements;

    @JsonProperty("totalPages")
    @Schema(description = "totalPages: total number of pages")
    private int totalPages;

    @JsonProperty("page")
    @Schema(description = "page: current page number (0-indexed)")
    private int page;

    @JsonProperty("size")
    @Schema(description = "size: number of records per page")
    private int size;

}
