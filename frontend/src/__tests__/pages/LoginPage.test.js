import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../../pages/LoginPage';

jest.mock('../../styles/LoginPage.css', () => ({}));
jest.mock('../../services/api', () => ({
  sendOTP: jest.fn(),
}));
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

import { sendOTP } from '../../services/api';

const renderLoginPage = () =>
  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );

describe('LoginPage', () => {
  beforeEach(() => jest.clearAllMocks());

  test('renders phone number input and send OTP button', () => {
    renderLoginPage();
    expect(screen.getByPlaceholderText('Enter your phone number')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /send otp/i })).toBeInTheDocument();
  });

  test('renders app title and subtitle', () => {
    renderLoginPage();
    expect(screen.getByText('Chat App')).toBeInTheDocument();
    expect(screen.getByText(/connect with anyone/i)).toBeInTheDocument();
  });

  test('shows validation error for short phone number', async () => {
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '12345');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(screen.getByText(/please enter a valid phone number/i)).toBeInTheDocument();
    });
    expect(sendOTP).not.toHaveBeenCalled();
  });

  test('shows validation error for empty phone number', async () => {
    renderLoginPage();

    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(screen.getByText(/please enter a valid phone number/i)).toBeInTheDocument();
    });
  });

  test('calls sendOTP with phone number on valid submit', async () => {
    sendOTP.mockResolvedValue({ data: { success: true, isNewUser: false } });
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(sendOTP).toHaveBeenCalledWith('+911234567890');
    });
  });

  test('shows loading state while sending OTP', async () => {
    sendOTP.mockImplementation(() => new Promise(() => {}));
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    expect(screen.getByRole('button', { name: /sending otp/i })).toBeDisabled();
  });

  test('shows error message when sendOTP fails', async () => {
    sendOTP.mockRejectedValue({ response: { data: { message: 'SMS service error' } } });
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(screen.getByText('SMS service error')).toBeInTheDocument();
    });
  });

  test('shows fallback error message when no response data', async () => {
    sendOTP.mockRejectedValue(new Error('Network error'));
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(screen.getByText('Failed to send OTP')).toBeInTheDocument();
    });
  });

  test('stores phone number and isNewUser in localStorage on success', async () => {
    sendOTP.mockResolvedValue({ data: { success: true, isNewUser: true } });
    renderLoginPage();

    const input = screen.getByPlaceholderText('Enter your phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.click(screen.getByRole('button', { name: /send otp/i }));

    await waitFor(() => {
      expect(localStorage.getItem('tempPhoneNumber')).toBe('+911234567890');
      expect(localStorage.getItem('isNewUser')).toBe('true');
    });
  });
});
