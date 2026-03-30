import React from "react";
import jsPDF from "jspdf";

const SalesReceipt = ({ totals, tankLevels, branchName }) => {

  const formatAmount = (value) => {
    return new Intl.NumberFormat("en-KE", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(parseFloat(value || 0));
  };

  const handleDownloadPDF = () => {
    if (!totals || !tankLevels) {
      alert("No receipt data available.");
      return;
    }

    const fields = [
      ["Total Expected", formatAmount(totals.totalExpected)],
      ["Total Cash", formatAmount(totals.totalCash)],
      ["Total MPesa", formatAmount(totals.totalMpesa)],
      ["Equitel", formatAmount(totals.totalEquitel)],
      ["Family Bank", formatAmount(totals.totalFamilyBank)],
      ["Visa Card", formatAmount(totals.totalVisaCard)],
      ["Coins", formatAmount(totals.totalOtherMobile)],
      ["Paid Debts", formatAmount(totals.totalPaidDebts)],
      ["Total Today`s Debts", formatAmount(totals.totalDebts)],
      ["Total Discounts", formatAmount(totals.totalDiscount)],
      ["Total Expenses", formatAmount(totals.totalExpenses)],
      ["Total Shorts/Loss", formatAmount(totals.totalShortLoss)],
      ["Total Diesel Used", formatAmount(totals.dieselConsumed)],
      ["Current Diesel Level (L)", formatAmount(tankLevels.diesel)],
      ["Total Petrol Used", formatAmount(totals.petrolConsumed)],
      ["Current Petrol Level (L)", formatAmount(tankLevels.petrol)],
    ];

    const height = 10 + fields.length * 6 + 40;

    const doc = new jsPDF({
      orientation: "portrait",
      unit: "mm",
      format: [80, height],
    });

    doc.setFontSize(11);
    doc.setFont("helvetica", "bold");
    doc.text("Sadop Energy Management Sales Receipt", 40, 10, { align: "center" });

    doc.setFontSize(9);
    doc.text(branchName?.toUpperCase() || "BRANCH", 40, 15, { align: "center" });

    let y = 25;

    fields.forEach(([label, value]) => {
      doc.text(label, 10, y);
      doc.text(value, 70, y, { align: "right" });
      y += 6;
    });

    doc.save("sadop_sales_receipt.pdf");
  };

  return (
    <button
      onClick={handleDownloadPDF}
      style={{
        backgroundColor: "#2ecc71",
        color: "white",
        border: "none",
        padding: "12px 20px",
        borderRadius: "12px",
        cursor: "pointer",
        width: "100%",
        marginBottom: "12px",
        fontSize: "16px",
        fontWeight: "500",
      }}
    >
      🧾 Generate Sales Receipt
    </button>
  );
};

export default SalesReceipt;