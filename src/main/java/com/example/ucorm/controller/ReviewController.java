package com.example.ucorm.controller;

import com.example.ucorm.dto.ApproveRequest;
import com.example.ucorm.dto.FetchReviewRequest;
import com.example.ucorm.model.Review;
import com.example.ucorm.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/reviews/fetch
    @PostMapping("/fetch")
    public ResponseEntity<List<Review>> fetchReviews(
            @RequestBody FetchReviewRequest request) throws Exception {
        return ResponseEntity.ok(
                reviewService.fetchAndSaveReviews(request.getPlaceId()));
    }

    // GET /api/reviews
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // POST /api/reviews/{id}/generate-ai
    @PostMapping("/{id}/generate-ai")
    public ResponseEntity<Review> generateAi(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reviewService.generateAiReplies(id));
    }

    // PUT /api/reviews/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<Review> approve(
            @PathVariable String id,
            @RequestBody ApproveRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(
                reviewService.approveReply(id, request.getSelectedReply()));
    }
}