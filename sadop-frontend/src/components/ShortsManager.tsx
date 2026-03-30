import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "./ShortsManager.css";

const ShortsManager = () => {
  const [shorts, setShorts] = useState([]);
  const [showShortsTable, setShowShortsTable] = useState(false);

  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const branchId = localStorage.getItem("branchId");

  const axiosConfig = {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Branch-Id": branchId, // Required by backend
    },
  };

  // Fetch all unsubmitted shorts
  const fetchShorts = async () => {
    try {
      const res = await axios.get("/api/shorts/all", axiosConfig);
      setShorts(res.data);
    } catch (err) {
      alert("Error fetching shorts");
    }
  };

  // Total short calculation
  const getTotalShort = () =>
    shorts.reduce((sum, s) => sum + parseFloat(s.totalShort), 0).toFixed(2);

  useEffect(() => {
    fetchShorts();
  }, []);

  return (
    <div className="shorts-container">
      <h2 className="shorts-heading">Manage Shorts</h2>

      <div className="button-group">
        <button
          onClick={() => setShowShortsTable(!showShortsTable)}
          className="btn btn-yellow"
        >
          {showShortsTable ? "Hide Current Shorts" : "Show Current Shorts"}
        </button>

        <button
          onClick={() => navigate("/manager-dashboard")}
          className="btn btn-gray"
        >
          Back to Dashboard
        </button>
      </div>

      {/* Shorts Table */}
      {showShortsTable && (
        <div className="bg-white p-6 rounded-2xl shadow-md mt-6">
          <div className="flex justify-between mb-4">
            <h3 className="text-xl font-semibold text-gray-800">🧾 Current Shorts</h3>
            <div className="text-lg font-bold text-red-600">
              Total: Ksh {getTotalShort()}
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full border text-sm rounded-lg overflow-hidden">
              <thead className="bg-gray-100 text-gray-700">
                <tr>
                  <th className="px-4 py-2 border">Attendant</th>
                  <th className="px-4 py-2 border">Total Short</th>
                  <th className="px-4 py-2 border">Last Action</th>
                  <th className="px-4 py-2 border">Last Updated</th>
                </tr>
              </thead>
              <tbody>
                {shorts.map((entry, index) => (
                  <tr
                    key={entry.attendantId}
                    className={index % 2 === 0 ? "bg-white" : "bg-gray-50"}
                  >
                    <td className="px-4 py-2 border">{entry.attendantName}</td>
                    <td className="px-4 py-2 border text-red-600 font-medium">
                      Ksh {Number(entry.totalShort).toLocaleString()}
                    </td>
                    <td className="px-4 py-2 border">
                      <span
                        className={`inline-block px-2 py-1 rounded text-xs font-semibold ${
                          entry.lastAction === "ADD"
                            ? "bg-yellow-100 text-yellow-700"
                            : "bg-green-100 text-green-700"
                        }`}
                      >
                        {entry.lastAction === "ADD" ? "Added" : "Repaid"}
                      </span>
                    </td>
                    <td className="px-4 py-2 border text-gray-600">
                      {new Date(entry.lastUpdated).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default ShortsManager;