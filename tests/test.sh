set -e  # exit on any command failing

docker-compose -f "$(dirname $0)/docker-compose.test.yml" up --build --remove-orphans --exit-code-from runner --abort-on-container-exit

