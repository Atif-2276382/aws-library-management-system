variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "backup_retention_days" {
  description = "RDS backup retention in days"
  type        = number
  default     = 7
}

variable "key_pair_name" {
  description = "EC2 key pair name"
  type        = string
}

variable "ssh_cidr" {
  description = "CIDR block for SSH access"
  type        = list(string)
  default     = ["0.0.0.0/0"]  # Change to your IP for security
}
