import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import UserHeader from '../components/UserHeader';
import SalesEntryForm from '../components/SalesEntryForm';
import ExpenseEntryForm from '../components/ExpenseEntryForm';
import RegisterUserForm from '../components/RegisterUserForm';
import dashboardBg from '../assets/backgrounds/dashboard-bg.jpg';

// ========================= CALCULATOR COMPONENT =========================
const CalculatorModal = ({ onClose }) => {
  const [input, setInput] = useState('');
  const [result, setResult] = useState('');
  const [history, setHistory] = useState([]);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [origin, setOrigin] = useState({ x: 0, y: 0 });
  const [equalsPressed, setEqualsPressed] = useState(false);

  const append = (val) => {
    if (equalsPressed) {
      setInput(val);
      setEqualsPressed(false);
    } else {
      setInput(input + val);
    }
  };

  const clear = () => {
    setInput('');
    setResult('');
    setEqualsPressed(false);
  };

  const calculate = () => {
    try {
      // Evaluate input safely
      // eslint-disable-next-line no-eval
      const res = eval(input).toString();
      setHistory([{ operation: input, answer: res }, ...history]);
      setInput(res);
      setResult(res);
      setEqualsPressed(true);
    } catch {
      setResult('');
    }
  };

  // Live update result while typing
  useEffect(() => {
    if (!equalsPressed && input !== '') {
      try {
        // eslint-disable-next-line no-eval
        const res = eval(input).toString();
        setResult(res);
      } catch {
        setResult('');
      }
    } else if (input === '') {
      setResult('');
    }
  }, [input, equalsPressed]);

  // Drag handlers
  const handleMouseDown = (e) => {
    setIsDragging(true);
    setOrigin({ x: e.clientX - position.x, y: e.clientY - position.y });
  };

  const handleMouseMove = (e) => {
    if (!isDragging) return;
    setPosition({ x: e.clientX - origin.x, y: e.clientY - origin.y });
  };

  const handleMouseUp = () => setIsDragging(false);

  return (
    <div
      style={calculatorOverlayStyle}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
    >
      <div
        style={{ ...calculatorContainerStyle, transform: `translate(${position.x}px, ${position.y}px)` }}
      >
        <div style={calculatorHeaderStyle} onMouseDown={handleMouseDown}>
          Calculator 🧮
        </div>

        <div style={calculatorDisplayStyle}>
          <div>{input || '0'}</div>
          {input && <div style={calculatorResultStyle}>= {result}</div>}
        </div>

        <div style={calculatorButtonsStyle}>
          {['7','8','9','/','4','5','6','*','1','2','3','-','0','.','+'].map((b) => (
            <button
              key={b}
              style={calculatorButtonStyle}
              onClick={() => append(b)}
            >
              {b}
            </button>
          ))}
          <button style={{...calculatorButtonStyle, backgroundColor:'#ff4d4d'}} onClick={clear}>C</button>
          <button style={{...calculatorButtonStyle, backgroundColor:'#4caf50'}} onClick={calculate}>=</button>
        </div>

        {/* History */}
        <div style={calculatorHistoryStyle}>
          {history.length === 0 ? (
            <div style={{ opacity: 0.5 }}>No history yet</div>
          ) : (
            history.map((h, index) => (
              <div key={index} style={historyItemStyle}>
                <div>{h.operation}</div>
                <div style={{ color:'#4caf50' }}>= {h.answer}</div>
              </div>
            ))
          )}
        </div>

        <button style={closeCalcButtonStyle} onClick={onClose}>Close</button>
      </div>
    </div>
  );
};

// ========================= MANAGER DASHBOARD =========================
const ManagerDashboard = () => {
  const [showSalesEntryForm, setShowSalesEntryForm] = useState(false);
  const [showExpenseForm, setShowExpenseForm] = useState(false);
  const [fullName, setFullName] = useState(null);
  const [role, setRole] = useState(null);
  const [showRegisterForm, setShowRegisterForm] = useState(false);
  const [showCalculator, setShowCalculator] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedRole = localStorage.getItem('role');
    const storedName = localStorage.getItem('fullName');

    if (
      !token ||
      (storedRole !== 'ROLE_MANAGER' &&
        storedRole !== 'ROLE_ASSISTANT_MANAGER' &&
        storedRole !== 'ROLE_CEO')
    ) {
      navigate('/login');
      return;
    }

    setFullName(storedName);
    setRole(storedRole);
  }, [navigate]);

  const handleOpenSalesEntryForm = () => setShowSalesEntryForm(true);
  const handleCloseSalesEntryForm = () => setShowSalesEntryForm(false);

  const handleOpenExpenseForm = () => setShowExpenseForm(true);
  const handleCloseExpenseForm = () => setShowExpenseForm(false);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
    navigate('/login');
  };

  const handleHover = (e) => {
    e.currentTarget.style.transform = 'translateY(-5px)';
    e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.3)';
  };

  const handleLeave = (e) => {
    e.currentTarget.style.transform = 'translateY(0)';
    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.2)';
  };

  const getCardStyle = (color) => ({
    background: color,
    border: '1px solid rgba(255,255,255,0.15)',
    borderRadius: '16px',
    padding: '20px 15px',
    cursor: 'pointer',
    transition: 'all 0.25s ease',
    boxShadow: '0 4px 12px rgba(0,0,0,0.4)',
    backdropFilter: 'blur(3px)',
    WebkitBackdropFilter: 'blur(3px)',
    minHeight: '100px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
  });

  return (
    <div style={containerStyle}>
      <div style={contentStyle}>
        {fullName && <UserHeader fullName={fullName} style={{ color: '#f0f0f0' }} />}

        <div style={taskSectionStyle}>
          <h3 style={taskHeaderStyle}>Manage your tasks</h3>

          <div style={gridStyle}>
            {/* Register Staff */}
            <div
              style={{
                ...getCardStyle('rgba(30, 40, 60, 0.9)'),
                opacity: role === 'ROLE_ASSISTANT_MANAGER' ? 0.6 : 1,
                cursor: role === 'ROLE_ASSISTANT_MANAGER' ? 'not-allowed' : 'pointer',
              }}
              onClick={() => { if (role !== 'ROLE_ASSISTANT_MANAGER') setShowRegisterForm(true); }}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>👤</div>
              <div style={labelStyle}>Register New Staff</div>
            </div>

            {/* Debts */}
            <div
              style={getCardStyle('rgba(70, 50, 90, 0.9)')}
              onClick={() => navigate('/debts')}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>💸</div>
              <div style={labelStyle}>Manage Debts</div>
            </div>

            {/* Sales */}
            <div
              style={getCardStyle('rgba(40, 60, 90, 0.9)')}
              onClick={handleOpenSalesEntryForm}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>🧾</div>
              <div style={labelStyle}>Daily Sales</div>
            </div>

            {/* Expenses */}
            <div
              style={getCardStyle('rgba(180, 50, 60, 0.9)')}
              onClick={handleOpenExpenseForm}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>📊</div>
              <div style={labelStyle}>Expenses</div>
            </div>

            {/* Pump Balances */}
            <div
              style={getCardStyle('rgba(150, 50, 90, 0.9)')}
              onClick={() => navigate('/initial-setup')}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>⛽</div>
              <div style={labelStyle}>Set Pump Balances</div>
            </div>

            {/* Shorts */}
            <div
              style={getCardStyle('rgba(40, 80, 50, 0.9)')}
              onClick={() => navigate('/shorts')}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>📉</div>
              <div style={labelStyle}>Manage Shorts</div>
            </div>

            {/* Summary */}
            <div
              style={getCardStyle('rgba(90, 50, 20, 0.9)')}
              onClick={() => navigate('/submitted-sales')}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>📅</div>
              <div style={labelStyle}>Generate Sales Summary</div>
            </div>

            {/* Calculator */}
            <div
              style={getCardStyle('rgba(50, 90, 100, 0.9)')}
              onClick={() => setShowCalculator(true)}
              onMouseEnter={handleHover}
              onMouseLeave={handleLeave}
            >
              <div style={iconStyle}>🖩</div>
              <div style={labelStyle}>Calculator</div>
            </div>

            {/* CEO */}
            {role === 'ROLE_CEO' && (
              <div
                style={getCardStyle('rgba(50, 0, 100, 0.9)')}
                onClick={() => navigate('/ceo-dashboard')}
                onMouseEnter={handleHover}
                onMouseLeave={handleLeave}
              >
                <div style={iconStyle}>🧠</div>
                <div style={labelStyle}>Back to Main Menu</div>
              </div>
            )}
          </div>
        </div>

        {/* Forms */}
        {showSalesEntryForm && <SalesEntryForm onClose={handleCloseSalesEntryForm} />}
        {showExpenseForm && <ExpenseEntryForm onClose={handleCloseExpenseForm} />}
        {showRegisterForm && <RegisterUserForm onClose={() => setShowRegisterForm(false)} />}
        {showCalculator && <CalculatorModal onClose={() => setShowCalculator(false)} />}
      </div>

      {/* Logout */}
      <div style={logoutButtonContainerStyle}>
        <button style={logoutButtonStyle} onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
};

// ================== STYLES ==================

const containerStyle = {
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  minHeight: '100vh',
  width: '100vw',
  flexDirection: 'column',
  backgroundImage: `url(${dashboardBg})`,
  backgroundSize: 'cover',
  backgroundPosition: 'center',
};

const contentStyle = {
  width: '100%',
  maxWidth: '1100px',
  minHeight: '100vh',
  backdropFilter: 'blur(0px)',
  WebkitBackdropFilter: 'blur(0px)',
  padding: '30px 20px',
  textAlign: 'center',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
};

const taskSectionStyle = { width: '100%' };
const taskHeaderStyle = { fontSize: '1.9rem', marginBottom: '20px', color: '#e0e0e0' };
const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '70px' };
const iconStyle = { fontSize: '88px', marginBottom: '8px', color: '#ffffff' };
const labelStyle = { fontSize: '17px', fontWeight: 'bold', color: '#f0f0f0', textAlign: 'center' };
const logoutButtonStyle = { backgroundColor: 'rgba(150,0,0,0.9)', color: 'white', padding: '10px 22px', border: 'none', borderRadius: '10px', cursor: 'pointer', fontSize: '15px', boxShadow: '0 4px 10px rgba(0,0,0,0.3)' };

// Calculator Styles
const calculatorOverlayStyle = { position: 'fixed', top:0, left:0, right:0, bottom:0, zIndex:1000 };
const calculatorHeaderStyle = { backgroundColor: '#444', padding: '8px', cursor: 'grab', color: '#fff', fontWeight: 'bold', borderTopLeftRadius: '15px', borderTopRightRadius: '15px', userSelect: 'none' };
const calculatorContainerStyle = { background:'#222', padding:'15px', borderRadius:'15px', width:'280px', textAlign:'center', position:'absolute', zIndex:1000 };
const calculatorDisplayStyle = { background:'#111', color:'#0f0', fontWeight:'bold', padding:'8px', borderRadius:'8px', marginBottom:'10px', minHeight:'40px', textAlign:'right' };
const calculatorResultStyle = { color:'#4caf50', fontWeight:'bold', marginTop:'4px', textAlign:'right' };
const calculatorButtonsStyle = { display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:'5px', marginBottom:'10px' };
const calculatorButtonStyle = { padding:'10px', borderRadius:'8px', border:'none', background:'#333', color:'#fff', fontWeight:'bold', cursor:'pointer', fontSize:'16px' };
const closeCalcButtonStyle = { padding:'6px 12px', borderRadius:'8px', border:'none', background:'#ff4d4d', color:'#fff', fontWeight:'bold', cursor:'pointer' };

// History Styles
const calculatorHistoryStyle = { maxHeight:'150px', overflowY:'auto', background:'#111', padding:'8px', borderRadius:'8px', marginBottom:'10px' };
const historyItemStyle = { borderBottom:'1px solid #444', padding:'4px 0', textAlign:'right' };

const logoutButtonContainerStyle = { marginTop: '20px' };

export default ManagerDashboard;