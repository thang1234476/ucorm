package com.example.ucorm.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.ucorm.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String COLLECTION = "reviews";
    private final AiReplyService aiReplyService;

    // ✅ 1. Fetch mock reviews và lưu Firestore
    public List<Review> fetchAndSaveReviews(String placeId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

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
                        .status("PENDING").build(),
                Review.builder()
                        .placeId(placeId).authorName("Sarah Johnson")
                        .reviewText("Amazing breakfast and stunning views from the room!")
                        .status("PENDING").build(),
                Review.builder()
                        .placeId(placeId).authorName("David Chen")
                        .reviewText("Room was not cleaned properly. Very disappointed.")
                        .status("PENDING").build());

        List<Review> saved = new ArrayList<>();
        for (Review review : mockReviews) {
            DocumentReference ref = db.collection(COLLECTION).document();
            review.setId(ref.getId());
            ref.set(toMap(review)).get();
            saved.add(review);
        }
        System.out.println("✅ Saved " + saved.size() + " reviews for placeId: " + placeId);
        return saved;
    }

    // ✅ 2. Lấy reviews theo placeId
    public List<Review> getReviewsByPlaceId(String placeId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION)
                .whereEqualTo("placeId", placeId)
                .get().get().getDocuments();

        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            reviews.add(fromDoc(doc));
        }
        return reviews;
    }

    // ✅ 3. Lấy tất cả reviews
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

    // ✅ 4. Generate AI replies → lưu Firestore
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

        return fromDoc(db.collection(COLLECTION)
                .document(reviewId).get().get());
    }

    // ✅ 5. Approve reply → RESOLVED
    public Review approveReply(String reviewId, String selectedReply)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "RESOLVED");
        updates.put("selectedReply", selectedReply);

        db.collection(COLLECTION).document(reviewId).update(updates).get();

        return fromDoc(db.collection(COLLECTION)
                .document(reviewId).get().get());
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