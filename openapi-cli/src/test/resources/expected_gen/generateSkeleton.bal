service /v1 on ep0, ep1 {

    resource function get pets(http:Caller caller, http:Request req, int limit) returns error? {

    }

    resource function post pets(http:Caller caller, http:Request req) returns error? {

    }

    resource function get pets/[string petId](http:Caller caller, http:Request req) returns error? {

    }
}
