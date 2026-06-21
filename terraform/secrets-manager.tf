resource "aws_secretsmanager_secret" "db_secret" {

  name = "library-management-db-secret"
}

resource "aws_secretsmanager_secret_version" "db_secret_version" {

  secret_id = aws_secretsmanager_secret.db_secret.id

  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db_password.result
    endpoint = aws_db_instance.mysql.endpoint
  })
}