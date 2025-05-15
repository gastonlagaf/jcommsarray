# Test Signaling Server

Sample application, used for ICE testing during development

## Bootstrap

There are two bootstrap methods:

### Pure Java Bootstrap

```shell
java -jar signaling-server-test.jar
```

### Docker Bootstrap

```shell
docker build -t signaling-server-test:latest .
docker run -d --name signaling-server-test -p 8080:8080 signaling-server-test:latest  
```