import React from "react";

export default function ScoreGauge({ score = 0, size = 160 }) {
    const clamped = Math.max(0, Math.min(100, score));
    const radius = (size - 16) / 2;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (clamped / 100) * circumference;

    // Color based on score
    let color = "#ef4444"; // red for critical
    if (clamped >= 80) color = "#10b981"; // green for excellent
    else if (clamped >= 60) color = "#3b82f6"; // blue for good
    else if (clamped >= 40) color = "#f59e0b"; // amber for needs work

    const getQualityLabel = () => {
        if (clamped >= 80) return { emoji: "✅", text: "Excellent", color: "text-green-400" };
        if (clamped >= 60) return { emoji: "👍", text: "Good", color: "text-blue-400" };
        if (clamped >= 40) return { emoji: "⚠️", text: "Needs Work", color: "text-yellow-400" };
        return { emoji: "🔴", text: "Critical", color: "text-red-400" };
    };

    const quality = getQualityLabel();

    return (
        <div className="flex flex-col items-center gap-4">
            <div className="relative inline-flex items-center justify-center" style={{ width: size, height: size }}>
                <svg width={size} height={size} className="-rotate-90">
                    {/* Background circle - dark */}
                    <circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke="#334155"
                        strokeWidth="10"
                        fill="none"
                    />
                    {/* Progress circle */}
                    <circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke={color}
                        strokeWidth="10"
                        fill="none"
                        strokeDasharray={circumference}
                        strokeDashoffset={offset}
                        strokeLinecap="round"
                        style={{ transition: "stroke-dashoffset 0.6s ease" }}
                    />
                </svg>
                <div className="absolute flex flex-col items-center">
                    <span className="text-4xl font-bold text-slate-100">{clamped.toFixed(0)}</span>
                    <span className="text-xs text-slate-400 font-medium">/ 100</span>
                </div>
            </div>

            {/* Quality indicator text */}
            <div className="text-center">
                <p className={`text-sm font-semibold ${quality.color}`}>
                    {quality.emoji} {quality.text}
                </p>
            </div>
        </div>
    );
}