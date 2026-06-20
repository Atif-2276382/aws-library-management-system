terraform {
  backend "s3" {
    bucket         = "librarym-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "librarym-terraform-locks"
    encrypt        = true
  }
}