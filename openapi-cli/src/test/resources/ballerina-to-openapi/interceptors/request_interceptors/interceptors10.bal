import ballerina/http;
import ballerina/http.httpscerr;

enum Path {
    FOO = "foo",
    BAR = "bar",
    BAZ = "baz"
}

type Course record {
    string name;
    int id;
};

type NewCourse record {|
    string newName;
    int newId;
|};

type HTTPVersionNotSupportedErrorDetail record {|
    *httpscerr:ErrorDetail;
    record {|
        string messgage;
        string version;
    |} body;
|};

type HTTPVersionNotSupportedError httpscerr:HTTPVersionNotSupportedError & error<HTTPVersionNotSupportedErrorDetail>;

type NotImplemented record {|
    *http:NotImplemented;
    record {|
        string message;
        Course[] courses;
        (int|string)...;
    |} body;
|};

service class RequestInterceptor1 {
    *http:RequestInterceptor;

    resource function 'default [FOO|BAZ p1]/[string p2]/[Path p3]/[1.0|1.1|2.0 p4]/[int... p5](http:RequestContext ctx)
            returns Course[]|http:NextService|error? {
        return;
    }
}

service class RequestInterceptor2 {
    *http:RequestInterceptor;

    resource function post [Path p1]/[string p2]/[FOO|BAR p3]/[float p4]/[1|2|3|4|5 p5]/[int p6](http:RequestContext ctx)
            returns http:NextService|NotImplemented|HTTPVersionNotSupportedError? {
        return;
    }
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [RequestInterceptor1, RequestInterceptor2] {
        return [];
    }

    resource function get foo/[string p1]/[FOO|BAR p2]/'2\.0/[1|2|3 p3]/'4() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function get foo/bar/[FOO|"no" p2]/'2\.0/[1|2|3 p3]/'4() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function get foo/baz/[FOO|"bar" p2]/'2\.2/[1|2|3 p3]/'4() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function get foo/[string p1]/[FOO|BAR p2]/'2\.0/[1|2|3 p3]/'4/bar() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    // This is added to check a edge case of the resource matcher
    // This will not be covered since currently we do not generate operation for resources with
    // the rest path parameter
    resource function get foo/[string p1]/[FOO|BAR p2]/'2\.0/[1|2|3 p3]/'4/[string... p4]() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function post foo/bar/[FOO|BAR p2]/'2\.0/[1|2|3 p3]/'4() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function post foo/bar/[FOO|"bar" p2]/'2\.0/[1|2|3 p3]/'4/'5() returns NewCourse {
        return {newName: "newName", newId: 1};
    }

    resource function post foo/bar/[FOO|BAR p2]/'2\.0/[1|2|3 p3]/'7/'5() returns NewCourse {
        return {newName: "newName", newId: 1};
    }
}
