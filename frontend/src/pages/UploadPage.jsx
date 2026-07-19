import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import FileDropzone from "../components/FileDropzone";
import Loader from "../components/Loader";
import { uploadProject } from "../services/projectService";
import { runReview } from "../services/reviewService";

const STAGES = {
  IDLE: "idle",
  UPLOADING: "uploading",
  ANALYZING: "analyzing",
};

export default function UploadPage() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [projectName, setProjectName] = useState("");
  const [stage, setStage] = useState(STAGES.IDLE);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    if (!file) {
      setError("Please choose a .java file or .zip project first.");
      return;
    }
    setError("");

    try {
      setStage(STAGES.UPLOADING);
      const project = await uploadProject(file, projectName);

      setStage(STAGES.ANALYZING);
      const review = await runReview(project.id);

      navigate(`/reviews/${review.id}`);
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong during upload/review.");
      setStage(STAGES.IDLE);
    }
  }

  if (stage !== STAGES.IDLE) {
    return (
        <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950">
          <div className="max-w-2xl mx-auto px-6 py-20">
            <Loader
                label={
                  stage === STAGES.UPLOADING
                      ? "Uploading your project..."
                      : "Running Checkstyle, PMD, SpotBugs, and generating the AI summary — this can take a moment..."
                }
            />
          </div>
        </div>
    );
  }

  return (
      <div className="min-h-screen bg-gradient-to-b from-slate-900 via-slate-900 to-slate-950">
        <div className="max-w-2xl mx-auto px-6 py-10">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-400 to-red-400 bg-clip-text text-transparent mb-2">
              New Code Review
            </h1>
            <p className="text-sm text-slate-400">
              Upload a single <code className="bg-slate-800/50 px-2 py-1 rounded text-blue-300">.java</code> file or a <code className="bg-slate-800/50 px-2 py-1 rounded text-blue-300">.zip</code> of your project.
            </p>
          </div>

          {/* Error Alert */}
          {error && (
              <div className="mb-6 p-4 bg-red-500/10 border border-red-500/30 rounded-lg backdrop-blur-xl">
                <p className="text-red-400 text-sm flex items-start gap-3">
                  <span className="text-lg">⚠️</span>
                  <span>{error}</span>
                </p>
              </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Project Name Input */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Project Name (optional)
              </label>
              <input
                  type="text"
                  value={projectName}
                  onChange={(e) => setProjectName(e.target.value)}
                  placeholder="e.g. Inventory Service"
                  className="w-full rounded-lg border border-slate-700/50 bg-slate-800/30 px-4 py-3 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition backdrop-blur-xl"
              />
            </div>

            {/* File Dropzone */}
            <FileDropzone onFileSelected={setFile} selectedFile={file} />

            {/* Submit Button */}
            <button
                type="submit"
                className="w-full bg-gradient-to-r from-blue-600 to-red-600 hover:from-blue-700 hover:to-red-700 text-white font-medium py-3 rounded-lg transition duration-200 transform hover:scale-105 active:scale-95"
            >
              Upload &amp; Run Review
            </button>

            {/* Info Box */}
            <div className="backdrop-blur-xl bg-gradient-to-br from-slate-900/60 to-slate-800/60 border border-slate-700/50 rounded-lg p-4">
              <p className="text-xs text-slate-400">
                <span className="text-blue-400 font-medium">💡 Tip:</span> We'll analyze your code with Checkstyle, PMD, and SpotBugs, then generate an AI summary. The process usually takes 1-2 minutes.
              </p>
            </div>
          </form>
        </div>
      </div>
  );
}