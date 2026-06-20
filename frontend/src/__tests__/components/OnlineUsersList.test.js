import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import OnlineUsersList from '../../components/OnlineUsersList';

jest.mock('../../styles/OnlineUsersList.css', () => ({}));
jest.mock('react-icons/fa', () => ({ FaCircle: () => <span data-testid="online-dot" /> }));

const makeUser = (overrides = {}) => ({
  id: 1,
  username: 'alice',
  phoneNumber: '+911111111111',
  firstName: 'Alice',
  lastName: 'W',
  profilePicture: null,
  ...overrides,
});

describe('OnlineUsersList', () => {
  test('shows empty state when no users online', () => {
    render(<OnlineUsersList users={[]} onSelectUser={jest.fn()} />);
    expect(screen.getByText('No users online')).toBeInTheDocument();
  });

  test('renders a list of online users', () => {
    const users = [
      makeUser({ id: 1, firstName: 'Alice', lastName: 'W' }),
      makeUser({ id: 2, firstName: 'Bob', lastName: null, phoneNumber: '+922222222222' }),
    ];
    render(<OnlineUsersList users={users} onSelectUser={jest.fn()} />);
    expect(screen.getByText('Alice W')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  test('shows phone number when user has no first name', () => {
    const user = makeUser({ firstName: null, lastName: null });
    render(<OnlineUsersList users={[user]} onSelectUser={jest.fn()} />);
    expect(screen.getAllByText('+911111111111').length).toBeGreaterThan(0);
  });

  test('shows avatar image when profilePicture is set', () => {
    const user = makeUser({ profilePicture: 'http://example.com/pic.jpg' });
    render(<OnlineUsersList users={[user]} onSelectUser={jest.fn()} />);
    expect(screen.getByAltText('alice')).toHaveAttribute('src', 'http://example.com/pic.jpg');
  });

  test('shows avatar initial when no profilePicture', () => {
    const user = makeUser({ profilePicture: null, firstName: 'Alice' });
    render(<OnlineUsersList users={[user]} onSelectUser={jest.fn()} />);
    expect(screen.getByText('A')).toBeInTheDocument();
  });

  test('calls onSelectUser with user when clicked', () => {
    const onSelectUser = jest.fn();
    const user = makeUser();
    render(<OnlineUsersList users={[user]} onSelectUser={onSelectUser} />);
    fireEvent.click(screen.getByText('Alice W').closest('.online-user-item'));
    expect(onSelectUser).toHaveBeenCalledWith(user);
  });

  test('shows online indicator for each user', () => {
    const users = [makeUser({ id: 1 }), makeUser({ id: 2, firstName: 'Bob', phoneNumber: '+92222' })];
    render(<OnlineUsersList users={users} onSelectUser={jest.fn()} />);
    expect(screen.getAllByTestId('online-dot')).toHaveLength(2);
  });

  test('shows phone number for user with only phone', () => {
    const user = makeUser({ firstName: null, lastName: null, phoneNumber: '+933333333' });
    render(<OnlineUsersList users={[user]} onSelectUser={jest.fn()} />);
    expect(screen.getAllByText('+933333333').length).toBeGreaterThan(0);
  });
});
