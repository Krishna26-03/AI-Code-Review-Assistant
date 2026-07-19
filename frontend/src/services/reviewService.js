import api from "./api";

export async function runReview(projectId) {
  const { data } = await api.post(`/reviews/project/${projectId}`);
  return data;
}

export async function getReview(id) {
  const { data } = await api.get(`/reviews/${id}`);
  return data;
}

export async function listReviewsForProject(projectId) {
  const { data } = await api.get(`/reviews/project/${projectId}`);
  return data;
}

export async function downloadReport(reviewId) {
  const response = await api.get(`/reviews/${reviewId}/report`, {
    responseType: "blob",
  });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: "application/pdf" }));
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `review-${reviewId}-report.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
