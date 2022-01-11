import ballerina/http;

service /v1 on new http:Listener(9090) {
    resource function post pet(http:Request req) {

    }
}
