import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getConversationDetail,
  sendMessage,
  markConversationAsRead,
} from '../services/api';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import '../styles/ChatPage.css';

const ChatPage = ({ userId }) => {
  const { conversationId } = useParams();
  const [conversation, setConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const messageEndRef = useRef(null);

  useEffect(() => {
    loadConversation();
    const interval = setInterval(() => {
      loadConversation();
    }, 3000); // Refresh messages every 3 seconds

    return () => clearInterval(interval);
  }, [conversationId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messageEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadConversation = async () => {
    try {
      setLoading(true);
      const response = await getConversationDetail(conversationId);
      setConversation(response.data);
      setMessages(response.data.messages || []);
      await markConversationAsRead(conversationId);
    } catch (err) {
      setError('Failed to load conversation');
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (content, mediaUrl, mediaType) => {
    try {
      await sendMessage(content, mediaUrl, mediaType, parseInt(conversationId));
      loadConversation();
    } catch (err) {
      setError('Failed to send message');
    }
  };

  if (loading && !conversation) {
    return <div className="loading">Loading...</div>;
  }

  if (!conversation) {
    return <div className="error">Failed to load conversation</div>;
  }

  const otherParticipant = conversation.participants.find((p) => p.id !== userId);

  return (
    <div className="chat-container">
      <div className="chat-header">
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          ← Back
        </button>
        <div className="chat-header-info">
          <h2>{conversation.isGroupChat ? conversation.name : otherParticipant?.phoneNumber}</h2>
          <p className="status">
            {conversation.isGroupChat
              ? `${conversation.participants.length} members`
              : otherParticipant?.status === 'online'
              ? 'Online'
              : `Last seen: ${new Date(otherParticipant?.lastSeen).toLocaleString()}`}
          </p>
        </div>
      </div>

      <MessageList messages={messages} currentUserId={userId} />

      <div ref={messageEndRef} />

      {error && <div className="error-message">{error}</div>}

      <MessageInput onSendMessage={handleSendMessage} />
    </div>
  );
};

export default ChatPage;
