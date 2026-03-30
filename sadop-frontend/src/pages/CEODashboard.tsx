import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import UserHeader from '../components/UserHeader';

const CEODashboard = () => {
  const [fullName, setFullName] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const name = localStorage.getItem('fullName');
    if (name) {
      setFullName(name);
    } else {
      console.warn("Full name not found in localStorage.");
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
    localStorage.removeItem('branchId'); // Clear CEO temporary branch if any
    localStorage.removeItem('branchName');
    navigate('/login');
  };

  const handleBranchSelect = (branchId, branchName) => {
    localStorage.setItem('branchId', branchId);
    localStorage.setItem('branchName', branchName);
    navigate('/manager-dashboard'); // CEO will now act as Manager in that branch
  };

  return (
    <div style={containerStyle}>
      <div style={contentStyle}>
        {fullName && <UserHeader fullName={fullName} role="CEO" />}

        <h1 style={headingStyle}>Director Dashboard</h1>

        {fullName && (
          <p style={descriptionStyle}>
            Welcome, {fullName}! Select a branch below to manage as Manager.
          </p>
        )}

        <div style={buttonGroupStyle}>
          <button
            style={branchButtonStyle}
            onClick={() => handleBranchSelect(1, 'Maua')}
          >
            🌿 Manage MAUA Branch
          </button>

          <button
            style={branchButtonStyle}
            onClick={() => handleBranchSelect(2, 'Mikinduri')}
          >
            🌿 Manage MIKINDURI Branch
          </button>

          <button
            style={branchButtonStyle}
            onClick={() => handleBranchSelect(3, 'Muriri')}
          >
            🌿 Manage MURIRI Branch
          </button>

          <button
            style={branchButtonStyle}
            onClick={() => handleBranchSelect(4, 'Wholesale')}
          >
            🏪 Manage WHOLESALE Branch
          </button>
        </div>

        <button style={logoutButtonStyle} onClick={handleLogout}>
          Logout
        </button>
      </div>
    </div>
  );
};

// Styles remain unchanged...
const containerStyle = {
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  height: '100vh',
  backgroundColor: '#f4f7fa',
};

const contentStyle = {
  width: '100%',
  maxWidth: '900px',
  backgroundColor: '#fff',
  padding: '30px',
  borderRadius: '8px',
  boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
  textAlign: 'center',
};

const headingStyle = {
  color: '#2c3e50',
  fontSize: '2.5rem',
  marginBottom: '20px',
};

const descriptionStyle = {
  color: '#7f8c8d',
  fontSize: '1.2rem',
  marginBottom: '30px',
};

const buttonGroupStyle = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '15px',
  marginBottom: '30px',
};

const branchButtonStyle = {
  backgroundColor: '#3498db',
  color: 'white',
  padding: '12px 24px',
  border: 'none',
  borderRadius: '5px',
  cursor: 'pointer',
  fontSize: '16px',
  width: '100%',
  maxWidth: '300px',
  transition: 'background-color 0.3s ease',
};

const logoutButtonStyle = {
  backgroundColor: '#e74c3c',
  color: 'white',
  padding: '12px 24px',
  border: 'none',
  borderRadius: '5px',
  cursor: 'pointer',
  fontSize: '16px',
  width: '100%',
  maxWidth: '300px',
  transition: 'background-color 0.3s ease',
};

export default CEODashboard;
