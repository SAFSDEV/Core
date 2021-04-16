@echo off

echo "stop the running containers seplus-hub seplus-node seplus-node2 seplus-test"
docker container stop seplus-hub seplus-node seplus-node2 seplus-test

echo "delete the stopped containers seplus-hub seplus-node seplus-node2 seplus-test"
docker container rm seplus-hub seplus-node seplus-node2 seplus-test

echo "delete the network 'seplus-grid-network'"
docker network rm seplus-grid-network