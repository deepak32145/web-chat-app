import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
  getAllOnlineUsers,
  searchUser,
  createOrGetConversation,
  getUserConversations,
  getConversationDetail,
  setUserOnline,
  setUserOffline,
} from '../services/api';
import ConversationList from '../components/ConversationList';
import OnlineUsersList from '../components/OnlineUsersList';
import UserSearchBar from '../components/UserSearchBar';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import '../styles/DashboardPage.css';

const DashboardPage = ({ userId, userPhone, userName, onLogout }) => {
  const [conversations, setConversations] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeChatConv, setActiveChatConv] = useState(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatPartner, setChatPartner] = useState(null);
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadConversations();
    loadOnlineUsers();
    setUserOnline(userId);

    // sendBeacon fires reliably even when the tab/browser is force-closed,
    // unlike fetch or cleanup functions which are skipped on hard close.
    const handleUnload = () => {
      navigator.sendBeacon(`${process.env.REACT_APP_API_URL || 'http://localhost:8080/api'}/users/${userId}/offline`);
    };
    window.addEventListener('beforeunload', handleUnload);

    const interval = setInterval(loadOnlineUsers, 10000);

    return () => {
      clearInterval(interval);
      window.removeEventListener('beforeunload', handleUnload);
      setUserOffline(userId);
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

      // Keep active chat partner's status in sync with each poll
      setChatPartner((prev) => {
        if (!prev) return prev;
        const isOnline = others.some((u) => u.id === prev.id);
        return { ...prev, status: isOnline ? 'online' : 'offline' };
      });
    } catch (err) {
      console.error('Failed to load online users');
    }
  };

  const connectWebSocket = useCallback((conversationId) => {
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/conversation/${conversationId}`, (frame) => {
          const msg = JSON.parse(frame.body);

          // Update open chat messages
          setChatMessages((prev) => {
            const withoutTemp = prev.filter(
              (m) => !(String(m.id).startsWith('temp-') && m.content === msg.content && m.sender?.id === msg.sender?.id)
            );
            if (withoutTemp.some((m) => m.id === msg.id)) return withoutTemp;
            return [...withoutTemp, msg];
          });

          // Update conversation list: replace last message preview and move to top
          setConversations((prev) => {
            const target = prev.find((c) => c.id === conversationId);
            if (!target) return prev;
            const updated = {
              ...target,
              messages: [
                ...(target.messages || [])
                  .filter((m) => !(String(m.id).startsWith('temp-') && m.content === msg.content && m.sender?.id === msg.sender?.id))
                  .filter((m) => m.id !== msg.id),
                msg,
              ],
            };
            return [updated, ...prev.filter((c) => c.id !== conversationId)];
          });
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

  const openChatWithConversation = async (conversation) => {
    setError('');
    connectWebSocket(conversation.id);
    // Show stale messages instantly, then replace with fresh data
    setChatMessages(sortMessages(conversation.messages));
    const other = conversation.participants?.find((p) => p.id !== parseInt(userId));
    setActiveChatConv(conversation);
    setChatPartner(other || null);
    try {
      const response = await getConversationDetail(conversation.id);
      const fresh = response.data;
      setChatMessages(sortMessages(fresh.messages));
      setActiveChatConv(fresh);
      // Sync fresh messages back into the conversation list entry
      setConversations((prev) =>
        prev.map((c) => (c.id === fresh.id ? { ...c, messages: fresh.messages } : c))
      );
    } catch {
      // stale data already shown above, no-op on failure
    }
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

  const handleSendMessage = (content, mediaUrl = null, mediaType = null) => {
    if (!stompClientRef.current?.connected || (!content.trim() && !mediaUrl) || !activeChatConv) return;

    const optimisticMessage = {
      id: `temp-${Date.now()}`,
      content,
      sender: { id: parseInt(userId) },
      createdAt: new Date().toISOString(),
      mediaUrl,
      mediaType,
    };
    setChatMessages((prev) => [...prev, optimisticMessage]);

    setConversations((prev) => {
      const target = prev.find((c) => c.id === activeChatConv.id);
      if (!target) return prev;
      const updated = { ...target, messages: [...(target.messages || []), optimisticMessage] };
      return [updated, ...prev.filter((c) => c.id !== activeChatConv.id)];
    });

    stompClientRef.current.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        userId: parseInt(userId),
        conversationId: activeChatConv.id,
        content,
        mediaUrl,
        mediaType,
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
          <div className="user-info-header">
            {userName && <span className="user-name">{userName}</span>}
            <span className="user-phone">{userPhone}</span>
          </div>
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
                    <span className={chatPartner?.status === 'online' ? 'online-badge' : 'offline-badge'}>
                      ● {chatPartner?.status === 'online' ? 'Online' : 'Offline'}
                    </span>
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
