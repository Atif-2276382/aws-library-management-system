variable "aws_region" {
  default = "us-east-1"
}

variable "project_name" {
  default = "library-management"
}

variable "vpc_id" {
  default = "vpc-051a45da976c8cb2f"
}

variable "public_subnet_1" {
  default = "subnet-057f8c1642c79951a"
}

variable "public_subnet_2" {
  default = "subnet-0e7b348136c617a45"
}

variable "db_username" {
  default = "libraryadmin"
}

variable "frontend_bucket_name" {
  default = "librarym-frontend-prod"
}