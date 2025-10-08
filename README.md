# Cloud Security Auditing Application

A Spring Boot application that enables natural language querying of AWS CloudTrail logs using AWS Bedrock (Claude) and Amazon Athena.

 - Features

- -? Natural language to SQL query conversion
- - Query AWS CloudTrail logs using plain English
- - AI-powered insights using AWS Bedrock (Claude)
- - Real-time query execution via Amazon Athena
- - Built-in data masking for sensitive information
- - CSV export for large datasets
- - Modern, responsive web interface

 - Security Notice

This application handles sensitive security data. Never commit AWS credentials or configuration with real values to version control.

All configuration uses:
- ? Environment variables (recommended for production)
- ? Local configuration files (in `.gitignore`)
- ? Data masking for sensitive fields
- ? AWS IAM roles (when running on AWS)

 - Prerequisites

- Java 17 or higher
- Maven 3.6+
- AWS Account with access to:
  - Amazon Athena
  - AWS Bedrock (Claude models)
  - Amazon S3 (for Athena results)
  - AWS CloudTrail (for log data)
- AWS CLI configured with credentials

 - Quick Start

# 1. Clone the Repository

```bash
git clone https://github.com/yourusername/cloud-security-auditing.git
cd cloud-security-auditing
```

# 2. Configure AWS Credentials

Option A: Using AWS CLI (Recommended)
```bash
aws configure
```

Option B: Using Environment Variables
```bash
export AWS_ACCESS_KEY_ID=your_access_key_id
export AWS_SECRET_ACCESS_KEY=your_secret_access_key
export AWS_REGION=us-west-2
```

# 3. Configure Application

Copy the example configuration:
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

Edit `application-local.yml` with your AWS resources:
```yaml
cloudaudit:
  database-name: your_athena_database
  table-name: your_cloudtrail_table
  s3-output-location: s3://your-bucket/results/
  aws-region: us-west-2
```

Note: `application-local.yml` is in `.gitignore` and will not be committed.

# 4. Build the Project

```bash
mvn clean install
```

# 5. Run the Application

Development (using application-local.yml):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Production (using environment variables):
```bash
export ATHENA_DATABASE_NAME=your_database
export ATHENA_TABLE_NAME=your_table
export S3_OUTPUT_LOCATION=s3://your-bucket/results/
mvn spring-boot:run
```

# 6. Access the Application

Open your browser to: http://localhost:8080

 - Usage Examples

Enter natural language queries such as:

- "Show me all failed login attempts in the last 24 hours"
- "List all EC2 instance creations this week"
- "Find all events from IP address 192.168.1.1"
- "What are the top 10 most common API calls?"
- "Show me all errors from the us-east-1 region"

 - Configuration

# Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `ATHENA_DATABASE_NAME` | Athena database name | Yes | - |
| `ATHENA_TABLE_NAME` | CloudTrail events table | Yes | - |
| `S3_OUTPUT_LOCATION` | S3 path for query results | Yes | - |
| `AWS_REGION` | AWS region | No | us-west-2 |
| `BEDROCK_MODEL_ID` | Bedrock model ID | No | anthropic.claude-v2:1 |
| `SERVER_PORT` | Application port | No | 8080 |

# Application Profiles

- `default` - Uses environment variables
- `local` - Uses application-local.yml (development)

 - Security Features

- ? Data Masking: Automatically masks sensitive information
  - IP addresses: `192.168.1.1` ? `*.*.*.*`
  - AWS Account IDs: `123456789012` ? ``
  - ARNs: Full ARNs masked to prevent information disclosure
  - Access Keys: Detected and masked
- ? SQL Query Masking: Database and table names hidden in UI
- ? No Hardcoded Credentials: All sensitive data externalized
- ? Audit Logging: All queries logged for security monitoring


 - Troubleshooting

# AWS Credentials Not Found
```
Error: Unable to load AWS credentials
Solution: Run 'aws configure' or set environment variables
```

# Athena Query Failed
```
Error: Access denied to S3 location
Solution: Verify S3 bucket permissions and IAM policy
```

# Bedrock Model Not Available
```
Error: Model access denied
Solution: Request model access in AWS Bedrock console
```

 - AWS Architecture

This application uses:
- Amazon Athena: Serverless SQL query engine
- Amazon S3: Stores CloudTrail logs
- AWS Glue Data Catalog: Table metadata
- AWS Bedrock: Claude LLM for natural language processing
- AWS CloudTrail: Source of security event data

 - Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Important: Never commit sensitive data! Always check with:
```bash
git status
git diff
```


 - Disclaimer

This application queries AWS CloudTrail logs which contain sensitive security information. Ensure:
- Proper access controls are in place
- Compliance with your organization's security policies
- Regular security audits
- Data retention policies are followed

 - Support

For issues or questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review AWS documentation for service-specific problems

 - Acknowledgments

- AWS Bedrock for Claude AI integration
- Spring Boot for the application framework
- AWS SDK for Java for AWS service integration

---

Built with - for secure cloud infrastructure monitoring
