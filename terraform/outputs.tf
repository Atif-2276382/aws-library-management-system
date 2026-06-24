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

output "cloudfront_domain_name" {

  value = aws_cloudfront_distribution.frontend.domain_name
}

output "cloudfront_distribution_id" {

  value = aws_cloudfront_distribution.frontend.id
}

output "backend_cloudfront_domain" {
  value = aws_cloudfront_distribution.backend.domain_name
}

output "backend_cloudfront_id" {
  value = aws_cloudfront_distribution.backend.id
}

