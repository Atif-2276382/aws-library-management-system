resource "random_password" "db_password" {
  length  = 20
  special = false
}

resource "aws_db_subnet_group" "library_db_subnet" {

  name = "library-db-subnet-group"

  subnet_ids = [
    var.public_subnet_1,
    var.public_subnet_2
  ]
}

resource "aws_db_instance" "mysql" {

  identifier = "library-management-db"

  engine         = "mysql"
  engine_version = "8.0"

  instance_class = "db.t3.micro"

  allocated_storage = 20

  db_name = "library_schema"

  username = var.db_username
  password = random_password.db_password.result

  publicly_accessible = false

  db_subnet_group_name = aws_db_subnet_group.library_db_subnet.name

  vpc_security_group_ids = [
    aws_security_group.rds_sg.id
  ]

  skip_final_snapshot = true
}