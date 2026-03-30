import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../axios"; // This should point to your Axios instance

const pumps = ["Diesel 1", "Diesel 2", "Super 1", "Super 2"];

export default function InitialPumpSetup() {
  const navigate = useNavigate();
  const [form, setForm] = useState(
    pumps.map((p) => ({
      pumpName: p,
      openingBalance: "",
      setupDate: "",
    }))
  );
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (index: number, field: string, value: string) => {
    const updated = [...form];
    updated[index][field] = value;
    setForm(updated);
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem("token");

      if (!token) {
        setMessage("❌ Error: User not authenticated.");
        setLoading(false);
        return;
      }

      const validEntries = form.filter(
        (entry) =>
          entry.openingBalance &&
          entry.openingBalance.trim() !== "" &&
          entry.setupDate
      );

      if (validEntries.length === 0) {
        setMessage("❌ Please fill in at least one pump balance and select a date.");
        setLoading(false);
        return;
      }

      for (const entry of validEntries) {
        await axios.post(
          "/api/initial-pump/setup",
          {
            pumpName: entry.pumpName,
            openingBalance: parseFloat(entry.openingBalance),
            setupDate: entry.setupDate,
          },
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      }

      setMessage("✅ Initial pump balances saved successfully!");
    } catch (error) {
      if (error.response && error.response.status === 401) {
        setMessage("❌ Error: Unauthorized access. Please log in again.");
      } else {
        setMessage("❌ Error: Some pumps may already have balances or other issues.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-lg mx-auto bg-white shadow-md rounded-lg">
      <h2 className="text-2xl font-bold mb-6 text-center">Initial Pump Setup</h2>

      {form.map((entry, idx) => (
        <div key={entry.pumpName} className="mb-5">
          <label className="block text-gray-700 font-medium mb-1">{entry.pumpName}</label>
          <input
            type="number"
            className="border border-gray-300 px-3 py-2 rounded w-full mb-2"
            placeholder="Enter opening balance (litres)"
            value={entry.openingBalance}
            onChange={(e) => handleChange(idx, "openingBalance", e.target.value)}
          />
          <label className="block text-gray-700 font-medium mb-1">Setup Date</label>
          <input
            type="date"
            className="border border-gray-300 px-3 py-2 rounded w-full"
            value={entry.setupDate}
            onChange={(e) => handleChange(idx, "setupDate", e.target.value)}
          />
        </div>
      ))}

      <button
        className="bg-blue-600 text-white px-4 py-2 rounded mt-3 w-full hover:bg-blue-700 transition duration-150"
        onClick={handleSubmit}
        disabled={loading}
      >
        {loading ? "Saving..." : "Save Initial Setup"}
      </button>

      <button
        className="bg-gray-600 text-white px-4 py-2 rounded mt-2 w-full hover:bg-gray-700 transition duration-150"
        onClick={() => navigate("/manager-dashboard")}
      >
        Back to Dashboard
      </button>

      {message && (
        <p className="mt-4 text-center text-sm text-gray-700">{message}</p>
      )}
    </div>
  );
}
