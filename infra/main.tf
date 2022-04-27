# Variables
variable region { 
    type = string 
    default = "sa-east-1"
}

data "aws_elastic_beanstalk_solution_stack" "coretto_latest" {
  most_recent = true
  name_regex = "^64bit Amazon Linux (.*) running Corretto 11$"
}

# Provider
provider "aws" {
  region = var.region
}

# Resources
resource "aws_elastic_beanstalk_application" "coupon_engine_app" {
  name        = "coupon-engine-app"
  description = "Coupon Engine App"
}

resource "aws_elastic_beanstalk_environment" "coupon_engine_app_env" {
  name                = aws_elastic_beanstalk_application.coupon_engine_app.name
  application         = aws_elastic_beanstalk_application.coupon_engine_app.name
  solution_stack_name = data.aws_elastic_beanstalk_solution_stack.coretto_latest.name
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "IamInstanceProfile"
    value = "aws-elasticbeanstalk-ec2-role"
  }
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "InstanceType"
    value = "t2.micro"
  }
  setting {
    namespace = "aws:autoscaling:asg"
    name      = "MinSize"
    value     = 1
  }
  setting {
    namespace = "aws:autoscaling:asg"
    name      = "MaxSize"
    value     = 2
  }
}