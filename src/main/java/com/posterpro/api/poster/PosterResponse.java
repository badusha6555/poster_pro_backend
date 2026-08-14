package com.posterpro.api.poster;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PosterResponse {
    private String url;
    private String format;
    private LocalDateTime urlExpiresAt;
}
