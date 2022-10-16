build-all: build-scim

start-project:
	docker-compose -f ./docker-compose.yml up --build --force-recreate -d

stop-project:
	docker-compose -f ./docker-compose.yml down

build-scim:
	docker build -f ../scim/Dockerfile -t scim ../scim/ 
start-scim:
	docker run -p 8081:8080 -p 9990:9990 scim 