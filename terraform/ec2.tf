resource "aws_instance" "backend" {

  ami                    = "ami-03d84abcde942cf8c"
  instance_type          = "t3.micro"
  subnet_id              = var.public_subnet_1
  key_name               = aws_key_pair.library_key.key_name
  vpc_security_group_ids = [aws_security_group.backend_sg.id]

  user_data = file("${path.module}/scripts/install-docker.sh")

  tags = {
    Name = "library-management-backend"
  }
}
