package com.example.ucorm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private String id;
    private String placeId;
    private String authorName;
    private String reviewText;
    private String status;
    private String standardReply;
    private String friendlyReply;
    private String recoveryReply;
    private String selectedReply;
}