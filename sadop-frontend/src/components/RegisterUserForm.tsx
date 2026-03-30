import React, { useState, useEffect } from 'react';
import axios from 'axios';

const RegisterUserForm = ({ onClose }) => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_ATTENDANT');
  const [allowedRoles, setAllowedRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [success, setSuccess] = useState(false);

  const getAuthToken = () => localStorage.getItem('token');

  const getUserRoleFromToken = () => {
    const token = getAuthToken();
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role;
  };

  useEffect(() => {
    const userRole = getUserRoleFromToken();
    if (!userRole) return;

    let roles = [];
    if (userRole === 'ROLE_CEO') {
      roles = [
        { display: 'Attendant', value: 'ROLE_ATTENDANT' },
        { display: 'Manager', value: 'ROLE_MANAGER' },
        { display: 'Assistant Manager', value: 'ROLE_ASSISTANT_MANAGER' },
        { display: 'Accountant', value: 'ROLE_CEO' },
      ];
    } else if (userRole === 'ROLE_MANAGER') {
      roles = [
        { display: 'Attendant', value: 'ROLE_ATTENDANT' },
        { display: 'Assistant Manager', value: 'ROLE_ASSISTANT_MANAGER' },
      ];
    }

    setAllowedRoles(roles);
    setRole(roles[0].value);
  }, []);

  const validateForm = () => {
    const phoneRegex = /^07\d{8}$/;
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{7,}$/;
    const emailRegex = /^[\w.-]+@gmail\.com$/;

    if (!phoneRegex.test(phone)) {
      setMessage('Please input a valid phone number.');
      setSuccess(false);
      return false;
    }

    if (!passwordRegex.test(password)) {
      setMessage('Password must be at least 7 characters and a combination of letters and numbers.');
      setSuccess(false);
      return false;
    }

    if (!emailRegex.test(email)) {
      setMessage('Please provide a valid Gmail address.');
      setSuccess(false);
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setMessage(null);
    setSuccess(false);

    try {
      const payload = { fullName, email, phone, password, role };

      const response = await axios.post('/api/users/register', payload, {
        headers: {
          Authorization: `Bearer ${getAuthToken()}`,
        },
      });

      if (response.data.success) {
        setMessage(response.data.message);
        setSuccess(true);
        setFullName('');
        setEmail('');
        setPhone('');
        setPassword('');
        setRole(allowedRoles[0].value);
      } else {
        setMessage(response.data.message);
        setSuccess(false);
      }
    } catch (error) {
      console.error(error);
      setMessage(error.response?.data?.message || 'Server error');
      setSuccess(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={modalStyle}>
      <div style={formContainerStyle}>
        <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Register New Staff</h2>

        {message && (
          <div style={{ color: success ? 'green' : 'red', marginBottom: '10px' }}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} style={formStyle}>
          <InputWithEmoji emoji="👤" placeholder="Full Name" value={fullName} onChange={(e) => setFullName(e.target.value)} required />

          <InputWithEmoji
            emoji="📧"
            placeholder="Email (example@gmail.com)"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <InputWithEmoji
            emoji="📞"
            placeholder="Phone (07xxxxxxxx)"
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
          />

          <InputWithEmoji
            emoji="🔒"
            placeholder="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <div style={inputGroupStyle}>
            <label style={labelStyle}>💼 Role</label>
            <select value={role} onChange={(e) => setRole(e.target.value)} style={selectStyle} required>
              {allowedRoles.map((r) => (
                <option key={r.value} value={r.value}>{r.display}</option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '10px' }}>
            <button type="submit" style={submitButtonStyle} disabled={loading}>
              {loading ? 'Registering...' : 'Register'}
            </button>
            <button type="button" style={cancelButtonStyle} onClick={onClose}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// --- Input with emoji ---
const InputWithEmoji = ({ emoji, ...props }) => (
  <div style={inputGroupStyle}>
    <div style={inputWrapperStyle}>
      <span>{emoji}</span>
      <input style={inputStyle} {...props} />
    </div>
  </div>
);

// --- Styles ---
const modalStyle = {
  position: 'fixed',
  top: 0,
  left: 0,
  width: '100vw',
  height: '100vh',
  backgroundColor: 'rgba(0,0,0,0.4)',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  zIndex: 1000,
};

const formContainerStyle = {
  backgroundColor: '#fff',
  padding: '30px',
  borderRadius: '8px',
  width: '400px',
  maxWidth: '90%',
  display: 'flex',
  flexDirection: 'column',
};

const formStyle = {
  display: 'flex',
  flexDirection: 'column',
};

const inputGroupStyle = {
  display: 'flex',
  flexDirection: 'column',
  marginBottom: '15px',
};

const inputWrapperStyle = {
  display: 'flex',
  alignItems: 'center',
  border: '1px solid #ccc',
  borderRadius: '8px',
  padding: '8px 10px',
  gap: '10px',
};

const inputStyle = {
  flex: 1,
  border: 'none',
  outline: 'none',
  fontSize: '16px',
};

const selectStyle = {
  padding: '10px',
  borderRadius: '8px',
  border: '1px solid #ccc',
  fontSize: '16px',
};

const labelStyle = {
  marginBottom: '5px',
  fontWeight: '500',
};

const submitButtonStyle = {
  padding: '12px',
  backgroundColor: '#2ecc71',
  color: 'white',
  border: 'none',
  borderRadius: '8px',
  cursor: 'pointer',
  fontSize: '16px',
  flex: 1,
};

const cancelButtonStyle = {
  padding: '12px',
  backgroundColor: '#e74c3c',
  color: 'white',
  border: 'none',
  borderRadius: '8px',
  cursor: 'pointer',
  fontSize: '16px',
  flex: 1,
};

export default RegisterUserForm;