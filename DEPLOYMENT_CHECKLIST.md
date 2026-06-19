# AWS Deployment Checklist

Complete this checklist before, during, and after AWS deployment.

## Pre-Deployment (Planning Phase)

### AWS Account Setup
- [ ] AWS account created
- [ ] IAM users with appropriate permissions created
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Access keys secured in safe location

### Project Preparation
- [ ] Code committed and pushed to Git (main branch)
- [ ] All tests passing locally
- [ ] Security scan completed (OWASP, CVE check)
- [ ] Dependencies updated to latest versions
- [ ] No hardcoded credentials in source code
- [ ] Environment variables documented

### AWS Service Planning
- [ ] VPC design documented
- [ ] Security groups and firewall rules defined
- [ ] Database backup strategy defined
- [ ] Monitoring and alerting configured
- [ ] Cost estimate reviewed

## Infrastructure Setup (Phase 1)

### Option A: Using Terraform
- [ ] Terraform installed (`terraform version`)
- [ ] AWS credentials configured
- [ ] `terraform/terraform.tfvars` created with correct values
- [ ] `terraform plan` executed and reviewed
- [ ] `terraform apply` executed
- [ ] All outputs (RDS endpoint, EC2 IP, etc.) saved

### Option B: Manual Setup
- [ ] VPC created (10.0.0.0/16)
- [ ] Public subnet created (10.0.1.0/24)
- [ ] Private subnet created (10.0.2.0/24)
- [ ] Internet Gateway attached
- [ ] Route tables configured
- [ ] Security groups created:
  - [ ] EC2 security group (allows 22, 8080)
  - [ ] RDS security group (allows 3306 from EC2)
- [ ] RDS MySQL instance created
  - [ ] Engine: MySQL 8.0.35+
  - [ ] Multi-AZ enabled (prod only)
  - [ ] Automated backups enabled
  - [ ] Encryption enabled
- [ ] EC2 instance launched
  - [ ] Instance type: t3.small or larger
  - [ ] Java 17 installed
  - [ ] Key pair secured locally

## Database Setup (Phase 2)

### RDS Configuration
- [ ] RDS endpoint noted: `____________________`
- [ ] RDS port noted: `____________________`
- [ ] Master username: `admin`
- [ ] Master password securely stored in AWS Secrets Manager
- [ ] Database name: `library_db`
- [ ] Backup retention: ≥7 days
- [ ] Multi-AZ enabled (for production)
- [ ] Automated backups configured
- [ ] Database encryption enabled

### Schema & Initial Data
- [ ] Connected to RDS using MySQL client
- [ ] Created tables (schema.sql executed)
- [ ] Loaded sample data (data.sql executed)
- [ ] Verified tables and data exist
- [ ] Test user created: `librarian` / `librarian123`

## Backend Deployment (Phase 3)

### Build & Prepare
- [ ] Maven build successful: `mvn clean package -DskipTests`
- [ ] JAR artifact created: `library-management-1.0.0.jar`
- [ ] Application properties updated:
  - [ ] Database URL points to RDS
  - [ ] JWT secret generated and configured
  - [ ] Log level set to appropriate level
  - [ ] Actuator endpoints configured

### EC2 Deployment
- [ ] SSH connection to EC2 verified
- [ ] Java 17 installed and verified: `java -version`
- [ ] Maven/Gradle installed (if building on EC2)
- [ ] Git cloned or JAR uploaded
- [ ] Systemd service file created: `/etc/systemd/system/libraryms.service`
- [ ] Environment file created: `/etc/libraryms/app.env`
- [ ] Service enabled: `sudo systemctl enable libraryms`
- [ ] Service started: `sudo systemctl start libraryms`
- [ ] Service status verified: `sudo systemctl status libraryms`
- [ ] Logs checked for errors: `sudo journalctl -u libraryms -f`
- [ ] Service restarts on failure configured
- [ ] Service auto-starts on reboot configured

### Backend Verification
- [ ] Backend responds on port 8080
- [ ] Health endpoint accessible: `curl http://<EC2_IP>:8080/api/actuator/health`
- [ ] Login endpoint works: `curl -X POST http://<EC2_IP>:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"librarian","password":"librarian123"}'`
- [ ] JWT token received in response
- [ ] Token stored in response
- [ ] Logs show no errors

### Monitoring
- [ ] CloudWatch Logs enabled for backend
- [ ] CloudWatch agent installed (optional)
- [ ] Alarms configured:
  - [ ] CPU > 80%
  - [ ] Memory > 80%
  - [ ] Disk > 80%
  - [ ] Service stopped

## Frontend Deployment (Phase 4)

### Build & Prepare
- [ ] Node.js 18+ installed: `node -v`
- [ ] npm dependencies installed: `npm ci`
- [ ] Frontend build successful: `npm run build`
- [ ] Build artifacts in `dist/` directory
- [ ] Environment configuration set:
  - [ ] `VITE_API_URL` points to EC2 backend
  - [ ] No hardcoded credentials

### S3 Setup
- [ ] S3 bucket created: `librarym-frontend-prod`
- [ ] Bucket versioning enabled
- [ ] Public access blocked
- [ ] Bucket policy configured for CloudFront access (if using CDN)
- [ ] CORS configuration set (if needed)

### Frontend Upload
- [ ] Build artifacts synced to S3:
  ```bash
  aws s3 sync dist s3://librarym-frontend-prod/ --delete
  ```
- [ ] Files verified in S3 console
- [ ] index.html present
- [ ] CSS and JS files present
- [ ] Asset files present

### CloudFront Setup (Optional, Recommended)
- [ ] CloudFront distribution created
- [ ] Origin set to S3 bucket
- [ ] Origin Access Identity (OAI) configured
- [ ] Cache behavior configured:
  - [ ] HTML: no-cache
  - [ ] JS/CSS/Images: 1 year cache
- [ ] Error pages configured (404 → index.html)
- [ ] Distribution deployed
- [ ] Distribution URL noted: `____________________`
- [ ] Cache invalidated: `aws cloudfront create-invalidation --distribution-id <ID> --paths "/*"`

### Frontend Verification
- [ ] S3 website accessible (if enabled)
- [ ] CloudFront distribution accessible
- [ ] Frontend loads without errors
- [ ] API calls work (check browser Network tab)
- [ ] Login form visible
- [ ] CORS headers present in responses

## Testing & Validation (Phase 5)

### Authentication Flow
- [ ] Login page displays
- [ ] Can login with demo credentials
- [ ] JWT token generated and stored
- [ ] Dashboard displays after login
- [ ] User info displayed correctly
- [ ] Logout works and clears token

### Role-Based Access
- [ ] Librarian can access: Books, Authors, Members, Lendings, Notifications
- [ ] Member can access: My Lendings, My Notifications, Browse Books
- [ ] Unauthorized pages blocked with redirect to login
- [ ] Error messages clear and helpful

### API Endpoints
- [ ] GET /api/books - retrieves list
- [ ] POST /api/lendings/issue - issues book
- [ ] PUT /api/lendings/return/{id} - returns book
- [ ] GET /api/notifications - retrieves notifications
- [ ] All endpoints return proper HTTP status codes

### Database Operations
- [ ] Data persists after page refresh
- [ ] Multiple user logins work independently
- [ ] Data integrity maintained
- [ ] No SQL errors in logs

### Performance
- [ ] Page load time < 3 seconds
- [ ] API response time < 500ms
- [ ] No memory leaks in logs
- [ ] No N+1 query problems

### Security
- [ ] HTTPS enforced (if using CloudFront)
- [ ] CORS properly configured
- [ ] JWT validation working
- [ ] No sensitive data in console logs
- [ ] CSRF protection enabled (backend)
- [ ] SQL injection protected (parameterized queries)
- [ ] XSS protection enabled

## Monitoring & Maintenance Setup (Phase 6)

### CloudWatch
- [ ] Log groups created for backend
- [ ] Log retention configured (30 days)
- [ ] Alarms configured:
  - [ ] Backend CPU alarm
  - [ ] Backend memory alarm
  - [ ] Database connection pool alarm
  - [ ] Application error rate alarm
- [ ] Dashboard created with key metrics
- [ ] SNS topic configured for alerts
- [ ] Email notifications enabled

### Automated Backups
- [ ] RDS automated backups enabled
- [ ] Backup retention period set (7+ days)
- [ ] Backup window configured
- [ ] Test backup restore procedure documented

### Logging & Auditing
- [ ] CloudTrail enabled for audit logging
- [ ] S3 access logging configured
- [ ] Application logs centralized in CloudWatch
- [ ] Log retention policies set

## Security Hardening (Phase 7)

### Networking Security
- [ ] VPC Flow Logs enabled
- [ ] Security group rules minimal (least privilege)
- [ ] NACLs configured (if needed)
- [ ] No unnecessary ports open
- [ ] SSH restricted to specific IPs (not 0.0.0.0/0)

### Secrets Management
- [ ] JWT secret in AWS Secrets Manager
- [ ] Database password in AWS Secrets Manager
- [ ] EC2 IAM role with minimal permissions
- [ ] No credentials in environment variables (use Secrets Manager)
- [ ] SSH key pair backup secured

### SSL/TLS
- [ ] ACM certificate requested (free)
- [ ] Certificate attached to CloudFront
- [ ] HTTPS enforced (redirect HTTP → HTTPS)
- [ ] HSTS header configured
- [ ] Certificate auto-renewal enabled

### Application Security
- [ ] Security headers configured:
  - [ ] X-Frame-Options: SAMEORIGIN
  - [ ] X-Content-Type-Options: nosniff
  - [ ] X-XSS-Protection: 1; mode=block
  - [ ] Referrer-Policy: strict-origin-when-cross-origin
- [ ] CORS whitelist configured (no wildcard)
- [ ] CSRF tokens verified
- [ ] SQL injection prevention validated
- [ ] XSS protection validated

## Post-Deployment (Phase 8)

### Documentation
- [ ] Architecture diagram created
- [ ] Deployment steps documented
- [ ] Runbook created for common issues
- [ ] Rollback procedure documented
- [ ] Contact information documented

### Automation
- [ ] CI/CD pipeline set up (GitHub Actions)
- [ ] Automated tests in pipeline
- [ ] Automated deployment on merge to main
- [ ] Deployment notifications configured

### Cost Management
- [ ] AWS Cost Explorer reviewed
- [ ] Monthly budget alert set
- [ ] Reserved instances considered (for production)
- [ ] Unused resources identified and removed

### Knowledge Transfer
- [ ] Team trained on deployment process
- [ ] On-call procedure established
- [ ] Escalation path defined
- [ ] Monitoring dashboards explained

## Rollback Plan

If critical issues found:

1. **Immediate**: Stop traffic to deployment
   ```bash
   # Disable EC2 instance
   aws ec2 stop-instances --instance-ids i-xxxxx
   
   # Revert CloudFront to previous S3 sync
   aws s3 sync s3://librarym-frontend-backup/ s3://librarym-frontend-prod/ --delete
   aws cloudfront create-invalidation --distribution-id <ID> --paths "/*"
   ```

2. **Communicate**: Notify team of issue
3. **Analyze**: Review logs and determine root cause
4. **Fix**: Apply fix to code and rebuild
5. **Retest**: Verify fix locally and in staging
6. **Redeploy**: Execute deployment process again

## Sign-Off

- [ ] Project Lead: _________________ Date: _______
- [ ] DevOps Engineer: _________________ Date: _______
- [ ] QA Lead: _________________ Date: _______
- [ ] Security Lead: _________________ Date: _______

---

**Deployment Date**: _______________
**Deployed By**: _______________
**Approved By**: _______________
**Issues Found**: _________________ / None

