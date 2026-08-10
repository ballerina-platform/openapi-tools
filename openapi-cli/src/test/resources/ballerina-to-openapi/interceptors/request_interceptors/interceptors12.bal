import ballerina/http;

type Bar record {|
    string message;
|};

type FooResponse record {|
    Bar[] bars;
|};

service class RequestInterceptor {
    *http:RequestInterceptor;

    resource function 'default [string... path](http:RequestContext ctx)
            returns http:NextService|http:Unauthorized|error {
        return http:UNAUTHORIZED;
    }
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns RequestInterceptor {
        return new RequestInterceptor();
    }

    resource function get foo() returns FooResponse|http:InternalServerError {
        FooResponse response = {
            bars: []
        };
        return response;
    }
}
