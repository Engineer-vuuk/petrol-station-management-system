// src/components/UserHeader.tsx
import React from 'react';

// A few motivational quotes for random selection
const quotes = [
  "Energy and persistence conquer all things. –",
  "Success is the sum of small efforts repeated day in and day out.",
  "The future depends on what you do today.",
  "Small leaks can sink great ships. Mind your spending.",
  "Where energy is focused, results follow. Fuel wisely!",
  " ENERGIZING YOUR HUSTLE",
  ];

interface UserHeaderProps {
  fullName: string;

}

const UserHeader: React.FC<UserHeaderProps> = ({ fullName, role }) => {
  const randomQuote = quotes[Math.floor(Math.random() * quotes.length)];

  return (
    <div style={headerStyle}>
      <h2 style={nameStyle}>Welcome, {fullName}</h2>

      <blockquote style={quoteStyle}>{randomQuote}</blockquote>
    </div>
  );
};

const headerStyle = {
  marginBottom: '30px',
  backgroundColor: '#ecf0f1',
  padding: '20px',
  borderRadius: '8px',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
};

const nameStyle = {
  margin: '0',
  color: '#2c3e50',
  fontSize: '24px',
};

const roleStyle = {
  margin: '5px 0',
  color: '#7f8c8d',
  fontSize: '18px',
};

const quoteStyle = {
  marginTop: '15px',
  fontStyle: 'italic',
  color: '#34495e',
  fontSize: '16px',
};

export default UserHeader;
