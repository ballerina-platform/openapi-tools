import ballerina/http;

service hello on new http:Listener(9090) {
    resource function get sayHello/[int[][] userId] () returns error? {
    }
}
