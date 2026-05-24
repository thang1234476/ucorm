export interface Review {
  id: string;
  placeId: string;
  authorName: string;
  reviewText: string;
  status: "PENDING" | "RESOLVED";
  standardReply?: string;
  friendlyReply?: string;
  recoveryReply?: string;
  selectedReply?: string;
}
