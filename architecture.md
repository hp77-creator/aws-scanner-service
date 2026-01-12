# Architecture Overview
This document serves as a critical, living template designed to equip agents with a rapid and comprehensive understanding of the codebase's architecture, enabling efficient navigation and effective contribution from day one. Update this document as the codebase evolves.

## 1. Project Structure
This section provides a high-level overview of the project's directory and file structure, categorised by architectural layer or major functional area.

[Project Root]/
├── scanner-api/          # Backend API Service
│   ├── src/              # Source code
│   │   ├── main/java/    # Java source
│   │   └── main/resources/# Configuration & resources
│   └── Dockerfile        # API Service Dockerfile
├── scanner-worker/       # Backend Worker Service
│   ├── src/              # Source code
│   └── Dockerfile        # Worker Service Dockerfile
├── scanner-common/       # Shared library for API and Worker
│   └── src/              # Shared models, utilities, and exceptions
├── terraform/            # Infrastructure as Code (AWS)
│   ├── modules/          # Reusable Terraform modules
│   └── main.tf           # Main infrastructure definition
├── docs/                 # Project documentation
├── scripts/              # Helper scripts
├── docker-compose.yml    # Local development environment setup
├── pom.xml               # Maven parent POM
└── README.md             # Project overview

## 2. High-Level System Diagram

[User] <--> [API Gateway] <--> [Internal ALB] <--> [Scanner API (Fargate)] <--> [PostgreSQL (RDS)]
                                                                    |
                                                                    +--> [SQS Queue] <--> [Scanner Worker (Fargate)] <--> [S3 Bucket]


## 3. Core Components

### 3.1. Frontend
*This project is a headless service and does not currently have a frontend user interface.*

### 3.2. Backend Services

#### 3.2.1. Scanner API
Name: Scanner API Service
Description: The entry point for the system. It handles HTTP requests to trigger scans, retrieve job statuses, and view findings. It offloads heavy processing to the SQS queue.
Technologies: Java 17, Spring Boot Web, Spring Data JPA.
Deployment: AWS ECS (Fargate), LocalStack (Dev).

#### 3.2.2. Scanner Worker
Name: Scanner Worker Service
Description: An asynchronous background worker that consumes scanning jobs from SQS. It downloads files from S3, streams them to detect sensitive data (PII) using regex patterns, and updates the database with findings.
Technologies: Java 17, Spring Boot, AWS SDK v2.
Deployment: AWS ECS (Fargate), LocalStack (Dev).

## 4. Data Stores

### 4.1. Primary Database
Name: Scanner Database
Type: PostgreSQL 15
Purpose: Stores persistent data including scan jobs, detailed findings, and configuration.
Key Schemas/Tables: `jobs`, `job_objects`, `findings`.

### 4.2. Object Storage
Name: S3 Bucket
Type: Amazon S3
Purpose: Stores the actual files to be scanned.
Key Usage: The worker streams objects directly from S3 for processing without loading the entire file into memory.

### 4.3. Message Queue
Name: Scanner Queue
Type: Amazon SQS
Purpose: Decouples the API from the Worker. Buffers scan requests to handle bursts and ensures reliable processing.

## 5. External Integrations / APIs

Service Name: AWS Simple Storage Service (S3)
Purpose: Source of files to scan.
Integration Method: AWS SDK for Java v2.

Service Name: AWS Simple Queue Service (SQS)
Purpose: Internal messaging.
Integration Method: AWS SDK for Java v2.

## 6. Deployment & Infrastructure

Cloud Provider: AWS
Key Services Used: 
- Compute: ECS Fargate
- Database: RDS for PostgreSQL
- Storage: S3
- Networking: VPC, Public/Private Subnets, NAT Gateway, API Gateway, ALB
- Messaging: SQS

CI/CD Pipeline: GitHub Actions (implied by `.github` presence if applicable, or manual/script-based currently).

Monitoring & Logging: CloudWatch Logs (for ECS tasks).

## 7. Security Considerations

Authentication: Pending implementation (currently API is open or secured via network boundaries).
Authorization: Least Privilege IAM Roles for ECS Tasks (Worker can read S3 but not delete buckets; API can write to SQS).
Data Encryption: 
- TLS in transit (API Gateway, S3/SQS interaction).
- RDS encryption at rest (if configured in Terraform).
Network Isolation: 
- Compute and DB resources reside in private subnets.
- No direct internet access for backend services (outbound via NAT).

## 8. Development & Testing Environment

Local Setup Instructions: 
1. Ensure Docker is running.
2. Run `docker-compose up --build`.
3. API available at `http://localhost:8080`.
4. LocalStack simulates AWS services at `http://localhost:4566`.


## 9. Future Considerations / Roadmap

-   Implement authentication (e.g., OAuth/OIDC) for the API.
-   Add support for more file formats (PDF, DOCX) and scanning rules.
-   Implement a real-time notification system (SNS/Webhooks) for completed scans.

## 10. Project Identification

Project Name: aws-scanner-service
Repository URL: (Repository URL not explicitly provided in context, placeholder)
Primary Contact/Team: hp77-creator
Date of Last Update: 2026-01-12

## 11. Glossary / Acronyms

PII: Personally Identifiable Information
SQS: Simple Queue Service
ECS: Elastic Container Service
ALB: Application Load Balancer
DLQ: Dead Letter Queue
