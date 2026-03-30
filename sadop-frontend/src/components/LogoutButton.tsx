// src/components/LogoutButton.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';

const LogoutButton = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Remove user from localStorage
    localStorage.removeItem('loggedInUser');

    // Redirect to login page
    navigate('/');
  };

  return <button onClick={handleLogout}>Logout</button>;
};

export default LogoutButton;
