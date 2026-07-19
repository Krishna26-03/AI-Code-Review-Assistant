import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listProjects } from "../services/projectService";
import Loader from "../components/Loader";

export default function DashboardPage() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    listProjects()
        .then(setProjects)
        .catch(() => setError("Could not load your projects."))
        .finally(() => setLoading(false));
  }, []);

  return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950">
        <div className="max-w-6xl mx-auto px-6 py-10">
          {/* Header Section */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-400 to-red-400 bg-clip-text text-transparent mb-2">
                Your Projects
              </h1>
              <p className="text-sm text-slate-400">
                Upload a Java file or project archive to get an AI-assisted review.
              </p>
            </div>
            <Link
                to="/upload"
                className="px-6 py-2.5 bg-gradient-to-r from-blue-600 to-red-600 hover:from-blue-700 hover:to-red-700 text-white rounded-lg text-sm font-medium transition self-start"
            >
              + New Review
            </Link>
          </div>

          {/* Loading State */}
          {loading && <Loader label="Loading your projects..." />}

          {/* Error State */}
          {error && (
              <div className="mb-6 p-4 bg-red-500/10 border border-red-500/30 rounded-lg">
                <p className="text-red-400 text-sm">{error}</p>
              </div>
          )}

          {/* Empty State */}
          {!loading && !error && projects.length === 0 && (
              <div className="backdrop-blur-xl bg-gradient-to-br from-slate-900/60 to-slate-800/60 border border-slate-700/50 rounded-2xl p-12 text-center">
                <div className="mb-4">
                  <svg className="w-16 h-16 mx-auto text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <p className="text-slate-300 font-medium mb-2">No projects yet</p>
                <p className="text-slate-400 text-sm mb-4">
                  Get started by uploading your first Java file or project archive.
                </p>
                <Link
                    to="/upload"
                    className="inline-block px-6 py-2.5 bg-gradient-to-r from-blue-600 to-red-600 hover:from-blue-700 hover:to-red-700 text-white rounded-lg text-sm font-medium transition"
                >
                  Upload First Project
                </Link>
              </div>
          )}

          {/* Projects Grid */}
          {!loading && !error && projects.length > 0 && (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {projects.map((p) => (
                    <Link
                        key={p.id}
                        to={p.latestReviewId ? `/reviews/${p.latestReviewId}` : `/projects/${p.id}`}
                        className="group relative backdrop-blur-xl bg-gradient-to-br from-slate-900/60 to-slate-800/60 border border-slate-700/50 hover:border-blue-500/50 rounded-2xl p-6 hover:shadow-2xl hover:shadow-blue-500/20 transition-all duration-300"
                    >
                      {/* Top gradient accent line */}
                      <div className="absolute top-0 left-0 right-0 h-0.5 bg-gradient-to-r from-blue-500 to-red-500 rounded-t-2xl opacity-0 group-hover:opacity-100 transition"></div>

                      {/* Content */}
                      <div className="flex items-start justify-between gap-3 mb-4">
                        <div className="flex-1 min-w-0">
                          <h3 className="font-semibold text-slate-100 group-hover:text-blue-300 transition truncate">
                            {p.projectName}
                          </h3>
                          <p className="text-xs text-slate-400 mt-1">
                            {p.uploadType.replace("_", " ")}
                          </p>
                        </div>

                        {/* Score Badge */}
                        {p.latestReviewScore != null ? (
                            <div className={`flex-shrink-0 px-3 py-1.5 rounded-lg text-sm font-bold ${
                                p.latestReviewScore >= 80
                                    ? "bg-green-500/20 text-green-400 border border-green-500/30"
                                    : p.latestReviewScore >= 50
                                        ? "bg-yellow-500/20 text-yellow-400 border border-yellow-500/30"
                                        : "bg-red-500/20 text-red-400 border border-red-500/30"
                            }`}>
                              {p.latestReviewScore.toFixed(0)}
                            </div>
                        ) : (
                            <div className="flex-shrink-0 px-3 py-1.5 rounded-lg text-xs font-medium text-red-300 bg-red-500/15 border border-red-500/40">
                              Error
                            </div>
                        )}
                      </div>

                      {/* Metadata */}
                      <div className="pt-4 border-t border-slate-700/50">
                        <p className="text-xs text-slate-400">
                          📅 {new Date(p.createdAt).toLocaleDateString()}
                        </p>
                        {p.latestReviewId && (
                            <p className="text-xs text-blue-400 mt-1">
                              ✓ Latest review available
                            </p>
                        )}
                      </div>
                    </Link>
                ))}
              </div>
          )}
        </div>
      </div>
  );
}