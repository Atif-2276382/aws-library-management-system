resource "aws_eip" "backend_ip" {

  domain = "vpc"

  instance = aws_instance.backend.id
}