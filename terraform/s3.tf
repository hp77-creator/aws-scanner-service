resource "aws_s3_bucket" "scanner_bucket" {
  bucket = "${var.project_name}-files-${data.aws_caller_identity.current.account_id}"
  
  tags = {
    Name = "${var.project_name}-bucket"
  }
  
  force_destroy = true # For easier cleanup during testing
}

# Block public access
resource "aws_s3_bucket_public_access_block" "scanner_bucket_access" {
  bucket = aws_s3_bucket.scanner_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

data "aws_caller_identity" "current" {}
