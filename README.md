🔐 Cloud Security Auditing

Query AWS CloudTrail logs using natural language, powered by AWS Bedrock (Claude AI).
This tool transforms human questions into SQL queries, audits your AWS environment in real time, and ensures sensitive data is masked automatically.

🚀 Features

🗣️ Natural Language to SQL Conversion – Ask questions like “Show all failed login attempts today”.

🔍 Real-Time CloudTrail Log Analysis – Query events dynamically from Athena.

🤖 AI-Powered Insights – Uses AWS Bedrock (Claude) for semantic query generation and analysis.

🔒 Built-In Data Masking – Automatically hides IPs, Account IDs, and ARNs in responses.

📥 CSV Export – Download large query results for offline analysis.

⚙️ Prerequisites

Before running the application, ensure you have:

☕ Java 17+

🧩 Maven 3.6+

🪣 AWS Account with permissions for:

Athena

Bedrock

CloudTrail

🧰 AWS CLI configured with credentials (aws configure)

🧭 Quick Start
1️⃣ Clone the Repository
git clone https://github.com/nikhilsana1004/cloud-security-auditing.git
cd cloud-security-auditing

2️⃣ Configure
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml


Edit application-local.yml with your AWS resource details.

3️⃣ Run the Application
mvn spring-boot:run -Dspring-boot.run.profiles=local

4️⃣ Access the UI

Open your browser and navigate to:

http://localhost:8080

🧩 Configuration Example

Set these values in application-local.yml:

cloudaudit:
  database-name: your_athena_database
  table-name: your_cloudtrail_table
  s3-output-location: s3://your-bucket/results/
  aws-region: us-west-2

💬 Usage Examples

You can query your CloudTrail logs using natural language prompts like:

“Show me all failed login attempts in the last 24 hours”

“List all EC2 instance creations this week”

“Find events from IP address 192.168.1.1”

🛡️ Security

✅ No credentials stored in code

✅ Automatic data masking

✅ Environment variable–based configuration

✅ SQL injection prevention

🧰 Tech Stack
Layer	Technology
Backend	Spring Boot 3.2.0
AI/NLP	AWS Bedrock (Claude)
Data Query	AWS Athena
UI	Thymeleaf
Testing	JUnit 5
Logging	SLF4J + Logback
🧠 How It Works

User enters a natural language query in the web interface.

Application sends the prompt to AWS Bedrock (Claude) for SQL generation.

The generated SQL query executes on AWS Athena against CloudTrail logs.

The results are sanitized, masked, and displayed or exported as CSV.