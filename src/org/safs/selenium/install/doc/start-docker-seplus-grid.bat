@echo off

echo "Create a network 'seplus-grid-network' so that the conttainers can conect each other by container's name"
docker network create seplus-grid-network

echo "Create a hub 'seplus-hub' in network 'seplus-grid-network', by default the 'hub' is running on port 4444. User can RDC the hub container by port 32800 with 'VNC Viewer'"
docker run -d --network=seplus-grid-network -p 32800:5900 --name seplus-hub ghcr.io/safsdev/seplus-hub

echo "Create a node 'seplus-node' in network 'seplus-grid-network', by default the 'node' is running on port 5555. User can RDC the node container by port 32801 with 'VNC Viewer'"
docker run -d -p 32801:5900 --network=seplus-grid-network -e HUB_HOST=seplus-hub --name seplus-node ghcr.io/safsdev/seplus-node

echo "Create an other node 'seplus-node2' in network 'seplus-grid-network', this node is running on port 5678. User can RDC the node container by port 32802 with 'VNC Viewer'"
docker run -d -p 32802:5900 --network=seplus-grid-network -e HUB_HOST=seplus-hub -e NODE_PORT=5678 --name seplus-node2 ghcr.io/safsdev/seplus-node

echo "Create a test container 'seplus-test' in network 'seplus-grid-network', user can RDC the test container by port 32803 with 'VNC Viewer' and run test on it."
docker run -d -p 32803:5900 --network=seplus-grid-network --name seplus-test ghcr.io/safsdev/seplus

echo "The status of network 'seplus-grid-network'"
docker network inspect seplus-grid-network

echo "The status of the containers"
docker container ls