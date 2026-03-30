// src/pages/LandingPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import bgImage from '../assets/backgrounds/station-bg.jpg';

const quotes = [
  "Energy is the foundation of our future. Let's harness it wisely.",
  "The future of energy is in sustainable solutions. Join us on the journey!",
  "Effective energy management leads to financial sustainability.",
  "In a world of rising costs, efficiency is the key to profitability."
];

const LandingPage = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    navigate('/login');
  };

  return (
    <div style={landingContainerStyle}>
      <div style={mainContentStyle}>

        {/* HERO */}
        <div style={heroSectionStyle}>
          <h1 style={heroHeadingStyle}>
            Sadop Petrol Station Sales Management System
          </h1>
          <p style={taglineStyle}>"ENERGIZING YOUR HUSTLE"</p>

          <p style={branchStyle}>"Every drop counts. Every decision matters."</p>

          <p style={heroSubheadingStyle}>
            Efficient. Sustainable. Profitable.
          </p>

          <button
            style={ctaButtonStyle}
            onClick={handleLoginClick}
            onMouseEnter={(e) => (e.currentTarget.style.transform = "scale(1.05)")}
            onMouseLeave={(e) => (e.currentTarget.style.transform = "scale(1)")}
          >
            Login
          </button>
        </div>

        {/* QUOTES */}
        <div style={sectionStyle}>
          {quotes.map((quote, index) => (
            <p key={index} style={quoteStyle}>
              "{quote}"
            </p>
          ))}
        </div>

        {/* FEATURES */}
        <div style={sectionStyle}>
          <h2 style={featuresHeadingStyle}>Why Choose this system?</h2>
          <ul style={featuresListStyle}>
            <li>⚡ Real-time energy monitoring</li>
            <li>💰 Smart financial insights</li>
            <li>🔒 Secure role-based access</li>
            <li>🛠️ Simple & efficient interface</li>
          </ul>
        </div>

        {/* FOOTER */}
        <footer style={footerStyle}>
          <p>© 2025 Petrol Station Sales Management System</p>
          <p>© Engineer Vuuk</p>
        </footer>

      </div>
    </div>
  );
};

//////////////////////////////////////////////////////
// 🔥 STYLES (PREMIUM UI)
//////////////////////////////////////////////////////

const landingContainerStyle = {
  fontFamily: 'Arial, sans-serif',
  backgroundImage: `url(${bgImage})`,
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  backgroundRepeat: 'no-repeat',
  backgroundAttachment: 'fixed',
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
};

const mainContentStyle = {
  background: 'rgba(0, 0, 0, 0.55)', // glassy transparency
  backdropFilter: 'blur(1px)',
  borderRadius: '20px',
  padding: '60px 30px',             // more padding to prevent cut-off
  width: '100vw',
  minHeight: '100vh',               // fill height but allow scrolling if needed
  color: 'white',
  boxShadow: '0 0 30px rgba(0, 0, 0, 0.3)',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'flex-start',     // align content to top
  alignItems: 'center',             // center horizontally
  overflowY: 'auto',                // allow vertical scroll if content is tall
};

const heroSectionStyle = {
  textAlign: 'center',
  marginBottom: '40px',
};

const heroHeadingStyle = {
  fontSize: '3rem',
  fontWeight: 'bold',
  marginBottom: '15px',
  color: '#ffffff',
};

const branchStyle = {
  fontWeight: 'bold',
  letterSpacing: '2px',
  marginBottom: '5px',
};

const taglineStyle = {
  fontStyle: 'italic',
  marginBottom: '15px',
  color: '#ff4d4d',
};

const heroSubheadingStyle = {
  fontSize: '1.4rem',
  marginBottom: '25px',
};

const ctaButtonStyle = {
  background: 'linear-gradient(45deg, #b30000, #ff1a1a)',
  color: 'white',
  padding: '14px 35px',
  fontSize: '18px',
  border: 'none',
  borderRadius: '8px',
  cursor: 'pointer',
  transition: 'all 0.3s ease',
};

const sectionStyle = {
  marginBottom: '40px',
  textAlign: 'center',
};

const quoteStyle = {
  fontSize: '1.1rem',
  marginBottom: '15px',
  fontStyle: 'italic',
  opacity: 0.9,
};

const featuresHeadingStyle = {
  fontSize: '1.8rem',
  marginBottom: '20px',
};

const featuresListStyle = {
  listStyle: 'none',
  padding: 0,
  lineHeight: '2',
  fontSize: '1.1rem',
};

const footerStyle = {
  marginTop: '30px',
  fontSize: '0.9rem',
  opacity: 0.8,
};

export default LandingPage;