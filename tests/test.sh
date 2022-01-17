set -e  # exit on any command failing

docker-compose -f docker-compose.test.yml up --build --remove-orphans --exit-code-from runner

