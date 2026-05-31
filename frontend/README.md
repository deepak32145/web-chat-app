# Chat App - React Frontend

A modern WhatsApp-like chat application built with React.

## Features

- **OTP-based Authentication**: Login with phone number and OTP verification
- **Real-time Messaging**: Send and receive messages instantly
- **User Search**: Search and add contacts by phone number
- **Online Status**: See who's online in the system
- **Conversation Management**: View latest conversations with unread message count
- **Group Chats**: Create and manage group conversations
- **Media Support**: Share images and videos (placeholder support)

## Project Structure

```
src/
├── components/          # Reusable React components
│   ├── ConversationList.js
│   ├── OnlineUsersList.js
│   ├── UserSearchBar.js
│   ├── MessageList.js
│   └── MessageInput.js
├── pages/              # Page components
│   ├── LoginPage.js
│   ├── OTPPage.js
│   ├── DashboardPage.js
│   └── ChatPage.js
├── services/           # API integration
│   └── api.js
├── context/            # React Context for state management
│   └── ChatContext.js
├── styles/             # CSS stylesheets
└── App.js              # Main App component
```

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file in the root directory:
```
REACT_APP_API_URL=http://localhost:8080/api
```

3. Start the development server:
```bash
npm start
```

4. Open http://localhost:3000 to view it in the browser.

## API Configuration

Update the `API_BASE_URL` in `src/services/api.js` to match your backend server:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Authentication Flow

1. User enters phone number on Login page
2. System sends OTP to the phone number
3. User verifies OTP on Verify OTP page
4. User optionally enters first and last name
5. User is logged in and redirected to Dashboard

## Key Technologies

- **React 18**: UI library
- **React Router v6**: Client-side routing
- **Axios**: HTTP client
- **Tailwind CSS**: Utility-first CSS framework
- **React Icons**: Icon library
- **Socket.IO** (planned): Real-time communication

## Future Enhancements

- WebSocket support for real-time messaging
- File upload and media sharing
- Call functionality
- Dark mode
- User profile customization
- Message search and filters
- Status updates
- Typing indicators
- Message reactions
