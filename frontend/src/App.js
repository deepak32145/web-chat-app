import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import LoginPage from './pages/LoginPage';
import OTPPage from './pages/OTPPage';
import DashboardPage from './pages/DashboardPage';
import ChatPage from './pages/ChatPage';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userId, setUserId] = useState(null);
  const [userPhone, setUserPhone] = useState(null);
  const [userName, setUserName] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUserId = localStorage.getItem('userId');
    const storedPhone = localStorage.getItem('phoneNumber');
    const storedName = localStorage.getItem('userName');

    if (token && storedUserId) {
      setIsAuthenticated(true);
      setUserId(storedUserId);
      setUserPhone(storedPhone);
      setUserName(storedName);
    }
  }, []);

  const handleLoginSuccess = (userId, phoneNumber, token, firstName, lastName) => {
    const displayName = [firstName, lastName].filter(Boolean).join(' ') || null;
    setIsAuthenticated(true);
    setUserId(userId);
    setUserPhone(phoneNumber);
    setUserName(displayName);
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
    localStorage.setItem('phoneNumber', phoneNumber);
    if (displayName) localStorage.setItem('userName', displayName);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setUserId(null);
    setUserPhone(null);
    setUserName(null);
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('phoneNumber');
    localStorage.removeItem('userName');
  };

  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />}
        />
        <Route
          path="/verify-otp"
          element={!isAuthenticated ? <OTPPage onSuccess={handleLoginSuccess} /> : <Navigate to="/dashboard" />}
        />
        <Route
          path="/dashboard"
          element={isAuthenticated ? <DashboardPage userId={userId} userPhone={userPhone} userName={userName} onLogout={handleLogout} /> : <Navigate to="/login" />}
        />
        <Route
          path="/chat/:conversationId"
          element={isAuthenticated ? <ChatPage userId={userId} /> : <Navigate to="/login" />}
        />
        <Route
          path="/"
          element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />}
        />
      </Routes>
    </Router>
  );
}

export default App;
