import ballerina/http;

service class ResponseInterceptor {
    *http:ResponseInterceptor;

    remote function interceptResponse(http:Request req, http:RequestContext ctx) returns http:NextService|error? {
        return ctx.next();
    }
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns ResponseInterceptor {
        return new;
    }

    resource function get greet() returns string {
        return "Hello, World!";
    }
}
