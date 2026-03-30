import React from "react";
import { Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import ManagerDashboard from "./pages/ManagerDashboard";
import CEODashboard from "./pages/CEODashboard"; // ✅ corrected import
import WorkerDashboard from "./pages/WorkerDashboard";
import Unauthorized from "./pages/Unauthorized";
import PrivateRoute from "./components/PrivateRoute";
import LandingPage from "./pages/LandingPage";
import InitialPumpSetup from "./components/InitialPumpSetup";
import DebtForm from "./components/DebtForm";
import ShortsManager from "./components/ShortsManager";
import SubmittedSalesPage from './components/SubmittedSalesPage'; // Adjust path


function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* CEO Dashboard */}
      <Route
        path="/ceo-dashboard"
        element={
          <PrivateRoute allowedRoles={["ROLE_CEO"]}>
            <CEODashboard /> {/* ✅ fixed component */}
          </PrivateRoute>
        }
      />

      {/* Manager Dashboard (CEO can also access) */}
      <Route
        path="/manager-dashboard"
        element={
          <PrivateRoute allowedRoles={["ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO"]}>
            <ManagerDashboard />
          </PrivateRoute>
        }
      />

      {/* Attendant Dashboard */}
      <Route
        path="/worker-dashboard"
        element={
          <PrivateRoute allowedRoles={["ROLE_ATTENDANT"]}>
            <WorkerDashboard />
          </PrivateRoute>
        }
      />

      {/* Pump Setup */}
      <Route
        path="/initial-setup"
        element={
          <PrivateRoute allowedRoles={["ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO"]}>
            <InitialPumpSetup />
          </PrivateRoute>
        }
      />

      {/* Debts */}
      <Route
        path="/debts"
        element={
          <PrivateRoute allowedRoles={["ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO"]}>
            <DebtForm />
          </PrivateRoute>
        }
      />

      {/* Shorts Management */}
      <Route
        path="/shorts"
        element={
          <PrivateRoute allowedRoles={["ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO"]}>
            <ShortsManager />
          </PrivateRoute>
        }
      />
      <Route path="/submitted-sales" element={<SubmittedSalesPage />} />
    </Routes>

  );
}

export default App;
