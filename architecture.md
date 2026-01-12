# Architecture Overview

This document describes the high-level architecture of the AWS Scanner Service, a scalable solution for detecting sensitive data in S3 buckets.

## System Diagram

```mermaid
graph TD
    User[User/Client] -->|HTTPS| APIGW[API Gateway]
    APIGW -->|VPC Link| ALB[Internal ALB]
    
    subgraph VPC [AWS VPC]
        subgraph Public Subnet
            NAT[NAT Gateway]
            Bastion[Bastion Host]
        end
        
        subgraph Private Subnet
            ALB
            API[Scanner API (Fargate)]
            Worker[Scanner Worker (Fargate)]
            RDS[(PostgreSQL RDS)]
        end
    end

    subgraph AWS Services
        S3[S3 Bucket]
        SQS[SQS Queue]
        ECR[ECR Registry]
    end

    ALB --> API
    API -->|Submit Job| SQS
    API -->|Read/Write| RDS
    API -->|Images| ECR

    Worker -->|Poll Job| SQS
    Worker -->|Read Files| S3
    Worker -->|Save Findings| RDS
    Worker -->|Images| ECR

    API -->|Outbound| NAT
    Worker -->|Outbound| NAT
```

## Component Details

### 1. Compute Layer (AWS Fargate)
The core logic runs on serverless containers managed by AWS ECS.

*   **Scanner API**:
    *   **Tech Stack**: Java 17, Spring Boot Web.
    *   **Responsibility**: Validates requests, creates scan jobs, and exposes results.
    *   **Scaling**: Replica count managed by ECS.
    *   **Network**: Runs in private subnets; accessible only via the Internal ALB.

*   **Scanner Worker**:
    *   **Tech Stack**: Java 17, Spring Boot, Regex Detectors.
    *   **Responsibility**: Asynchronous processing. Reads file streams from S3, detects PII (SSN, Phone Numbers), and records findings.
    *   **Scaling**: Auto-scales (1-5 tasks) based on the SQS `ApproximatedNumberOfMessagesVisible` metric.

### 2. message Queuing (Amazon SQS)
*   **Decoupling**: Ensures the API remains responsive even under heavy load.
*   **Reliability**: Includes a Dead Letter Queue (DLQ) to capture failed jobs after 3 retries.

### 3. Storage Layer
*   **Amazon S3**:
    *   Target storage for file uploads.
    *   Accessed via IAM Roles (Task Execution Role) assigned to Fargate tasks.
*   **Amazon RDS (PostgreSQL)**:
    *   Stores job metadata and sensitive findings.
    *   Secured via Security Groups (ingress allowed only from ECS tasks).

### 4. Networking & Access
*   **API Gateway (HTTP API)**:
    *   Public entry point.
    *   Uses a **VPC Link** to securely tunnel traffic to the internal ALB.
*   **Bastion Host**:
    *   EC2 instance in the public subnet.
    *   Provides secure SSH tunnel/jump box access to the private RDS database for debugging/maintenance.

## Security Controls
*   **Least Privilege**: IAM roles for ECS tasks grant only necessary permissions (e.g., S3 read-only for worker, SQS send for API).
*   **Network Isolation**: All compute and database resources reside in private subnets with no direct internet access.
*   **Secrets Management**: Database passwords and sensitive keys are injected as environment variables via Terraform (not hardcoded).
