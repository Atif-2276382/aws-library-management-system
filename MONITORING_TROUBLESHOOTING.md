# AWS Deployment - Monitoring & Troubleshooting Guide

## Real-Time Monitoring

### CloudWatch Dashboard Setup

Create a dashboard to monitor all key metrics:

```bash
# Create CloudWatch dashboard
aws cloudwatch put-dashboard \
  --dashboard-name LibraryMS-Prod \
  --dashboard-body file://cloudwatch-dashboard.json
```

### Key Metrics to Monitor

| Metric | Threshold | Action |
|--------|-----------|--------|
| EC2 CPU Utilization | > 80% | Scale up or optimize code |
| EC2 Memory Usage | > 80% | Check for memory leaks |
| RDS CPU | > 75% | Add read replicas or optimize queries |
| RDS Storage | > 80% | Increase allocated storage |
| Application Error Rate | > 1% | Review logs and fix errors |
| API Response Time | > 500ms | Check database queries, add caching |
| Database Connections | > 80% of max | Increase connection pool or check for leaks |

---

## Application Logs

### Access Application Logs

```bash
# SSH into EC2
ssh -i librarym-key.pem ec2-user@<EC2_IP>

# View real-time logs
sudo journalctl -u libraryms -f

# View last 100 lines
sudo journalctl -u libraryms -n 100

# View logs from specific time range
sudo journalctl -u libraryms --since "2024-01-01 10:00:00" --until "2024-01-01 11:00:00"

# View logs with grep filter
sudo journalctl -u libraryms | grep "ERROR"
sudo journalctl -u libraryms | grep "SQLException"

# Export logs to file
sudo journalctl -u libraryms > libraryms-logs.txt
```

### View CloudWatch Logs

```bash
# List log groups
aws logs describe-log-groups

# View log streams
aws logs describe-log-streams --log-group-name /aws/ec2/libraryms

# Get latest logs
aws logs tail /aws/ec2/libraryms --follow --max-items 100

# Search logs
aws logs filter-log-events \
  --log-group-name /aws/ec2/libraryms \
  --filter-pattern "ERROR"
```

---

## Common Issues & Solutions

### Issue 1: Backend Service Stops Randomly

**Symptoms**: Service crashes without warning, logs show gaps

**Diagnosis**:
```bash
# Check if service restarted
sudo journalctl -u libraryms --output verbose | grep "Restart"

# Check system resources
free -h  # Memory
df -h    # Disk
top      # CPU usage

# Check Java heap usage
jstat -gc <PID> 1000
```

**Solutions**:
1. **Memory Leak**: Increase heap size in systemd service
   ```bash
   sudo vim /etc/systemd/system/libraryms.service
   # Add to [Service] section:
   # ExecStart=java -Xmx1024m -XX:+UseG1GC -jar ...
   ```

2. **Disk Space**: Clean up old logs
   ```bash
   # Compress old logs
   find /var/log/libraryms -name "*.log" -mtime +30 -exec gzip {} \;
   
   # Delete ancient logs
   find /var/log/libraryms -name "*.log.gz" -mtime +90 -delete
   ```

3. **Out of Connections**: Increase connection pool
   ```yaml
   # application-prod.yml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 30  # Increase from 20
   ```

### Issue 2: Database Slow Queries

**Symptoms**: API responses slow (> 1000ms), user complaints

**Diagnosis**:
```bash
# Enable slow query log on RDS
aws rds modify-db-instance \
  --db-instance-identifier librarym-mysql \
  --enable-cloudwatch-logs-exports slowquery \
  --apply-immediately

# View slow queries
aws logs tail /aws/rds/instance/librarym-mysql/slowquery
```

**Solutions**:
1. **Add Database Indexes**:
   ```sql
   -- Check current indexes
   SHOW INDEX FROM lendings;
   
   -- Add missing indexes
   CREATE INDEX idx_member_id ON lendings(member_id);
   CREATE INDEX idx_book_id ON lendings(book_id);
   CREATE INDEX idx_return_date ON lendings(return_date);
   ```

2. **Optimize N+1 Queries**: Use `@EntityGraph` or `fetch join`
   ```java
   @EntityGraph(attributePaths = {"book", "member"})
   List<Lending> findAll();
   ```

3. **Enable Query Caching**:
   ```yaml
   spring:
     cache:
       type: redis
   ```

### Issue 3: High Memory Usage

**Symptoms**: EC2 instance slowing down, OOM errors in logs

**Diagnosis**:
```bash
# Check memory usage
free -h
top

# Check Java heap
jmap -heap <PID> | grep -A 5 "Heap Configuration"

# Get heap dump (WARNING: will pause app)
jmap -dump:live,format=b,file=heap.bin <PID>
```

**Solutions**:
1. **Increase EC2 Instance Size**:
   ```bash
   # Stop instance
   aws ec2 stop-instances --instance-ids i-xxxxx
   
   # Change instance type (t3.small → t3.medium)
   aws ec2 modify-instance-attribute --instance-id i-xxxxx --instance-type t3.medium
   
   # Start instance
   aws ec2 start-instances --instance-ids i-xxxxx
   ```

2. **Find Memory Leaks**:
   ```bash
   # Analyze heap dump with Eclipse MAT
   # Download: https://www.eclipse.org/mat/downloads.php
   # Load heap.bin file and analyze object retention paths
   ```

### Issue 4: CORS Errors

**Symptoms**: Frontend can't call backend, browser shows CORS error

**Error**: `Access to XMLHttpRequest has been blocked by CORS policy`

**Solution**:
```java
// Update SecurityConfig.java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(request -> {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "https://yourdomain.com",
            "https://www.yourdomain.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        return configuration;
    }));
    return http.build();
}
```

### Issue 5: Database Connection Pool Exhausted

**Symptoms**: `Cannot get a connection, pool error Timeout waiting for idle object`

**Diagnosis**:
```sql
-- Check active connections
SHOW PROCESSLIST;

-- Check max connections
SHOW VARIABLES LIKE 'max_connections';
```

**Solutions**:
1. **Increase Connection Pool**:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 30
         minimum-idle: 5
   ```

2. **Close Connections Properly**: Ensure all connections are returned
   ```java
   // Use try-with-resources
   try (Connection conn = dataSource.getConnection()) {
       // Use connection
   } // Automatically closed
   ```

3. **Monitor Connection Usage**:
   ```sql
   -- Find idle connections
   SHOW PROCESSLIST WHERE command = 'Sleep' AND time > 600;
   KILL <PROCESS_ID>;
   ```

### Issue 6: SSL Certificate Expired

**Symptoms**: Browsers show security warning, HTTPS fails

**Solution**:
```bash
# List certificates
aws acm list-certificates

# Request new certificate
aws acm request-certificate \
  --domain-name yourdomain.com \
  --validation-method DNS

# Validate and attach to CloudFront
# (Usually auto-renewed if created via ACM)
```

### Issue 7: Frontend Not Updating After Deployment

**Symptoms**: New code deployed but users see old version

**Causes**: CloudFront or browser cache

**Solutions**:
1. **Invalidate CloudFront Cache**:
   ```bash
   aws cloudfront create-invalidation \
     --distribution-id <ID> \
     --paths "/*"
   ```

2. **Clear Browser Cache**:
   - Ctrl+Shift+Delete (Windows)
   - Cmd+Shift+Delete (Mac)

3. **Hard Refresh**:
   - Ctrl+F5 (Windows)
   - Cmd+Shift+R (Mac)

---

## Performance Tuning

### Database Query Optimization

```sql
-- Check slow queries
SELECT * FROM mysql.slow_log LIMIT 10;

-- Analyze query performance
EXPLAIN SELECT * FROM lendings 
  WHERE member_id = ? 
  AND return_date > NOW();

-- Add necessary indexes
ALTER TABLE lendings ADD INDEX idx_member_return 
  (member_id, return_date);
```

### Application Performance

```yaml
# application-prod.yml - Performance tuning
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 25
          fetch_size: 100
        order_updates: true
        order_inserts: true
  servlet:
    multipart:
      max-file-size: 10MB

server:
  compression:
    enabled: true
    mime-types: application/json,text/html,text/css,application/javascript
    min-response-size: 1024
```

### Frontend Performance

```bash
# Analyze bundle size
npm install -D @vitejs/plugin-visualize

# In vite.config.js:
import { visualizer } from "rollup-plugin-visualize";

export default {
  plugins: [visualizer()]
}

npm run build  # Generates stats.html
```

---

## Backup & Disaster Recovery

### Automated RDS Backups

```bash
# Verify backup settings
aws rds describe-db-instances \
  --db-instance-identifier librarym-mysql \
  --query 'DBInstances[0].[BackupRetentionPeriod,PreferredBackupWindow]'

# Output should show: [7, "03:00-04:00"] (7 days, 3 AM UTC)

# Manual backup
aws rds create-db-snapshot \
  --db-instance-identifier librarym-mysql \
  --db-snapshot-identifier librarym-backup-$(date +%Y%m%d)
```

### Test Restore Procedure

```bash
# Create DB from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier librarym-mysql-restored \
  --db-snapshot-identifier librarym-backup-20240101

# Wait for restoration
aws rds wait db-instance-available \
  --db-instance-identifier librarym-mysql-restored
```

### Frontend Backup (S3)

```bash
# Sync S3 to local backup
aws s3 sync s3://librarym-frontend-prod ./backup-$(date +%Y%m%d)

# List old versions (if versioning enabled)
aws s3api list-object-versions \
  --bucket librarym-frontend-prod | head -20
```

---

## Alerts & Notifications

### Create CloudWatch Alarms

```bash
# High CPU alarm
aws cloudwatch put-metric-alarm \
  --alarm-name libraryms-cpu-high \
  --alarm-description "CPU above 80%" \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --alarm-actions arn:aws:sns:us-east-1:123456789:alerts

# RDS storage alarm
aws cloudwatch put-metric-alarm \
  --alarm-name libraryms-db-storage-high \
  --alarm-description "Database storage above 80%" \
  --metric-name FreeStorageSpace \
  --namespace AWS/RDS \
  --statistic Average \
  --period 300 \
  --threshold 20000000000 \
  --comparison-operator LessThanThreshold

# Application error rate alarm
aws cloudwatch put-metric-alarm \
  --alarm-name libraryms-error-rate-high \
  --alarm-description "Application errors above 5%" \
  --metric-name ApplicationErrors \
  --namespace CustomApp \
  --statistic Sum \
  --period 300 \
  --threshold 5 \
  --comparison-operator GreaterThanThreshold
```

### Email Notifications

```bash
# Create SNS topic
aws sns create-topic --name libraryms-alerts

# Subscribe email
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:123456789:libraryms-alerts \
  --protocol email \
  --notification-endpoint your-email@example.com
```

---

## Security Auditing

### Check Security Configuration

```bash
# Verify HTTPS is enforced
curl -I https://yourdomain.com

# Check security headers
curl -I https://yourdomain.com | grep -i "X-"

# Verify CORS is configured
curl -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: POST" \
  https://api.yourdomain.com/auth/login -v

# Check database encryption
aws rds describe-db-instances \
  --query 'DBInstances[0].StorageEncrypted'
```

### Review Access Logs

```bash
# CloudTrail logs
aws cloudtrail lookup-events \
  --max-results 50 | jq '.Events[] | {EventTime, EventName, Username}'

# S3 access logs
aws s3api get-bucket-logging \
  --bucket librarym-frontend-prod

# VPC Flow Logs
aws ec2 describe-flow-logs --filter "Name=resource-id,Values=<EC2_ID>"
```

---

## Maintenance Schedule

### Daily
- [ ] Check CloudWatch dashboards for anomalies
- [ ] Review application logs for errors
- [ ] Monitor CPU/Memory/Disk usage

### Weekly
- [ ] Review slow query logs
- [ ] Check backup status
- [ ] Review security events in CloudTrail

### Monthly
- [ ] Review AWS costs
- [ ] Update dependencies (security patches)
- [ ] Test disaster recovery procedure
- [ ] Review and rotate secrets

### Quarterly
- [ ] Security audit
- [ ] Capacity planning review
- [ ] Performance optimization review
- [ ] Cost optimization analysis

---

## Emergency Procedures

### If Backend is Down

```bash
# 1. Check service status
sudo systemctl status libraryms

# 2. View last 50 lines of logs
sudo journalctl -u libraryms -n 50

# 3. Try restarting
sudo systemctl restart libraryms
sleep 5
sudo systemctl status libraryms

# 4. If still down, check database
mysql -h <RDS_ENDPOINT> -u admin -p -e "SELECT 1;"

# 5. Manual start for debugging
java -jar /opt/libraryms/backend/target/library-management-1.0.0.jar

# 6. If unrecoverable, rollback to previous JAR
sudo systemctl stop libraryms
sudo cp /opt/libraryms/backup/library-management-previous.jar /opt/libraryms/target/
sudo systemctl start libraryms
```

### If Database is Down

```bash
# 1. Check RDS status
aws rds describe-db-instances \
  --db-instance-identifier librarym-mysql \
  --query 'DBInstances[0].DBInstanceStatus'

# 2. Reboot RDS
aws rds reboot-db-instance \
  --db-instance-identifier librarym-mysql

# 3. If corruption suspected, restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier librarym-mysql-restored \
  --db-snapshot-identifier <recent_snapshot>

# 4. Update backend connection string
sudo vim /etc/libraryms/app.env
sudo systemctl restart libraryms
```

### If Ransomware/Data Breach Suspected

```bash
# 1. ISOLATE IMMEDIATELY
aws ec2 modify-instance-attribute \
  --instance-id <EC2_ID> \
  --no-source-dest-check

# 2. Stop database replication (if any)
aws rds modify-db-instance \
  --db-instance-identifier librarym-mysql \
  --apply-immediately \
  --backup-retention-period 35  # Extend retention

# 3. Take forensic snapshot
aws ec2 create-image \
  --instance-id <EC2_ID> \
  --name forensics-$(date +%s)

# 4. Disable IAM users/access keys
aws iam update-access-key \
  --access-key-id <KEY_ID> \
  --status Inactive

# 5. Enable CloudTrail for audit
aws cloudtrail start-logging --name management-trail
```

---

## Checklists

### Weekly Maintenance Checklist
- [ ] Review error logs
- [ ] Check CloudWatch metrics
- [ ] Verify backups completed
- [ ] Monitor disk usage
- [ ] Review recent deployments

### Monthly Review
- [ ] Analyze slow queries
- [ ] Review security events
- [ ] Update security patches
- [ ] Test disaster recovery
- [ ] Review and optimize costs

### Before Production Release
- [ ] All tests passing
- [ ] Load testing completed
- [ ] Security review passed
- [ ] Performance benchmarked
- [ ] Rollback plan documented
- [ ] On-call person designated

