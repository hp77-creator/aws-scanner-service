resource "aws_sqs_queue" "scan_jobs_dlq" {
  name = "scan-jobs-dlq"
}

resource "aws_sqs_queue" "scan_jobs" {
  name                       = "scan-jobs"
  visibility_timeout_seconds = 60 # Set to > worker processing time
  message_retention_seconds  = 86400
  
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.scan_jobs_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name = "${var.project_name}-queue"
  }
}
