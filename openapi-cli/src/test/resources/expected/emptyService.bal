import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: ""
}
@http:ServiceConfig {
    basePath: "/"
}

service   on ep0 {

}