// src/utils/auth.js

export const getUserRole = () => {
  return localStorage.getItem("role");
};

export const isLoggedIn = () => {
  return !!localStorage.getItem("token");
};

export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
};
