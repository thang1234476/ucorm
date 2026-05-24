import { useState } from "react";
import type { Review } from "../types";
import { generateAiReplies, approveReply } from "../services/api";
import ReplySelector from "./ReplySelector";

interface Props {
  review: Review;
  onUpdate: (updated: Review) => void;
}

export default function ReviewCard({ review, onUpdate }: Props) {
  const [generating, setGenerating] = useState(false);
  const [approving, setApproving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isResolved = review.status === "RESOLVED";
  const hasReplies = !!review.standardReply;

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    try {
      const updated = await generateAiReplies(review.id);
      onUpdate(updated);
    } catch {
      // Đổi câu báo lỗi chuyên nghiệp hơn, giấu việc dùng API
      setError("Unable to load response suggestions. Please try again.");
    } finally {
      setGenerating(false);
    }
  };

  const handleApprove = async (reviewId: string, selectedReply: string) => {
    setApproving(true);
    try {
      const updated = await approveReply(reviewId, selectedReply);
      onUpdate(updated);
    } catch {
      setError("Failed to approve reply. Please try again.");
    } finally {
      setApproving(false);
    }
  };

  return (
    <div className={`rounded-xl border p-5 shadow-sm transition-all duration-200 ${isResolved ? "border-green-200 bg-green-50/50" : "border-gray-200 bg-white hover:shadow-md"}`}>
      <div className="flex items-start justify-between gap-3 mb-4">
        <div className="flex items-center gap-3">
          {/* Avatar chuyển sang tone màu Slate trung tính, lịch sự */}
          <div className="w-10 h-10 rounded-full bg-slate-200 flex items-center justify-center text-slate-700 font-bold text-sm flex-shrink-0">
            {review.authorName.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="font-semibold text-gray-900 text-sm">{review.authorName}</p>
            <p className="text-xs text-gray-500 font-mono">ID: {review.placeId}</p>
          </div>
        </div>
        <span className={`px-2.5 py-1 rounded-md text-xs font-semibold flex-shrink-0 ${isResolved ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-600"}`}>
          {isResolved ? "Resolved" : "Needs Attention"}
        </span>
      </div>

      <div className="text-sm text-gray-800 bg-gray-50 rounded-lg p-4 border border-gray-100 mb-5 relative">
        {/* Thêm một chút CSS để câu review nhìn giống bong bóng chat của khách */}
        <div className="absolute top-0 left-4 -mt-2 w-4 h-4 bg-gray-50 border-t border-l border-gray-100 transform rotate-45"></div>
        {review.reviewText}
      </div>

      {error && (
        <p className="text-xs text-red-600 bg-red-50 rounded-md p-2.5 mb-4">
          {error}
        </p>
      )}

      {isResolved && review.selectedReply && (
        <div className="mt-2 p-3 bg-white rounded-lg border border-green-200 shadow-sm">
          <p className="text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wider">Sent Response</p>
          <p className="text-sm text-gray-800">{review.selectedReply}</p>
        </div>
      )}

      {!isResolved && !hasReplies && (
        <button
          onClick={handleGenerate}
          disabled={generating}
          // Nút bấm chuyển sang màu tối (slate-800) mang lại cảm giác công cụ quản trị vững chãi
          className="w-full py-2.5 bg-slate-800 text-white rounded-lg font-medium hover:bg-slate-900 disabled:opacity-70 transition-all text-sm shadow-sm flex items-center justify-center gap-2"
        >
          {generating ? (
            <>
              <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Analyzing context...
            </>
          ) : (
            // Bỏ icon robot, dùng icon tia chớp hoặc text thuần
            "Suggest Responses"
          )}
        </button>
      )}

      {!isResolved && hasReplies && (
        <ReplySelector review={review} onApprove={handleApprove} approving={approving} />
      )}
    </div>
  );
}