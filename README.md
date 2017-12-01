
# Jersey Kloudless Example

An example uploading to different storage providers, using the Kloudless API and Java Kloudless Java SDK.


### Build and Run

There seems to be some kind of discrepency between the Kloudless API release and what methods are supposed to
be available. For this project, you will need to build the SDK [from source][sdk]:

1. [Clone the project][sdk]
2. Change the `<version>` in the pom.xml file to `1.1.1-SNAPSHOT`
3. Run `mvn install -DskipTests`. You will need to have GPG installed (check `gpg` command) with a key created.

For _this_ project:

1. Run `mvn package`
2. Run `mvn jetty:run`

When the server has started, you should be able to access `http://localhost:8080`.


### TODO

* Fix bugs in front provider selection
* Make persisting tokens optional.



[sdk]: https://github.com/Kloudless/kloudless-java

