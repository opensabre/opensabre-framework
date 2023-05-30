package io.github.opensabre.boot.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RestMappingInfo {
    @NonNull
    private String url;
    @NonNull
    private String method;
}
