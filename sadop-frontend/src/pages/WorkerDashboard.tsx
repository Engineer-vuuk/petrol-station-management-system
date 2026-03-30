import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import UserHeader from "../components/UserHeader"; // Adjust the path if necessary

const WorkerDashboard = () => {
  const [fullName, setFullName] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const name = localStorage.getItem("fullName");
    if (name) {
      setFullName(name);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("fullName");
    navigate("/login");
  };

  return (
    <div style={containerStyle}>
      <div style={dashboardStyle}>
        {fullName && <UserHeader fullName={fullName} role="Fuel Attendant" />}

        <h1 style={headingStyle}>Worker Dashboard</h1>
        <p style={paragraphStyle}>Here you can view and manage your pump activities.</p>

        <button onClick={handleLogout} style={logoutButtonStyle}>
          Logout
        </button>
      </div>
    </div>
  );
};

// Styles
const containerStyle = {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  height: "100vh",
  backgroundColor: "#f4f7fa",
};

const dashboardStyle = {
  backgroundColor: "#fff",
  padding: "30px",
  borderRadius: "8px",
  boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
  textAlign: "center",
  maxWidth: "600px",
  width: "100%",
};

const headingStyle = {
  color: "#2c3e50",
  fontSize: "2.5rem",
  marginBottom: "20px",
};

const paragraphStyle = {
  color: "#7f8c8d",
  fontSize: "1.2rem",
  marginBottom: "30px",
};

const logoutButtonStyle = {
  backgroundColor: "#e74c3c",
  color: "white",
  padding: "12px 24px",
  border: "none",
  borderRadius: "5px",
  cursor: "pointer",
  fontSize: "16px",
  transition: "background-color 0.3s ease",
};

export default WorkerDashboard;
