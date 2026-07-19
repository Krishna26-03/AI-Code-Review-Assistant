import React, { useCallback, useRef, useState } from "react";

export default function FileDropzone({ onFileSelected, selectedFile }) {
  const [dragActive, setDragActive] = useState(false);
  const inputRef = useRef(null);

  const handleDrag = useCallback((e, active) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(active);
  }, []);

  const handleDrop = useCallback(
      (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
          onFileSelected(e.dataTransfer.files[0]);
        }
      },
      [onFileSelected]
  );

  const handleChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      onFileSelected(e.target.files[0]);
    }
  };

  return (
      <div
          onDragEnter={(e) => handleDrag(e, true)}
          onDragOver={(e) => handleDrag(e, true)}
          onDragLeave={(e) => handleDrag(e, false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current.click()}
          className={`relative cursor-pointer backdrop-blur-xl rounded-2xl border-2 p-12 text-center transition-all duration-200 ${
              dragActive
                  ? "border-blue-500 bg-blue-500/10 shadow-2xl shadow-blue-500/30"
                  : "border-dashed border-slate-600 bg-slate-800/30 hover:border-blue-500/50 hover:bg-slate-800/50"
          }`}
      >
        <input
            ref={inputRef}
            type="file"
            accept=".java,.zip"
            onChange={handleChange}
            className="hidden"
        />

        {selectedFile ? (
            // Selected State
            <div className="space-y-3">
              <div className="text-4xl">✅</div>
              <p className="text-slate-100 font-semibold text-lg">{selectedFile.name}</p>
              <p className="text-xs text-slate-400">
                {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
              </p>
              <label className="inline-block mt-4 text-blue-400 hover:text-blue-300 cursor-pointer text-sm font-medium transition">
                Change file
                <input
                    type="file"
                    onChange={handleChange}
                    className="hidden"
                    accept=".java,.zip"
                />
              </label>
            </div>
        ) : (
            // Default State
            <div className="space-y-4">
              <div className="text-5xl">📁</div>
              <div>
                <p className="text-slate-100 font-semibold text-lg mb-1">
                  Drag and drop your file here
                </p>
                <p className="text-sm text-slate-400">or click to select</p>
              </div>
              <button
                  type="button"
                  onClick={() => inputRef.current.click()}
                  className="inline-block mt-4 px-6 py-2.5 bg-gradient-to-r from-blue-600 to-red-600 hover:from-blue-700 hover:to-red-700 text-white rounded-lg font-medium transition duration-200"
              >
                Browse Files
              </button>
              <p className="text-xs text-slate-500 mt-3">
                Supported: .java and .zip files (max 100MB)
              </p>
            </div>
        )}
      </div>
  );
}