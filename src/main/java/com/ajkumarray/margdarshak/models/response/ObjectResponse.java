package com.ajkumarray.margdarshak.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectResponse extends ParentResponse {

    @JsonProperty("lc")
    private Object list;

}
