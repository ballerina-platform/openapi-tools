import ballerina/http;
import ballerina/openapi;
import ballerina/graphql;

@openapi:ServiceInfo {
    contract: "hello_openapi.yaml",
    'version: "3.0.0"
}
service /greeting on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}
service graphql:Service /query on new graphql:Listener(8080) {
   resource function get name() returns string {
       return "Jack";
   }
}

