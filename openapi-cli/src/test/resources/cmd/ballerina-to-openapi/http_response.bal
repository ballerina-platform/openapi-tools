import ballerina/http;

service /v1 on new http:Listener(9090) {
    resource function get pet() returns http:Response {
        http:Response response = new;
        return response;
    }
}
