import React from "react";

export default function Loader({ label }) {
    return (
        <div className="flex flex-col items-center justify-center min-h-96 gap-6">
            {/* Animated Loading Spinner */}
            <div className="relative w-20 h-20">
                <div className="absolute inset-0 rounded-full border-4 border-slate-700"></div>
                <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-blue-400 border-r-red-400 animate-spin"></div>
            </div>

            {/* Floating orbs */}
            <div className="flex gap-4">
                <div className="w-3 h-3 rounded-full bg-blue-400 animate-pulse"></div>
                <div className="w-3 h-3 rounded-full bg-red-400 animate-pulse" style={{ animationDelay: "0.2s" }}></div>
                <div className="w-3 h-3 rounded-full bg-purple-400 animate-pulse" style={{ animationDelay: "0.4s" }}></div>
            </div>

            {/* Loading label */}
            {label && (
                <div className="text-center">
                    <p className="text-slate-300 font-medium mb-2">{label}</p>
                    <p className="text-xs text-slate-500">This may take a few moments...</p>
                </div>
            )}
        </div>
    );
}
