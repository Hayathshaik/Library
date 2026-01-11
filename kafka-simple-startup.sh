#!/bin/bash
set -e

KAFKA_VERSION="3.6.0"
SCALA_VERSION="2.13"
KAFKA_HOME="$HOME/kafka"
KAFKA_DIR="$KAFKA_HOME/kafka_${SCALA_VERSION}-${KAFKA_VERSION}"

echo "Step 1: Check Kafka installation..."

if [ -d "$KAFKA_DIR" ]; then
    echo "✓ Kafka found at $KAFKA_DIR"
else
    echo "Installing Kafka..."
    mkdir -p "$KAFKA_HOME"
    cd "$KAFKA_HOME"
    wget -q "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"
    tar xzf kafka.tgz
    rm kafka.tgz
    echo "✓ Kafka installed"
fi

echo ""
echo "Step 2: Starting Zookeeper..."
cd "$KAFKA_DIR"
nohup ./bin/zookeeper-server-start.sh ./config/zookeeper.properties > /tmp/zookeeper.log 2>&1 &
sleep 2

echo "Step 3: Starting Kafka..."
nohup ./bin/kafka-server-start.sh ./config/server.properties > /tmp/kafka.log 2>&1 &
sleep 5

echo "Step 4: Creating topic..."
./bin/kafka-topics.sh --create --topic book_events \
    --bootstrap-server localhost:9092 \
    --partitions 1 \
    --replication-factor 1 \
    --if-not-exists 2>/dev/null || true

echo ""
echo "✓ Kafka startup completed!"
echo "Check logs: tail -f /tmp/kafka.log"

