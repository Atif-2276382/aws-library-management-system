# Environment Configuration Guide

This document provides templates for configuring the Library Management System in different environments.

## Development Environment

### backend/application.properties

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=mySecretKey123456789
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.library=DEBUG
logging.level.org.springframework.security=DEBUG

# Scheduling
spring.task.scheduling.thread-name-prefix=libraryms-scheduler-
spring.task.scheduling.pool.size=1
```

### frontend/.env

```
VITE_API_URL=http://localhost:8080/api
```

---

## Staging Environment

### backend/application-staging.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
  shutdown: graceful

spring:
  datasource:
    url: jdbc:mysql://staging-db.rds.amazonaws.com:3306/library_db
    username: admin
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 15
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        format_sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: WARN
    com.library: INFO
  file:
    name: /var/log/libraryms/application.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
```

### frontend/.env.staging

```
VITE_API_URL=https://api-staging.librarym.com/api
```

---

## Production Environment

### backend/application-prod.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
  shutdown: graceful
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        jdbc:
          batch_size: 25
          fetch_size: 100
        order_updates: true
        order_inserts: true
        format_sql: false
        generate_statistics: false

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: WARN
    com.library: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/libraryms/application.log
    max-size: 50MB
    max-history: 90
    total-size-cap: 1GB

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      cloudwatch:
        enabled: true
    tags:
      application: libraryms
      environment: production
```

### frontend/.env.production

```
VITE_API_URL=https://api.librarym.com/api
```

---

## Docker Environment

### backend/.docker/Dockerfile.prod

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/library-management-1.0.0.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Security: Run as non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]
```

---

## AWS Environment Variables

Store these in AWS Secrets Manager or Systems Manager Parameter Store:

### Database Credentials

```json
{
  "username": "admin",
  "password": "GeneratedSecurePassword123!",
  "engine": "mysql",
  "host": "librarym-mysql.xxxxx.rds.amazonaws.com",
  "port": 3306,
  "dbname": "library_db"
}
```

### Application Secrets

```json
{
  "jwt_secret": "GeneratedSecretKeyAtLeast64Characters",
  "database_url": "jdbc:mysql://librarym-mysql.xxxxx.rds.amazonaws.com:3306/library_db",
  "api_base_url": "https://api.librarym.com"
}
```

### EC2 Environment File (/etc/libraryms/app.env)

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://librarym-mysql.xxxxx.rds.amazonaws.com:3306/library_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=GeneratedSecurePassword123!
JWT_SECRET=GeneratedSecretKeyAtLeast64Characters
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS="-Xmx1024m -XX:+UseG1GC -Dspring.profiles.active=prod"
```

---

## Configuration Comparison

| Setting | Development | Staging | Production |
|---------|-------------|---------|-----------|
| Log Level | DEBUG | INFO | WARN |
| Database DDL | update | validate | validate |
| Connection Pool | 5 | 10 | 20 |
| Metrics Export | No | No | CloudWatch |
| HTTPS | No | Yes | Yes |
| Cache Control | No caching | Short TTL | Long TTL |
| CORS | localhost:* | Staging domain | Production domain |

---

## Security Checklist

- [ ] JWT secret is strong (>32 chars, random)
- [ ] Database password is strong and unique
- [ ] Sensitive values stored in AWS Secrets Manager
- [ ] HTTPS enabled on production
- [ ] Security headers configured (X-Frame-Options, X-Content-Type-Options, etc.)
- [ ] Database backup retention configured (7+ days)
- [ ] Encryption at rest enabled
- [ ] VPC security groups restricted
- [ ] Public endpoints validated (/api/auth/login, /api/auth/register only)

---

## Deployment Scripts

### Deploy to Staging

```bash
#!/bin/bash
set -e

ENVIRONMENT=staging
AWS_REGION=us-east-1

# Build backend
cd backend
mvn clean package -DskipTests -Dspring.profiles.active=$ENVIRONMENT

# Build frontend
cd ../frontend
npm ci
VITE_API_URL=https://api-staging.librarym.com/api npm run build

# Deploy frontend
aws s3 sync dist s3://librarym-frontend-staging/ --delete --region $AWS_REGION

# Deploy backend to EC2
scp -i ~/.ssh/staging-key.pem target/library-management-1.0.0.jar ec2-user@staging-backend:/tmp/
ssh -i ~/.ssh/staging-key.pem ec2-user@staging-backend << 'EOF'
  sudo systemctl stop libraryms
  sudo cp /tmp/library-management-1.0.0.jar /opt/libraryms/
  sudo systemctl start libraryms
  sleep 5
  curl -f http://localhost:8080/actuator/health
EOF

echo "Staging deployment completed successfully"
```

### Deploy to Production

```bash
#!/bin/bash
set -e

ENVIRONMENT=prod
AWS_REGION=us-east-1

echo "Starting production deployment..."

# Build and push Docker images
./build-docker-images.sh

# Update ECS task definitions
aws ecs update-service \
  --cluster libraryms-prod \
  --service backend \
  --force-new-deployment \
  --region $AWS_REGION

# Wait for deployment
aws ecs wait services-stable \
  --cluster libraryms-prod \
  --services backend \
  --region $AWS_REGION

# Health check
curl -f https://api.librarym.com/actuator/health

# Smoke tests
./smoke-tests.sh

echo "Production deployment completed successfully"
```

---

## Environment Validation Script

```bash
#!/bin/bash
# validate-config.sh - Validates environment configuration

set -e

check_var() {
  if [ -z "${!1}" ]; then
    echo "❌ Missing: $1"
    return 1
  else
    echo "✓ $1 is set"
    return 0
  fi
}

echo "Validating environment configuration..."

# Check database
check_var "SPRING_DATASOURCE_URL"
check_var "SPRING_DATASOURCE_USERNAME"
check_var "SPRING_DATASOURCE_PASSWORD"

# Check JWT
check_var "JWT_SECRET"

# Check application settings
check_var "SPRING_PROFILES_ACTIVE"

# Database connectivity test
echo "Testing database connection..."
mysql -h $(echo $SPRING_DATASOURCE_URL | cut -d'/' -f3) \
  -u $SPRING_DATASOURCE_USERNAME \
  -p$SPRING_DATASOURCE_PASSWORD \
  -e "SELECT 1;" || echo "⚠️  Database connection failed"

echo "✓ All required environment variables are set"
```

