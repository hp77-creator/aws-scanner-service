resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
  
  tags = {
    Name = "${var.project_name}-cluster"
  }
}

# Log Group
resource "aws_cloudwatch_log_group" "scanner_logs" {
  name              = "/ecs/${var.project_name}"
  retention_in_days = 7
}

# Task Definition - WORKER
resource "aws_ecs_task_definition" "worker" {
  family                   = "${var.project_name}-worker"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "worker"
      image     = "${aws_ecr_repository.scanner_worker.repository_url}:v2"
      essential = true
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "aws" },
        { name = "AWS_REGION", value = var.aws_region },
        { name = "SQS_QUEUE_URL", value = aws_sqs_queue.scan_jobs.url },
        { name = "DATABASE_URL", value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${aws_db_instance.postgres.db_name}" },
        { name = "DATABASE_USER", value = aws_db_instance.postgres.username },
        { name = "DATABASE_PASSWORD", value = var.db_password }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.scanner_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "worker"
        }
      }
    }
  ])
}

# Task Definition - API
resource "aws_ecs_task_definition" "api" {
  family                   = "${var.project_name}-api"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "api"
      image     = aws_ecr_repository.scanner_api.repository_url
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "aws" },
        { name = "AWS_REGION", value = var.aws_region },
        { name = "SQS_QUEUE_URL", value = aws_sqs_queue.scan_jobs.url },
        { name = "DATABASE_URL", value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${aws_db_instance.postgres.db_name}" },
        { name = "DATABASE_USER", value = aws_db_instance.postgres.username },
        { name = "DATABASE_PASSWORD", value = var.db_password }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.scanner_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "api"
        }
      }
    }
  ])
}

# Application Load Balancer (Internal for API)
resource "aws_lb" "api" {
  name               = "${var.project_name}-alb"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.ecs_sg.id] # Reuse ECS SG or create ALB specific SG
  subnets            = aws_subnet.private[*].id        # Internal ALB in private subnets
}

# We need a listener and target group for ALB
resource "aws_lb_target_group" "api" {
  name        = "${var.project_name}-api-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"
  
  health_check {
    path = "/actuator/health" # Assuming Spring Boot
  }
}

resource "aws_lb_listener" "api" {
  load_balancer_arn = aws_lb.api.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api.arn
  }
}

# ECS Service - API
resource "aws_ecs_service" "api" {
  name            = "${var.project_name}-api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api.arn
    container_name   = "api"
    container_port   = 8080
  }
  
  depends_on = [aws_lb_listener.api]
}

# ECS Service - WORKER
resource "aws_ecs_service" "worker" {
  name            = "${var.project_name}-worker"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.worker.arn
  desired_count   = 1 # initial count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = false
  }
}

# Auto Scaling for Worker
resource "aws_appautoscaling_target" "worker_target" {
  max_capacity       = 5
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.worker.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "worker_scale_policy" {
  name               = "worker-scale-sqs"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.worker_target.resource_id
  scalable_dimension = aws_appautoscaling_target.worker_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.worker_target.service_namespace

  target_tracking_scaling_policy_configuration {
    # Custom metric for SQS queue depth per task? 
    # The requirement says "target tracking on ApproximateNumberOfMessagesVisible".
    # Standard target tracking works with pre-defined metrics. For SQS custom metric, we usually define a custom metric specification.
    
    customized_metric_specification {
      metric_name = "ApproximateNumberOfMessagesVisible"
      namespace   = "AWS/SQS"
      statistic   = "Average"
      unit        = "Count"
      
      dimensions {
        name  = "QueueName"
        value = aws_sqs_queue.scan_jobs.name
      }
    }
    
    target_value = 100.0 # Scale if backlog > 100
  }
}
