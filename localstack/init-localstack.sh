#!/bin/bash
# LocalStack Initialization Script
# This script runs when LocalStack is ready
# It creates S3 bucket, SQS queue, and uploads test data

set -e

echo "======================================"
echo "Initializing LocalStack for AWS Scanner Service"
echo "======================================"

# Wait a bit for LocalStack to be fully ready
sleep 2

# Create S3 bucket
echo "Creating S3 bucket: test-bucket"
awslocal s3 mb s3://test-bucket || echo "Bucket already exists"

# Upload test data files to S3
echo "Uploading test data files to S3..."
awslocal s3 cp /test-data/ s3://test-bucket/ --recursive

# List uploaded files
echo "Files in test-bucket:"
awslocal s3 ls s3://test-bucket/

# Create SQS queue
echo "Creating SQS queue: scanner-queue"
QUEUE_URL=$(awslocal sqs create-queue --queue-name scanner-queue --output text --query 'QueueUrl' 2>/dev/null || awslocal sqs get-queue-url --queue-name scanner-queue --output text --query 'QueueUrl')
echo "Queue URL: $QUEUE_URL"

# Get queue attributes
echo "Queue attributes:"
awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names All

echo "======================================"
echo "LocalStack initialization complete!"
echo "======================================"
echo "S3 Bucket: test-bucket"
echo "SQS Queue: scanner-queue"
echo "Queue URL: $QUEUE_URL"
echo "======================================"
