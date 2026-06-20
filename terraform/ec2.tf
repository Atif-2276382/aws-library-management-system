resource "aws_instance" "backend" {

  ami = data.aws_ami.amazon_linux.id

  instance_type = "t3.micro"

  key_name = "library-key"

  user_data = file("scripts/install-docker.sh")

  tags = {
    Name = "library-management-backend"
  }
}