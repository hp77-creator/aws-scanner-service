provider "aws" {
  region                      = var.aws_region
  skip_credentials_validation = var.use_localstack
  skip_metadata_api_check     = var.use_localstack
  skip_requesting_account_id  = var.use_localstack
  s3_use_path_style           = var.use_localstack

  # Only use endpoints if use_localstack is true
  endpoints {
    apigateway     = var.use_localstack ? var.localstack_endpoint : null
    apigatewayv2   = var.use_localstack ? var.localstack_endpoint : null
    cloudformation = var.use_localstack ? var.localstack_endpoint : null
    cloudwatch     = var.use_localstack ? var.localstack_endpoint : null
    dynamodb       = var.use_localstack ? var.localstack_endpoint : null
    ec2            = var.use_localstack ? var.localstack_endpoint : null
    es             = var.use_localstack ? var.localstack_endpoint : null
    elasticache    = var.use_localstack ? var.localstack_endpoint : null
    firehose       = var.use_localstack ? var.localstack_endpoint : null
    iam            = var.use_localstack ? var.localstack_endpoint : null
    kinesis        = var.use_localstack ? var.localstack_endpoint : null
    lambda         = var.use_localstack ? var.localstack_endpoint : null
    rds            = var.use_localstack ? var.localstack_endpoint : null
    redshift       = var.use_localstack ? var.localstack_endpoint : null
    route53        = var.use_localstack ? var.localstack_endpoint : null
    s3             = var.use_localstack ? var.localstack_endpoint : null
    secretsmanager = var.use_localstack ? var.localstack_endpoint : null
    ses            = var.use_localstack ? var.localstack_endpoint : null
    sns            = var.use_localstack ? var.localstack_endpoint : null
    sqs            = var.use_localstack ? var.localstack_endpoint : null
    ssm            = var.use_localstack ? var.localstack_endpoint : null
    stepfunctions  = var.use_localstack ? var.localstack_endpoint : null
    sts            = var.use_localstack ? var.localstack_endpoint : null
    ecs            = var.use_localstack ? var.localstack_endpoint : null
  }
}
