import React from "react";
import { Navigate } from "react-router-dom";

const PrivateRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  console.log("🔐 PrivateRoute - token:", token);
  console.log("🔐 PrivateRoute - role:", role);
  console.log("✅ Allowed roles:", allowedRoles);

  if (!token || !role) {
    // Wait or redirect only if truly unauthenticated
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles.includes(role)) {
    return children;
  }

  return <Navigate to="/unauthorized" replace />;
};

export default PrivateRoute;
