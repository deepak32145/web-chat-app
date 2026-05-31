import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add userId to headers if available
apiClient.interceptors.request.use((config) => {
  const userId = localStorage.getItem('userId');
  if (userId) {
    config.headers.userId = userId;
  }
  return config;
});

// Auth APIs
export const sendOTP = (phoneNumber) => {
  return apiClient.post('/auth/send-otp', { phoneNumber });
};

export const verifyOTP = (phoneNumber, otp, firstName, lastName) => {
  return apiClient.post('/auth/verify-otp', {
    phoneNumber,
    otp,
    firstName,
    lastName,
  });
};

// User APIs
export const searchUser = (phoneNumber) => {
  return apiClient.post('/users/search', { phoneNumber });
};

export const getAllOnlineUsers = () => {
  return apiClient.get('/users/online');
};

export const getUserById = (userId) => {
  return apiClient.get(`/users/${userId}`);
};

export const setUserOnline = (userId) => {
  return apiClient.put(`/users/${userId}/online`);
};

export const setUserOffline = (userId) => {
  return apiClient.put(`/users/${userId}/offline`);
};

export const updateUserProfile = (userId, firstName, lastName, bio, profilePicture) => {
  return apiClient.put(`/users/${userId}/profile`, null, {
    params: { firstName, lastName, bio, profilePicture },
  });
};

// Conversation APIs
export const createOrGetConversation = (otherUserId) => {
  return apiClient.post('/conversations/create', null, {
    params: { otherUserId },
  });
};

export const createGroupConversation = (name, isGroupChat, groupIcon, participantIds) => {
  return apiClient.post('/conversations/group', {
    name,
    isGroupChat,
    groupIcon,
    participantIds,
  });
};

export const getUserConversations = () => {
  return apiClient.get('/conversations');
};

export const getConversationDetail = (conversationId) => {
  return apiClient.get(`/conversations/${conversationId}`);
};

// Message APIs
export const sendMessage = (content, mediaUrl, mediaType, conversationId) => {
  return apiClient.post('/messages', {
    content,
    mediaUrl,
    mediaType,
    conversationId,
  });
};

export const getMessages = (conversationId) => {
  return apiClient.get(`/messages/conversation/${conversationId}`);
};

export const getUnreadMessages = (conversationId) => {
  return apiClient.get(`/messages/unread/${conversationId}`);
};

export const markMessageAsRead = (messageId) => {
  return apiClient.put(`/messages/${messageId}/read`);
};

export const markConversationAsRead = (conversationId) => {
  return apiClient.put(`/messages/conversation/${conversationId}/read`);
};

export default apiClient;
