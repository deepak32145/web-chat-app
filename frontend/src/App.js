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

  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUserId = localStorage.getItem('userId');
    const storedPhone = localStorage.getItem('phoneNumber');
    
    if (token && storedUserId) {
      setIsAuthenticated(true);
      setUserId(storedUserId);
      setUserPhone(storedPhone);
    }
  }, []);

  const handleLoginSuccess = (userId, phoneNumber, token) => {
    setIsAuthenticated(true);
    setUserId(userId);
    setUserPhone(phoneNumber);
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
    localStorage.setItem('phoneNumber', phoneNumber);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setUserId(null);
    setUserPhone(null);
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('phoneNumber');
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
          element={isAuthenticated ? <DashboardPage userId={userId} userPhone={userPhone} onLogout={handleLogout} /> : <Navigate to="/login" />} 
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
