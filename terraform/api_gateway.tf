resource "aws_apigatewayv2_api" "api" {
  name          = "${var.project_name}-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_stage" "api" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_apigatewayv2_vpc_link" "api" {
  name               = "${var.project_name}-vpc-link"
  security_group_ids = [aws_security_group.ecs_sg.id] # SG for VPC link ENI
  subnet_ids         = aws_subnet.private[*].id
}

resource "aws_apigatewayv2_integration" "api" {
  api_id           = aws_apigatewayv2_api.api.id
  integration_type = "HTTP_PROXY"
  connection_type  = "VPC_LINK"
  connection_id    = aws_apigatewayv2_vpc_link.api.id
  integration_method = "ANY"
  integration_uri    = aws_lb_listener.api.arn # Allow terraform to manage this?
}

resource "aws_apigatewayv2_route" "proxy" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.api.id}"
}

output "api_url" {
  value = aws_apigatewayv2_api.api.api_endpoint
}
