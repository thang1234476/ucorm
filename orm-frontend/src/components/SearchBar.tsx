import { useState } from "react";

interface Props {
  onFetch: (placeId: string) => void;
  loading: boolean;
}

export default function SearchBar({ onFetch, loading }: Props) {
  const [placeId, setPlaceId] = useState("");

  const handleClick = () => {
    if (placeId.trim()) onFetch(placeId.trim());
  };

  return (
    <div className="flex flex-col sm:flex-row gap-3">
      {/* Ô Input có chứa icon SVG bên trong */}
      <div className="relative flex-1">
        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
          <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <input
          type="text"
          value={placeId}
          onChange={(e) => setPlaceId(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleClick()}
          placeholder="Enter Google Place ID (e.g. ChIJN1...)"
          className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-gray-200 focus:outline-none focus:border-slate-500 focus:ring-1 focus:ring-slate-500 text-sm bg-white shadow-sm transition-all text-gray-800 placeholder-gray-400"
        />
      </div>

      {/* Nút Fetch màu Slate đồng bộ */}
      <button
        onClick={handleClick}
        disabled={loading || !placeId.trim()}
        className="px-6 py-2.5 bg-slate-800 text-white rounded-lg font-medium hover:bg-slate-900 disabled:opacity-70 disabled:cursor-not-allowed transition-all text-sm whitespace-nowrap shadow-sm flex items-center justify-center gap-2"
      >
        {loading ? (
          <>
            <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
            Fetching Data...
          </>
        ) : (
          "Load Reviews"
        )}
      </button>
    </div>
  );
}