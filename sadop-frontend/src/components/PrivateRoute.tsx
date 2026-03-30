// src/components/PrivateRoute.js
import { Navigate } from "react-router-dom";
import { isLoggedIn, getUserRole } from "../utils/auth";

const PrivateRoute = ({ children, allowedRoles }) => {
  const role = getUserRole();

  if (!isLoggedIn()) return <Navigate to="/login" />;

  if (allowedRoles.includes(role)) {
    return children;
  }

  return <Navigate to="/unauthorized" />;
};

export default PrivateRoute;
