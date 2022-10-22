build-all: build-scim

start-project:
	docker-compose -f ./docker-compose.yml up --build --force-recreate -d

stop-project:
	docker-compose -f ./docker-compose.yml down

build-scim:
	docker build -f ./scim/Dockerfile -t scim-personify ../scim/ 
start-scim:
	docker rm scim 
	./scim/run_container.sh 
