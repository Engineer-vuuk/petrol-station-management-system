import React, { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import "./Login.css";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [redirecting, setRedirecting] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/auth/login`, {
        email,
        password,
      });

      const { token, role, fullName, branchId, branchName } = response.data;

      // ✅ Allow CEO (branchId can be null or undefined)
      if (!token || !role || !fullName) {
        setErrorMessage("Invalid response from the server.");
        return;
      }

      // Log everything
      console.log("JWT Token:", token);
      console.log("User Role:", role);
      console.log("Branch ID:", branchId);
      console.log("Branch Name:", branchName);

      // Save to localStorage
      localStorage.setItem("token", token);
      localStorage.setItem("role", role);
      localStorage.setItem("fullName", fullName);

      // Only set branch info if available (CEO won't have)
      if (branchId !== null && branchId !== undefined) {
        localStorage.setItem("branchId", branchId);
        localStorage.setItem("branchName", branchName);
      }

      setRedirecting(true);
      setTimeout(() => {
        if (role === "ROLE_MANAGER" || role === "ROLE_ASSISTANT_MANAGER") {
          navigate("/manager-dashboard");
        } else if (role === "ROLE_CEO") {
          navigate("/ceo-dashboard"); // ✅ fixed
        } else if (role === "ROLE_ATTENDANT") {
          navigate("/worker-dashboard");
        } else {
          setErrorMessage("Unknown role.");
          setRedirecting(false);
        }
      }, 100);
    } catch (error) {
      console.error("Login error:", error);
      setErrorMessage("Invalid email or password.");
    }
  };

  return (
    <div className="login-wrapper">
      <form className="login-card" onSubmit={handleLogin}>
        <h2>Login</h2>

        <label>Email</label>
        <input
          type="email"
          placeholder="Enter your email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <label>Password</label>
        <div className="password-wrapper">
          <input
            type={showPassword ? "text" : "password"}
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <span
            className="toggle-password"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? "Hide" : "Show"}
          </span>
        </div>

        <div className="forgot-link">
          <a href="#">Forgot Password?</a>
        </div>

        {errorMessage && <p className="error">{errorMessage}</p>}
        {redirecting && <p style={{ color: "green" }}>Redirecting...</p>}

        <button type="submit" className="login-btn">Sign In</button>

        <p className="signup">
          NO UNAUTHORIZED PERSONELS IN THIS SITE
        </p>
        <p>© copyright Engineer Vuuk.</p>
      </form>
    </div>
  );
};

export default Login;
