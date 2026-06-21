output "ec2_public_ip" {
  value = aws_eip.backend_ip.public_ip
}

output "rds_endpoint" {
  value = aws_db_instance.mysql.endpoint
}

output "ecr_repo_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "secret_arn" {
  value = aws_secretsmanager_secret.db_secret.arn
}