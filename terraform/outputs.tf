output "cloudfront_url" {
  value = aws_cloudfront_distribution.frontend.domain_name
}

output "ecr_repo_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "rds_endpoint" {
  value = aws_db_instance.mysql.endpoint
}