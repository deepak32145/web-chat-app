import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MessageInput from '../../components/MessageInput';

jest.mock('../../styles/MessageInput.css', () => ({}));
jest.mock('../../services/api', () => ({
  uploadFile: jest.fn(),
}));

import { uploadFile } from '../../services/api';

describe('MessageInput', () => {
  beforeEach(() => jest.clearAllMocks());

  test('renders text input and send button', () => {
    render(<MessageInput onSendMessage={jest.fn()} />);
    expect(screen.getByPlaceholderText('Type a message...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '' })).toBeInTheDocument();
  });

  test('send button is disabled when input is empty', () => {
    render(<MessageInput onSendMessage={jest.fn()} />);
    const buttons = screen.getAllByRole('button');
    const sendBtn = buttons.find(b => b.classList.contains('send-btn'));
    expect(sendBtn).toBeDisabled();
  });

  test('send button is enabled when text is entered', async () => {
    render(<MessageInput onSendMessage={jest.fn()} />);

    const input = screen.getByPlaceholderText('Type a message...');
    await userEvent.type(input, 'Hello');

    const buttons = screen.getAllByRole('button');
    const sendBtn = buttons.find(b => b.classList.contains('send-btn'));
    expect(sendBtn).not.toBeDisabled();
  });

  test('calls onSendMessage with text content on submit', async () => {
    const onSendMessage = jest.fn().mockResolvedValue(undefined);
    render(<MessageInput onSendMessage={onSendMessage} />);

    const input = screen.getByPlaceholderText('Type a message...');
    await userEvent.type(input, 'Hello world');
    fireEvent.submit(input.closest('form'));

    await waitFor(() => {
      expect(onSendMessage).toHaveBeenCalledWith('Hello world', null, null);
    });
  });

  test('clears input after sending message', async () => {
    const onSendMessage = jest.fn().mockResolvedValue(undefined);
    render(<MessageInput onSendMessage={onSendMessage} />);

    const input = screen.getByPlaceholderText('Type a message...');
    await userEvent.type(input, 'Hello');
    fireEvent.submit(input.closest('form'));

    await waitFor(() => {
      expect(input.value).toBe('');
    });
  });

  test('does not call onSendMessage when input is empty and no file selected', () => {
    const onSendMessage = jest.fn();
    render(<MessageInput onSendMessage={onSendMessage} />);

    fireEvent.submit(screen.getByPlaceholderText('Type a message...').closest('form'));

    expect(onSendMessage).not.toHaveBeenCalled();
  });

  test('uploads file and calls onSendMessage with media url', async () => {
    uploadFile.mockResolvedValue({
      data: { url: '/files/photo.jpg', mediaType: 'image' },
    });
    const onSendMessage = jest.fn().mockResolvedValue(undefined);

    render(<MessageInput onSendMessage={onSendMessage} />);

    const file = new File(['content'], 'photo.jpg', { type: 'image/jpeg' });
    const fileInput = document.querySelector('input[type="file"]');
    await userEvent.upload(fileInput, file);

    fireEvent.submit(fileInput.closest('form'));

    await waitFor(() => {
      expect(uploadFile).toHaveBeenCalled();
      expect(onSendMessage).toHaveBeenCalledWith(
        '',
        expect.stringContaining('/files/photo.jpg'),
        'image'
      );
    });
  });

  test('shows image preview when image file is selected', async () => {
    render(<MessageInput onSendMessage={jest.fn()} />);

    const file = new File(['content'], 'photo.jpg', { type: 'image/jpeg' });
    const fileInput = document.querySelector('input[type="file"]');

    Object.defineProperty(fileInput, 'files', { value: [file] });
    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(screen.getByAltText('preview')).toBeInTheDocument();
    });
  });

  test('clear file button removes file preview', async () => {
    render(<MessageInput onSendMessage={jest.fn()} />);

    const file = new File(['data'], 'doc.txt', { type: 'text/plain' });
    const fileInput = document.querySelector('input[type="file"]');

    Object.defineProperty(fileInput, 'files', { value: [file] });
    fireEvent.change(fileInput);

    await waitFor(() => screen.getByText('doc.txt'));

    fireEvent.click(screen.getByTitle ? document.querySelector('.clear-file-btn') : screen.getByRole('button', { name: '' }));

    await waitFor(() => {
      expect(screen.queryByText('doc.txt')).not.toBeInTheDocument();
    });
  });
});
