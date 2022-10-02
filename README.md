# plannng-service
Backend for task scheduling service. 
Just an exercise in writing scala backends.
Uses postgres sql databse for task storage and redis for user sessions.
- Allows creating tasks that are grouped in projects
- Supports users and recrticting access to projects to specific users
- Suports adding tags to tasks for grouping and search
- Supports adding comments to tasks
- Supports assigning spent time to tasks to track an effort
- Supports setting arbitrary task statuses for tasks to organize kanban-like workflow
