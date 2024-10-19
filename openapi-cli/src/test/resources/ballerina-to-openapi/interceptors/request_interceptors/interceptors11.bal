import ballerina/http;
import ballerina/http.httpscerr;
import ballerina/time;

type ErrorPayloadNew record {|
    time:Civil timestamp;
    string message = "something went wrong";
    int code?;
|};

type InternalServerError record {|
    *http:InternalServerError;
    ErrorPayloadNew body;
|};

enum Path {
    FOO = "foo",
    BAR = "bar"
}

type PathType1 Path|"baz";

type PathType2 PathType1;

service class RequestInterceptor1 {
    *http:RequestInterceptor;

    resource function put .(http:RequestContext ctx) returns http:NotImplemented|http:NextService|error? {
        return ctx.next();
    }
}

service class RequestInterceptor2 {
    *http:RequestInterceptor;

    resource function 'default foo/[PathType2 p1]/[boolean p2]/[decimal p3]/'4\.56(http:RequestContext ctx) returns http:HttpVersionNotSupported|http:NextService|error? {
        return ctx.next();
    }
}

service class RequestErrorInterceptor {
    *http:RequestErrorInterceptor;

    resource function 'default [string... path](error err, http:RequestContext ctx) returns InternalServerError|http:NextService|httpscerr:NotFoundError? {
        return checkpanic ctx.next();
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [RequestInterceptor1, RequestInterceptor2, RequestErrorInterceptor] {
        return [];
    }

    resource function put .(Person p) returns () {
        return;
    }

    resource function put foo(Person p) {
        return;
    }

    resource function put [string path](Person p) returns Person {
        return p;
    }

    resource function get foo/bar/'true/'4\.35/'4\.56() returns Person[] {
        return [];
    }

    resource function post foo/baz/'false/'5\.2/'4\.56() returns record {|Person p;|} {
        return {p: {name: "John", age: 25, address: "Colombo"}};
    }

    resource function post foo/foo/[boolean b]/[decimal d]/'4\.56() returns string|Person|xml {
        return "Hello, World!";
    }
}
