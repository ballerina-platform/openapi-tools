import ballerina/http;

type Error distinct error;
type UserNotFound distinct Error;

type VersionNotSupported distinct Error;

type User record {|
    string name;
    int age;
|};

service class RequestInterceptor {
    *http:RequestInterceptor;

	resource function 'default [string... path](http:RequestContext ctx) returns VersionNotSupported|string|http:NextService? {
		return checkpanic ctx.next();
	}
}

service class RequestErrorInterceptor {
    *http:RequestErrorInterceptor;

	resource function 'default [string... path](error err, http:RequestContext ctx) returns http:NotImplemented|http:NextService|error? {
		return ctx.next();
	}
}

service class ResponseInterceptor {
    *http:ResponseInterceptor;

	remote function interceptResponse(http:RequestContext ctx) returns http:NextService? {
		return checkpanic ctx.next();
	}
}

service class ResponseErrorInterceptor {
    *http:ResponseErrorInterceptor;

	remote function interceptResponseError(error err, http:RequestContext ctx) returns http:NotFound|http:NextService? {
		return checkpanic ctx.next();
	}
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [ResponseInterceptor, ResponseErrorInterceptor, RequestInterceptor, RequestErrorInterceptor] {
        return [];
    }

    resource function get users() returns User[] {
        return [{name: "John", age: 30}, {name: "Doe", age: 40}];
    }

    resource function post users(User[] users) returns User[] {
        return users;
    }

    resource function get users/[int id]() returns User|UserNotFound {
        return {name: "John", age: 30};
    }

    resource function post user() returns User {
        return {name: "John", age: 30};
    }
}
