# AWS Infrastructure as Code using Terraform
# This creates RDS, EC2, VPC, and Security Groups

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Environment = var.environment
      Project     = "LibraryManagementSystem"
      ManagedBy   = "Terraform"
    }
  }
}

# VPC
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "librarym-vpc"
  }
}

# Public Subnet
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    Name = "librarym-public-subnet"
  }
}

# Private Subnet (for RDS)
resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = data.aws_availability_zones.available.names[1]

  tags = {
    Name = "librarym-private-subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "librarym-igw"
  }
}

# Route Table
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block      = "0.0.0.0/0"
    gateway_id      = aws_internet_gateway.main.id
  }

  tags = {
    Name = "librarym-rt"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# Security Group for EC2
resource "aws_security_group" "backend" {
  name   = "librarym-backend-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.ssh_cidr
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "librarym-backend-sg"
  }
}

# Security Group for RDS
resource "aws_security_group" "database" {
  name   = "librarym-db-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "librarym-db-sg"
  }
}

# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "librarym-db-subnet"
  subnet_ids = [aws_subnet.private.id, aws_subnet.public.id]

  tags = {
    Name = "librarym-db-subnet"
  }
}

# RDS MySQL Instance
resource "aws_db_instance" "mysql" {
  identifier              = "librarym-mysql"
  engine                  = "mysql"
  engine_version          = "8.0.35"
  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  storage_type            = "gp3"
  storage_encrypted       = true
  db_name                 = "library_db"
  username                = "admin"
  password                = random_password.db_password.result
  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [aws_security_group.database.id]
  publicly_accessible     = false
  skip_final_snapshot     = var.environment == "dev" ? true : false
  backup_retention_period = var.backup_retention_days
  multi_az                = var.environment == "prod" ? true : false

  cloudwatch_logs_export_config {
    log_types = ["error", "general", "slowquery"]
  }

  tags = {
    Name = "librarym-mysql"
  }
}

# Random password for RDS
resource "random_password" "db_password" {
  length  = 32
  special = true
}

# EC2 Instance
resource "aws_instance" "backend" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = var.instance_type
  key_name               = var.key_pair_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.backend.id]
  iam_instance_profile   = aws_iam_instance_profile.backend.name

  user_data = base64encode(templatefile("${path.module}/backend-userdata.sh", {
    db_host     = aws_db_instance.mysql.address
    db_user     = aws_db_instance.mysql.username
    db_password = random_password.db_password.result
    jwt_secret  = random_password.jwt_secret.result
  }))

  tags = {
    Name = "librarym-backend"
  }

  depends_on = [aws_db_instance.mysql]
}

# IAM Role for EC2
resource "aws_iam_role" "backend" {
  name = "librarym-backend-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.backend.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_instance_profile" "backend" {
  name = "librarym-backend-profile"
  role = aws_iam_role.backend.name
}

# JWT Secret
resource "random_password" "jwt_secret" {
  length  = 64
  special = true
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

# Outputs
output "rds_endpoint" {
  value = aws_db_instance.mysql.address
}

output "rds_port" {
  value = aws_db_instance.mysql.port
}

output "ec2_public_ip" {
  value = aws_instance.backend.public_ip
}

output "ec2_public_dns" {
  value = aws_instance.backend.public_dns
}

output "db_password" {
  value     = random_password.db_password.result
  sensitive = true
}

output "jwt_secret" {
  value     = random_password.jwt_secret.result
  sensitive = true
}
