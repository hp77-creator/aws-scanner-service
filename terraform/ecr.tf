resource "aws_ecr_repository" "scanner_api" {
  name                 = "${var.project_name}-api"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

resource "aws_ecr_repository" "scanner_worker" {
  name                 = "${var.project_name}-worker"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}
