import React, { useState, useEffect } from 'react';
import api from '../axios';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import './SalesEntryForm.css';



const SalesEntryForm = ({ onClose }) => {
  const today = new Date().toISOString().split('T')[0];
  const [draftEntries, setDraftEntries] = useState([]);
  const [newEntries, setNewEntries] = useState(
    Array.from({ length: 4 }, () => getBlankEntry(today))
  );
  const [pumpClosingBalances, setPumpClosingBalances] = useState({});
  const [tankLevels, setTankLevels] = useState({ petrol: 0, diesel: 0 });
  const [restock, setRestock] = useState({
    driverName: '',
    petrolRestocked: '',
    dieselRestocked: '',
  });
  const [showRestock, setShowRestock] = useState(false);
  const [restockTime, setRestockTime] = useState(null);
  const [recentRestocks, setRecentRestocks] = useState([]);
  const [showRecentRestocks, setShowRecentRestocks] = useState(false);
  const totals = React.useMemo(() => {
    const combinedEntries = [...draftEntries, ...newEntries.filter(e => e.isSaved)];

    const n = (v) => parseFloat(v) || 0;

    const sum = (field) => combinedEntries.reduce((acc, e) => acc + n(e[field]), 0);

    const totalCash       = sum('totalCash');
    const totalMpesa      = sum('totalMpesa');
    const totalEquitel    = sum('equitel');
    const totalFamilyBank = sum('familyBank');
    const totalVisaCard   = sum('visaCard');
    const totalOtherMobile= sum('otherMobileMoney');
    const totalDebts      = sum('totalDebts');
    const totalDiscount   = sum('discount');
    const totalPaidDebts  = sum('paidDebts');
    const totalExpenses   = sum('totalExpenses');
    const totalExpected   = sum('expectedCash');



    const totalShortLoss = combinedEntries.reduce((acc, e) => {
      const expected = n(e.expectedCash);
      const short = expected
        - n(e.totalCash) - n(e.totalMpesa) - n(e.equitel)
        - n(e.familyBank) - n(e.visaCard) - n(e.otherMobileMoney)
        - n(e.totalDebts) - n(e.discount) - n(e.totalExpenses);
      return acc + short;
    }, 0);

    const dieselConsumed = combinedEntries
      .filter(e => e.pumpName?.toLowerCase().includes('diesel'))
      .reduce((acc, e) => acc + n(e.fuelConsumed), 0);

    const petrolConsumed = combinedEntries
      .filter(e => e.pumpName?.toLowerCase().includes('petrol'))
      .reduce((acc, e) => acc + n(e.fuelConsumed), 0);

    return {
      totalCash, totalMpesa, totalDebts, totalDiscount, totalExpected,
       totalShortLoss, dieselConsumed, petrolConsumed,
      totalEquitel, totalFamilyBank, totalVisaCard,
      totalOtherMobile, totalPaidDebts, totalExpenses,
      totalCreditSales: sum('creditSales'),
    };
  }, [draftEntries, newEntries]);


  const pumps = ['diesel1', 'diesel2', 'petrol1', 'petrol2'];



  useEffect(() => {
    const fetchDraftData = async () => {
      try {
        const token = localStorage.getItem('token');
        const branchId = localStorage.getItem('branchId'); // ADD THIS
        const response = await api.get('/sales-entries/get-drafts', {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-Branch-Id': branchId,
          },
        });

        if (response.data && response.data.length > 0) {
          const safeDrafts = response.data.map((d) => ({
            ...getBlankEntry(today),
            ...d,
            isSaved: true,
          }));
          setDraftEntries(safeDrafts);
        }
      } catch (error) {
        console.error('Failed to fetch draft:', error);
        alert('Failed to load draft data.');
      }
    };

    const fetchTankLevels = async () => {
      try {
        const token = localStorage.getItem('token');
        const branchId = localStorage.getItem('branchId');
        const response = await api.get('/fuel-tanks/total', {
                           headers: {
                             Authorization: `Bearer ${token}`,
                             'X-Branch-Id': branchId,
                           },
                           params: {
                             branchId: branchId, // ✅ this part was missing
                           }
                         });

        setTankLevels(response.data);
      } catch (error) {
        console.error('Failed to fetch tank levels', error);
      }
    };

    fetchDraftData();
    fetchTankLevels();
  }, []);

  useEffect(() => {
    const fetchRecentRestocks = async () => {
      try {
        const token = localStorage.getItem('token');
        const branchId = localStorage.getItem('branchId');
        const response = await api.get('/fuel-restock/recent', {
                           headers: {
                             Authorization: `Bearer ${token}`,
                             'X-Branch-Id': branchId,
                           },
                           params: { branchId },
                         });



        setRecentRestocks(response.data);
      } catch (error) {
        console.error('Failed to fetch recent restocks', error);
      }
    };


    if (showRecentRestocks) {
      fetchRecentRestocks();
    }
  }, [showRecentRestocks]);

 const handleDeleteDraft = async (entryId, index) => {
   if (!window.confirm('Are you sure you want to delete this draft?')) return;

   try {
     const token = localStorage.getItem('token');
     const branchId = localStorage.getItem('branchId'); // ✅ Make sure this is set

     await api.delete(`/sales-entries/delete/${entryId}`, {
       headers: {
         Authorization: `Bearer ${token}`,
         'X-Branch-Id': branchId, // ✅ Required for CEO users
       },
     });


     setDraftEntries(prev => prev.filter((_, i) => i !== index));
     alert('Draft deleted successfully.');
   } catch (error) {
     console.error('Failed to delete draft:', error);
     alert('Error deleting draft entry.');
   }
 };

 const validateAttendantId = async (attendantId: number): Promise<boolean> => {
   const token = localStorage.getItem('token');
   const branchId = localStorage.getItem('branchId');

   try {
     const res = await api.get(`/sales-entries/validate-attendant/${attendantId}`, {
       headers: {
         Authorization: `Bearer ${token}`,
         'X-Branch-Id': branchId,
       },
     });

     return res.data.valid === true;
   } catch (err) {
     console.error("❌ Attendant validation failed:", err);
     return false;
   }
 };


  const handlePumpChange = async (index, pumpName) => {
    try {
      const token = localStorage.getItem('token');
      const branchId = localStorage.getItem('branchId');
      const response = await api.get(`/sales-entries/get-last-closing-balance/${pumpName}`, {
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Branch-Id': branchId,
        },
      });


      const latestClosingBalance = response.data || 0;

      setPumpClosingBalances((prev) => ({
        ...prev,
        [pumpName]: latestClosingBalance,
      }));

      setNewEntries((prevEntries) => {
        const updatedEntries = [...prevEntries];
        // Create a new object for the entry instead of mutating directly
        const updatedEntry = {
          ...updatedEntries[index],
          pumpName,
          openingBalance: latestClosingBalance,
        };
        updateCalculatedFields(updatedEntry);
        updatedEntries[index] = updatedEntry;
        return updatedEntries;
      });
    } catch (error) {
      console.error('Error fetching closing balance:', error);
    }
  };

  const updateCalculatedFields = (entry) => {
    const n = (v) => isNaN(parseFloat(v)) ? 0 : parseFloat(v);

    const fuel = n(entry.closingBalance) - n(entry.openingBalance);
    const expected = fuel * n(entry.pricePerLitre);
    const short = expected
      - n(entry.totalCash) - n(entry.totalMpesa) - n(entry.equitel)
      - n(entry.familyBank) - n(entry.visaCard) - n(entry.otherMobileMoney)
      - n(entry.totalDebts) - n(entry.discount) - n(entry.totalExpenses);


    Object.assign(entry, {
      fuelConsumed: fuel,
      expectedCash: expected,
      shortOrLoss: short
    });
  };


  const handleChange = (index, field, value) => {
    setNewEntries((prev) => {
      const updatedEntries = [...prev];
      const entry = { ...updatedEntries[index], [field]: value };
      updateCalculatedFields(entry);
      updatedEntries[index] = entry;
      return updatedEntries;
    });
  };

  const handleSave = async (index) => {
    const currentRow = { ...newEntries[index] };

    const requiredFields = [
      'pumpName',
      'attendantId',
      'openingBalance',
      'closingBalance',
      'pricePerLitre',
      'totalCash',
      'totalMpesa',
      'totalDebts',
    ];

    for (let field of requiredFields) {
      if (!currentRow[field]) {
        alert(`Please fill in the '${field}' field before saving.`);
        return;
      }
    }

    const token = localStorage.getItem('token');
    const branchId = localStorage.getItem('branchId');
    const fuelUsed = Number(currentRow.fuelConsumed) || 0;
    const attendantId = parseInt(currentRow.attendantId, 10);

    // ✅ Step 1: Validate Attendant ID
    const isValid = await validateAttendantId(attendantId);
    if (!isValid) {
      alert(`Attendant ID ${attendantId} is invalid, not an attendant, or not in your branch.`);
      return;
    }

    // ✅ Step 2: Check Fuel Availability
    try {
      const checkResponse = await api.get('/fuel-tanks/check-fuel', {
        params: {
          pumpName: currentRow.pumpName,
          amount: fuelUsed,
          branchId,
        },
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Branch-Id': branchId,
        },
      });

      if (!checkResponse.data) {
        alert(`Not enough fuel in tank for ${currentRow.pumpName}. Please restock first.`);
        return;
      }
    } catch (error) {
      console.error("❌ Fuel check failed:", error);
      alert("Error checking fuel availability.");
      return;
    }

    // ✅ Step 3: Save Entry as Draft
    try {
      const response = await api.post(
        '/sales-entries/save',
        {
          ...currentRow,
          attendantId,
          status: 'draft',
          managerId: localStorage.getItem('managerId') || null,
          branchId,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-Branch-Id': branchId,
          },
        }
      );

      const savedEntry = response.data;

      // ✅ Step 4: Update UI
      const updatedEntries = [...newEntries];
      updatedEntries[index] = {
        ...currentRow,
        ...savedEntry,
        isSaved: true,
        submittedAt: savedEntry.submittedAt || new Date().toISOString(),
      };

      // ✅ Step 5: Add blank row
      const blank = getBlankEntry(today);
      blank.pumpName = '';
      updatedEntries.push(blank);
      setNewEntries(updatedEntries);

      // ✅ Step 6: Update Tank Levels
      if (currentRow.pumpName?.toLowerCase().includes('petrol')) {
        setTankLevels((prev) => ({ ...prev, petrol: prev.petrol - fuelUsed }));
      } else if (currentRow.pumpName?.toLowerCase().includes('diesel')) {
        setTankLevels((prev) => ({ ...prev, diesel: prev.diesel - fuelUsed }));
      }

      alert('✅ Saved as draft.');
    } catch (error) {
      console.error('❌ Save failed:', error);
      alert('Failed to save. Please check your details and try again.');
      return;
    }

    // ✅ Step 7: Post-Save - Fetch latest closing balance
    try {
      await fetchSalesEntries();

      const latest = await api.get(
        `/sales-entries/get-last-closing-balance/${currentRow.pumpName}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'X-Branch-Id': branchId,
          },
        }
      );

      const latestBalance = latest.data || 0;
      setPumpClosingBalances((prev) => ({
        ...prev,
        [currentRow.pumpName]: latestBalance,
      }));

      console.log(`ℹ️ Latest closing balance for ${currentRow.pumpName}:`, latestBalance);
    } catch (postError) {
      console.warn('⚠️ Post-save update failed:', postError);
    }
  };

const getHeaders = () => {
  const token = localStorage.getItem("token");
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
};

const token = localStorage.getItem("token");
const branchId = localStorage.getItem("branchId");

const axiosConfig = {
  headers: {
    Authorization: `Bearer ${token}`,
    "X-Branch-Id": branchId, // Required if user is branch manager
  },
};
  const [attendants, setAttendants] = useState([]);
  useEffect(() => {
    const fetchAttendants = async () => {
      try {
        const res = await api.get('/sales-entries/attendants', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
            'X-Branch-Id': localStorage.getItem('branchId'),
          },
        });
        setAttendants(res.data);
      } catch (err) {
        console.error("Failed to fetch attendants:", err);
        alert("Failed to fetch attendants");
      }
    };
    fetchAttendants();
  }, []);

const handleDownloadPDF = () => {
  const fields = [
    ['Total Expected', formatAmount(totals.totalExpected)],

    ['Total Cash', formatAmount(totals.totalCash)],
    ['Total MPesa', formatAmount(totals.totalMpesa)],
    ['Equitel', formatAmount(totals.totalEquitel)],
    ['Family Bank', formatAmount(totals.totalFamilyBank)],
    ['Visa Card', formatAmount(totals.totalVisaCard)],
    ['Coins', formatAmount(totals.totalOtherMobile)],
    ['Paid Debts', formatAmount(totals.totalPaidDebts)],
    ['Total Today`s Debts', formatAmount(totals.totalDebts)],
    ['Total Discounts', formatAmount(totals.totalDiscount)],
    ['Total Expenses', formatAmount(totals.totalExpenses)],
    ['Total Shorts/Loss', formatAmount(totals.totalShortLoss)],
    ['Total Diesel Used', formatAmount(totals.dieselConsumed)],
    ['Current Diesel Level (L)', formatAmount(tankLevels.diesel)],
    ['Total Petrol Used', formatAmount(totals.petrolConsumed)],
    ['Current Petrol Level (L)', formatAmount(tankLevels.petrol)],
  ];

  const height = 10 + fields.length * 6 + 40;

  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: [80, height],
  });

  // Header
  doc.setFontSize(11);
  doc.setFont('helvetica', 'bold');
  doc.text('Sadop Energy Management Sales Receipt', 40, 10, { align: 'center' });

  doc.setFontSize(9);
  doc.setFont('helvetica', 'bold');
  doc.text('MIKINDURI BRANCH', 40, 13, { align: 'center' });  // 3 units down

  doc.setFontSize(9);
  doc.setFont('helvetica', 'italic');
  doc.text('ENERGIZING YOUR HUSTLE', 40, 16, { align: 'center' });  // 3 units down

  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.text('Sales Totals Summary', 40, 19, { align: 'center' });  // 3 units down


  // Table
  let y = 28;
  doc.setFontSize(9);
  doc.setLineWidth(0.1);
  doc.setDrawColor(150);
  doc.setLineDashPattern([1, 1], 0);

  fields.forEach(([label, value]) => {
    doc.text(label, 10, y);
    doc.line(30, y, 65, y);
    doc.text(value, 70, y, { align: 'right' });
    y += 6;
  });

  // Quote
  y += 6;
  doc.setLineDashPattern([], 0);
  doc.setFontSize(7.5);
  doc.setFont('times', 'italic');
  doc.setTextColor(80, 80, 80);
  doc.text(
    '"Success is the sum of small efforts,\nrepeated day in and day out."',
    40,
    y,
    { align: 'center', maxWidth: 65 }
  );
  y += 10;
  doc.text('- engineer vuuk', 40, y, { align: 'center' });

  doc.save('sadop_sales_receipt.pdf');
};

const getAttendantName = (id: number | string) => {
  const found = attendants.find((a) => a.id === Number(id));
  return found?.fullName || found?.username || `Attendant #${id}`;
};




const formatAmount = (value) => {
  return new Intl.NumberFormat('en-KE', {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(parseFloat(value || 0));
};

const handleDownloadSalesPDF = async () => {
  try {
    const doc = new jsPDF({ unit: 'pt', format: 'a4', orientation: 'portrait' });
    const entries = [...draftEntries, ...newEntries.filter((e) => e.isSaved)];

    if (entries.length === 0) {
      alert('No saved entries to export.');
      return;
    }

    const today = new Date().toISOString().split("T")[0];
    const token = localStorage.getItem('token');
    const branchId = localStorage.getItem('branchId');

    let activeDebts = [];
    let paidTodayDebts = [];
    let draftExpenses = [];

    // === FETCH DEBTS ===
    try {
          const res1 = await api.get('/debts/active', {
                  headers: {
                    Authorization: `Bearer ${token}`,
                    'X-Branch-Id': branchId, // ✅ Fix here too
                  },
                  params: {
                    branchId, // optional depending on logic
                  },
                });
          activeDebts = res1.data;
        } catch (err) {
          console.error('❌ Error fetching active debts:', err);
        }

    try {
          const res2 = await api.get('/debts/daily', {
                  headers: {
                    Authorization: `Bearer ${token}`,
                    'X-Branch-Id': branchId, // ✅ Add this
                  },
                  params: {
                    status: 'PAID',
                    date: today,
                    branchId, // optional depending on your controller logic, but likely safe to keep
                  },
                });

          paidTodayDebts = res2.data;
        } catch (err) {
          console.error('❌ Error fetching today\'s paid debts:', err);
        }


    // === SALES ENTRIES TABLE ===
    doc.setFontSize(16);
    doc.text('Sadop Energy - Sales Entries Report MIKINDURI BRANCH', doc.internal.pageSize.getWidth() / 2, 30, { align: 'center' });

    const fields = [
      { label: 'Date', key: 'salesDate' },
      { label: 'Attendant', key: 'attendantId' },
      { label: 'Pump', key: 'pumpName' },
      { label: 'Opening', key: 'openingBalance' },
      { label: 'Closing', key: 'closingBalance' },
      { label: 'Price per Litre', key: 'pricePerLitre' },
      { label: 'Cash', key: 'totalCash' },
      { label: 'MPesa', key: 'totalMpesa' },
      { label: 'Equitel', key: 'equitel' },
      { label: 'Family Bank', key: 'familyBank' },
      { label: 'Visa Card', key: 'visaCard' },
      { label: 'Coins', key: 'otherMobileMoney' },         // <-- coins field
      { label: 'Debts', key: 'totalDebts' },
      { label: 'Discount', key: 'discount' },
      { label: 'Paid Debts', key: 'paidDebts' },           // <-- paid debts field
      { label: 'Total Expenses', key: 'totalExpenses' },   // <-- expenses field
      { label: 'Expected Cash', key: 'expectedCash' },
      { label: 'Fuel Consumed', key: 'fuelConsumed' },
      { label: 'Short or Loss', key: 'shortOrLoss' },

      { label: 'Entry#', key: 'entryNumber' },
      { label: 'Submitted At', key: 'submittedAt' },
      { label: 'Remarks', key: 'remarks' },
    ];

    const chunkSize = 8;
    const totalPages = Math.ceil(entries.length / chunkSize);

    for (let page = 0; page < totalPages; page++) {
      const chunkEntries = entries.slice(page * chunkSize, (page + 1) * chunkSize);
      const head = [['Field', ...chunkEntries.map((_, i) => `Entry ${page * chunkSize + i + 1}`)]];
      const body = fields.map(field => [
        field.label,
        ...chunkEntries.map(entry => {
          const val = entry[field.key];
          if (field.key === 'attendantId') return getAttendantName(val);
          if (field.key === 'submittedAt') return val ? new Date(val).toLocaleString() : '-';
          if (field.key === 'salesDate') return val ? new Date(val).toLocaleDateString() : '-';
          if (typeof val === 'number') return formatAmount(val);
          return val ?? '-';
        })
      ]);
      if (page > 0) doc.addPage();
      autoTable(doc, {
        startY: 50,
        head,
        body,
        theme: 'striped',
        styles: {
          fontSize: 8,
          cellPadding: 3,
          overflow: 'linebreak',
          valign: 'top',
          halign: 'left',
          lineWidth: 0.1,
          lineColor: [200, 200, 200],
          minCellHeight: Math.floor(600 / fields.length),
        },
        headStyles: {
          fillColor: [41, 128, 185],
          textColor: 255,
          fontStyle: 'bold',
          halign: 'center',
          fontSize: 9,
          lineWidth: 0.2,
          lineColor: [180, 180, 180],
        },
        columnStyles: {
          0: { cellWidth: 90, fontStyle: 'bold' },
          ...Object.fromEntries(chunkEntries.map((_, i) => [i + 1, { cellWidth: 60 }]))
        },
        tableWidth: 'auto',
        pageBreak: 'avoid',
      });

      // If last page, add totals summary here
      if (page === totalPages - 1) {
        // Calculate totals for numeric fields
        const sumFields = [
          'totalCash', 'totalMpesa', 'equitel', 'familyBank', 'visaCard',
          'otherMobileMoney', 'totalDebts', 'discount',
          'paidDebts', 'totalExpenses', 'expectedCash', 'fuelConsumed',
          'shortOrLoss'
        ];

        const totals = {};
        for (const key of sumFields) {
          totals[key] = entries.reduce((sum, e) => sum + (parseFloat(e[key]) || 0), 0);
        }

        // Add some vertical spacing after the last table
        const startY = doc.lastAutoTable.finalY + 20;
        doc.setFontSize(12);
        doc.setTextColor(41, 128, 185);
        doc.text('Sales Entries Totals Summary', 40, startY);

        doc.setFontSize(10);
        doc.setTextColor(0);

        const lineHeight = 15;
        let currentY = startY + 20;

        // Format mapping to nicer labels for totals
        const labelsMap = {
          totalCash: 'Total Cash',
          totalMpesa: 'Total MPesa',
          equitel: 'Total Equitel',
          familyBank: 'Total Family Bank',
          visaCard: 'Total Visa Card',
          otherMobileMoney: 'Total Coins',
          totalDebts: 'Total Debts',
          discount: 'Total Discount',
          paidDebts: 'Total Paid Debts',
          totalExpenses: 'Total Expenses',
          expectedCash: 'Total Expected Cash',
          fuelConsumed: 'Total Fuel Consumed',
          shortOrLoss: 'Total Short or Loss'

        };

        // Show all totals, two columns max (optional)
        const keys = Object.keys(totals);
        for (let i = 0; i < keys.length; i++) {
          const key = keys[i];
          const label = labelsMap[key] || key;
          const val = formatAmount(totals[key]);
          const x = 40 + (i % 2) * 250;
          if (i % 2 === 0 && i !== 0) currentY += lineHeight;
          doc.text(`${label}: KES ${val}`, x, currentY);
        }
      }
    }

   if (activeDebts.length || paidTodayDebts.length) {
     doc.addPage();
     doc.setFontSize(14);
     doc.text('Debt Summary', 40, 40);

     const today = new Date().toISOString().split("T")[0]; // ✅ FIXED: define today correctly

     const formatDate = (dateStr) => {
       if (!dateStr) return '—';
       return new Date(dateStr).toLocaleString();
     };

     // === Filter paidTodayDebts to today's date only ===
     const paidTodayFiltered = paidTodayDebts.filter(d => {
       if (!d.paidAt) return false;
       const paidDate = new Date(d.paidAt).toISOString().split("T")[0];
       return paidDate === today;
     });

     console.log("paidTodayFiltered", paidTodayFiltered); // ✅ DEBUGGING LOG

     const totalPaidToday = paidTodayFiltered.reduce((sum, d) => sum + parseFloat(d.amountPaid || 0), 0);
     const totalFullPaidToday = paidTodayFiltered
       .filter(d => parseFloat(d.amountPaid || 0) >= parseFloat(d.amountOwed || 0))
       .reduce((sum, d) => sum + parseFloat(d.amountPaid || 0), 0);
     const totalPartialPaidToday = totalPaidToday - totalFullPaidToday;

     const fullPaidToday = paidTodayFiltered.filter(d => parseFloat(d.amountPaid || 0) >= parseFloat(d.amountOwed || 0));
     const partialPaidToday = paidTodayFiltered.filter(d => parseFloat(d.amountPaid || 0) < parseFloat(d.amountOwed || 0));

     console.log("partialPaidToday", partialPaidToday); // ✅ DEBUGGING LOG
     console.log("fullPaidToday", fullPaidToday);       // ✅ DEBUGGING LOG

     const totalActivePaid = activeDebts.reduce((sum, d) => sum + parseFloat(d.amountPaid || 0), 0);
     const totalPaid = totalActivePaid + totalPartialPaidToday + totalFullPaidToday;

     const totalActiveOwed = activeDebts.reduce((sum, d) => sum + parseFloat(d.amountOwed || 0), 0);
     const totalFullPaidOwedToday = fullPaidToday.reduce((sum, d) => sum + parseFloat(d.amountOwed || 0), 0);

     const totalOwed = totalActiveOwed + totalFullPaidOwedToday;
     const totalUnpaid = totalOwed - totalPaid;

     // === ACTIVE DEBTS TABLE ===
     if (activeDebts.length) {
       doc.setFontSize(12);
       doc.text('Unpaid and Partially Paid Debts', 40, 60);
       autoTable(doc, {
         startY: 70,
         head: [['Debtor', 'Phone', 'Attendant ID', 'Owed', 'Paid', 'Remaining', 'Created At', 'Paid At', 'Payment Mode']],
         body: activeDebts.map(d => [
           d.debtorName || '—',
           d.phone || '—',
          getAttendantName(d.attendantId),
           `KES ${formatAmount(d.amountOwed)}`,
           `KES ${formatAmount(d.amountPaid || 0)}`,
           `KES ${formatAmount((d.amountOwed || 0) - (d.amountPaid || 0))}`,
           formatDate(d.createdAt),
           formatDate(d.paidAt),
           d.lastPaymentMode || '—'
         ]),
         styles: { fontSize: 9 }
       });
     }

     // === FULLY PAID TODAY TABLE ===
     if (fullPaidToday.length) {
       const y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 20 : 100;
       doc.setFontSize(12);
       doc.text("Debts Fully Paid Today", 40, y);
       autoTable(doc, {
         startY: y + 10,
         head: [['Debtor', 'Phone', 'Attendant ID', 'Owed', 'Paid', 'Remaining', 'Created At', 'Paid At', 'Payment Mode']],
         body: fullPaidToday.map(d => [
           d.debtorName || '—',
           d.phone || '—',
          getAttendantName(d.attendantId),
           `KES ${formatAmount(d.amountOwed)}`,
           `KES ${formatAmount(d.amountPaid)}`,
           `KES ${formatAmount((d.amountOwed || 0) - (d.amountPaid || 0))}`,
           formatDate(d.createdAt),
           formatDate(d.paidAt),
           d.lastPaymentMode || '—'
         ]),
         styles: { fontSize: 9 }
       });
     }

     // === PARTIALLY PAID TODAY TABLE ===
     if (partialPaidToday.length) {
      const y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 5 : 60;
       doc.setFontSize(12);
       doc.text("Debts Partially Paid Today", 40, y);
       autoTable(doc, {
         startY: y + 10,
         head: [['Debtor', 'Phone', 'Attendant ID', 'Owed', 'Paid', 'Remaining', 'Created At', 'Paid At', 'Payment Mode']],
         body: partialPaidToday.map(d => [
           d.debtorName || '—',
           d.phone || '—',
           getAttendantName(d.attendantId),
           `KES ${formatAmount(d.amountOwed)}`,
           `KES ${formatAmount(d.amountPaid)}`,
           `KES ${formatAmount((d.amountOwed || 0) - (d.amountPaid || 0))}`,
           formatDate(d.createdAt),
           formatDate(d.paidAt),
           d.lastPaymentMode || '—'
         ]),
         styles: { fontSize: 9 }
       });
     }

     // === TOTALS SECTION ===
     const totalY = doc.lastAutoTable ? doc.lastAutoTable.finalY + 20 : 150;
     doc.setFontSize(11);
     doc.text(`Total Owed: KES ${formatAmount(totalOwed)}`, 40, totalY);
     doc.text(`Total Paid: KES ${formatAmount(totalPaid)}`, 40, totalY + 15);
     doc.text(`Total Unpaid: KES ${formatAmount(totalUnpaid)}`, 40, totalY + 30);
     doc.text(`Today's Full Paid Total: KES ${formatAmount(totalFullPaidToday)}`, 40, totalY + 45);
    }
 // === FETCH DRAFT EXPENSES ===
 try {
   const res3 = await api.get('/expenses/unsubmitted', {
     headers: { Authorization: `Bearer ${token}` },
     params: { branchId },
   });
   draftExpenses = res3.data;  // use the already-declared variable
 } catch (err) {
   console.error('❌ Error fetching draft expenses:', err);
 }

    // === DRAFT EXPENSES SUMMARY ===
    if (draftExpenses.length) {
      doc.addPage();
      doc.setFontSize(14);
      doc.text('Expenses Summary', 40, 40);

      const expenseTableBody = draftExpenses.map(exp => [
        exp.type || '—',
        `KES ${formatAmount(exp.amount)}`,
        exp.date ? new Date(exp.date).toLocaleDateString() : '—',
        exp.status || '—'
      ]);

      autoTable(doc, {
        startY: 60,
        head: [['Type', 'Amount', 'Date', 'Status']],
        body: expenseTableBody,
        theme: 'striped',
        styles: {
          fontSize: 7,
          cellPadding: 2,
          overflow: 'linebreak',
          valign: 'top',
          halign: 'left',
          lineWidth: 0.1,
          lineColor: [200, 200, 200],
        },
        headStyles: {
          fillColor: [41, 128, 185],
          textColor: 255,
          fontStyle: 'bold',
          halign: 'center',
          fontSize: 10,
          lineWidth: 0.2,
          lineColor: [180, 180, 180],
        },
        columnStyles: {
          0: { cellWidth: 120 },
          1: { cellWidth: 80 },
          2: { cellWidth: 80 },
          3: { cellWidth: 80 },
        },
        tableWidth: 'auto',
        pageBreak: 'avoid',
      });
  // === TOTAL EXPENSES ===
  const totalExpenses = draftExpenses.reduce((sum, exp) => sum + parseFloat(exp.amount || 0), 0);
  const yAfterTable = doc.lastAutoTable.finalY + 20;
  doc.setFontSize(10);
  doc.setTextColor(0);
  doc.text(`Total Expenses: KES ${formatAmount(totalExpenses)}`, 40, yAfterTable);

    }


    doc.save('sadop_sales_summarry.pdf');

  } catch (err) {
    console.error('❌ PDF Generation Error:', err);
    alert('Error generating PDF. See console for details.');
  }
};




const handleSubmit = async () => {
  const drafts = [...draftEntries, ...newEntries].filter(
    (e) => e.status === 'draft' && e.isSaved
  );

  if (drafts.length === 0) {
    alert('No saved draft entries to submit.');
    return;
  }

  const token = localStorage.getItem('token');
  const managerId = localStorage.getItem('managerId');
  const branchId = localStorage.getItem('branchId');

  try {
    // ✅ Submit drafts
    const response = await api.post(
      '/sales-entries/submit',
      { entries: drafts, managerId: managerId || null, branchId },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Branch-Id': branchId,
        },
      }
    );

    if (response.status === 200) {
      alert('All draft entries successfully submitted!');

      // Update status locally
      const updateStatus = (entries) =>
        entries.map((e) =>
          e.status === 'draft' && e.isSaved ? { ...e, status: 'submitted' } : e
        );

      setDraftEntries((prev) => updateStatus(prev));
      setNewEntries((prev) => updateStatus(prev));
    } else {
      alert('Something went wrong during submission.');
      return; // Stop further actions if status isn't 200
    }
  } catch (error) {
    console.error('❌ Submit failed:', error);
    alert('Failed to submit entries.');
    return; // Don’t continue to post-submission if this fails
  }

  // ✅ Post-submission refresh handled separately
  try {
    await fetchSalesEntries();
  } catch (postError) {
    console.warn('⚠️ Post-submit fetch failed:', postError);
    // You can toast a warning here instead if you like
  }
};


  const handleRestockChange = (field, value) => {
    setRestock({ ...restock, [field]: value });
  };

  const handleRestockSubmit = async () => {
    try {
      const token = localStorage.getItem('token');
      const branchId = localStorage.getItem('branchId'); // ✅ Declare once outside
      const now = new Date().toISOString();
      const requests = [];

      if (restock.petrolRestocked && parseFloat(restock.petrolRestocked) > 0) {
        requests.push(
          api.post('/fuel-restock/save', {
            fuelType: 'Petrol',
            quantity: parseFloat(restock.petrolRestocked),
            supplier: restock.driverName || '',
            deliveryNote: 'Restock via frontend',
            restockDate: now,
          }, { headers: { Authorization: `Bearer ${token}`, 'X-Branch-Id': branchId } })
        );
      }

      if (restock.dieselRestocked && parseFloat(restock.dieselRestocked) > 0) {
        requests.push(
          api.post('/fuel-restock/save', {
            fuelType: 'Diesel',
            quantity: parseFloat(restock.dieselRestocked),
            supplier: restock.driverName || '',
            deliveryNote: 'Restock via frontend',
            restockDate: now,
          }, { headers: { Authorization: `Bearer ${token}`, 'X-Branch-Id': branchId } })
        );
      }

      await Promise.all(requests);


      alert('Restock(s) saved.');
      setRestock({ driverName: '', petrolRestocked: '', dieselRestocked: '' });
      setRestockTime(new Date().toLocaleString());

      const refreshed = await api.get('/fuel-tanks/total', {
        headers: {
          Authorization: `Bearer ${token}`,
          'X-Branch-Id': branchId,
        },
        params: { branchId }, // 👈 This is REQUIRED by backend controller
      });


      setTankLevels(refreshed.data);
    } catch (err) {
      console.error('Restock error', err);
      alert('Failed to save restock.');
    }
  };

  const [showTankLevels, setShowTankLevels] = useState(false);




  const renderRow = (entry, index, isNew = false) => {
    // Disable inputs if saved or status not draft
    const editable = !entry.isSaved && entry.status === 'draft';

    return (
      <tr key={`${isNew ? 'new' : 'draft'}-${index}`}>
       <td>
         <input
           type="date"
           value={
             entry.salesDate
               ? entry.salesDate
               : new Date().toISOString().split('T')[0] // default to today's date
           }
           onChange={(e) => {
             const newDate = e.target.value || new Date().toISOString().split('T')[0];
             handleChange(index, 'salesDate', newDate); // update state only, no auto-save
           }}
           disabled={!editable} // only editable in draft mode
         />
       </td>


        <td>
          <select
            value={entry.attendantId}
            onChange={(e) => handleChange(index, 'attendantId', e.target.value)}
          >
            <option value="">Select Attendant</option>
            {attendants.map((att) => (
              <option key={att.id} value={att.id}>
                {att.id} - {att.fullName}
              </option>
            ))}
          </select>
        </td>

        <td>
          <select
            value={entry.pumpName}
            onChange={(e) => handlePumpChange(index, e.target.value)}
            disabled={!editable}
          >
            <option value="">Select</option>
            {pumps.map((p) => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>
        </td>
        <td><input type="number" value={entry.openingBalance} disabled /></td>
        <td><input type="number" value={entry.closingBalance} onChange={(e) => handleChange(index, 'closingBalance', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.pricePerLitre} onChange={(e) => handleChange(index, 'pricePerLitre', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.totalCash} onChange={(e) => handleChange(index, 'totalCash', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.totalMpesa} onChange={(e) => handleChange(index, 'totalMpesa', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.equitel} onChange={(e) => handleChange(index, 'equitel', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.familyBank} onChange={(e) => handleChange(index, 'familyBank', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.visaCard} onChange={(e) => handleChange(index, 'visaCard', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.otherMobileMoney} onChange={(e) => handleChange(index, 'otherMobileMoney', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.totalDebts} onChange={(e) => handleChange(index, 'totalDebts', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.discount} onChange={(e) => handleChange(index, 'discount', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.paidDebts} onChange={(e) => handleChange(index, 'paidDebts', e.target.value)} disabled={!editable} /></td>
        <td><input type="number" value={entry.totalExpenses} onChange={(e) => handleChange(index, 'totalExpenses', e.target.value)} disabled={!editable} /></td>
        <td><input disabled value={entry.expectedCash.toFixed(2)} /></td>
        <td><input disabled value={entry.fuelConsumed.toFixed(2)} /></td>
        <td><input disabled value={entry.shortOrLoss.toFixed(2)} /></td>
        <td><input disabled value={entry.entryNumber || '—'} /></td>
        <td><input disabled value={entry.submittedAt ? new Date(entry.submittedAt).toLocaleString() : '—'} /></td>
        <td><input value={entry.remarks} onChange={(e) => handleChange(index, 'remarks', e.target.value)} disabled={!editable} /></td>
        <td>
          {editable && (
            <button onClick={() => handleSave(index)} style={saveButtonStyle}>Save</button>
          )}
          {!isNew && entry.isSaved && (
            <button
              onClick={() => handleDeleteDraft(entry.id, index)}
              style={{ ...saveButtonStyle, backgroundColor: '#e74c3c', marginLeft: 5 }}
            >
              Delete
            </button>
          )}
        </td>

      </tr>
    );
  };

  return (
    <div style={modalStyle}>
      <h2>Sales Entry</h2>

      {/* Toggle button to show/hide Tank Levels */}
      <div style={{ marginBottom: '15px' }}>
        <button
          onClick={() => setShowTankLevels(!showTankLevels)}
          style={{
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            padding: '8px 14px',
            borderRadius: '5px',
            cursor: 'pointer',
            marginBottom: '10px',
          }}
        >
          {showTankLevels ? 'Hide Tank Levels' : 'Show Tank Levels'}
        </button>
      </div>

      {/* Tank Levels section - conditionally rendered */}
      {showTankLevels && (
        <div style={{ marginBottom: '20px' }}>
          <h3>Tank Levels</h3>
          <p>Petrol: {tankLevels.petrol} Litres</p>
          <p>Diesel: {tankLevels.diesel} Litres</p>
        </div>
      )}

      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={() => setShowRestock(!showRestock)}
          style={{
            backgroundColor: '#27ae60',
            color: 'white',
            border: 'none',
            padding: '8px 14px',
            borderRadius: '5px',
            cursor: 'pointer',
            marginBottom: '10px',
            marginRight: '10px',
          }}
        >
          {showRestock ? 'Hide Fuel Restock' : 'Show Fuel Restock'}
        </button>
        {showRestock && (
          <div>
            <h3>Restock Fuel</h3>
            <input
              type="text"
              placeholder="Driver Name"
              value={restock.driverName}
              onChange={(e) => handleRestockChange('driverName', e.target.value)}
              style={{ marginBottom: '8px', display: 'block', width: '100%', padding: '6px' }}
            />
            <input
              type="number"
              placeholder="Petrol Restocked (L)"
              value={restock.petrolRestocked}
              onChange={(e) => handleRestockChange('petrolRestocked', e.target.value)}
              style={{ marginBottom: '8px', display: 'block', width: '100%', padding: '6px' }}
            />
            <input
              type="number"
              placeholder="Diesel Restocked (L)"
              value={restock.dieselRestocked}
              onChange={(e) => handleRestockChange('dieselRestocked', e.target.value)}
              style={{ marginBottom: '8px', display: 'block', width: '100%', padding: '6px' }}
            />
            <button
              onClick={handleRestockSubmit}
              style={{
                backgroundColor: '#2980b9',
                color: 'white',
                border: 'none',
                padding: '8px 14px',
                borderRadius: '5px',
                cursor: 'pointer',
                marginTop: '10px',
              }}
            >
              Save Restock
            </button>
            {restockTime && <p style={{ marginTop: '8px' }}>Restocked At: {restockTime}</p>}
          </div>
        )}
      </div>

      <div>
        <button
          onClick={() => setShowRecentRestocks(!showRecentRestocks)}
          style={{
            backgroundColor: '#8e44ad',
            color: 'white',
            border: 'none',
            padding: '8px 14px',
            borderRadius: '5px',
            cursor: 'pointer',
            marginBottom: '10px',
          }}
        >
          {showRecentRestocks ? 'Hide Recent Restocks' : 'View Recent Restocks'}
        </button>
        {showRecentRestocks && (
          <div>
            <h3>Recent Fuel Restocks</h3>
            <table style={tableStyle}>
              <thead>
                <tr>
                  <th>Fuel Type</th>
                  <th>Quantity (L)</th>
                  <th>Supplier</th>
                  <th>Restock Date</th>
                </tr>
              </thead>
              <tbody>
                {recentRestocks.map((r, i) => (
                  <tr key={i}>
                    <td>{r.fuelType}</td>
                    <td>{r.quantity}</td>
                    <td>{r.supplier}</td>
                    <td>{new Date(r.restockDate).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>



      <div style={{ overflowY: 'scroll', height: '50vh' }}>
        <table className="compact-table" style={tableStyle}>
          <thead>
            <tr>
              <th>Date</th>
              <th>Attendant</th>
              <th>Pump</th>
              <th>Opening</th>
              <th>Closing</th>
              <th>Price</th>
              <th>Cash</th>
              <th>MPesa</th>
              <th>Equitel</th>
              <th>Family Bank</th>
              <th>Visa Card</th>
              <th>Coins</th>
              <th>Debts</th>
              <th>Discount</th>
              <th>Paid Debts</th>
              <th>Total Expenses</th>
              <th>Expected</th>
              <th>Fuel Consumed</th>
              <th>Short/Loss</th>
              <th>Entry#</th>
              <th>Submitted At</th>
              <th>Remarks</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {draftEntries.map((entry, index) => renderRow(entry, index, false))}
            {newEntries.map((entry, index) => renderRow(entry, index, true, true))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan="6"><strong>Totals</strong></td>
              <td>{totals.totalCash.toFixed(2)}</td>
              <td>{totals.totalMpesa.toFixed(2)}</td>
              <td>{totals.totalEquitel.toFixed(2)}</td>
              <td>{totals.totalFamilyBank.toFixed(2)}</td>
              <td>{totals.totalVisaCard.toFixed(2)}</td>
              <td>{totals.totalOtherMobile.toFixed(2)}</td>
              <td>{totals.totalDebts.toFixed(2)}</td>
              <td>{totals.totalDiscount.toFixed(2)}</td>
              <td>{totals.totalPaidDebts.toFixed(2)}</td>
              <td>{totals.totalExpenses.toFixed(2)}</td>
              <td>{totals.totalExpected.toFixed(2)}</td>
              <td>
                Diesel1/2: {totals.dieselConsumed.toFixed(2)} <br />
                Petrol1/2: {totals.petrolConsumed.toFixed(2)}
              </td>
              <td>{totals.totalShortLoss.toFixed(2)}</td>

              <td colSpan="4"></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <div style={footerStyle}>
        <button onClick={onClose} style={{ background: '#e74c3c', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 5, marginRight: 10, cursor: 'pointer' }}>Cancel</button>
        <button onClick={handleSubmit} style={{ background: '#2ecc71', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 5, marginRight: 10, cursor: 'pointer' }}>Submit</button>
        <button onClick={handleDownloadPDF} style={{ background: '#f39c12', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 5, marginRight: 10, marginTop: 20, cursor: 'pointer' }}>Generate Total Sales Receipt</button>
        <button onClick={handleDownloadSalesPDF} style={{ background: '#3498db', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: 5, cursor: 'pointer' }}>Download Sales Data</button>
      </div>


    </div>
  );
};

const getBlankEntry = (salesDate) => ({
  salesDate,
  pumpName: '',
  openingBalance: '',
  closingBalance: '',
  attendantId: '',
  totalCash: '',
  totalMpesa: '',
  totalDebts: '',
  paidDebts: '',
  totalExpenses: '',
  equitel: '',
  familyBank: '',
  visaCard: '',
  otherMobileMoney: '',
  creditSales: '',
  pricePerLitre: '',
  fuelConsumed: 0,
  expectedCash: 0,

  shortOrLoss: 0,
  remarks: '',
  isSaved: false,
  submittedAt: null,
  status: 'draft',
});

const modalStyle = {
  position: 'fixed',
  top: '5%',
  left: '5%',
  right: '5%',
  bottom: '5%',
  backgroundColor: 'white',
  padding: '20px',
  borderRadius: '8px',
  boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
  overflow: 'auto',
};

const tableStyle = {
  width: '100%',
  borderCollapse: 'collapse',
};

const saveButtonStyle = {
  backgroundColor: '#4CAF50',
  color: 'white',
  padding: '5px 10px',
  border: 'none',
  cursor: 'pointer',
};

const footerStyle = {
  textAlign: 'right',
  marginTop: '20px',
};


export default SalesEntryForm;