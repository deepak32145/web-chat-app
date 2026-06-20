import React from 'react';
import { render, screen } from '@testing-library/react';
import MessageList from '../../components/MessageList';

jest.mock('../../styles/MessageList.css', () => ({}));

const makeMessage = (overrides = {}) => ({
  id: 1,
  content: 'Hello',
  mediaUrl: null,
  mediaType: null,
  isRead: false,
  sender: { id: 2, username: 'bob' },
  createdAt: '2024-01-01T10:00:00.000Z',
  ...overrides,
});

describe('MessageList', () => {
  test('shows empty state when no messages', () => {
    render(<MessageList messages={[]} currentUserId={1} />);
    expect(screen.getByText('No messages yet')).toBeInTheDocument();
  });

  test('renders message content', () => {
    render(<MessageList messages={[makeMessage({ content: 'Hi there' })]} currentUserId={1} />);
    expect(screen.getByText('Hi there')).toBeInTheDocument();
  });

  test('applies "sent" class for messages from current user', () => {
    const msg = makeMessage({ sender: { id: 1 } });
    const { container } = render(<MessageList messages={[msg]} currentUserId={1} />);
    expect(container.querySelector('.message.sent')).toBeInTheDocument();
  });

  test('applies "received" class for messages from other user', () => {
    const msg = makeMessage({ sender: { id: 2 } });
    const { container } = render(<MessageList messages={[msg]} currentUserId={1} />);
    expect(container.querySelector('.message.received')).toBeInTheDocument();
  });

  test('sorts messages by createdAt ascending', () => {
    const messages = [
      makeMessage({ id: 2, content: 'Second', createdAt: '2024-01-01T10:01:00.000Z' }),
      makeMessage({ id: 1, content: 'First', createdAt: '2024-01-01T10:00:00.000Z' }),
    ];

    render(<MessageList messages={messages} currentUserId={1} />);

    const messageEls = screen.getAllByText(/First|Second/);
    expect(messageEls[0].textContent).toBe('First');
    expect(messageEls[1].textContent).toBe('Second');
  });

  test('renders image media with link', () => {
    const msg = makeMessage({
      content: '',
      mediaUrl: 'http://example.com/photo.jpg',
      mediaType: 'image',
    });

    render(<MessageList messages={[msg]} currentUserId={1} />);

    const img = screen.getByAltText('attachment');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', 'http://example.com/photo.jpg');
  });

  test('renders video media element', () => {
    const msg = makeMessage({
      content: '',
      mediaUrl: 'http://example.com/video.mp4',
      mediaType: 'video',
    });

    const { container } = render(<MessageList messages={[msg]} currentUserId={1} />);
    expect(container.querySelector('video')).toBeInTheDocument();
  });

  test('renders generic file download link', () => {
    const msg = makeMessage({
      content: '',
      mediaUrl: 'http://example.com/doc.pdf',
      mediaType: 'application/pdf',
    });

    render(<MessageList messages={[msg]} currentUserId={1} />);
    expect(screen.getByText('Download File')).toBeInTheDocument();
  });

  test('renders multiple messages', () => {
    const messages = [
      makeMessage({ id: 1, content: 'Msg 1' }),
      makeMessage({ id: 2, content: 'Msg 2' }),
      makeMessage({ id: 3, content: 'Msg 3' }),
    ];

    render(<MessageList messages={messages} currentUserId={1} />);

    expect(screen.getByText('Msg 1')).toBeInTheDocument();
    expect(screen.getByText('Msg 2')).toBeInTheDocument();
    expect(screen.getByText('Msg 3')).toBeInTheDocument();
  });

  test('handles null messages prop gracefully', () => {
    render(<MessageList messages={null} currentUserId={1} />);
    expect(screen.getByText('No messages yet')).toBeInTheDocument();
  });
});
