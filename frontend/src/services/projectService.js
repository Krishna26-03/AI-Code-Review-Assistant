import api from "./api";

export async function uploadProject(file, projectName) {
  const formData = new FormData();
  formData.append("file", file);
  if (projectName) formData.append("projectName", projectName);

  const { data } = await api.post("/projects/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

export async function listProjects() {
  const { data } = await api.get("/projects");
  return data;
}

export async function getProject(id) {
  const { data } = await api.get(`/projects/${id}`);
  return data;
}
