# Real-Time Messaging System (AWS WebSocket API)

A serverless real-time messaging application using AWS API Gateway WebSockets, Lambda, and DynamoDB.

## Prerequisites

- AWS Account with admin permissions
- AWS CLI configured (`aws configure`)
- AWS SAM CLI installed (`brew install aws-sam-cli`)
- Java 11+ JDK
- Gradle 7+
- Node.js (for client-side testing)

## Deployment

### 1. Build the Application

```bash
# Build the Lambda deployment package
mvn clean build

# Package the application
sam build
```

# Configuration
## Environment Variables
Set these in AWS Lambda console after deployment:


- MESSAGES_TABLE : 
DynamoDB table for messages

- USERS_TABLE : 	
DynamoDB table for users
- JWT_SECRET :
Secret for JWT tokens
- COGNITO_USER_POOL_ID :	
Cognito User Pool ID
- COGNITO_CLIENT_ID	:
Cognito App Client ID


# Architecture

Client → WebSocket API → Lambda ($connect, $disconnect, $default)  
    → Lambda (HTTP API routes)  
    → DynamoDB (Messages, Users)  
    → SNS (Notifications)


# Register a user
curl -X POST $REST_API_URL/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testuser","password":"Testpass123!"}'

# Send message via HTTP
curl -X POST $REST_API_URL/messages \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{"recipientId":"user456","content":"Hello via HTTP"}'

