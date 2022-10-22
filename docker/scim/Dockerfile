FROM maven:3.8.6-openjdk-18

RUN mkdir /scim && cd /scim \
    && git clone https://bitbucket.org/wouter29/personify-scim-server.git  

RUN mkdir /tmp_schem/

RUN cd /scim/personify-scim-server \
    && mvn clean install -Dgpg.skip 

WORKDIR /scim/personify-scim-server/

ENTRYPOINT ["java","-jar", "-Dserver.port=8080","./target/personify-scim-server-1.5.2.RELEASE.jar", "-Dscim.schema.location=/schemas/disc_schemas_custom.json"]
