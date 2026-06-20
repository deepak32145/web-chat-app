import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import ConversationList from '../../components/ConversationList';

jest.mock('../../styles/ConversationList.css', () => ({}));

const makeConversation = (overrides = {}) => ({
  id: 1,
  name: 'Test Chat',
  isGroupChat: false,
  groupIcon: null,
  participants: [
    { id: 1, phoneNumber: '+911111111111', firstName: 'Alice', lastName: 'A' },
    { id: 2, phoneNumber: '+922222222222', firstName: 'Bob', lastName: 'B' },
  ],
  messages: [],
  ...overrides,
});

describe('ConversationList', () => {
  test('renders a list of conversations', () => {
    const conversations = [
      makeConversation({ id: 1, name: 'Chat 1' }),
      makeConversation({ id: 2, name: 'Chat 2', participants: [
        { id: 1, phoneNumber: '+91111' },
        { id: 3, phoneNumber: '+93333', firstName: 'Carol' },
      ]}),
    ];

    render(<ConversationList conversations={conversations} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByText('Bob B')).toBeInTheDocument();
    expect(screen.getByText('Carol')).toBeInTheDocument();
  });

  test('shows "No messages yet" for conversation with no messages', () => {
    render(
      <ConversationList
        conversations={[makeConversation()]}
        userId={1}
        onSelect={jest.fn()}
      />
    );
    expect(screen.getByText('No messages yet')).toBeInTheDocument();
  });

  test('shows last message preview truncated to 50 chars', () => {
    const longMsg = 'A'.repeat(60);
    const conversation = makeConversation({
      messages: [{
        id: 10,
        content: longMsg,
        isRead: true,
        sender: { id: 2 },
        createdAt: new Date().toISOString(),
      }],
    });

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByText(`${'A'.repeat(50)}...`)).toBeInTheDocument();
  });

  test('calls onSelect with conversation when clicked', () => {
    const onSelect = jest.fn();
    const conversation = makeConversation();

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={onSelect} />);

    fireEvent.click(screen.getByText('Bob B').closest('.conversation-item'));

    expect(onSelect).toHaveBeenCalledWith(conversation);
  });

  test('shows unread badge for unread messages from other user', () => {
    const conversation = makeConversation({
      messages: [
        { id: 10, content: 'Hi', isRead: false, sender: { id: 2 }, createdAt: new Date().toISOString() },
        { id: 11, content: 'Hello', isRead: false, sender: { id: 2 }, createdAt: new Date().toISOString() },
      ],
    });

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByText('2')).toBeInTheDocument();
  });

  test('does not show unread badge when all messages are read', () => {
    const conversation = makeConversation({
      messages: [
        { id: 10, content: 'Hi', isRead: true, sender: { id: 2 }, createdAt: new Date().toISOString() },
      ],
    });

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={jest.fn()} />);

    expect(screen.queryByText('1')).not.toBeInTheDocument();
  });

  test('shows group chat name for group conversations', () => {
    const groupConv = makeConversation({
      isGroupChat: true,
      name: 'Team Alpha',
      participants: [
        { id: 1, phoneNumber: '+91111' },
        { id: 2, phoneNumber: '+92222', firstName: 'Bob' },
      ],
    });

    render(<ConversationList conversations={[groupConv]} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByText('Team Alpha')).toBeInTheDocument();
  });

  test('shows phone number when other participant has no name', () => {
    const conversation = makeConversation({
      participants: [
        { id: 1, phoneNumber: '+91111' },
        { id: 2, phoneNumber: '+92222' },
      ],
    });

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByText('+92222')).toBeInTheDocument();
  });

  test('shows group icon image when groupIcon is set', () => {
    const conversation = makeConversation({
      isGroupChat: true,
      name: 'Team',
      groupIcon: 'http://example.com/icon.png',
    });

    render(<ConversationList conversations={[conversation]} userId={1} onSelect={jest.fn()} />);

    expect(screen.getByAltText('Team')).toBeInTheDocument();
  });

  test('renders empty list without crashing', () => {
    const { container } = render(
      <ConversationList conversations={[]} userId={1} onSelect={jest.fn()} />
    );
    expect(container.querySelector('.conversation-list')).toBeInTheDocument();
  });
});
