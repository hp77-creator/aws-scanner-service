# Testing Guide

## 1. Load Testing (Upload 500+ Files)

Use the provided helper script to generate and upload 500 small files to your S3 bucket. Some files will purposely contain "SSN-like" patterns to trigger findings.

```bash
./scripts/generate_test_load.sh
```

**What this does:**
*   Generates 500 text files locally.
*   Every 10th file contains a fake SSN.
*   Uploads them to `s3://aws-scanner-files-649068094730/load-test/`.

---

## 2. API End-to-End Test

### Variables
export API_URL="https://8fs2se1gs8.execute-api.us-east-1.amazonaws.com"
export BUCKET="aws-scanner-files-649068094730"
export PREFIX="load-test"

### Step 1: Trigger Scan
```bash
curl -X POST "$API_URL/scan" \
  -H "Content-Type: application/json" \
  -d "{\"bucket\": \"$BUCKET\", \"prefix\": \"$PREFIX\"}"
```
**Response:**
```json
{
  "jobId": "uuid-of-the-job",
  ...
}
```
*Copy the `jobId` for the next steps.*

### Step 2: Poll Job Status
Check if the job is complete. The count of `succeeded` should eventually match the total files (500).

```bash
export JOB_ID="<YOUR_JOB_ID>"
curl "$API_URL/jobs/$JOB_ID"
```
**Response:**
```json
{"queued": 50, "processing": 10, "succeeded": 440, "failed": 0}
```

### Step 3: Fetch Results
Once the job is complete, you can fetch the findings (files where sensitive data was detected).

```bash
# Get results for the scanned prefix
curl "$API_URL/results?bucketId=$BUCKET&prefix=$PREFIX"
```

---

## 3. Monitoring Queues (SQS)

You can view the "Queue Depth" (number of messages waiting) to see the system processing the load.

### Main Queue (scan-jobs)
```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.us-east-1.amazonaws.com/649068094730/scan-jobs \
  --attribute-names ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible
```

*   `ApproximateNumberOfMessages`: Messages waiting to be processed.
*   `ApproximateNumberOfMessagesNotVisible`: Messages currently being processed by workers.

### Dead Letter Queue (DLQ)
Check if any jobs failed processing after retries.

```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.us-east-1.amazonaws.com/649068094730/scan-jobs-dlq \
  --attribute-names ApproximateNumberOfMessages
```
If this number is > 0, some messages failed permanently.
