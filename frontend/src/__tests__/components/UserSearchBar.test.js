import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UserSearchBar from '../../components/UserSearchBar';

jest.mock('../../styles/UserSearchBar.css', () => ({}));

describe('UserSearchBar', () => {
  test('renders phone number input and search button', () => {
    render(<UserSearchBar onSearch={jest.fn()} />);
    expect(screen.getByPlaceholderText('Search by phone number')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  test('calls onSearch with entered phone number on submit', async () => {
    const onSearch = jest.fn();
    render(<UserSearchBar onSearch={onSearch} />);

    const input = screen.getByPlaceholderText('Search by phone number');
    await userEvent.type(input, '+911234567890');
    fireEvent.submit(input.closest('form'));

    expect(onSearch).toHaveBeenCalledWith('+911234567890');
  });

  test('clears input after successful search', async () => {
    render(<UserSearchBar onSearch={jest.fn()} />);
    const input = screen.getByPlaceholderText('Search by phone number');

    await userEvent.type(input, '+911234567890');
    fireEvent.submit(input.closest('form'));

    expect(input.value).toBe('');
  });

  test('does not call onSearch when input is empty', () => {
    const onSearch = jest.fn();
    render(<UserSearchBar onSearch={onSearch} />);

    fireEvent.submit(screen.getByPlaceholderText('Search by phone number').closest('form'));

    expect(onSearch).not.toHaveBeenCalled();
  });

  test('does not call onSearch when input is only whitespace', async () => {
    const onSearch = jest.fn();
    render(<UserSearchBar onSearch={onSearch} />);

    const input = screen.getByPlaceholderText('Search by phone number');
    await userEvent.type(input, '   ');
    fireEvent.submit(input.closest('form'));

    expect(onSearch).not.toHaveBeenCalled();
  });

  test('updates input value as user types', async () => {
    render(<UserSearchBar onSearch={jest.fn()} />);
    const input = screen.getByPlaceholderText('Search by phone number');

    await userEvent.type(input, '+91123');

    expect(input.value).toBe('+91123');
  });
});
