import React from 'react';
import { FaCircle } from 'react-icons/fa';
import '../styles/OnlineUsersList.css';

const OnlineUsersList = ({ users, onSelectUser }) => {
  if (users.length === 0) {
    return <p className="empty-state">No users online</p>;
  }

  return (
    <div className="online-users-list">
      {users.map((user) => (
        <div
          key={user.id}
          className="online-user-item"
          onClick={() => onSelectUser(user)}
        >
          <div className="user-avatar">
            {user.profilePicture ? (
              <img src={user.profilePicture} alt={user.username} />
            ) : (
              <div className="avatar-placeholder">
                {(user.firstName || user.phoneNumber || 'U').charAt(0).toUpperCase()}
              </div>
            )}
            <FaCircle className="online-indicator" />
          </div>
          <div className="user-info">
            <p className="user-name">
              {user.firstName
                ? `${user.firstName} ${user.lastName || ''}`.trim()
                : user.phoneNumber}
            </p>
            <p className="user-phone">{user.phoneNumber}</p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default OnlineUsersList;
