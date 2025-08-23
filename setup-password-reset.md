# Password Reset Setup Guide

## 🚀 Quick Setup Steps

### 1. Environment Configuration

**Backend - Visitor Pass Service (.env file)**
```bash
# Create: backend/visitor-pass-service/.env
DB_PASSWORD=your_mysql_password
JWT_SECRET=a-very-long-and-secure-random-string-for-jwt-at-least-32-characters
INTERNAL_API_KEY=another-long-random-string-for-service-communication
```

**Backend - Notification Service (.env file)**
```bash
# Create: backend/notification-service/.env
DB_PASSWORD=your_mysql_password
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

### 2. Gmail App Password Setup (for email testing)

1. Go to Google Account settings
2. Enable 2-Factor Authentication
3. Generate App Password for "Mail"
4. Use this App Password (not your regular password) in MAIL_PASSWORD

### 3. Start Services in Order

**Terminal 1 - Start MySQL**
```bash
# Make sure MySQL is running on port 3306
```

**Terminal 2 - Start RabbitMQ**
```bash
# Install and start RabbitMQ
# Windows: Download from https://www.rabbitmq.com/download.html
# Or use Docker: docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**Terminal 3 - Start Visitor Pass Service**
```bash
cd backend/visitor-pass-service/visitor-pass-service
./mvnw.cmd spring-boot:run
```

**Terminal 4 - Start Notification Service**
```bash
cd backend/notification-service/notification-service
./mvnw.cmd spring-boot:run
```

**Terminal 5 - Start Frontend**
```bash
cd frontend/visitor-pass-frontend
npm install
npm start
```

### 4. Test the Setup

**Step 1: Test API Directly**
Open the test file in browser: `test-password-reset.html`

**Step 2: Test Email Functionality**
```bash
curl -X POST http://localhost:8080/api/auth/test-email \
-H "Content-Type: application/json" \
-d '{"email":"your.test@email.com"}'
```

**Step 3: Test Full Flow**
1. Go to http://localhost:4200/login
2. Click "Forgot your password?"
3. Enter a valid user email
4. Check logs and email

### 5. Troubleshooting

**Check Backend Logs for:**
- "Initiating password reset for email: [email]"
- "Password reset event sent successfully for user: [email]"
- "Received PasswordResetEvent for user: [email]"

**Common Issues:**

1. **RabbitMQ Connection Failed**
   - Ensure RabbitMQ is running on port 5672
   - Check firewall settings

2. **Email Not Sent**
   - Verify Gmail App Password
   - Check notification service logs
   - Test with /test-email endpoint

3. **User Not Found**
   - Ensure user exists in database
   - Check email spelling

4. **Database Connection**
   - Verify MySQL is running
   - Check .env file configuration

### 6. Manual Database Check

```sql
-- Check if user exists
SELECT * FROM users WHERE email = 'your.test@email.com';

-- Check password reset tokens
SELECT * FROM password_reset_tokens ORDER BY created_at DESC;

-- Check email audit logs (in notification_db)
SELECT * FROM email_audit_logs ORDER BY created_at DESC;
```

### 7. Service URLs

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- Notification Service: http://localhost:8081
- RabbitMQ Management: http://localhost:15672 (guest/guest)

### 8. Test Endpoints

- Forgot Password: POST http://localhost:8080/api/auth/forgot-password
- Reset Password: POST http://localhost:8080/api/auth/reset-password
- Test Email: POST http://localhost:8080/api/auth/test-email
