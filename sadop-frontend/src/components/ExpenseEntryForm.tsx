import React, { useEffect, useState } from 'react';
import api from '../axios';

const ExpenseEntryForm = ({ onClose }) => {
  const today = new Date().toISOString().split('T')[0];
  const [draftExpenses, setDraftExpenses] = useState([]);
  const [newExpense, setNewExpense] = useState({ type: '', amount: '', date: today });
  const [totalAmount, setTotalAmount] = useState(0);

  const token = localStorage.getItem('token');
  const branchId = localStorage.getItem('branchId');

  useEffect(() => {
    const fetchDrafts = async () => {
      try {
        if (!branchId || branchId === 'null') {
          alert('Branch ID is missing. Please log in again.');
          return;
        }

        const response = await api.get(`/expenses/unsubmitted?branchId=${branchId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        setDraftExpenses(response.data || []);
      } catch (err) {
        console.error('Error fetching draft expenses:', err);
        alert('Failed to fetch draft expenses.');
      }
    };

    fetchDrafts();
  }, [branchId, token]);

  useEffect(() => {
    const sum = draftExpenses.reduce((acc, exp) => acc + parseFloat(exp.amount || 0), 0);
    setTotalAmount(sum);
  }, [draftExpenses]);

  // ✅ Auto-submit after 12 hours
  useEffect(() => {
    if (!draftExpenses || draftExpenses.length === 0) return;

    const interval = setInterval(() => {
      console.log('⏰ Auto-submitting draft expenses...');
      handleSubmit(true); // pass "true" to skip closing modal/alerts
    }, 12 * 60 * 60 * 1000); // 12 hours in ms

    return () => clearInterval(interval);
  }, [draftExpenses]); // re-run when drafts change

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewExpense(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    if (!newExpense.type || !newExpense.amount) {
      alert('Please enter both expense type and amount.');
      return;
    }

    if (isNaN(newExpense.amount) || parseFloat(newExpense.amount) <= 0) {
      alert('Amount must be a positive number.');
      return;
    }

    try {
      const payload = {
        type: newExpense.type,
        amount: parseFloat(newExpense.amount),
        date: newExpense.date,
        status: 'DRAFT',
      };

      if (branchId && branchId !== 'null') {
        payload.branch = { id: Number(branchId) };
      }

      const response = await api.post('/expenses/save', payload, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setDraftExpenses(prev => [...prev, response.data]);
      setNewExpense({ type: '', amount: '', date: today });
      alert('Expense saved as draft.');
    } catch (err) {
      console.error('Error saving expense:', err);
      alert('Failed to save expense.');
    }
  };

  const handleDeleteExpense = async (id) => {
    try {
      await api.delete(`/expenses/delete/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setDraftExpenses(prev => prev.filter(exp => exp.id !== id));
      alert('Draft expense deleted.');
    } catch (err) {
      console.error('Error deleting expense:', err);
      alert('Failed to delete expense.');
    }
  };

  const handleSubmit = async (auto = false) => {
    if (draftExpenses.length === 0) {
      if (!auto) alert('No expenses to submit.');
      return;
    }

    try {
      const payload = draftExpenses.map((exp) => ({
        ...exp,
        branch: branchId && branchId !== 'null' ? { id: Number(branchId) } : null,
        status: 'SUBMITTED',
      }));

      await api.post('/expenses/submit', payload, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!auto) alert('Expenses submitted successfully.');
      setDraftExpenses([]);
      setNewExpense({ type: '', amount: '', date: today });
      if (!auto) onClose();
    } catch (err) {
      console.error('Submission failed:', err);
      if (!auto) alert('Submission failed.');
    }
  };

  return (
    <div style={modalStyle}>
      <h2>Expense Entry</h2>
      <div style={formStyle}>
        <div style={inputGroupStyle}>
          <label style={labelStyle}>Expense Type</label>
          <input
            type="text"
            name="type"
            placeholder="Expense Type"
            value={newExpense.type}
            onChange={handleChange}
            style={inputStyle}
          />
        </div>

        <div style={inputGroupStyle}>
          <label style={labelStyle}>Amount</label>
          <input
            type="number"
            name="amount"
            placeholder="Amount"
            value={newExpense.amount}
            onChange={handleChange}
            style={inputStyle}
          />
        </div>

        <div style={inputGroupStyle}>
          <label style={labelStyle}>Expense Date</label>
          <input
            type="date"
            name="date"
            value={newExpense.date}
            onChange={handleChange}
            style={inputStyle}
          />
        </div>

        <button onClick={handleSave} style={saveButtonStyle}>Save as Draft</button>
      </div>

      <h3>Draft Expenses</h3>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th>Type</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {draftExpenses.map((exp, index) => (
            <tr key={exp.id || index}>
              <td>{exp.type}</td>
              <td>{parseFloat(exp.amount).toFixed(2)}</td>
              <td>{exp.date ? new Date(exp.date).toLocaleDateString() : 'N/A'}</td>
              <td>
                {(() => {
                  const createdAt = new Date(exp.createdAt);
                  const now = new Date();
                  const diffInMinutes = (now - createdAt) / 60000; // difference in minutes
                  return diffInMinutes <= 2 ? ( // changed from 3 to 2 minutes
                    <button
                      onClick={() => handleDeleteExpense(exp.id)}
                      style={{
                        backgroundColor: '#e74c3c',
                        color: '#fff',
                        border: 'none',
                        padding: '6px 12px',
                        borderRadius: '4px',
                        cursor: 'pointer',
                      }}
                    >
                      Delete
                    </button>
                  ) : <span style={{ color: '#999' }}>Cannot delete</span>;
                })()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: '10px', fontWeight: 'bold' }}>
        Total Draft Expenses: Ksh{totalAmount.toFixed(2)}
      </div>

      <div style={footerStyle}>
        <button onClick={onClose} style={cancelButtonStyle}>Cancel</button>
        <button onClick={() => handleSubmit(false)} style={submitButtonStyle}>Submit All</button>
      </div>
    </div>
  );
};

// --- Styles ---
const modalStyle = { position: 'fixed', top: '10%', left: '10%', right: '10%', bottom: '10%', backgroundColor: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', overflowY: 'auto', maxHeight: '80vh' };
const formStyle = { display: 'flex', flexDirection: 'column', gap: '15px', marginBottom: '20px' };
const inputGroupStyle = { display: 'flex', flexDirection: 'column', gap: '5px' };
const labelStyle = { fontSize: '14px', color: '#555' };
const inputStyle = { padding: '10px', fontSize: '16px', border: '1px solid #ccc', borderRadius: '4px' };
const saveButtonStyle = { backgroundColor: '#4CAF50', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px' };
const tableStyle = { width: '100%', borderCollapse: 'collapse', marginTop: '20px' };
const footerStyle = { display: 'flex', justifyContent: 'space-between', marginTop: '20px' };
const submitButtonStyle = { backgroundColor: '#3498db', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' };
const cancelButtonStyle = { backgroundColor: '#e74c3c', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' };

export default ExpenseEntryForm;
