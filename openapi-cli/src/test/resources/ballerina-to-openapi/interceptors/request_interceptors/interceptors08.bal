import ballerina/http;

type Course record {|
    string name;
    int duration;
    string lecturer;
|};

enum Path {
    FOO = "foo",
    BAR = "bar"
}

service class RequestInterceptor {
    *http:RequestInterceptor;

    resource function get [Path path1]/["foo"|"bar" path2](http:RequestContext ctx) returns Course|http:NextService? {
        return checkpanic ctx.next();
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns RequestInterceptor {
        return new;
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

    resource function get bar/foo() returns Person {
        Person person = {name: "John", age: 30, address: "Colombo"};
        return person;
    }

    resource function post foo/[string path]() returns string {
        return "Hello, World!";
    }

    resource function get [Path path1]/[Path path2]() returns string {
        return "Hello, World!";
    }

    resource function get ["bar"|"foo" path1]/foo() returns Person {
        Person person = {name: "John", age: 30, address: "Colombo"};
        return person;
    }
}
