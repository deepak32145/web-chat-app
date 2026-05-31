import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendOTP } from '../services/api';
import '../styles/LoginPage.css';

const LoginPage = () => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSendOTP = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!phoneNumber || phoneNumber.length < 10) {
        setError('Please enter a valid phone number');
        setLoading(false);
        return;
      }

      const response = await sendOTP(phoneNumber);
      
      if (response.data.success) {
        localStorage.setItem('tempPhoneNumber', phoneNumber);
        localStorage.setItem('isNewUser', response.data.isNewUser ? 'true' : 'false');
        navigate('/verify-otp');
      } else {
        setError(response.data.message || 'Failed to send OTP');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Chat App</h1>
        <p className="subtitle">Connect with anyone, anytime</p>
        
        <form onSubmit={handleSendOTP}>
          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="Enter your phone number"
              disabled={loading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Sending OTP...' : 'Send OTP'}
          </button>
        </form>

        <p className="info-text">
          We'll send a one-time password to your phone number for verification.
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
