import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function post reservation("active"|"inactive"|"terminated"? status){
    }
}