resource "aws_db_instance" "mysql" {

  identifier = "library-management-db"

  engine = "mysql"
  engine_version = "8.0"

  instance_class = "db.t3.micro"

  allocated_storage = 20

  db_name  = "library_schema"

  username = var.db_username
  password = var.db_password

  publicly_accessible = false

  skip_final_snapshot = true
}