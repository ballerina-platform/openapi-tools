import ballerina/http;

type Category record {
    string id;
    string name;
};
service hello on new http:Listener(9090) {
    resource function get sayHello/[Category [] category]( ) returns error? {

    }
}
