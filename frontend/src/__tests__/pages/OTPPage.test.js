import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import OTPPage from '../../pages/OTPPage';

jest.mock('../../styles/OTPPage.css', () => ({}));
jest.mock('../../services/api', () => ({
  verifyOTP: jest.fn(),
}));

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

import { verifyOTP } from '../../services/api';

const renderOTPPage = (onSuccess = jest.fn()) =>
  render(
    <MemoryRouter>
      <OTPPage onSuccess={onSuccess} />
    </MemoryRouter>
  );

describe('OTPPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.setItem('tempPhoneNumber', '+911234567890');
    localStorage.setItem('isNewUser', 'false');
  });

  afterEach(() => {
    localStorage.clear();
  });

  test('renders OTP input and verify button', () => {
    renderOTPPage();
    expect(screen.getByPlaceholderText('Enter 6-digit OTP')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /verify & login/i })).toBeInTheDocument();
  });

  test('displays phone number from localStorage', () => {
    renderOTPPage();
    expect(screen.getByText(/\+911234567890/)).toBeInTheDocument();
  });

  test('redirects to login when no phone in localStorage', () => {
    localStorage.removeItem('tempPhoneNumber');
    renderOTPPage();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('shows validation error for OTP shorter than 6 digits', async () => {
    renderOTPPage();

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '123');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(screen.getByText(/please enter a valid 6-digit otp/i)).toBeInTheDocument();
    });
    expect(verifyOTP).not.toHaveBeenCalled();
  });

  test('calls verifyOTP with correct args on valid submit', async () => {
    verifyOTP.mockResolvedValue({ data: { id: 1, token: 'tok', firstName: 'John', lastName: 'Doe' } });
    const onSuccess = jest.fn();
    renderOTPPage(onSuccess);

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '123456');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(verifyOTP).toHaveBeenCalledWith('+911234567890', '123456', null, null);
    });
  });

  test('calls onSuccess and navigates to dashboard on valid OTP', async () => {
    verifyOTP.mockResolvedValue({ data: { id: 1, token: 'tok', firstName: 'John', lastName: 'Doe' } });
    const onSuccess = jest.fn();
    renderOTPPage(onSuccess);

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '123456');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith(1, '+911234567890', 'tok', 'John', 'Doe');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  test('shows error on invalid OTP response', async () => {
    verifyOTP.mockResolvedValue({ data: {} });
    renderOTPPage();

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '000000');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid otp/i)).toBeInTheDocument();
    });
  });

  test('shows error message on API failure', async () => {
    verifyOTP.mockRejectedValue({ response: { data: { message: 'OTP expired' } } });
    renderOTPPage();

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '123456');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(screen.getByText('OTP expired')).toBeInTheDocument();
    });
  });

  test('shows name fields for new users', () => {
    localStorage.setItem('isNewUser', 'true');
    renderOTPPage();

    expect(screen.getByPlaceholderText('Your first name')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Your last name')).toBeInTheDocument();
  });

  test('does not show name fields for existing users', () => {
    localStorage.setItem('isNewUser', 'false');
    renderOTPPage();

    expect(screen.queryByPlaceholderText('Your first name')).not.toBeInTheDocument();
  });

  test('passes firstName and lastName for new user', async () => {
    localStorage.setItem('isNewUser', 'true');
    verifyOTP.mockResolvedValue({ data: { id: 1, token: 'tok', firstName: 'Alice', lastName: 'W' } });
    renderOTPPage();

    await userEvent.type(screen.getByPlaceholderText('Enter 6-digit OTP'), '123456');
    await userEvent.type(screen.getByPlaceholderText('Your first name'), 'Alice');
    await userEvent.type(screen.getByPlaceholderText('Your last name'), 'Wonder');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    await waitFor(() => {
      expect(verifyOTP).toHaveBeenCalledWith('+911234567890', '123456', 'Alice', 'Wonder');
    });
  });

  test('back to login button navigates to /login', () => {
    renderOTPPage();
    fireEvent.click(screen.getByRole('button', { name: /back to login/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('OTP input only accepts digits', async () => {
    renderOTPPage();
    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, 'abc123def456');
    expect(input.value).toBe('123456');
  });

  test('shows loading state during verification', async () => {
    verifyOTP.mockImplementation(() => new Promise(() => {}));
    renderOTPPage();

    const input = screen.getByPlaceholderText('Enter 6-digit OTP');
    await userEvent.type(input, '123456');
    fireEvent.click(screen.getByRole('button', { name: /verify & login/i }));

    expect(screen.getByRole('button', { name: /verifying/i })).toBeDisabled();
  });
});
