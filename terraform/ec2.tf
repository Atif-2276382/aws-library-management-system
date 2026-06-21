data "aws_ami" "amazon_linux" {

  most_recent = true

  owners = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_instance" "backend" {

  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t3.micro"
  subnet_id              = var.public_subnet_1
  key_name               = aws_key_pair.library_key.key_name
  vpc_security_group_ids = [aws_security_group.backend_sg.id]

  user_data = file("${path.module}/scripts/install-docker.sh")

  tags = {
    Name = "library-management-backend"
  }
}