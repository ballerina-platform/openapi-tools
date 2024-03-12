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

service class ResponseErrorInterceptor1 {
    *http:ResponseErrorInterceptor;

    remote function interceptResponseError(error err, http:RequestContext ctx) returns string|InternalServerError|error {
        return {
            body: {
                timestamp: {
                    year: 2021,
                    month: 1,
                    day: 1,
                    hour: 10,
                    minute: 10
                }
            }
        };
    }
}

service class ResponseErrorInterceptor2 {
    *http:ResponseErrorInterceptor;

    remote function interceptResponseError(error err, http:RequestContext ctx) returns map<string> {
        return {};
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [ResponseErrorInterceptor1, ResponseErrorInterceptor2] {
        return [];
    }

    resource function post person(Person p) returns Person|error {
        return p;
    }

    resource function post persons(Person[] p) returns Person[]|http:BadRequest|httpscerr:DefaultStatusCodeError {
        return p;
    }
}
