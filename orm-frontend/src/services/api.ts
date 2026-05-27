import axios from "axios";
import type { Review } from "../types";

const api = axios.create({
  baseURL: "https://ucorm-p4de.onrender.com",
});

export const fetchReviews = (placeId: string): Promise<Review[]> =>
  api.post("/api/reviews/fetch", { placeId }).then((r) => r.data);

export const getReviews = (): Promise<Review[]> =>
  api.get("/api/reviews").then((r) => r.data);

export const generateAiReplies = (id: string): Promise<Review> =>
  api.post(`/api/reviews/${id}/generate-ai`).then((r) => r.data);

export const approveReply = (id: string, selectedReply: string): Promise<Review> =>
  api.put(`/api/reviews/${id}/approve`, { selectedReply }).then((r) => r.data);
