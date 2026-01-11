#!/bin/bash

# Kafka Setup and Startup Script for WSL
# This script downloads, installs, and starts Kafka with Zookeeper

KAFKA_VERSION="3.6.0"
SCALA_VERSION="2.13"
KAFKA_HOME="$HOME/kafka"
KAFKA_DIR="$KAFKA_HOME/kafka_${SCALA_VERSION}-${KAFKA_VERSION}"

echo "======================================"
echo "Kafka Setup for WSL"
echo "======================================"

# Check if Kafka is already installed
if [ -d "$KAFKA_DIR" ]; then
    echo "✓ Kafka found at $KAFKA_DIR"
else
    echo "→ Installing Kafka $KAFKA_VERSION..."
    mkdir -p "$KAFKA_HOME"
    cd "$KAFKA_HOME"

    # Download Kafka
    KAFKA_URL="https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"
    echo "  Downloading from: $KAFKA_URL"
    wget "$KAFKA_URL" -O kafka.tgz
    tar xzf kafka.tgz
    rm kafka.tgz
    echo "✓ Kafka installed successfully"
fi

# Start Zookeeper in background
echo ""
echo "→ Starting Zookeeper..."
cd "$KAFKA_DIR"
nohup bin/zookeeper-server-start.sh config/zookeeper.properties > /tmp/zookeeper.log 2>&1 &
ZOOKEEPER_PID=$!
echo "✓ Zookeeper started (PID: $ZOOKEEPER_PID)"

# Wait for Zookeeper to be ready
sleep 3

# Start Kafka broker in background
echo "→ Starting Kafka broker..."
nohup bin/kafka-server-start.sh config/server.properties > /tmp/kafka.log 2>&1 &
KAFKA_PID=$!
echo "✓ Kafka started (PID: $KAFKA_PID)"

# Wait for Kafka to be ready
sleep 3

# Verify Kafka is listening
echo ""
echo "→ Verifying Kafka is listening on port 9092..."
if netstat -tulpn 2>/dev/null | grep -q 9092; then
    echo "✓ Kafka is listening on port 9092"
else
    echo "✗ Kafka is NOT listening on port 9092"
    echo "  Check logs: tail -f /tmp/kafka.log"
    exit 1
fi

# Create the book_events topic if it doesn't exist
echo ""
echo "→ Creating topic 'book_events' (if not exists)..."
bin/kafka-topics.sh --create --topic book_events \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1 \
    --if-not-exists

echo ""
echo "======================================"
echo "Kafka is ready!"
echo "======================================"
echo "Logs:"
echo "  Zookeeper: tail -f /tmp/zookeeper.log"
echo "  Kafka:     tail -f /tmp/kafka.log"
echo ""
echo "To stop Kafka, run: pkill -f kafka-server-start"
echo "To stop Zookeeper, run: pkill -f zookeeper-server-start"

