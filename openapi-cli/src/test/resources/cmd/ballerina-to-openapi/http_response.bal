import ballerina/http;

service / on new http:Listener(9090) {
    resource function get pet() returns http:Response|error? {
        http:Response response = new;
        return response;
    }
}
