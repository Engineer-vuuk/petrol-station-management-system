import React, { useState } from "react";
import axios from "axios";
import "./SignUp.css";

const SignUp = () => {
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    role: "MANAGER", // Default role
  });

  const [showPassword, setShowPassword] = useState(false);
  const [message, setMessage] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("http://localhost:8080/api/auth/register", formData);
      setMessage("User registered successfully.");
      console.log("Signup response:", response.data);
    } catch (error) {
      console.error("Signup error:", error.response?.data || error.message);
      setMessage("Signup failed. Check input fields or try again.");
    }
  };

  return (
    <div className="signup-wrapper">
      <form className="signup-card" onSubmit={handleSignUp}>
        <h2>Sign Up</h2>

        <label>Full Name</label>
        <input
          type="text"
          name="fullName"
          value={formData.fullName}
          onChange={handleChange}
          placeholder="Enter your full name"
          required
        />

        <label>Email</label>
        <input
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="Enter your email"
          required
        />

        <label>Phone</label>
        <input
          type="text"
          name="phone"
          value={formData.phone}
          onChange={handleChange}
          placeholder="Enter your phone number"
          required
        />

        <label>Password</label>
        <div className="password-wrapper">
          <input
            type={showPassword ? "text" : "password"}
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Create a password"
            required
          />
          <span
            className="toggle-password"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? "Hide" : "Show"}
          </span>
        </div>

        <label>Role</label>
        <select name="role" value={formData.role} onChange={handleChange} required>
          <option value="MANAGER">Manager</option>
          <option value="ASSISTANT_MANAGER">Assistant Manager</option>
          <option value="CEO">CEO</option>
          <option value="ATTENDANT">Attendant</option>
        </select>

        {message && <p className="message">{message}</p>}

        <button type="submit" className="signup-btn">Register</button>

        <p className="login-link">
          Already have an account? <a href="/login">Login</a>
        </p>
      </form>
    </div>
  );
};

export default SignUp;
