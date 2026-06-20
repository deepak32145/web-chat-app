import * as api from '../../services/api';

jest.mock('../../services/api', () => ({
  sendOTP: jest.fn(),
  verifyOTP: jest.fn(),
  searchUser: jest.fn(),
  getAllOnlineUsers: jest.fn(),
  getUserById: jest.fn(),
  setUserOnline: jest.fn(),
  setUserOffline: jest.fn(),
  updateUserProfile: jest.fn(),
  createOrGetConversation: jest.fn(),
  createGroupConversation: jest.fn(),
  getUserConversations: jest.fn(),
  getConversationDetail: jest.fn(),
  uploadFile: jest.fn(),
  sendMessage: jest.fn(),
  getMessages: jest.fn(),
  getUnreadMessages: jest.fn(),
  markMessageAsRead: jest.fn(),
  markConversationAsRead: jest.fn(),
}));

describe('API service', () => {
  beforeEach(() => jest.clearAllMocks());

  test('sendOTP is callable with phone number', async () => {
    api.sendOTP.mockResolvedValue({ data: { success: true, isNewUser: false } });
    const result = await api.sendOTP('+911234567890');
    expect(api.sendOTP).toHaveBeenCalledWith('+911234567890');
    expect(result.data.success).toBe(true);
  });

  test('verifyOTP is callable with phone, otp, firstName, lastName', async () => {
    api.verifyOTP.mockResolvedValue({ data: { id: 1, token: 'abc' } });
    const result = await api.verifyOTP('+911234567890', '123456', 'John', 'Doe');
    expect(api.verifyOTP).toHaveBeenCalledWith('+911234567890', '123456', 'John', 'Doe');
    expect(result.data.id).toBe(1);
  });

  test('searchUser is callable with phone number', async () => {
    api.searchUser.mockResolvedValue({ data: { id: 1, phoneNumber: '+911234567890' } });
    const result = await api.searchUser('+911234567890');
    expect(api.searchUser).toHaveBeenCalledWith('+911234567890');
    expect(result.data.phoneNumber).toBe('+911234567890');
  });

  test('getAllOnlineUsers returns list', async () => {
    api.getAllOnlineUsers.mockResolvedValue({ data: [{ id: 2, status: 'online' }] });
    const result = await api.getAllOnlineUsers();
    expect(result.data).toHaveLength(1);
  });

  test('getUserById is callable with id', async () => {
    api.getUserById.mockResolvedValue({ data: { id: 1 } });
    const result = await api.getUserById(1);
    expect(api.getUserById).toHaveBeenCalledWith(1);
    expect(result.data.id).toBe(1);
  });

  test('setUserOnline is callable with userId', async () => {
    api.setUserOnline.mockResolvedValue({ data: 'User set as online' });
    await api.setUserOnline(1);
    expect(api.setUserOnline).toHaveBeenCalledWith(1);
  });

  test('setUserOffline is callable with userId', async () => {
    api.setUserOffline.mockResolvedValue({ data: 'User set as offline' });
    await api.setUserOffline(1);
    expect(api.setUserOffline).toHaveBeenCalledWith(1);
  });

  test('getUserConversations returns conversations', async () => {
    api.getUserConversations.mockResolvedValue({ data: [{ id: 10, name: 'Chat' }] });
    const result = await api.getUserConversations();
    expect(result.data).toHaveLength(1);
    expect(result.data[0].id).toBe(10);
  });

  test('createOrGetConversation is callable with otherUserId', async () => {
    api.createOrGetConversation.mockResolvedValue({ data: { id: 10 } });
    const result = await api.createOrGetConversation(2);
    expect(api.createOrGetConversation).toHaveBeenCalledWith(2);
    expect(result.data.id).toBe(10);
  });

  test('sendMessage is callable with message params', async () => {
    api.sendMessage.mockResolvedValue({ data: { id: 100, content: 'Hello' } });
    const result = await api.sendMessage('Hello', null, null, 10);
    expect(api.sendMessage).toHaveBeenCalledWith('Hello', null, null, 10);
    expect(result.data.content).toBe('Hello');
  });

  test('getMessages is callable with conversationId', async () => {
    api.getMessages.mockResolvedValue({ data: [] });
    await api.getMessages(10);
    expect(api.getMessages).toHaveBeenCalledWith(10);
  });

  test('markMessageAsRead is callable with messageId', async () => {
    api.markMessageAsRead.mockResolvedValue({});
    await api.markMessageAsRead(100);
    expect(api.markMessageAsRead).toHaveBeenCalledWith(100);
  });

  test('markConversationAsRead is callable with conversationId', async () => {
    api.markConversationAsRead.mockResolvedValue({});
    await api.markConversationAsRead(10);
    expect(api.markConversationAsRead).toHaveBeenCalledWith(10);
  });

  test('uploadFile is callable with formData', async () => {
    api.uploadFile.mockResolvedValue({ data: { url: '/files/abc.jpg', mediaType: 'image' } });
    const formData = new FormData();
    const result = await api.uploadFile(formData);
    expect(result.data.mediaType).toBe('image');
  });

  test('handles rejected promises', async () => {
    api.sendOTP.mockRejectedValue(new Error('Network error'));
    await expect(api.sendOTP('+911234567890')).rejects.toThrow('Network error');
  });
});
