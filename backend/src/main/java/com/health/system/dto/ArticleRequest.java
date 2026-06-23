package com.health.system.dto;

import lombok.Data;

@Data
public class ArticleRequest {
    private String title;
    private String category;
    private String summary;
    private String content;
    private String tags;
    private String coverImage;
    private Integer status;
}
