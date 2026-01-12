variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project Name"
  type        = string
  default     = "aws-scanner"
}

variable "environment" {
  description = "Environment (dev, prod, etc.)"
  type        = string
  default     = "dev"
}

variable "use_localstack" {
  description = "Set to true to use LocalStack"
  type        = bool
  default     = false
}

variable "localstack_endpoint" {
  description = "LocalStack endpoint URL"
  type        = string
  default     = "http://localhost:4566"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_key" {
  description = "SSH Public Key for Bastion"
  type        = string
  default     = "" # User must provide this
}

variable "db_password" {
  description = "Password for the RDS database"
  type        = string
  sensitive   = true
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH into Bastion Host"
  type        = string
  default     = "0.0.0.0/0" # WARNING: Change this to your IP in production
}
