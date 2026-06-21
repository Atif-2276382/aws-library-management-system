resource "aws_key_pair" "library_key" {
  key_name   = "library-key"
  public_key = file("../backend/library-key.pub")
}