# cloudwatch.tf

resource "aws_cloudwatch_log_group" "backend_logs" {

  name = "/library-management/backend"

  retention_in_days = 7

  tags = {
    Name = "library-management-backend-logs"
  }
}