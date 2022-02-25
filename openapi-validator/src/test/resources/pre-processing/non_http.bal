import ballerina/openapi;
import ballerina/http;
import ballerina/graphql;

// Non-http service
@openapi:ServiceInfo{
    contract:""
}
service graphql:Service /query on new graphql:Listener(8080) {
   resource function get name() returns string {
       return "Jack";
   }
}