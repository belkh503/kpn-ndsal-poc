.PHONY = build docker test

MVN = mvn
GRL = gradle
DCR = docker

default: build
all: docker

build:
	${MVN} clean install -DskipTests=true -Dmaven.multiModuleProjectDirectory=kpn-ndsal-resourcemanager

docker: build
	${DCR} build -t ndsal/resourcemanager-worker-service:latest .

test:
	${MVN} test -Dmaven.multiModuleProjectDirectory=kpn-ndsal-resourcemanager

run_neo4j:
    ${DCR} run --name testneo4j -p7474:7474 -p7687:7687 -d --env NEO4J_dbms_security_auth__enabled=false neo4j:4.4

# docs:
#     gradle asciidoctorPdf
#     mvn
