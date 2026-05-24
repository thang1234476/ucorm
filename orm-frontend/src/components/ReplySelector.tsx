import { useState } from "react";
import type { Review } from "../types";

interface Props {
  review: Review;
  onApprove: (reviewId: string, selectedReply: string) => void;
  approving: boolean;
}

const OPTIONS = [
  {
    key: "standardReply" as keyof Review,
    label: "Standard",
    idle: "border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50",
    active: "border-slate-800 bg-slate-50 ring-1 ring-slate-800",
  },
  {
    key: "friendlyReply" as keyof Review,
    label: "Warm & Friendly",
    idle: "border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50",
    active: "border-slate-800 bg-slate-50 ring-1 ring-slate-800",
  },
  {
    key: "recoveryReply" as keyof Review,
    label: "Service Recovery",
    idle: "border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50",
    active: "border-slate-800 bg-slate-50 ring-1 ring-slate-800",
  },
];

export default function ReplySelector({ review, onApprove, approving }: Props) {
  const [selected, setSelected] = useState<string | null>(null);

  return (
    <div className="mt-5 space-y-3 border-t border-gray-100 pt-4">
      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
        Suggested Responses
      </p>

      <div className="grid gap-3">
        {OPTIONS.map((opt) => {
          const text = review[opt.key] as string | undefined;
          if (!text) return null;

          const isSelected = selected === text;

          return (
            <div
              key={opt.key}
              onClick={() => setSelected(text)}
              className={`p-4 rounded-lg border cursor-pointer transition-all duration-200 ${isSelected ? opt.active : opt.idle}`}
            >
              <div className="flex items-center justify-between mb-1.5">
                <p className={`text-xs font-bold uppercase tracking-wide ${isSelected ? 'text-slate-800' : 'text-gray-500'}`}>
                  {opt.label}
                </p>
                {/* Dấu tick SVG tinh tế khi được chọn */}
                {isSelected && (
                  <svg className="w-4 h-4 text-slate-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </div>
              <p className={`text-sm leading-relaxed ${isSelected ? 'text-slate-900 font-medium' : 'text-gray-700'}`}>
                {text}
              </p>
            </div>
          );
        })}
      </div>

      {selected && (
        <button
          onClick={() => onApprove(review.id, selected)}
          disabled={approving}
          className="w-full mt-4 py-2.5 bg-slate-800 text-white rounded-lg font-medium hover:bg-slate-900 disabled:opacity-70 transition-all text-sm shadow-sm flex items-center justify-center gap-2"
        >
          {approving ? (
            <>
              <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Approving...
            </>
          ) : (
            "Approve & Send"
          )}
        </button>
      )}
    </div>
  );
}