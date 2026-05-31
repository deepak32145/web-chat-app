import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
  getAllOnlineUsers,
  searchUser,
  createOrGetConversation,
  getUserConversations,
  setUserOnline,
  setUserOffline,
} from '../services/api';
import ConversationList from '../components/ConversationList';
import OnlineUsersList from '../components/OnlineUsersList';
import UserSearchBar from '../components/UserSearchBar';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import '../styles/DashboardPage.css';

const DashboardPage = ({ userId, userPhone, onLogout }) => {
  const [conversations, setConversations] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeChatConv, setActiveChatConv] = useState(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatPartner, setChatPartner] = useState(null);
  const stompClientRef = useRef(null);
  const presenceClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadConversations();
    loadOnlineUsers();
    setUserOnline(userId);

    // Persistent presence connection — backend's SessionDisconnectEvent fires
    // when this drops (tab close, crash, network loss), setting user offline automatically
    const presenceClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { userId: String(userId) },
      reconnectDelay: 5000,
    });
    presenceClient.activate();
    presenceClientRef.current = presenceClient;

    const interval = setInterval(loadOnlineUsers, 30000);

    return () => {
      clearInterval(interval);
      setUserOffline(userId);
      if (presenceClientRef.current) {
        presenceClientRef.current.deactivate();
      }
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [userId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const loadConversations = async () => {
    try {
      setLoading(true);
      const response = await getUserConversations();
      setConversations(response.data || []);
    } catch (err) {
      console.error('Failed to load conversations');
    } finally {
      setLoading(false);
    }
  };

  const loadOnlineUsers = async () => {
    try {
      const response = await getAllOnlineUsers();
      const others = (response.data || []).filter((u) => u.id !== parseInt(userId));
      setOnlineUsers(others);
    } catch (err) {
      console.error('Failed to load online users');
    }
  };

  const connectWebSocket = useCallback((conversationId) => {
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
    }

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/conversation/${conversationId}`, (frame) => {
          const msg = JSON.parse(frame.body);
          setChatMessages((prev) => [...prev, msg]);
        });
      },
      onStompError: (frame) => {
        console.error('WebSocket error', frame);
      },
    });

    client.activate();
    stompClientRef.current = client;
  }, []);

  const sortMessages = (messages) =>
    [...(messages || [])].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

  const openChatWithConversation = (conversation) => {
    setError('');
    const other = conversation.participants?.find((p) => p.id !== parseInt(userId));
    setActiveChatConv(conversation);
    setChatPartner(other || null);
    setChatMessages(sortMessages(conversation.messages));
    connectWebSocket(conversation.id);
  };

  const openChatWithUser = async (user) => {
    try {
      setError('');
      const convResponse = await createOrGetConversation(user.id);
      const conversation = convResponse.data;
      setActiveChatConv(conversation);
      setChatPartner(user);
      setChatMessages(sortMessages(conversation.messages));
      connectWebSocket(conversation.id);
      loadConversations();
    } catch (err) {
      setError('Could not open chat');
    }
  };

  const handleSearchUser = async (phoneNumber) => {
    try {
      setError('');
      const response = await searchUser(phoneNumber);
      if (response.data) {
        await openChatWithUser(response.data);
      } else {
        setError('User not found');
      }
    } catch (err) {
      setError('User not registered with the app');
    }
  };

  const handleSendMessage = (content) => {
    if (!stompClientRef.current?.connected || !content.trim() || !activeChatConv) return;
    stompClientRef.current.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        userId: parseInt(userId),
        conversationId: activeChatConv.id,
        content,
        mediaUrl: null,
        mediaType: null,
      }),
    });
  };

  const closeChat = () => {
    setActiveChatConv(null);
    setChatMessages([]);
    setChatPartner(null);
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }
  };

  const handleLogout = () => {
    setUserOffline(userId);
    onLogout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Chat App</h1>
        <div className="header-actions">
          <span className="user-phone">{userPhone}</span>
          <button onClick={handleLogout} className="btn-logout">Logout</button>
        </div>
      </div>

      <div className="dashboard-content">
        <div className="sidebar">
          <UserSearchBar onSearch={handleSearchUser} />

          {error && <p className="error-msg">{error}</p>}

          <div className="section">
            <h2>Messages</h2>
            {loading ? (
              <p>Loading...</p>
            ) : conversations.length > 0 ? (
              <ConversationList
                conversations={conversations}
                userId={parseInt(userId)}
                onSelect={openChatWithConversation}
              />
            ) : (
              <p className="empty-state">No conversations yet</p>
            )}
          </div>

          <div className="section">
            <h2>Online Users</h2>
            {onlineUsers.length > 0 ? (
              <OnlineUsersList users={onlineUsers} onSelectUser={openChatWithUser} />
            ) : (
              <p className="empty-state">No users online</p>
            )}
          </div>
        </div>

        <div className="main-content">
          {activeChatConv ? (
            <div className="chat-panel">
              <div className="chat-panel-header">
                <div className="chat-partner-info">
                  <div className="chat-partner-avatar">
                    {(chatPartner?.firstName || chatPartner?.phoneNumber || 'U').charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h3>{chatPartner?.firstName
                      ? `${chatPartner.firstName} ${chatPartner.lastName || ''}`.trim()
                      : chatPartner?.phoneNumber}
                    </h3>
                    <span className="online-badge">● Online</span>
                  </div>
                </div>
                <button className="btn-close-chat" onClick={closeChat}>✕</button>
              </div>

              <div className="chat-panel-messages">
                <MessageList messages={chatMessages} currentUserId={parseInt(userId)} />
                <div ref={messagesEndRef} />
              </div>

              <MessageInput onSendMessage={handleSendMessage} />
            </div>
          ) : (
            <div className="welcome-message">
              <h2>Welcome to Chat App</h2>
              <p>Click an online user or search by phone number to start chatting</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
