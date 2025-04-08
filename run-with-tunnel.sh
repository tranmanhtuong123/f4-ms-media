#!/bin/bash

# Load environment variables from .env.local
set -a
source .env.local
set +a

# Create SSH tunnel for the services
echo "Setting up SSH tunnel to $REMOTE_HOST..."
# Using password authentication (will prompt for password)
ssh -f -N -R $REMOTE_PORT:localhost:$LOCAL_PORT $USER@$REMOTE_HOST

# Check if tunnel was created successfully
if [ $? -ne 0 ]; then
  echo "Failed to create SSH tunnel. Exiting."
  exit 1
fi

echo "SSH tunnel created successfully."
echo "Forwarding local port $LOCAL_PORT to remote $REMOTE_HOST:$REMOTE_PORT"

# Run the main class directly
echo "Starting application..."

# Method 1: Using classpath with target/classes directory
java -cp target/classes \
     -Dserver.port=$SERVER_PORT \
     -Dspring.profiles.active=dev \
     com.mycompany.myapp.MsMediaApp

# When the application is stopped, close the SSH tunnel
echo "Application stopped. Closing SSH tunnel..."
tunnel_pid=$(pgrep -f "$LOCAL_PORT:$REMOTE_HOST:$REMOTE_PORT")
if [ ! -z "$tunnel_pid" ]; then
  kill $tunnel_pid
  echo "SSH tunnel closed."
fi 