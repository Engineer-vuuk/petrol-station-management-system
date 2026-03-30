import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import axios from 'axios';

const SubmittedSalesPage = () => {
  const [sales, setSales] = useState([]);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [summary, setSummary] = useState(null);
  const navigate = useNavigate();

  const formatAmount = (val) =>
    Number(val || 0).toLocaleString('en-KE', { minimumFractionDigits: 2 });

  const fetchSubmittedSalesBetween = async () => {
    try {
      if (!startDate || !endDate) {
        alert('Please select both start and end dates.');
        return;
      }

      const token = localStorage.getItem('token');
      const branchId = localStorage.getItem('branchId');

      const axiosInstance = axios.create({
        baseURL: `${import.meta.env.VITE_API_BASE_URL}/api/sales-entries`,
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Branch-Id': branchId,
        },
      });

      const response = await axiosInstance.get('/submitted-between', {
        params: { startDate, endDate },
      });

      setSales(response.data.entries || []);
      setSummary(response.data);
    } catch (error) {
      console.error('Failed to fetch submitted sales:', error);
      alert('Failed to fetch submitted sales.');
    }
  };

  const calculateTotal = (field) =>
    sales.reduce((sum, e) => sum + (parseFloat(e[field]) || 0), 0);

  const totalFuelByType = (type) =>
    sales.reduce((sum, e) => {
      if (e.pumpName?.toLowerCase().includes(type)) {
        return sum + (parseFloat(e.fuelConsumed) || 0);
      }
      return sum;
    }, 0);

const handleDownloadPDF = () => {
  if (!sales.length) {
    alert('No sales data to export.');
    return;
  }

  const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });

  const formattedStart = new Date(startDate).toLocaleDateString('en-KE');
  const formattedEnd = new Date(endDate).toLocaleDateString('en-KE');
  const branchName = localStorage.getItem('branchName') || 'Branch';

  doc.setFontSize(14);
  doc.setFont(undefined, 'bold');
  doc.text('Sales Report Summary', 300, 30, { align: 'center' });

  doc.setFontSize(11);
  doc.setFont(undefined, 'bold');
  doc.text(`Branch: ${branchName}`, 40, 50);
  doc.text(`Date Range: ${formattedStart} - ${formattedEnd}`, 500, 50);

  // Define headers
  const headers = [
    'Date', 'Attendant', 'Pump', 'Opening (L)', 'Closing (L)', 'Price/Litre',
    'Cash (KES)', 'MPesa (KES)', 'Equitel (KES)', 'Family Bank (KES)',
    'Visa Card (KES)', 'Coins (KES)', 'Debts (KES)', 'Discount (KES)',
    'Paid Debts (KES)', 'Expenses (KES)', 'Expected Cash (KES)',
    'Fuel Consumed (L)', 'Short/Loss (KES)', 'Entry #', 'Submitted At', 'Remarks'
  ];

  // Data body rows
  const body = sales.map(entry => [
    entry.salesDate,
    entry.attendantId,
    entry.pumpName,
    formatAmount(entry.openingBalance),
    formatAmount(entry.closingBalance),
    formatAmount(entry.pricePerLitre),
    formatAmount(entry.totalCash),
    formatAmount(entry.totalMpesa),
    formatAmount(entry.equitel),
    formatAmount(entry.familyBank),
    formatAmount(entry.visaCard),
    formatAmount(entry.otherMobileMoney),
    formatAmount(entry.totalDebts),
    formatAmount(entry.discount),
    formatAmount(entry.paidDebts),
    formatAmount(entry.totalExpenses),
    formatAmount(entry.expectedCash),
    formatAmount(entry.fuelConsumed),
    formatAmount(entry.shortOrLoss),
    entry.entryNumber,
    entry.submittedAt ? new Date(entry.submittedAt).toLocaleString() : '-',
    entry.remarks || ''
  ]);

  // Totals row
  const total = field => sales.reduce((sum, e) => sum + (parseFloat(e[field]) || 0), 0);
  const totalsRow = [
    'TOTALS', '', '',
    formatAmount(total('openingBalance')),
    formatAmount(total('closingBalance')),
    '', // price/Litre — not meaningful as total
    formatAmount(total('totalCash')),
    formatAmount(total('totalMpesa')),
    formatAmount(total('equitel')),
    formatAmount(total('familyBank')),
    formatAmount(total('visaCard')),
    formatAmount(total('otherMobileMoney')),
    formatAmount(total('totalDebts')),
    formatAmount(total('discount')),
    formatAmount(total('paidDebts')),
    formatAmount(total('totalExpenses')),
    formatAmount(total('expectedCash')),
    formatAmount(total('fuelConsumed')),
    formatAmount(total('shortOrLoss')),
    '', '', ''
  ];

  autoTable(doc, {
    head: [headers],
    body: body,
    foot: [totalsRow],
    startY: 70,
    styles: { fontSize: 8, cellPadding: 4 },
    headStyles: { fillColor: [33, 150, 243], textColor: 255 },
    footStyles: { fillColor: [239, 239, 239], textColor: 20, fontStyle: 'bold' },
    margin: { left: 20, right: 20 },
    theme: 'grid'
  });

  // Summary block
  let y = doc.lastAutoTable.finalY + 30;
  doc.setFontSize(11);
  doc.setFont(undefined, 'bold');
  doc.text('Fuel Summary:', 40, y);
  y += 18;

  const fuelByType = type =>
    sales.reduce((sum, e) =>
      e.pumpName?.toLowerCase().includes(type) ? sum + (parseFloat(e.fuelConsumed) || 0) : sum,
    0);

  const summaryLines = [
    `Total Fuel Consumed: ${formatAmount(total('fuelConsumed'))} L`,
    `• Petrol: ${formatAmount(fuelByType('petrol'))} L`,
    `• Diesel: ${formatAmount(fuelByType('diesel'))} L`
  ];

  summaryLines.forEach(line => {
    doc.text(line, 60, y);
    y += 14;
  });

  doc.save('submitted_sales_report.pdf');
};

  return (
    <div style={{ padding: '20px' }}>
      <button onClick={() => navigate(-1)} style={{ marginBottom: 20 }}>
        ← Back to Dashboard
      </button>

      <h2>📊 Submitted Sales</h2>

      <div style={{ marginBottom: '20px' }}>
        <label>Start Date: </label>
        <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        <label style={{ marginLeft: '10px' }}>End Date: </label>
        <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        <button
          onClick={fetchSubmittedSalesBetween}
          style={{
            marginLeft: '10px',
            padding: '5px 10px',
            backgroundColor: '#2ecc71',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
          }}
        >
          🔍 Fetch Range
        </button>
      </div>

      {sales.length === 0 ? (
        <p>No entries found.</p>
      ) : (
        <>
          <table border="1" cellPadding="6" cellSpacing="0" style={{ width: '100%', fontSize: '12px' }}>
            <thead style={{ backgroundColor: '#f4f4f4' }}>
              <tr>
                <th>Date</th><th>Attendant</th><th>Pump</th><th>Opening</th><th>Closing</th><th>Price</th>
                <th>Cash</th><th>MPesa</th><th>Equitel</th><th>Family Bank</th><th>Visa Card</th><th>Coins</th>
                <th>Debts</th><th>Discount</th><th>Paid Debts</th><th>Expenses</th>
                <th>Expected</th><th>Fuel Consumed</th><th>Short/Loss</th><th>Entry#</th><th>Submitted At</th><th>Remarks</th>
              </tr>
            </thead>
            <tbody>
              {sales.map((entry, index) => (
                <tr key={index}>
                  <td>{entry.salesDate}</td>
                  <td>{entry.attendantId}</td>
                  <td>{entry.pumpName}</td>
                  <td>{formatAmount(entry.openingBalance)}</td>
                  <td>{formatAmount(entry.closingBalance)}</td>
                  <td>{formatAmount(entry.pricePerLitre)}</td>
                  <td>{formatAmount(entry.totalCash)}</td>
                  <td>{formatAmount(entry.totalMpesa)}</td>
                  <td>{formatAmount(entry.equitel)}</td>
                  <td>{formatAmount(entry.familyBank)}</td>
                  <td>{formatAmount(entry.visaCard)}</td>
                  <td>{formatAmount(entry.otherMobileMoney)}</td>
                  <td>{formatAmount(entry.totalDebts)}</td>
                  <td>{formatAmount(entry.discount)}</td>
                  <td>{formatAmount(entry.paidDebts)}</td>
                  <td>{formatAmount(entry.totalExpenses)}</td>
                  <td>{formatAmount(entry.expectedCash)}</td>
                  <td>{formatAmount(entry.fuelConsumed)}</td>
                  <td>{formatAmount(entry.shortOrLoss)}</td>
                  <td>{entry.entryNumber}</td>
                  <td>{entry.submittedAt ? new Date(entry.submittedAt).toLocaleString() : '-'}</td>
                  <td>{entry.remarks}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr style={{ backgroundColor: '#eafaf1', fontWeight: 'bold' }}>
                <td colSpan="6">TOTALS</td>
                <td>{formatAmount(calculateTotal('totalCash'))}</td>
                <td>{formatAmount(calculateTotal('totalMpesa'))}</td>
                <td>{formatAmount(calculateTotal('equitel'))}</td>
                <td>{formatAmount(calculateTotal('familyBank'))}</td>
                <td>{formatAmount(calculateTotal('visaCard'))}</td>
                <td>{formatAmount(calculateTotal('otherMobileMoney'))}</td>

                <td>{formatAmount(calculateTotal('totalDebts'))}</td>
                <td>{formatAmount(calculateTotal('discount'))}</td>
                <td>{formatAmount(calculateTotal('paidDebts'))}</td>
                <td>{formatAmount(calculateTotal('totalExpenses'))}</td>
                <td>{formatAmount(calculateTotal('expectedCash'))}</td>
                <td>
                  <div>Total: {formatAmount(calculateTotal('fuelConsumed'))}L</div>
                  <div>Petrol: {formatAmount(totalFuelByType('petrol'))}L</div>
                  <div>Diesel: {formatAmount(totalFuelByType('diesel'))}L</div>
                </td>
                <td>{formatAmount(calculateTotal('shortOrLoss'))}</td>
                <td colSpan="3"></td>
              </tr>
            </tfoot>
          </table>

          <button
            onClick={handleDownloadPDF}
            style={{
              marginTop: '20px',
              backgroundColor: '#3498db',
              color: 'white',
              padding: '10px 18px',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
            }}
          >
            📥 Download as PDF
          </button>
        </>
      )}
    </div>
  );
};

export default SubmittedSalesPage;
