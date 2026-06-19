# AWS Deployment - Quick Start Guide

This guide provides step-by-step instructions for deploying the Library Management System to AWS.

## Prerequisites

- AWS Account with appropriate IAM permissions
- AWS CLI v2 installed and configured: `aws configure`
- Docker installed (for local testing)
- Git, Maven, Node.js 18+

## Option 1: Quick Start with Docker Compose (Local Testing)

Perfect for testing the full stack locally before deploying to AWS.

```bash
# Clone repository
git clone https://github.com/your-repo/library-management.git
cd library-management

# Build and run all services
docker-compose up -d

# Access services
# Frontend: http://localhost
# Backend: http://localhost:8080
# MySQL: localhost:3306
```

### Test Login
```bash
# Demo credentials
Username: librarian
Password: librarian123
```

Stop services:
```bash
docker-compose down
```

---

## Option 2: Deploy with Terraform (Recommended for Production)

Fully automated AWS infrastructure creation.

### 1. Prepare Terraform

```bash
cd terraform

# Create terraform.tfvars with your configuration
cat > terraform.tfvars <<EOF
aws_region       = "us-east-1"
environment      = "prod"
instance_type    = "t3.small"
db_instance_class = "db.t3.micro"
key_pair_name    = "your-key-pair"  # Create in AWS console first
EOF
```

### 2. Initialize and Deploy

```bash
# Initialize Terraform
terraform init

# Review planned changes
terraform plan

# Apply configuration
terraform apply

# Note the outputs (RDS endpoint, EC2 IP, etc.)
terraform output
```

### 3. Configure Backend

```bash
# SSH into EC2 instance
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# Configure environment
sudo vi /etc/libraryms/app.env
# Update with RDS endpoint and JWT secret from terraform output

# Restart service
sudo systemctl restart libraryms
sudo journalctl -u libraryms -f  # View logs
```

### 4. Deploy Frontend

```bash
# Build frontend
cd frontend
npm install
npm run build

# Deploy to S3
aws s3 sync dist s3://librarym-frontend-prod/ --delete

# Invalidate CloudFront cache (if using CDN)
aws cloudfront create-invalidation --distribution-id <DIST_ID> --paths "/*"
```

---

## Option 3: Manual AWS Setup (Step-by-Step)

For detailed control over each component.

### Step 1: Create RDS MySQL Database

```bash
# Create DB instance
aws rds create-db-instance \
  --db-instance-identifier librarym-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username admin \
  --master-user-password 'YourSecurePassword123!' \
  --allocated-storage 20 \
  --db-name library_db

# Wait for DB to be available
aws rds wait db-instance-available --db-instance-identifier librarym-mysql

# Get endpoint
aws rds describe-db-instances \
  --db-instance-identifier librarym-mysql \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text
```

### Step 2: Launch EC2 Instance

```bash
# Create security group
aws ec2 create-security-group \
  --group-name librarym-backend-sg \
  --description "Security group for backend"

# Allow ports 22 (SSH) and 8080 (backend)
aws ec2 authorize-security-group-ingress \
  --group-name librarym-backend-sg \
  --protocol tcp --port 22 --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-name librarym-backend-sg \
  --protocol tcp --port 8080 --cidr 0.0.0.0/0

# Launch EC2 instance
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.small \
  --key-name your-key \
  --security-groups librarym-backend-sg

# Get public IP
aws ec2 describe-instances \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text
```

### Step 3: Deploy Backend on EC2

```bash
# SSH into instance
ssh -i your-key.pem ec2-user@<PUBLIC_IP>

# Install Java
sudo yum update -y
sudo yum install java-17-amazon-corretto-jdk -y

# Clone and build
git clone https://github.com/your-repo/library-management.git
cd library-management/backend
mvn clean package -DskipTests

# Create systemd service
sudo tee /etc/systemd/system/libraryms.service > /dev/null <<'EOF'
[Unit]
Description=Library Management Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/library-management/backend
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://RDS-ENDPOINT:3306/library_db"
Environment="SPRING_DATASOURCE_USERNAME=admin"
Environment="SPRING_DATASOURCE_PASSWORD=YourPassword123"
Environment="JWT_SECRET=your-secret-key"
ExecStart=java -jar target/library-management-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Start service
sudo systemctl daemon-reload
sudo systemctl enable libraryms
sudo systemctl start libraryms

# View logs
sudo journalctl -u libraryms -f
```

### Step 4: Deploy Frontend to S3

```bash
# Create S3 bucket
aws s3 mb s3://librarym-frontend-prod

# Build React app
cd frontend
npm install
VITE_API_URL=http://<EC2_IP>:8080/api npm run build

# Upload to S3
aws s3 sync dist s3://librarym-frontend-prod/ --delete

# Set CORS and bucket policy for web hosting
aws s3 website s3://librarym-frontend-prod/ --index-document index.html --error-document index.html
```

### Step 5: Setup CloudFront CDN (Optional)

```bash
# Create Origin Access Identity
aws cloudfront create-cloud-front-origin-access-identity \
  --cloud-front-origin-access-identity-config '{"CallerReference":"librarym-oai","Comment":"OAI for LibraryMS"}'

# Create distribution pointing to S3
aws cloudfront create-distribution --distribution-config file://cloudfront-config.json
```

---

## Verification Checklist

After deployment, verify:

- [ ] RDS database is running and accessible
- [ ] EC2 instance is running and can SSH into
- [ ] Spring Boot backend is running on port 8080
- [ ] Frontend is deployed to S3/CloudFront
- [ ] Backend logs show no errors: `sudo journalctl -u libraryms -f`
- [ ] Test login endpoint: `curl -X POST http://<EC2_IP>:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"librarian","password":"librarian123"}'`
- [ ] Frontend loads and login form displays
- [ ] Can login successfully with demo credentials
- [ ] Can view dashboard and access features per role

---

## Environment Variables Reference

### Backend (application-prod.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://librarym-mysql.xxxxx.rds.amazonaws.com:3306/library_db
    username: admin
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

### Frontend (.env.production)

```
VITE_API_URL=https://api.yourdomain.com
```

---

## Cost Estimation

| Component | Tier | Monthly Cost |
|-----------|------|--------------|
| RDS (db.t3.micro, multi-AZ) | Production | $50-80 |
| EC2 (t3.small, 730h/month) | On-demand | $20-30 |
| S3 + CloudFront | Minimal traffic | $5-15 |
| Data Transfer | Within AWS | ~$0 |
| **Total** | | **~$75-125** |

---

## Troubleshooting

### Backend won't start

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@<IP>

# Check logs
sudo journalctl -u libraryms -f

# Check database connection
mysql -h RDS-ENDPOINT -u admin -p -e "SELECT 1;"

# Check Java/Maven
java -version
mvn -version
```

### Frontend blank page

1. Check browser console for errors (F12)
2. Verify CORS configuration in SecurityConfig.java
3. Check S3 bucket permissions and CORS rules
4. Test backend API directly: `curl http://<EC2_IP>:8080/api/auth/me -H "Authorization: Bearer TOKEN"`

### Database connection errors

```bash
# Test connection from EC2
mysql -h <RDS-ENDPOINT> -u admin -p

# Check Security Groups
aws ec2 describe-security-groups --group-ids sg-xxxxx
```

---

## Cleanup

### Delete all AWS resources

```bash
# If using Terraform
cd terraform
terraform destroy

# If manual setup
aws rds delete-db-instance --db-instance-identifier librarym-mysql --skip-final-snapshot
aws ec2 terminate-instances --instance-ids i-xxxxx
aws s3 rb s3://librarym-frontend-prod --force
aws ec2 delete-security-group --group-name librarym-backend-sg
```

---

## Next Steps

1. [Set up custom domain with Route 53](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/routing-to-cloudfront-distribution.html)
2. [Enable HTTPS/TLS with Certificate Manager](https://docs.aws.amazon.com/acm/)
3. [Setup monitoring with CloudWatch](https://docs.aws.amazon.com/cloudwatch/)
4. [Implement backup strategies](https://docs.aws.amazon.com/aws-backup/)
5. [Setup CI/CD pipeline with CodePipeline](https://docs.aws.amazon.com/codepipeline/)

