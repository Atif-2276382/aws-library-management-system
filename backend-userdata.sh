#!/bin/bash
# EC2 User Data Script for Spring Boot Backend Deployment

set -e

# Update system
sudo yum update -y

# Install Java 17
sudo yum install java-17-amazon-corretto-jdk -y

# Install git
sudo yum install git -y

# Install Docker (optional, for containerized deployment)
sudo amazon-linux-extras install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Create application directory
sudo mkdir -p /opt/libraryms
sudo chown ec2-user:ec2-user /opt/libraryms
cd /opt/libraryms

# Clone repository
git clone https://github.com/your-username/library-management.git .
cd backend

# Build application
mvn clean package -DskipTests -q

# Create systemd service
sudo tee /etc/systemd/system/libraryms.service > /dev/null <<'EOF'
[Unit]
Description=Library Management System Backend
After=network.target
Wants=libraryms.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/libraryms/backend
EnvironmentFile=/etc/libraryms/app.env
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -jar target/library-management-1.0.0.jar
Restart=always
RestartSec=10s
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# Create environment file (update with actual values)
sudo mkdir -p /etc/libraryms
sudo tee /etc/libraryms/app.env > /dev/null <<'EOF'
SPRING_DATASOURCE_URL=jdbc:mysql://RDS-ENDPOINT:3306/library_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=YourPassword123
JWT_SECRET=your-super-secret-key-change-this
SPRING_PROFILES_ACTIVE=prod
EOF

sudo chown ec2-user:ec2-user /etc/libraryms/app.env
sudo chmod 600 /etc/libraryms/app.env

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable libraryms
sudo systemctl start libraryms

# Install CloudWatch agent (optional)
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
sudo rpm -U ./amazon-cloudwatch-agent.rpm

# Log setup completion
echo "Library Management System backend setup completed" | sudo tee -a /var/log/libraryms-setup.log
