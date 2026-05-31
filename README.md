# WhatsApp-like Chat Application

A modern, full-stack real-time chat application built with Spring Boot and React. Features OTP-based phone number authentication, real-time messaging, user search, and online status tracking.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Frontend Setup](#frontend-setup)
- [Running the Application](#running-the-application)
- [Database Schema](#database-schema)
- [Future Enhancements](#future-enhancements)

## ✨ Features

### Authentication & Security
- **OTP-based Phone Authentication**: Users login using their phone number with OTP verification
- **Phone Number Verification**: Ensures only registered phone numbers can access the app
- **Session Management**: Token-based authentication (JWT - to be implemented)

### Messaging
- **1-on-1 Conversations**: Direct messaging between users
- **Group Chats**: Create and manage group conversations
- **Message Status**: Track read/unread messages
- **Message History**: View complete conversation history with timestamps
- **Media Support**: Share images and videos (placeholder infrastructure)

### User Management
- **User Search**: Find and add contacts by phone number
- **Online Status**: Real-time online/offline status tracking
- **Last Seen**: Track when users were last active
- **User Profiles**: Manage first name, last name, bio, and profile picture

### Dashboard
- **Conversation List**: View all conversations sorted by most recent
- **Unread Count**: Badge showing number of unread messages per conversation
- **Online Users List**: See who's currently online
- **Quick Search**: Search and message any registered phone number

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Hibernate JPA
- **API**: RESTful REST API
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Styling**: Tailwind CSS + Custom CSS
- **Icons**: React Icons
- **Real-time** (planned): Socket.IO

## 📁 Project Structure

```
java-host-code/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── videoapp/
│   │   │   │           ├── controller/
│   │   │   │           │   ├── AuthController.java
│   │   │   │           │   ├── MessageController.java
│   │   │   │           │   ├── ConversationController.java
│   │   │   │           │   └── UserSearchController.java
│   │   │   │           ├── service/
│   │   │   │           │   ├── OTPService.java
│   │   │   │           │   ├── MessageService.java
│   │   │   │           │   ├── ConversationService.java
│   │   │   │           │   └── UserService.java
│   │   │   │           ├── model/
│   │   │   │           │   ├── User.java
│   │   │   │           │   ├── Message.java
│   │   │   │           │   └── Conversation.java
│   │   │   │           ├── repository/
│   │   │   │           │   ├── UserRepository.java
│   │   │   │           │   ├── MessageRepository.java
│   │   │   │           │   └── ConversationRepository.java
│   │   │   │           ├── dto/
│   │   │   │           │   ├── SendOTPRequest.java
│   │   │   │           │   ├── VerifyOTPRequest.java
│   │   │   │           │   ├── MessageDTO.java
│   │   │   │           │   ├── ConversationDTO.java
│   │   │   │           │   └── UserDTO.java
│   │   │   │           └── ChatAppApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   └── DATABASE_SCHEMA.md
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── ConversationList.js
    │   │   ├── OnlineUsersList.js
    │   │   ├── UserSearchBar.js
    │   │   ├── MessageList.js
    │   │   └── MessageInput.js
    │   ├── pages/
    │   │   ├── LoginPage.js
    │   │   ├── OTPPage.js
    │   │   ├── DashboardPage.js
    │   │   └── ChatPage.js
    │   ├── services/
    │   │   └── api.js
    │   ├── context/
    │   │   └── ChatContext.js
    │   ├── styles/
    │   │   ├── LoginPage.css
    │   │   ├── OTPPage.css
    │   │   ├── DashboardPage.css
    │   │   ├── ChatPage.css
    │   │   ├── ConversationList.css
    │   │   ├── OnlineUsersList.css
    │   │   ├── UserSearchBar.css
    │   │   ├── MessageList.css
    │   │   └── MessageInput.css
    │   ├── App.js
    │   ├── index.js
    │   └── index.css
    ├── public/
    │   └── index.html
    ├── package.json
    ├── tailwind.config.js
    ├── postcss.config.js
    └── README.md
```

## 🚀 Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 14+ and npm
- MySQL 8.0+
- Maven (for backend build)

### Backend Setup

1. **Clone the repository**
```bash
cd java-host-code/backend
```

2. **Create MySQL Database**
```sql
CREATE DATABASE chat_app_db;
```

3. **Update application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chat_app_db
spring.datasource.username=root
spring.datasource.password=your_password
```

4. **Build the project**
```bash
mvn clean package
```

5. **Run the application**
```bash
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd java-host-code/frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Create .env file**
```
REACT_APP_API_URL=http://localhost:8080/api
```

4. **Start development server**
```bash
npm start
```

The frontend will be available at `http://localhost:3000`

## 📚 API Documentation

### Authentication Endpoints

#### Send OTP
```
POST /api/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "+919876543210"
}

Response:
{
  "message": "OTP sent successfully to +919876543210",
  "success": true
}
```

#### Verify OTP
```
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+919876543210",
  "otp": "123456",
  "firstName": "John",
  "lastName": "Doe"
}

Response:
{
  "id": 1,
  "username": "+919876543210",
  "email": null,
  "token": "jwt_token_here_1",
  "type": "Bearer"
}
```

### User Endpoints

#### Search User
```
POST /api/users/search
Content-Type: application/json
userId: <user_id>

{
  "phoneNumber": "+919876543211"
}

Response:
{
  "id": 2,
  "username": "+919876543211",
  "email": "john@example.com",
  "phoneNumber": "+919876543211",
  "firstName": "Jane",
  "lastName": "Smith",
  "profilePicture": null,
  "bio": "Hello!",
  "status": "online"
}
```

#### Get Online Users
```
GET /api/users/online
Headers:
  userId: <user_id>

Response:
[
  {
    "id": 2,
    "username": "+919876543211",
    "phoneNumber": "+919876543211",
    "firstName": "Jane",
    "status": "online"
  }
]
```

#### Set User Status
```
PUT /api/users/{userId}/online
PUT /api/users/{userId}/offline
Headers:
  userId: <user_id>
```

### Conversation Endpoints

#### Create or Get 1-on-1 Conversation
```
POST /api/conversations/create?otherUserId=2
Headers:
  userId: <user_id>

Response:
{
  "id": 1,
  "name": "+919876543210 - +919876543211",
  "isGroupChat": false,
  "participants": [...],
  "messages": [...]
}
```

#### Get User's Conversations
```
GET /api/conversations
Headers:
  userId: <user_id>

Response:
[
  {
    "id": 1,
    "name": "+919876543210 - +919876543211",
    "isGroupChat": false,
    "messages": [...]
  }
]
```

### Message Endpoints

#### Send Message
```
POST /api/messages
Headers:
  userId: <user_id>
Content-Type: application/json

{
  "content": "Hello!",
  "mediaUrl": null,
  "mediaType": null,
  "conversationId": 1
}

Response:
{
  "id": 1,
  "content": "Hello!",
  "sender": {...},
  "conversationId": 1,
  "isRead": false,
  "createdAt": "2024-05-24T10:30:00"
}
```

#### Get Messages
```
GET /api/messages/conversation/{conversationId}
Headers:
  userId: <user_id>
```

## 🖥️ Frontend Features

### Authentication Flow
1. **Login Page**: User enters phone number
2. **OTP Verification**: User receives and enters OTP
3. **Profile Setup**: Optional first/last name entry
4. **Dashboard**: View conversations and online users

### Dashboard
- **Conversation List**: All user conversations with unread count
- **Online Users**: Real-time list of online users
- **Search Bar**: Quick search to message any phone number

### Chat Interface
- **Message History**: View all messages in conversation
- **Real-time Messaging**: Send and receive messages
- **User Info**: See contact info and online status
- **Message Status**: Visual indicators for sent/received messages

## 📊 Database Schema

### Users Table
- `id`: Primary key
- `phoneNumber`: Unique phone number (login identifier)
- `otp`: 6-digit OTP for verification
- `otpExpiry`: OTP expiration timestamp
- `isPhoneVerified`: Phone verification status
- `status`: online/offline/away
- `lastSeen`: Last activity timestamp

### Conversations Table
- `id`: Primary key
- `name`: Conversation name (phone numbers for 1-on-1)
- `isGroupChat`: Group vs 1-on-1 indicator
- `participants`: Many-to-many relationship with Users
- `messages`: One-to-many relationship with Messages

### Messages Table
- `id`: Primary key
- `content`: Message text
- `mediaUrl`: URL for media files
- `mediaType`: image/video/audio
- `sender`: Reference to User
- `conversation`: Reference to Conversation
- `isRead`: Read status
- `createdAt`: Message timestamp

## 🔄 Application Flow

```
User Logs In
    ↓
Phone Number Entry (LoginPage)
    ↓
Send OTP to Phone
    ↓
Verify OTP (OTPPage)
    ↓
Profile Setup (Optional)
    ↓
Dashboard (View Conversations & Online Users)
    ↓
Search & Message User OR Select Conversation
    ↓
Chat Interface (Send/Receive Messages)
```

## 📝 TODO - Future Implementation

### Backend
- [ ] JWT Token Generation & Validation
- [ ] OTP Service Integration (Twilio/AWS SNS)
- [ ] WebSocket/Socket.IO for Real-time Messaging
- [ ] File Upload API for Media
- [ ] Input Validation & Error Handling
- [ ] Custom Exception Classes
- [ ] Comprehensive Logging with SLF4J
- [ ] Unit & Integration Tests
- [ ] API Documentation (Swagger/OpenAPI)
- [ ] Rate Limiting & Security Headers

### Frontend
- [ ] WebSocket Integration for Real-time Messages
- [ ] Message Search & Filters
- [ ] Dark Mode
- [ ] Typing Indicators
- [ ] Message Reactions
- [ ] Call/Video Call Feature
- [ ] Status Stories
- [ ] Progressive Web App (PWA) Support
- [ ] Offline Message Queue
- [ ] Image/File Upload UI

### DevOps
- [ ] Docker Support
- [ ] Docker Compose for Full Stack
- [ ] CI/CD Pipeline
- [ ] Production Deployment
- [ ] Performance Optimization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the MIT License.

## 🎯 Key Implementation Details

### OTP Flow
- OTP is generated as 6-digit random number
- OTP validity is 5 minutes
- OTP is stored in database with expiry timestamp
- In production, integrate SMS service to send OTP

### Message Polling
- Frontend polls messages every 3 seconds
- Marks messages as read when conversation is opened
- Fetches last 50 messages per conversation

### Online Status
- User marked as online when logging in
- Marked offline on logout
- Online users list refreshes every 30 seconds
- Last seen timestamp updated on status change

## 💡 Notes

- **Development Only**: OTP is printed to console for testing
- **JWT**: Currently using placeholder tokens, implement proper JWT
- **Real-time**: Current implementation uses polling, use WebSocket for production
- **CORS**: Configured to accept from all origins, restrict in production
