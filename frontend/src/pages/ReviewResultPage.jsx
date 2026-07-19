import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ScoreGauge from "../components/ScoreGauge";
import FindingCard from "../components/FindingCard";
import Loader from "../components/Loader";
import { getReview, downloadReport } from "../services/reviewService";

const SEVERITY_ORDER = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"];

export default function ReviewResultPage() {
  const { id } = useParams();
  const [review, setReview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    getReview(id)
        .then(setReview)
        .catch(() => setError("Could not load this review."))
        .finally(() => setLoading(false));
  }, [id]);

  async function handleDownload() {
    setDownloading(true);
    try {
      await downloadReport(id);
    } finally {
      setDownloading(false);
    }
  }

  if (loading) return <Loader label="Loading review..." />;
  if (error) return <p className="text-center text-red-600 mt-16">{error}</p>;
  if (!review) return null;

  const findings = filter === "ALL" ? review.findings : review.findings.filter((f) => f.severity === filter);
  const sortedFindings = [...findings].sort(
      (a, b) => SEVERITY_ORDER.indexOf(a.severity) - SEVERITY_ORDER.indexOf(b.severity)
  );

  const counts = SEVERITY_ORDER.reduce((acc, s) => {
    acc[s] = review.findings.filter((f) => f.severity === s).length;
    return acc;
  }, {});

  return (
      <div className="max-w-4xl mx-auto px-6 py-10">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl font-bold text-slate-800">{review.projectName}</h1>
            <p className="text-sm text-slate-400">
              Review #{review.id} • {new Date(review.createdAt).toLocaleString()} • {review.status}
            </p>
          </div>
          <button
              onClick={handleDownload}
              disabled={downloading}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-900 disabled:opacity-60 text-white rounded-md text-sm font-medium transition self-start"
          >
            {downloading ? "Preparing PDF..." : "Download PDF Report"}
          </button>
        </div>

        <div className="grid sm:grid-cols-[auto,1fr] gap-8 items-start bg-white border border-slate-200 rounded-xl p-6 mb-8">
          <ScoreGauge score={review.reviewScore || 0} />
          <div className="flex flex-col min-w-0">
            <h2 className="font-semibold text-slate-800 mb-2">Summary</h2>
            <div className="max-h-96 overflow-y-auto pr-2">
              <p className="text-sm text-slate-600 whitespace-pre-line">{review.summary}</p>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2 mb-4">
          <FilterChip label={`All (${review.findings.length})`} active={filter === "ALL"} onClick={() => setFilter("ALL")} />
          {SEVERITY_ORDER.map((s) => (
              <FilterChip key={s} label={`${s} (${counts[s]})`} active={filter === s} onClick={() => setFilter(s)} />
          ))}
        </div>

        <div className="space-y-3">
          {sortedFindings.length === 0 ? (
              <p className="text-sm text-slate-400 text-center py-8">No findings in this category.</p>
          ) : (
              sortedFindings.map((f) => <FindingCard key={f.id} finding={f} />)
          )}
        </div>
      </div>
  );
}

function FilterChip({ label, active, onClick }) {
  return (
      <button
          onClick={onClick}
          className={`px-3 py-1.5 rounded-full text-xs font-medium border transition ${
              active
                  ? "bg-brand-600 text-white border-brand-600"
                  : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"
          }`}
      >
        {label}
      </button>
  );
}