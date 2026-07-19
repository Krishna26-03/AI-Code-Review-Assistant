import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children }) {
  // Severity Color Map
  const severityColors = {
    CRITICAL: { bg: "red-500", text: "red-400", border: "red-500" },
    HIGH: { bg: "orange-500", text: "orange-400", border: "orange-500" },
    MEDIUM: { bg: "yellow-500", text: "yellow-400", border: "yellow-500" },
    LOW: { bg: "blue-500", text: "blue-400", border: "blue-500" },
    INFO: { bg: "slate-500", text: "slate-400", border: "slate-500" },
  };

// Score Color Map
  const scoreColors = {
    excellent: "green-400",   // 80+
    good: "blue-400",         // 60-79
    needsWork: "yellow-400",  // 40-59
    critical: "red-400",      // 0-39
  };

// Status Color Map
  const statusColors = {
    COMPLETED: "green-400",
    IN_PROGRESS: "yellow-400",
    FAILED: "red-400",
  };
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}
