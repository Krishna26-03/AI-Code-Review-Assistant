import api from "./api";

export async function register(name, email, password) {
  const { data } = await api.post("/auth/register", { name, email, password });
  persistSession(data);
  return data;
}

export async function login(email, password) {
  const { data } = await api.post("/auth/login", { email, password });
  persistSession(data);
  return data;
}

export function logout() {
  localStorage.removeItem("acr_token");
  localStorage.removeItem("acr_user");
}

export function getCurrentUser() {
  const raw = localStorage.getItem("acr_user");
  return raw ? JSON.parse(raw) : null;
}

export function isAuthenticated() {
  return !!localStorage.getItem("acr_token");
}

function persistSession(authResponse) {
  localStorage.setItem("acr_token", authResponse.token);
  localStorage.setItem(
    "acr_user",
    JSON.stringify({
      userId: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
    })
  );
}
