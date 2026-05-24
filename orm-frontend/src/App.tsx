import { useState, useEffect } from "react";
import type { Review } from "./types";
import { fetchReviews, getReviews } from "./services/api";
import SearchBar from "./components/SearchBar";
import ReviewCard from "./components/ReviewCard";

export default function App() {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [fetching, setFetching] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);

  useEffect(() => {
    getReviews()
      .then(setReviews)
      .catch(() => { })
      .finally(() => setInitialLoading(false));
  }, []);

  const handleFetch = async (placeId: string) => {
    setFetching(true);
    setFetchError(null);
    try {
      const newReviews = await fetchReviews(placeId);
      setReviews((prev) => [...newReviews, ...prev]);
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

        {reviews.length > 0 && (
          <div className="grid grid-cols-3 gap-4">
            {[
              { label: "Total", value: reviews.length, color: "text-blue-600" },
              { label: "Pending", value: pending, color: "text-yellow-600" },
              { label: "Resolved", value: resolved, color: "text-green-600" },
            ].map((stat) => (
              <div key={stat.label} className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
                <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
                <p className="text-xs text-gray-500 mt-1">{stat.label}</p>
              </div>
            ))}
          </div>
        )}

        <section>
          <h2 className="text-base font-bold text-gray-900 mb-4">
            Reviews{" "}
            {reviews.length > 0 && (
              <span className="text-sm font-normal text-gray-400">({reviews.length} total)</span>
            )}
          </h2>

          {initialLoading && (
            <div className="text-center py-16 text-gray-400">
              <div className="text-4xl mb-3 animate-pulse">⏳</div>
              <p className="text-sm">Loading reviews from Firestore...</p>
            </div>
          )}

          {!initialLoading && reviews.length === 0 && (
            <div className="text-center py-16 bg-white rounded-2xl border-2 border-dashed border-gray-200">
              <div className="text-5xl mb-4">📭</div>
              <p className="text-gray-500 font-semibold">No reviews yet</p>
              <p className="text-gray-400 text-sm mt-1">Enter a Place ID above and click Fetch Reviews</p>
            </div>
          )}

          <div className="space-y-4">
            {reviews.map((review) => (
              <ReviewCard key={review.id} review={review} onUpdate={handleUpdate} />
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}
