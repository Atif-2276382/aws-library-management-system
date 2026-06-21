terraform {
  backend "s3" {
    bucket       = "librarym-terraform-state"
    key          = "prod/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
  }
}