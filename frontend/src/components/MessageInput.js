import React, { useState } from 'react';
import { FaPaperPlane } from 'react-icons/fa';
import '../styles/MessageInput.css';

const MessageInput = ({ onSendMessage }) => {
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!message.trim()) return;

    setLoading(true);
    try {
      await onSendMessage(message, null, null);
      setMessage('');
    } catch (error) {
      console.error('Failed to send message:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="message-input" onSubmit={handleSendMessage}>
      <input
        type="text"
        placeholder="Type a message..."
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        disabled={loading}
      />
      <button type="submit" disabled={loading || !message.trim()} className="send-btn">
        <FaPaperPlane />
      </button>
    </form>
  );
};

export default MessageInput;
