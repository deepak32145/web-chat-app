import React, { createContext, useState, useCallback } from 'react';

export const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
  const [conversations, setConversations] = useState([]);
  const [currentConversation, setCurrentConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const addConversation = useCallback((conversation) => {
    setConversations((prev) => {
      const exists = prev.find((c) => c.id === conversation.id);
      if (exists) {
        return prev.map((c) => (c.id === conversation.id ? conversation : c));
      }
      return [conversation, ...prev];
    });
  }, []);

  const addMessage = useCallback((message) => {
    setMessages((prev) => [...prev, message]);
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const value = {
    conversations,
    setConversations,
    currentConversation,
    setCurrentConversation,
    messages,
    setMessages,
    onlineUsers,
    setOnlineUsers,
    loading,
    setLoading,
    error,
    setError,
    addConversation,
    addMessage,
    clearError,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};
