pipeline {
    agent {
        docker {
            image 'maven:3.8.1-adoptopenjdk-11' 
            args '-v /root/.m2:/root/.m2' 
        }
    }
    stages {
        stage('Build') { 
           steps {
		sh 'mvn -f Scim2Connector/scim-2-connector/pom.xml -B -DskipTests clean package -X -s settings.xml' 
            }
        }
    }
}
