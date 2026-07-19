import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(name, email, password);
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950 flex flex-col items-center justify-center px-4">
        {/* Decorative Elements */}
        <div className="absolute top-0 left-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2"></div>
        <div className="absolute bottom-0 right-0 w-96 h-96 bg-red-500/10 rounded-full blur-3xl translate-x-1/2 translate-y-1/2"></div>

        {/* Register Card */}
        <div className="relative z-10 w-full max-w-md">
          <div className="backdrop-blur-xl bg-gradient-to-br from-slate-900/60 to-slate-800/60 border border-slate-700/50 rounded-2xl p-8 space-y-6">
            {/* Header */}
            <div className="text-center">
              <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-red-400 bg-clip-text text-transparent mb-2">
                Create your account
              </h1>
              <p className="text-sm text-slate-400">
                Start getting AI-powered reviews of your Java code.
              </p>
            </div>

            {/* Error Alert */}
            {error && (
                <div className="p-3.5 bg-red-500/10 border border-red-500/30 rounded-lg">
                  <p className="text-red-400 text-sm flex items-start gap-2">
                    <span className="text-lg">⚠️</span>
                    <span>{error}</span>
                  </p>
                </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Name Input */}
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Full Name
                </label>
                <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Jane Doe"
                    className="w-full rounded-lg border border-slate-700/50 bg-slate-800/30 px-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition backdrop-blur-xl"
                />
              </div>

              {/* Email Input */}
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Email
                </label>
                <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@company.com"
                    className="w-full rounded-lg border border-slate-700/50 bg-slate-800/30 px-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition backdrop-blur-xl"
                />
              </div>

              {/* Password Input */}
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Password
                </label>
                <input
                    type="password"
                    required
                    minLength={8}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="At least 8 characters"
                    className="w-full rounded-lg border border-slate-700/50 bg-slate-800/30 px-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition backdrop-blur-xl"
                />
                <p className="text-xs text-slate-400 mt-1">
                  Use at least 8 characters for security
                </p>
              </div>

              {/* Submit Button */}
              <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-gradient-to-r from-blue-600 to-red-600 hover:from-blue-700 hover:to-red-700 disabled:opacity-60 text-white font-medium py-2.5 rounded-lg transition duration-200 transform hover:scale-105 active:scale-95"
              >
                {loading ? "Creating account..." : "Sign up"}
              </button>
            </form>

            {/* Divider */}
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-700/50"></div>
              </div>
              <div className="relative flex justify-center text-xs">
                <span className="px-2 text-slate-500">or</span>
              </div>
            </div>

            {/* Login Link */}
            <p className="text-sm text-slate-400 text-center">
              Already have an account?{" "}
              <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium transition">
                Log in
              </Link>
            </p>
          </div>

          {/* Footer Info */}
          <div className="mt-6 text-center space-y-2">
            <p className="text-xs text-slate-500">
              🔒 Your data is secure and encrypted
            </p>
            <p className="text-xs text-slate-500">
              By signing up, you agree to our Terms of Service
            </p>
          </div>
        </div>
      </div>
  );
}