import ballerina/http;

type User record {|
    string name;
    int age;
|};

service class RequestInterceptor {
    *http:RequestInterceptor;

	resource function 'default [string... path](http:RequestContext ctx) returns http:NextService|error? {
		return ctx.next();
	}
}

service class RequestErrorInterceptor {
    *http:RequestErrorInterceptor;

	resource function 'default [string... path](error err, http:RequestContext ctx) returns http:NextService|error? {
		return ctx.next();
	}
}

service class ResponseInterceptor {
    *http:ResponseInterceptor;

	remote function interceptResponse(http:RequestContext ctx) returns http:NextService|error? {
		return ctx.next();
	}
}

service class ResponseErrorInterceptor {
    *http:ResponseErrorInterceptor;

	remote function interceptResponseError(error err, http:RequestContext ctx) returns http:NextService|error? {
		return ctx.next();
	}
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns http:Interceptor[] {
        return [new RequestInterceptor(), new RequestErrorInterceptor(), new ResponseInterceptor(), new ResponseErrorInterceptor()];
    }

    resource function get users() returns User[] {
        return [{name: "John", age: 30}, {name: "Doe", age: 40}];
    }

    resource function post users(User[] users) returns User[] {
        return users;
    }

    resource function get users/[int id]() returns User {
        return {name: "John", age: 30};
    }

    resource function post user() returns User {
        return {name: "John", age: 30};
    }
}
