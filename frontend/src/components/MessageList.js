import React from 'react';
import '../styles/MessageList.css';

const MessageList = ({ messages, currentUserId }) => {
  const sorted = [...(messages || [])].sort(
    (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
  );

  return (
    <div className="message-list">
      {sorted.length === 0 ? (
        <p className="empty-state">No messages yet</p>
      ) : (
        sorted.map((message) => (
          <div
            key={message.id}
            className={`message ${message.sender.id === currentUserId ? 'sent' : 'received'}`}
          >
            <div className="message-content">
              {message.mediaUrl && (
                <div className="message-media">
                  {message.mediaType === 'image' && <img src={message.mediaUrl} alt="media" />}
                  {message.mediaType === 'video' && <video src={message.mediaUrl} controls />}
                </div>
              )}
              <p>{message.content}</p>
              <span className="message-time">
                {new Date(message.createdAt).toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </span>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default MessageList;
