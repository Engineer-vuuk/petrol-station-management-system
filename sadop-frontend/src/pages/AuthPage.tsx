import React, { useState } from "react";
import Login from "./Login";
import SignUp from "./SignUp";
import "./AuthPage.css";

const AuthPage = () => {
  const [activeTab, setActiveTab] = useState("login");

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-tabs">
          <button
            className={activeTab === "login" ? "active" : ""}
            onClick={() => setActiveTab("login")}
          >
            Login
          </button>
          <button
            className={activeTab === "signup" ? "active" : ""}
            onClick={() => setActiveTab("signup")}
          >
            Sign Up
          </button>
        </div>
        <div className="auth-form">
          {activeTab === "login" ? <Login /> : <SignUp />}
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
