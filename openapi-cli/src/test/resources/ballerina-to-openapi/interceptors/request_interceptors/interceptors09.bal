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

    resource function post [Path path1]/["foo"|"bar" path2]/[1|2|3|4... path3](http:RequestContext ctx) returns Course|http:NextService? {
        return checkpanic ctx.next();
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

    resource function post foo/bar/'3() returns Person {
        Person person = {name: "John", age: 30, address: "Colombo"};
        return person;
    }

    resource function get foo/bar() returns string {
        return "Hello, World!";
    }

    resource function post bar/foo/'4/'2/'2/'1() returns int {
        return 4221;
    }

    resource function post ["foo"|BAR path1]/[FOO|"bar" path2]/[1|2|3 path]/'4() returns http:Response? {
        return new;
    }
}
