import React from 'react';
import { FaFileDownload } from 'react-icons/fa';
import '../styles/MessageList.css';

const MessageList = ({ messages, currentUserId }) => {
  const sorted = [...(messages || [])].sort(
    (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
  );

  const renderMedia = (message) => {
    if (!message.mediaUrl) return null;

    if (message.mediaType === 'image') {
      return (
        <div className="message-media">
          <a href={`${message.mediaUrl}?download=true`} target="_blank" rel="noreferrer">
            <img src={message.mediaUrl} alt="attachment" />
          </a>
        </div>
      );
    }

    if (message.mediaType === 'video') {
      return (
        <div className="message-media">
          <video src={message.mediaUrl} controls />
        </div>
      );
    }

    // Generic file download
    return (
      <div className="message-media">
        <a
          href={`${message.mediaUrl}?download=true`}
          target="_blank"
          rel="noreferrer"
          className="file-download-link"
        >
          <FaFileDownload />
          <span>Download File</span>
        </a>
      </div>
    );
  };

  return (
    <div className="message-list">
      {sorted.length === 0 ? (
        <p className="empty-state">No messages yet</p>
      ) : (
        sorted.map((message) => (
          <div
            key={message.id}
            className={`message ${message.sender?.id === currentUserId ? 'sent' : 'received'}`}
          >
            <div className="message-content">
              {renderMedia(message)}
              {message.content && <p>{message.content}</p>}
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
