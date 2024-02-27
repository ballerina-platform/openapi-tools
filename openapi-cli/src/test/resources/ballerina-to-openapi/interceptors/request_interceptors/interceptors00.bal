import ballerina/http;

service class RequestInterceptor {
    *http:RequestInterceptor;

    resource function 'default [string... path](http:RequestContext ctx) returns http:NextService|error? {
        return ctx.next();
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [RequestInterceptor] {
        return [new];
    }

    resource function get hello() returns string {
        return "Hello, World!";
    }

    resource function post foo/bar() returns Person {
        Person person = {name: "John", age: 30, address: "Colombo"};
        return person;
    }

    resource function get foo/bar() returns string {
        return "Hello, World!";
    }

    resource function get foo/[string path]() returns string {
        return "Hello, World!";
    }
}
