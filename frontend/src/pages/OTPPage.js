import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifyOTP } from '../services/api';
import '../styles/OTPPage.css';

const OTPPage = ({ onSuccess }) => {
  const [otp, setOtp] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isNewUser, setIsNewUser] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const tempPhone = localStorage.getItem('tempPhoneNumber');
    if (!tempPhone) {
      navigate('/login');
    } else {
      setPhoneNumber(tempPhone);
      setIsNewUser(localStorage.getItem('isNewUser') === 'true');
    }
  }, [navigate]);

  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!otp || otp.length !== 6) {
        setError('Please enter a valid 6-digit OTP');
        setLoading(false);
        return;
      }

      const response = await verifyOTP(
        phoneNumber,
        otp,
        isNewUser ? firstName || null : null,
        isNewUser ? lastName || null : null
      );
      
      if (response.data && response.data.id) {
        onSuccess(response.data.id, phoneNumber, response.data.token);
        localStorage.removeItem('tempPhoneNumber');
        navigate('/dashboard');
      } else {
        setError('Invalid OTP. Please try again.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to verify OTP');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="otp-container">
      <div className="otp-box">
        <h1>Verify OTP</h1>
        <p className="subtitle">Enter the OTP sent to {phoneNumber}</p>
        
        <form onSubmit={handleVerifyOTP}>
          <div className="form-group">
            <label>One-Time Password</label>
            <input
              type="text"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              placeholder="Enter 6-digit OTP"
              maxLength="6"
              disabled={loading}
            />
          </div>

          {isNewUser && (
            <>
              <div className="form-group">
                <label>First Name (Optional)</label>
                <input
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  placeholder="Your first name"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label>Last Name (Optional)</label>
                <input
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  placeholder="Your last name"
                  disabled={loading}
                />
              </div>
            </>
          )}

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Verifying...' : 'Verify & Login'}
          </button>
        </form>

        <button
          type="button"
          onClick={() => navigate('/login')}
          className="btn-secondary"
        >
          Back to Login
        </button>
      </div>
    </div>
  );
};

export default OTPPage;
