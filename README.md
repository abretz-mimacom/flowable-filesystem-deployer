# Flowable Filesystem Deployer

## Filesystem/git deployer
Exposes a REST endpoint that can receive application deployment requests.

## Description
Flowable can deplapplicationsto multiple endpoints from Designer is so configured.
This application provides a REST endpoint to receive a deployment request, save the application to a specific location in the filesystem.
The application may be retained in .zip format or each file may be extracted and saved individually.
If the folder is a git repository, a commit will be made for any updated files.

## Designer Configuration
Flowable design application must be configured to deploy to this endpoint.

```properties
flowable.modeler.app.deployment.1.name=Filesystem Deployment
flowable.modeler.app.deployment.1.deployment-api-url=http://<host>:<port>8123/
```
## Deployer Configuration
The application can be configured using the following properties

| Property              | Example Value          | Description                                |
|-----------------------|------------------------|--------------------------------------------|
| server.port           | 8123                   | The port the deployer will be listening on |
| destination.dir       | C:/Dev/project         | The folder to deploy the application to    |
| is.git.repository     | false                  | Set to true if this is a git repo          |
| explode.archive       | false                  | Set to true if you wish archive exploded   |     

If the git flag is set, then a standard message with the App ID will be added to the commit.


## License
Apache 2.0

## Project status
A simple demo project that I have been asked for many times now.
Thought I needed to put it somewhere others can share
