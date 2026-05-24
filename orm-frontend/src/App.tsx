import { useState } from "react";
import type { Review } from "./types";
import { fetchReviews } from "./services/api";
import SearchBar from "./components/SearchBar";
import ReviewCard from "./components/ReviewCard";

export default function App() {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [fetching, setFetching] = useState(false);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [currentPlaceId, setCurrentPlaceId] = useState<string | null>(null);

  const handleFetch = async (placeId: string) => {
    setFetching(true);
    setFetchError(null);
    try {
      // Mỗi lần fetch → replace toàn bộ list bằng reviews của placeId mới
      const newReviews = await fetchReviews(placeId);
      setReviews(newReviews);
      setCurrentPlaceId(placeId);
    } catch {
      setFetchError("Cannot connect to backend. Make sure Spring Boot is running on :8080");
    } finally {
      setFetching(false);
    }
  };

  const handleUpdate = (updated: Review) => {
    setReviews((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
  };

  const pending = reviews.filter((r) => r.status === "PENDING").length;
  const resolved = reviews.filter((r) => r.status === "RESOLVED").length;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">

      <main className="max-w-3xl mx-auto px-6 py-8 space-y-6">
        {/* Search Box */}
        <section className="bg-white rounded-2xl p-6 shadow-sm border border-gray-200">
          <h2 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">
            Fetch Reviews by Place ID
          </h2>
          <SearchBar onFetch={handleFetch} loading={fetching} />
          {fetchError && (
            <p className="text-xs text-red-500 bg-red-50 border border-red-200 rounded-lg p-2 mt-3">
              ⚠️ {fetchError}
            </p>
          )}
        </section>

        {/* Stats */}
        {reviews.length > 0 && (
          <div className="grid grid-cols-3 gap-4">
            {[
              { label: "Total", value: reviews.length, color: "text-blue-600" },
              { label: "Pending", value: pending, color: "text-yellow-600" },
              { label: "Resolved", value: resolved, color: "text-green-600" },
            ].map((stat) => (
              <div key={stat.label}
                className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
                <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
                <p className="text-xs text-gray-500 mt-1">{stat.label}</p>
              </div>
            ))}
          </div>
        )}

        {/* Reviews */}
        <section>
          <h2 className="text-base font-bold text-gray-900 mb-4">
            Reviews
            {currentPlaceId && (
              <span className="ml-2 text-xs font-normal text-gray-400 bg-gray-100 px-2 py-1 rounded-lg">
                Place: {currentPlaceId}
              </span>
            )}
          </h2>

          {/* Empty state — chưa fetch lần nào */}
          {!currentPlaceId && (
            <div className="text-center py-16 bg-white rounded-2xl border-2 border-dashed border-gray-200">
              <div className="text-5xl mb-4">🔍</div>
              <p className="text-gray-500 font-semibold">Enter a Place ID to get started</p>
              <p className="text-gray-400 text-sm mt-1">
                Reviews will appear here after you fetch
              </p>
            </div>
          )}

          {/* Đã fetch nhưng không có review */}
          {currentPlaceId && reviews.length === 0 && !fetching && (
            <div className="text-center py-16 bg-white rounded-2xl border-2 border-dashed border-gray-200">
              <div className="text-5xl mb-4">📭</div>
              <p className="text-gray-500 font-semibold">No reviews found</p>
              <p className="text-gray-400 text-sm mt-1">Try a different Place ID</p>
            </div>
          )}

          {/* Loading */}
          {fetching && (
            <div className="text-center py-16 text-gray-400">
              <div className="text-4xl mb-3 animate-pulse">⏳</div>
              <p className="text-sm">Fetching reviews...</p>
            </div>
          )}

          {/* Review Cards */}
          {!fetching && (
            <div className="space-y-4">
              {reviews.map((review) => (
                <ReviewCard key={review.id} review={review} onUpdate={handleUpdate} />
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}