import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { useNavigate } from 'react-router-dom';


const DebtForm = () => {
  const [attendantId, setAttendantId] = useState('');
  const [debtorName, setDebtorName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [amount, setAmount] = useState('');
  const [debts, setDebts] = useState([]);
  const [totals, setTotals] = useState({
    unpaid: 0,
    paid: 0,
    total: 0,
    todayFullPaidTotal: 0,
    todayPartialPaidTotal: 0,
  });
  const [repayAmounts, setRepayAmounts] = useState({});
  const [repayModes, setRepayModes] = useState({});
  const [allDebts, setAllDebts] = useState([]);
  const [debtsFetched, setDebtsFetched] = useState(false);
  const [filter, setFilter] = useState('');
  const [viewPaid, setViewPaid] = useState(false);

  const token = localStorage.getItem('token');

  const axiosInstance = axios.create({
    baseURL: `${import.meta.env.VITE_API_BASE_URL}/api/debts`,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });


  const getHeaders = () => {
    const token = localStorage.getItem('token');
    const branchId = localStorage.getItem('branchId');
    return {
      Authorization: `Bearer ${token}`,
      ...(branchId && { 'X-Branch-Id': branchId }),
    };
  };

  const ATTENDANTS = [
    { id: '2', name: 'peter.' },
    { id: '3', name: 'Mercy.' },
    { id: '4', name: 'Joy.' },
    { id: '5', name: 'Josphat.' },
    { id: '6', name: 'new attendant.' },
    { id: '7', name: 'Eric.' },
  ];



const navigate = useNavigate();

  const fetchAndStoreDebts = async () => {
    try {
      const response = await axiosInstance.get('/all', {
        headers: getHeaders(),
      });

      const fetchedDebts = response.data;

      const todayDate = new Date().toLocaleDateString();

      const paidTotal = fetchedDebts.reduce((sum, d) => sum + d.amountPaid, 0);
      const unpaidTotal = fetchedDebts
        .filter((d) => d.status !== 'PAID')
        .reduce((sum, d) => sum + (d.amountOwed - d.amountPaid), 0);
      const totalAmount = fetchedDebts.reduce((sum, d) => sum + d.amountOwed, 0);

      const todayFullPaidTotal = fetchedDebts.reduce((sum, d) => {
        if (d.status === 'PAID' && d.paidAt) {
          const paidAtDate = new Date(d.paidAt).toLocaleDateString();
          if (paidAtDate === todayDate) return sum + d.amountOwed;
        }
        return sum;
      }, 0);

      const todayPartialPaidTotal = fetchedDebts.reduce((sum, d) => {
        if (d.status !== 'PAID' && d.paidAt) {
          const paidAtDate = new Date(d.paidAt).toLocaleDateString();
          if (paidAtDate === todayDate) return sum + (d.latestPaidAmount || 0);
        }
        return sum;
      }, 0);

      setAllDebts(fetchedDebts);
      setTotals({
        unpaid: unpaidTotal,
        paid: paidTotal,
        total: totalAmount,
        todayFullPaidTotal,
        todayPartialPaidTotal,
      });
      setDebtsFetched(true);

      const filtered = viewPaid
        ? fetchedDebts.filter((d) => d.status === 'PAID')
        : fetchedDebts.filter((d) => d.status !== 'PAID');
      setDebts(filtered);
    } catch (error) {
      console.error('Error fetching debts:', error);
      toast.error('Failed to fetch debts.');
    }
  };

  useEffect(() => {
    fetchAndStoreDebts();
    const interval = setInterval(fetchAndStoreDebts, 30000); // Poll every 30s
    return () => clearInterval(interval);
  }, [viewPaid]);

  const handleCreateDebt = async () => {
    if (!attendantId || !debtorName || !amount) {
      toast.warn('Please fill in required fields');
      return;
    }

    try {
      await axiosInstance.post('', {
        attendantId: parseInt(attendantId),
        debtorName,
        phone: phoneNumber,
        amountOwed: parseFloat(amount),
        amountPaid: 0,
      }, {
        headers: getHeaders(),
      });


      setAttendantId('');
      setDebtorName('');
      setPhoneNumber('');
      setAmount('');
      toast.success('Debt saved successfully');
      fetchAndStoreDebts();
    } catch (error) {
      console.error('Error saving debt:', error);
      toast.error('Failed to save');
    }
  };

  const handleRepayDebt = async (id, repayAmount) => {
    const parsedAmount = parseFloat(repayAmount);
    const paymentMode = repayModes[id] || 'CASH';

    if (!parsedAmount || parsedAmount <= 0) {
      toast.warn('Please enter a valid repayment amount.');
      return;
    }
const isDuplicate = debts.some(
  (d) => d.debtorName === debtorName && d.amountOwed === parseFloat(amount)
);

if (isDuplicate) {
  toast.warn('A similar debt entry already exists.');
  return;
}


    const paidAt = new Date().toISOString();

    try {
      await axiosInstance.put(`/${id}/pay`, null, {
        headers: getHeaders(),
        params: {
          amount: parsedAmount,
          paidAt,
          mode: paymentMode,
        },
      });


      setRepayAmounts((prev) => ({ ...prev, [id]: '' }));
      setRepayModes((prev) => ({ ...prev, [id]: '' }));
      toast.success('Debt repayment submitted successfully');
      fetchAndStoreDebts();
    } catch (error) {
      console.error('Error repaying debt:', error);
      toast.error('Failed to repay debt. Please ensure you pay the full amount.');
    }
  };

  const handleDeleteDebt = async (id) => {
    try {
      await axiosInstance.delete(`/${id}`, {
        headers: getHeaders(),
      });
      toast.success('Debt deleted successfully');
      fetchAndStoreDebts();
    } catch (error) {
      console.error('Error deleting debt:', error);
      toast.error('Failed to delete debt.');
    }
  };

  const filteredDebts = debts.filter((debt) =>
    debt.debtorName.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div style={containerStyle}>
      <ToastContainer />
      <h2>💳 Manage Debts</h2>

      <div style={formStyle}>
        <select
          value={attendantId}
          onChange={(e) => setAttendantId(e.target.value)}
        >
          <option value="">Select Attendant</option>
          <option value="2">2 - peter.</option>
          <option value="3">3 - Mercy.</option>
          <option value="4">4 - Joy.</option>
          <option value="5">5 - Josphat.</option>
          <option value="7">7 - Eric.</option>
          <option value="6">6 - new attendant.</option>
          {/* Add more attendants here if needed */}
        </select>

        <input
          type="text"
          placeholder="Debtor's Name"
          value={debtorName}
          onChange={(e) => setDebtorName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Phone (optional)"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
        />
        <input
          type="number"
          placeholder="Amount Owed"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <button onClick={handleCreateDebt}>Save Debt</button>
      </div>

      <div style={{ marginBottom: '10px' }}>
        <button onClick={() => setViewPaid(false)} style={buttonStyleBlue}>
          📌 View Unpaid/Partial Debts
        </button>
        <button onClick={() => setViewPaid(true)} style={buttonStyleGreen}>
          ✅ View Paid Debts
        </button>
      </div>

      <input
        type="text"
        placeholder="Search debtor..."
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        style={{ marginBottom: '10px', padding: '6px', width: '100%' }}
      />

      <table style={tableStyle}>
        <thead>
          <tr>
            <th>Debtor</th>
            <th>Phone</th>
            <th>Attendant ID</th>
            <th>Owed</th>
            <th>Paid</th>
            <th>Remaining</th>
            <th>Created At</th>
            <th>Paid At</th>
            <th>Latest Paid Amount</th>
            <th>Mode of Payment</th>
            {!viewPaid && <th>Repay Amount</th>}
            {!viewPaid && <th>Repay Mode</th>}
            {!viewPaid && <th>Submit Payment</th>}
            {!viewPaid && <th>Delete Debt</th>}
          </tr>
        </thead>
        <tbody>
          {filteredDebts.length > 0 ? (
            filteredDebts.map((debt) => {
              const unpaid = debt.amountOwed - debt.amountPaid;
              const modeOfPayment =
                debt.lastPaymentMode || (debt.status === 'PAID' ? 'Unknown' : '');

              return (
                <tr key={debt.id}>
                  <td>{debt.debtorName}</td>
                  <td>{debt.phone || '—'}</td>
                  <td>{debt.attendantId}</td>
                  <td>{debt.amountOwed.toFixed(2)}</td>
                  <td>{debt.amountPaid.toFixed(2)}</td>
                  <td>{unpaid <= 0 ? 'Paid' : unpaid.toFixed(2)}</td>
                  <td>{new Date(debt.createdAt).toLocaleString()}</td>
                  <td>{debt.paidAt ? new Date(debt.paidAt).toLocaleString() : '—'}</td>
                  <td>{debt.latestPaidAmount ? debt.latestPaidAmount.toFixed(2) : '—'}</td>
                  <td>{modeOfPayment}</td>
                  {!viewPaid && (
                    <>
                      <td>
                        <input
                          type="number"
                          min="0"
                          max={unpaid}
                          value={repayAmounts[debt.id] || ''}
                          onChange={(e) =>
                            setRepayAmounts((prev) => ({
                              ...prev,
                              [debt.id]: e.target.value,
                            }))
                          }
                          style={{ width: '80px' }}
                        />
                      </td>
                      <td>
                        <select
                          value={repayModes[debt.id] || 'CASH'}
                          onChange={(e) =>
                            setRepayModes((prev) => ({
                              ...prev,
                              [debt.id]: e.target.value,
                            }))
                          }
                          style={{ width: '100px' }}
                        >
                          <option value="CASH">Cash</option>
                          <option value="MPESA">MPesa</option>
                          <option value="BANK">Bank</option>
                        </select>
                      </td>
                      <td>
                        <button
                          onClick={() => handleRepayDebt(debt.id, repayAmounts[debt.id])}
                          disabled={
                            !repayAmounts[debt.id] ||
                            parseFloat(repayAmounts[debt.id]) <= 0 ||
                            parseFloat(repayAmounts[debt.id]) > unpaid
                          }
                        >
                          Submit
                        </button>
                      </td>
                      <td>
                        {debt.amountPaid === 0 && debt.status !== 'PAID' ? (
                          (() => {
                            const createdAt = new Date(debt.createdAt);
                            const now = new Date();
                            const diffInMinutes = (now - createdAt) / 60000; // Difference in minutes

                            if (diffInMinutes <= 3) {
                              return (
                                <button
                                  onClick={() => handleDeleteDebt(debt.id)}
                                  style={{ backgroundColor: 'red', color: 'white' }}
                                >
                                  Delete
                                </button>
                              );
                            } else {
                              return <span style={{ color: '#999' }}>Cannot delete</span>;
                            }
                          })()
                        ) : (
                          <span style={{ color: '#999' }}>—</span>
                        )}

                      </td>
                    </>
                  )}
                </tr>
              );
            })
          ) : (
            <tr>
              <td colSpan={viewPaid ? 10 : 14} style={{ textAlign: 'center' }}>
                {debtsFetched ? 'No debts found.' : 'Loading debts...'}
              </td>
            </tr>
          )}
        </tbody>

        {/* 🔽 Footer with neat row-wise summary */}
        <tfoot>
          <tr>
            <td><b>Total Owed:</b></td>
            <td colSpan={viewPaid ? 13 : 13}><b>{totals.total.toFixed(2)}</b></td>
          </tr>
          <tr>
            <td><b>Total Paid:</b></td>
            <td colSpan={viewPaid ? 13 : 13}><b>{totals.paid.toFixed(2)}</b></td>
          </tr>
          <tr>
            <td><b>Total Unpaid:</b></td>
            <td colSpan={viewPaid ? 13 : 13}><b>{totals.unpaid.toFixed(2)}</b></td>
          </tr>
          <tr>
            <td><b>Today's Full Paid Total:</b></td>
            <td colSpan={viewPaid ? 13 : 13}><b>{totals.todayFullPaidTotal.toFixed(2)}</b></td>
          </tr>
          <tr>
            <td><b>Today's Partial Paid Total:</b></td>
            <td colSpan={viewPaid ? 13 : 13}><b>{totals.todayPartialPaidTotal.toFixed(2)}</b></td>
          </tr>
        </tfoot>
      </table>
      <button
        onClick={() => navigate('/manager-dashboard')}
        style={{
          backgroundColor: '#34495e',
          color: '#fff',
          padding: '8px 16px',
          border: 'none',
          borderRadius: '4px',
          marginBottom: '10px',
          cursor: 'pointer',
        }}
      >
        ← Back to Dashboard
      </button>

   </div>
  );
};


const containerStyle = {
  maxWidth: '1200px',
  margin: '20px auto',
  padding: '20px',
  backgroundColor: '#f9f9f9',
  borderRadius: '8px',
  boxShadow: '0 0 8px rgba(0,0,0,0.1)',
};

const formStyle = {
  display: 'flex',
  gap: '10px',
  marginBottom: '20px',
};

const buttonStyleBlue = {
  backgroundColor: '#007BFF',
  color: '#fff',
  border: 'none',
  padding: '8px 16px',
  marginRight: '10px',
  cursor: 'pointer',
  borderRadius: '4px',
};

const buttonStyleGreen = {
  backgroundColor: '#28a745',
  color: '#fff',
  border: 'none',
  padding: '8px 16px',
  cursor: 'pointer',
  borderRadius: '4px',
};

const tableStyle = {
  width: '100%',
  borderCollapse: 'collapse',
  textAlign: 'left',
};

export default DebtForm;
