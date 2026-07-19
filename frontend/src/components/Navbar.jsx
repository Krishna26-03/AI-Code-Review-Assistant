import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
      <nav className="bg-gradient-to-r from-slate-50 to-slate-100 border-b-4 border-slate-400 shadow-2xl sticky top-0 z-10">
        <div className="max-w-full px-8 py-5 flex items-center justify-between gap-6">
          {/* LEFT SECTION - Logo & Name */}
          <Link to="/" className="flex items-center gap-3 flex-shrink-0 hover:opacity-85 transition group">
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-600 to-blue-800 text-white flex items-center justify-center text-base font-black shadow-2xl group-hover:shadow-blue-400/50 group-hover:scale-110 transition-all duration-300">
              AI
            </div>
            <div className="hidden sm:block">
              <div className="text-lg font-black text-slate-900 tracking-tight leading-tight">Code Review</div>
              <div className="text-xs font-bold text-blue-600 tracking-wide">ASSISTANT</div>
            </div>
          </Link>

          {/* CENTER SECTION - Animated decorative element */}
          {isAuthenticated && (
              <div className="flex-1 flex items-center justify-center gap-2 mx-4">
                <div className="h-1 flex-1 bg-gradient-to-r from-transparent via-blue-400 to-transparent rounded-full"></div>
                <div className="flex items-center gap-1.5 px-3 py-2 bg-white rounded-full shadow-md border-2 border-blue-200">
                  <div className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-pulse"></div>
                  <div className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" style={{ animationDelay: '0.2s' }}></div>
                  <div className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-pulse" style={{ animationDelay: '0.4s' }}></div>
                </div>
                <div className="h-1 flex-1 bg-gradient-to-r from-transparent via-blue-400 to-transparent rounded-full"></div>
              </div>
          )}

          {/* RIGHT SECTION - Navigation & User */}
          {isAuthenticated ? (
              <div className="flex items-center gap-5 flex-shrink-0">
                {/* Navigation Buttons */}
                <div className="flex items-center gap-3">
                  <Link
                      to="/dashboard"
                      className="px-5 py-2.5 rounded-xl text-slate-700 font-bold text-sm bg-white hover:bg-blue-50 active:bg-blue-100 hover:shadow-lg hover:scale-105 transition-all duration-200 border-2 border-slate-300 hover:border-blue-400"
                  >
                    Dashboard
                  </Link>
                  <Link
                      to="/upload"
                      className="px-5 py-2.5 rounded-xl text-white font-bold text-sm bg-gradient-to-br from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 active:scale-95 hover:shadow-2xl hover:scale-105 transition-all duration-200 border-2 border-blue-900 shadow-lg"
                  >
                    New Review
                  </Link>
                </div>

                {/* Dark Separator - Much darker now */}
                <div className="w-1.5 h-9 bg-gradient-to-b from-slate-600 via-slate-700 to-slate-600 rounded-full shadow-md"></div>

                {/* User Info */}
                <div className="flex items-center gap-4">
                  <div className="flex flex-col text-right">
                    <span className="text-xs font-bold text-slate-600 tracking-wide">LOGGED IN AS</span>
                    <span className="text-sm font-black text-slate-900">{user?.name}</span>
                  </div>
                  <button
                      onClick={handleLogout}
                      className="px-5 py-2.5 rounded-xl bg-gradient-to-br from-red-500 to-red-600 text-white hover:from-red-600 hover:to-red-700 font-bold text-sm transition-all duration-200 hover:shadow-2xl hover:scale-105 active:scale-95 border-2 border-red-800 shadow-lg"
                  >
                    Log out
                  </button>
                </div>
              </div>
          ) : (
              <div className="flex items-center gap-3 flex-shrink-0">
                <Link
                    to="/login"
                    className="px-5 py-2.5 rounded-xl text-slate-700 font-bold text-sm bg-white hover:bg-blue-50 transition-all duration-200 border-2 border-slate-300 hover:shadow-lg"
                >
                  Log in
                </Link>
                <Link
                    to="/register"
                    className="px-5 py-2.5 rounded-xl bg-gradient-to-br from-blue-600 to-blue-700 text-white hover:from-blue-700 hover:to-blue-800 font-bold text-sm hover:shadow-2xl transition-all duration-200 active:scale-95 border-2 border-blue-900 shadow-lg"
                >
                  Sign up
                </Link>
              </div>
          )}
        </div>

        {/* Animated top accent line */}
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-blue-600 via-blue-500 to-blue-600"></div>
      </nav>
  );
}