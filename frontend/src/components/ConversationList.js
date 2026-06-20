import React from 'react';
import '../styles/ConversationList.css';

const ConversationList = ({ conversations, userId, onSelect }) => {
  const getDisplayName = (conversation) => {
    if (conversation.isGroupChat) return conversation.name;
    const other = conversation.participants?.find((p) => p.id !== userId);
    if (!other) return conversation.name;
    if (other.firstName) return `${other.firstName} ${other.lastName || ''}`.trim();
    return other.phoneNumber || conversation.name;
  };

  const getAvatarInitial = (conversation) => {
    return getDisplayName(conversation).charAt(0).toUpperCase();
  };

  const getLatestMessage = (messages) => {
    if (!messages || messages.length === 0) return null;
    return [...messages].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0];
  };

  const getUnreadCount = (messages) => {
    return (messages || []).filter((m) => !m.isRead && m.sender?.id !== userId).length;
  };

  const getLastMessage = (messages) => {
    const latest = getLatestMessage(messages);
    if (!latest) return 'No messages yet';
    const text = latest.content || '';
    return text.substring(0, 50) + (text.length > 50 ? '...' : '');
  };

  const getLastMessageTime = (messages) => {
    const latest = getLatestMessage(messages);
    if (!latest) return '';
    return new Date(latest.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="conversation-list">
      {conversations.map((conversation) => (
        <div
          key={conversation.id}
          className="conversation-item"
          onClick={() => onSelect(conversation)}
        >
          <div className="conversation-avatar">
            {conversation.groupIcon ? (
              <img src={conversation.groupIcon} alt={getDisplayName(conversation)} />
            ) : (
              <div className="avatar-placeholder">{getAvatarInitial(conversation)}</div>
            )}
          </div>

          <div className="conversation-details">
            <div className="conversation-header">
              <h3>{getDisplayName(conversation)}</h3>
              <span className="timestamp">{getLastMessageTime(conversation.messages)}</span>
            </div>
            <p className="last-message">{getLastMessage(conversation.messages)}</p>
          </div>

          {getUnreadCount(conversation.messages) > 0 && (
            <div className="unread-badge">{getUnreadCount(conversation.messages)}</div>
          )}
        </div>
      ))}
    </div>
  );
};

export default ConversationList;
