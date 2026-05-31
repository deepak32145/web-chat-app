# Chat App - Backend Setup

## Database Schema

This SQL script creates the necessary tables for the chat application.

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    profile_picture VARCHAR(500),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    otp VARCHAR(6),
    otp_expiry DATETIME,
    is_phone_verified BOOLEAN DEFAULT FALSE,
    bio VARCHAR(500),
    status VARCHAR(50) DEFAULT 'offline',
    last_seen DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Conversations Table
```sql
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    is_group_chat BOOLEAN DEFAULT FALSE,
    group_icon VARCHAR(500),
    created_by_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_id) REFERENCES users(id)
);
```

### Conversation Participants Table
```sql
CREATE TABLE conversation_participants (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Messages Table
```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(2000) NOT NULL,
    media_url VARCHAR(500),
    media_type VARCHAR(50),
    sender_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);
```

## Indexes

```sql
CREATE INDEX idx_user_phone ON users(phone_number);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_message_conversation ON messages(conversation_id);
CREATE INDEX idx_message_is_read ON messages(is_read);
CREATE INDEX idx_message_created_at ON messages(created_at);
```

## Application Properties

Configure in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.videoapp=DEBUG
```

## API Endpoints

### Authentication
- `POST /api/auth/send-otp` - Send OTP to phone number
- `POST /api/auth/verify-otp` - Verify OTP and login

### Users
- `POST /api/users/search` - Search user by phone number
- `GET /api/users/online` - Get all online users
- `GET /api/users/{userId}` - Get user details
- `PUT /api/users/{userId}/online` - Set user as online
- `PUT /api/users/{userId}/offline` - Set user as offline
- `PUT /api/users/{userId}/profile` - Update user profile

### Conversations
- `POST /api/conversations/create` - Create or get 1-on-1 conversation
- `POST /api/conversations/group` - Create group conversation
- `GET /api/conversations` - Get user's conversations
- `GET /api/conversations/{conversationId}` - Get conversation details

### Messages
- `POST /api/messages` - Send message
- `GET /api/messages/conversation/{conversationId}` - Get messages by conversation
- `GET /api/messages/unread/{conversationId}` - Get unread messages
- `PUT /api/messages/{messageId}/read` - Mark message as read
- `PUT /api/messages/conversation/{conversationId}/read` - Mark conversation as read

## Running the Application

1. **Build the project**:
```bash
mvn clean package
```

2. **Run the application**:
```bash
mvn spring-boot:run
```

Or:
```bash
java -jar target/chat-app-1.0.0.jar
```

3. **Access the application**:
   - Backend API: http://localhost:8080
   - Frontend (React): http://localhost:3000

## TODO - Implementation Notes

1. **JWT Token Generation**: Currently using placeholder tokens. Implement proper JWT token generation and validation.
2. **OTP Service Integration**: Integrate with SMS service providers (Twilio, AWS SNS, etc.) to send actual OTPs.
3. **Real-time Messaging**: Implement WebSocket using Socket.IO for real-time message delivery.
4. **File Upload**: Add file upload functionality for media sharing.
5. **Security**: Implement proper security with JWT, CORS configuration, and input validation.
6. **Error Handling**: Comprehensive error handling and custom exception classes.
7. **Logging**: Implement proper logging with SLF4J and Logback.
8. **Testing**: Add unit tests and integration tests.
