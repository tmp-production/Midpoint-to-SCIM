SET JAR_PATH="../Scim2Connector/scim-2-connector/target/scim-2-connector-1.0-SNAPSHOT.jar"

copy %JAR_PATH% ./midpoint/pio/icf-connectors/

docker-compose -f ./docker-compose.yml up --build --force-recreate -d