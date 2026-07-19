import React from "react";

const STYLES = {
  CRITICAL: "bg-red-100 text-red-700 border-red-200",
  HIGH: "bg-orange-100 text-orange-700 border-orange-200",
  MEDIUM: "bg-amber-100 text-amber-700 border-amber-200",
  LOW: "bg-blue-100 text-blue-700 border-blue-200",
  INFO: "bg-slate-100 text-slate-600 border-slate-200",
};

export default function SeverityBadge({ severity }) {
  const styles = {
    CRITICAL: {
      bg: "bg-red-500/20",
      text: "text-red-400",
      border: "border-red-500/50",
      icon: "🔴",
    },
    HIGH: {
      bg: "bg-orange-500/20",
      text: "text-orange-400",
      border: "border-orange-500/50",
      icon: "🟠",
    },
    MEDIUM: {
      bg: "bg-yellow-500/20",
      text: "text-yellow-400",
      border: "border-yellow-500/50",
      icon: "🟡",
    },
    LOW: {
      bg: "bg-blue-500/20",
      text: "text-blue-400",
      border: "border-blue-500/50",
      icon: "🔵",
    },
    INFO: {
      bg: "bg-slate-500/20",
      text: "text-slate-400",
      border: "border-slate-500/50",
      icon: "⚪",
    },
  };

  const style = styles[severity] || styles.INFO;

  return (
      <span className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-lg font-semibold text-xs border ${style.bg} ${style.text} ${style.border} transition-all hover:scale-105`}>
      <span className="text-base">{style.icon}</span>
        {severity}
    </span>
  );
}
