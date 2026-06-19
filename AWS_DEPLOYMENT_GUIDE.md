# Library Management System - AWS Deployment Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Deployment](#step-by-step-deployment)
4. [Database Setup (RDS)](#database-setup-rds)
5. [Backend Deployment (Spring Boot on EC2)](#backend-deployment-spring-boot-on-ec2)
6. [Frontend Deployment (React on S3 + CloudFront)](#frontend-deployment-react-on-s3--cloudfront)
7. [Environment Configuration](#environment-configuration)
8. [Security Best Practices](#security-best-practices)
9. [Monitoring & Troubleshooting](#monitoring--troubleshooting)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    AWS Infrastructure                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Frontend (React)              Backend (Spring Boot)         │
│  ┌──────────────────┐          ┌──────────────────┐         │
│  │   S3 Bucket      │          │   EC2 Instance   │         │
│  │  (static files)  │          │   (Java app)     │         │
│  └────────┬─────────┘          └────────┬─────────┘         │
│           │                              │                   │
│           └──────────────┬───────────────┘                   │
│                          │                                   │
│                   ┌──────▼──────┐                           │
│                   │ CloudFront  │                           │
│                   │  (CDN)      │                           │
│                   └─────────────┘                           │
│                                                               │
│  Database                                                    │
│  ┌──────────────────┐                                       │
│  │ RDS MySQL        │                                       │
│  │ (managed DB)     │                                       │
│  └──────────────────┘                                       │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Recommended AWS Services
- **Database**: RDS MySQL 8.0 (t3.micro for development, t3.small+ for production)
- **Backend**: EC2 (t3.micro/t3.small) + Load Balancer (optional)
- **Frontend**: S3 + CloudFront + Route 53 (DNS)
- **Secrets**: AWS Secrets Manager
- **Networking**: VPC, Security Groups, NAT Gateway
- **Monitoring**: CloudWatch, CloudTrail

---

## Prerequisites

Before starting, ensure you have:

1. **AWS Account** with appropriate IAM permissions
2. **AWS CLI** installed and configured
   ```bash
   aws configure
   ```
3. **Docker** installed (optional but recommended)
4. **Maven** 3.9+ (for building backend)
5. **Node.js** 18+ (for building frontend)
6. **Git** for version control

---

## Step-by-Step Deployment

### Phase 1: Setup AWS Resources

#### 1.1 Create VPC and Security Groups

```bash
# Create VPC
aws ec2 create-vpc --cidr-block 10.0.0.0/16 --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=LibraryMS-VPC}]'

# Create Public Subnet
aws ec2 create-subnet --vpc-id vpc-xxxxx --cidr-block 10.0.1.0/24 --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=Public-Subnet}]'

# Create Private Subnet (for RDS)
aws ec2 create-subnet --vpc-id vpc-xxxxx --cidr-block 10.0.2.0/24 --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=Private-Subnet}]'
```

#### 1.2 Create Security Groups

```bash
# Security Group for EC2 (Backend)
aws ec2 create-security-group \
  --group-name librarym-backend-sg \
  --description "Security group for Spring Boot backend" \
  --vpc-id vpc-xxxxx

# Allow port 8080 (backend) and 22 (SSH)
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxx \
  --protocol tcp --port 8080 --cidr 0.0.0.0/0 \
  --group-id sg-xxxxx \
  --protocol tcp --port 22 --cidr 0.0.0.0/0

# Security Group for RDS (Database)
aws ec2 create-security-group \
  --group-name librarym-db-sg \
  --description "Security group for MySQL database" \
  --vpc-id vpc-xxxxx

# Allow port 3306 (MySQL) from backend
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxx-db \
  --protocol tcp --port 3306 \
  --source-security-group-id sg-xxxxx
```

---

## Database Setup (RDS)

### Create RDS MySQL Instance

```bash
aws rds create-db-instance \
  --db-instance-identifier librarym-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username admin \
  --master-user-password 'YourSecurePassword123!' \
  --allocated-storage 20 \
  --storage-type gp3 \
  --db-name library_db \
  --vpc-security-group-ids sg-xxxxx \
  --db-subnet-group-name default \
  --publicly-accessible false \
  --backup-retention-period 7 \
  --enable-cloudwatch-logs-exports error general slowquery
```

### Wait for RDS to be available

```bash
aws rds wait db-instance-available --db-instance-identifier librarym-mysql
aws rds describe-db-instances --db-instance-identifier librarym-mysql --query 'DBInstances[0].Endpoint.Address'
```

Store the **Endpoint** (host) for later use.

---

## Backend Deployment (Spring Boot on EC2)

### Step 1: Launch EC2 Instance

```bash
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.small \
  --key-name your-key-pair \
  --security-group-ids sg-xxxxx \
  --subnet-id subnet-xxxxx \
  --iam-instance-profile Name=EC2-App-Role \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=LibraryMS-Backend}]' \
  --user-data file://backend-userdata.sh
```

### Step 2: Connect to EC2 and Deploy

```bash
ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>

# Install Java 17
sudo yum update -y
sudo yum install java-17-amazon-corretto-jdk -y

# Clone repository or copy JAR
git clone https://github.com/your-repo/library-management.git
cd library-management/backend

# Build JAR
mvn clean package -DskipTests

# Create systemd service for auto-restart
sudo tee /etc/systemd/system/libraryms.service > /dev/null <<EOF
[Unit]
Description=Library Management System - Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/library-management/backend
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://RDS-ENDPOINT:3306/library_db"
Environment="SPRING_DATASOURCE_USERNAME=admin"
Environment="SPRING_DATASOURCE_PASSWORD=YourPassword123"
Environment="JWT_SECRET=your-secret-key-here"
ExecStart=java -jar target/library-management-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable libraryms
sudo systemctl start libraryms

# View logs
sudo journalctl -u libraryms -f
```

---

## Frontend Deployment (React on S3 + CloudFront)

### Step 1: Build Frontend

```bash
cd frontend
npm install
npm run build
```

This creates an optimized build in `frontend/dist/`.

### Step 2: Create S3 Bucket

```bash
aws s3 mb s3://librarym-frontend-prod --region us-east-1

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket librarym-frontend-prod \
  --versioning-configuration Status=Enabled

# Block public access (CloudFront will handle access)
aws s3api put-public-access-block \
  --bucket librarym-frontend-prod \
  --public-access-block-configuration "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
```

### Step 3: Upload Build Files to S3

```bash
aws s3 sync frontend/dist s3://librarym-frontend-prod/ \
  --delete \
  --cache-control "public, max-age=300" \
  --exclude "*.html" && \
aws s3 sync frontend/dist s3://librarym-frontend-prod/ \
  --delete \
  --cache-control "public, max-age=0, must-revalidate" \
  --include "*.html"
```

### Step 4: Create CloudFront Distribution

```bash
# Create distribution
aws cloudfront create-distribution --distribution-config file://cloudfront-config.json

# Get Distribution ID
aws cloudfront list-distributions --query 'DistributionList.Items[0].Id'
```

See `cloudfront-config.json` file for configuration.

### Step 5: Configure Backend API URL

Update frontend environment to point to backend:

**frontend/.env.production**
```
VITE_API_URL=https://your-backend-domain.com/api
```

Rebuild and redeploy.

---

## Environment Configuration

### Backend (application-prod.yml)

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

logging:
  level:
    root: INFO
    com.library: DEBUG
```

### Frontend (.env.production)

```
VITE_API_URL=https://api.librarym.com
```

---

## Security Best Practices

### 1. Use AWS Secrets Manager

```bash
# Store database password
aws secretsmanager create-secret \
  --name librarym/db-password \
  --secret-string '{"password":"YourSecurePassword123"}'

# Retrieve in EC2
aws secretsmanager get-secret-value --secret-id librarym/db-password
```

### 2. IAM Roles & Policies

Create an IAM role for EC2 with permissions to:
- Read from Secrets Manager
- Write to CloudWatch Logs
- Access S3 (optional, for backups)

### 3. HTTPS/TLS

- Use **AWS Certificate Manager (ACM)** for free SSL/TLS certificates
- Attach to CloudFront and Application Load Balancer (ALB)

### 4. CORS Configuration

Backend `SecurityConfig`:
```java
config.setAllowedOrigins(List.of(
  "https://librarym.com",
  "https://www.librarym.com"
));
```

### 5. Network Security

- Keep RDS in private subnet (no public access)
- Use Security Groups to restrict traffic
- Enable VPC Flow Logs for monitoring

---

## Monitoring & Troubleshooting

### CloudWatch Monitoring

```bash
# View backend logs
aws logs tail /aws/ec2/libraryms --follow

# Create alarm for high CPU
aws cloudwatch put-metric-alarm \
  --alarm-name libraryms-high-cpu \
  --alarm-description "Alert when backend CPU exceeds 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --alarm-actions arn:aws:sns:us-east-1:123456789:my-topic
```

### Test Backend

```bash
curl -X POST http://your-ec2-ip:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"librarian","password":"librarian123"}'
```

### Test Frontend

Visit: `https://d123.cloudfront.net`

---

## Estimated AWS Costs (Monthly)

| Service | Tier | Estimated Cost |
|---------|------|-----------------|
| EC2 (t3.small) | On-demand | $20-30 |
| RDS MySQL (t3.micro) | Multi-AZ | $50-80 |
| S3 | Storage + requests | $2-5 |
| CloudFront | Data transfer | $5-15 |
| **Total** | | **$77-130/month** |

Use **AWS Calculator** for accurate estimates: https://calculator.aws/

---

## Next Steps

1. Create AWS account and set up CLI
2. Deploy RDS MySQL database
3. Deploy Spring Boot backend on EC2
4. Build and deploy React frontend to S3 + CloudFront
5. Configure custom domain via Route 53
6. Set up monitoring and alerts
7. Test end-to-end functionality

