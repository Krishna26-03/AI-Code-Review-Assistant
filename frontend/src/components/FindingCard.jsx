import React from "react";
import SeverityBadge from "./SeverityBadge";

export default function FindingCard({ finding }) {
    return (
        <div className="group relative backdrop-blur-xl bg-gradient-to-br from-slate-900/60 to-slate-800/60 border border-slate-700/50 hover:border-blue-500/50 rounded-2xl p-6 hover:shadow-2xl hover:shadow-blue-500/20 transition-all duration-300">
            {/* Top gradient accent line */}
            <div className="absolute top-0 left-0 right-0 h-0.5 bg-gradient-to-r from-blue-500 to-red-500 rounded-t-2xl opacity-0 group-hover:opacity-100 transition"></div>

            {/* Header */}
            <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-2 flex-wrap">
                    <SeverityBadge severity={finding.severity} />
                    {finding.sourceTool && (
                        <span className="text-xs uppercase tracking-wide text-slate-400 font-medium px-2 py-1 bg-slate-800/50 rounded">
              {finding.sourceTool}
            </span>
                    )}
                </div>
                {finding.fileName && (
                    <span className="text-xs text-slate-400 font-mono flex-shrink-0">
            {finding.fileName}
                        {finding.lineNumber ? `:${finding.lineNumber}` : ""}
          </span>
                )}
            </div>

            {/* Title */}
            <h4 className="font-semibold text-slate-100 text-sm mb-2 group-hover:text-blue-300 transition">
                {finding.issue}
            </h4>

            {/* Explanation */}
            {finding.explanation && (
                <p className="text-sm text-slate-300 mb-3 leading-relaxed">
                    {finding.explanation}
                </p>
            )}

            {/* Suggestion */}
            {finding.suggestion && (
                <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-3 mb-3">
                    <p className="text-xs text-green-400 font-medium mb-1">💡 Suggestion</p>
                    <p className="text-xs text-green-300">{finding.suggestion}</p>
                </div>
            )}

            {/* Code snippet if available */}
            {finding.code && (
                <div className="bg-slate-950/50 border border-slate-700/30 rounded-lg p-3 overflow-x-auto">
          <pre className="text-xs text-slate-300 font-mono whitespace-pre-wrap break-words">
            {finding.code}
          </pre>
                </div>
            )}
        </div>
    );
}