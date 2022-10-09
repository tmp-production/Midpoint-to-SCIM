build-all: build-midpoint


build-midpoint:
	docker build -f midpoint/Dockerfile -t midpoint ..
start-project:
	docker-compose -f ./docker-compose.yml up --build --force-recreate -d
stop-project:
	docker-compose -f ./docker-compose.yml down
