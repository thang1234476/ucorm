package com.example.ucorm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.ucorm.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String COLLECTION = "reviews";

    @Value("${google.places.api.key}")
    private String placesApiKey;

    private final AiReplyService aiReplyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ 1. Fetch reviews thật từ Google Places API
    public List<Review> fetchAndSaveReviews(String placeId)
            throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        List<Review> reviews = new ArrayList<>();

        try {
            // Gọi Google Places API (New) - Place Details
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            // Dùng Places API (Legacy) - đơn giản hơn cho MVP
            String url = "https://maps.googleapis.com/maps/api/place/details/json"
                    + "?place_id=" + placeId
                    + "&fields=name,reviews,rating"
                    + "&reviews_sort=newest"
                    + "&key=" + placesApiKey
                    + "&language=en";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Places API status: " + response.statusCode());

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText();

            System.out.println("Places API status field: " + status);

            if ("OK".equals(status)) {
                JsonNode reviewsNode = root.path("result").path("reviews");

                if (reviewsNode.isArray() && reviewsNode.size() > 0) {
                    for (JsonNode r : reviewsNode) {
                        String reviewText = r.path("text").asText("").trim();
                        if (reviewText.isEmpty())
                            continue;

                        Review review = Review.builder()
                                .placeId(placeId)
                                .authorName(r.path("author_name").asText("Anonymous"))
                                .reviewText(reviewText)
                                .status("PENDING")
                                .build();

                        // Lưu vào Firestore
                        DocumentReference ref = db.collection(COLLECTION).document();
                        review.setId(ref.getId());
                        ref.set(toMap(review)).get();
                        reviews.add(review);
                    }
                    System.out.println("Fetched " + reviews.size() + " real reviews from Google Places");
                    return reviews;
                }
            }

            // Nếu Places API fail hoặc không có review → dùng mock
            System.out.println("Places API returned status: " + status + " → using mock data");

        } catch (Exception e) {
            System.err.println("Places API error: " + e.getMessage() + " → using mock data");
        }

        // Fallback: mock reviews
        return saveMockReviews(placeId, db);
    }

    // Mock reviews fallback
    private List<Review> saveMockReviews(String placeId, Firestore db)
            throws ExecutionException, InterruptedException {

        List<Review> mockReviews = Arrays.asList(
                Review.builder()
                        .placeId(placeId).authorName("John Doe")
                        .reviewText("Great hotel service, very clean and friendly staff!")
                        .status("PENDING").build(),
                Review.builder()
                        .placeId(placeId).authorName("Anna Smith")
                        .reviewText("Wifi was terrible. Could not work from the room at all.")
                        .status("PENDING").build(),
                Review.builder()
                        .placeId(placeId).authorName("Michael Lee")
                        .reviewText("Good location but breakfast could be improved.")
                        .status("PENDING").build());

        List<Review> saved = new ArrayList<>();
        for (Review review : mockReviews) {
            DocumentReference ref = db.collection(COLLECTION).document();
            review.setId(ref.getId());
            ref.set(toMap(review)).get();
            saved.add(review);
        }
        return saved;
    }

    // ✅ 2. Lấy tất cả reviews từ Firestore
    public List<Review> getAllReviews()
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION).get().get().getDocuments();

        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            reviews.add(fromDoc(doc));
        }
        return reviews;
    }

    // ✅ 3. Gọi Gemini generate replies → lưu Firestore
    public Review generateAiReplies(String reviewId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection(COLLECTION)
                .document(reviewId).get().get();

        if (!doc.exists())
            throw new RuntimeException("Review not found: " + reviewId);

        String reviewText = doc.getString("reviewText");
        Map<String, String> replies = aiReplyService.generateReplies(reviewText);

        Map<String, Object> updates = new HashMap<>();
        updates.put("standardReply", replies.get("standardReply"));
        updates.put("friendlyReply", replies.get("friendlyReply"));
        updates.put("recoveryReply", replies.get("recoveryReply"));

        db.collection(COLLECTION).document(reviewId).update(updates).get();

        return fromDoc(db.collection(COLLECTION).document(reviewId).get().get());
    }

    // ✅ 4. Approve reply → RESOLVED
    public Review approveReply(String reviewId, String selectedReply)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "RESOLVED");
        updates.put("selectedReply", selectedReply);

        db.collection(COLLECTION).document(reviewId).update(updates).get();

        return fromDoc(db.collection(COLLECTION).document(reviewId).get().get());
    }

    // ─── Helpers ─────────────────────────────────────────
    private Map<String, Object> toMap(Review r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("placeId", r.getPlaceId());
        m.put("authorName", r.getAuthorName());
        m.put("reviewText", r.getReviewText());
        m.put("status", r.getStatus());
        m.put("standardReply", r.getStandardReply());
        m.put("friendlyReply", r.getFriendlyReply());
        m.put("recoveryReply", r.getRecoveryReply());
        m.put("selectedReply", r.getSelectedReply());
        return m;
    }

    private Review fromDoc(DocumentSnapshot doc) {
        return Review.builder()
                .id(doc.getString("id"))
                .placeId(doc.getString("placeId"))
                .authorName(doc.getString("authorName"))
                .reviewText(doc.getString("reviewText"))
                .status(doc.getString("status"))
                .standardReply(doc.getString("standardReply"))
                .friendlyReply(doc.getString("friendlyReply"))
                .recoveryReply(doc.getString("recoveryReply"))
                .selectedReply(doc.getString("selectedReply"))
                .build();
    }
}