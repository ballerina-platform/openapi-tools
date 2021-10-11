import ballerina/http;

listener http:Listener helloEp = new (9090);

@http:ServiceConfig {
    treatNilableAsOptional: false
}
service /payloadV on helloEp {
    # Mock example
    #
    # + pet - Mock optional query parameter Description
    # + return - Return Value Description
    resource function get pets(decimal? pet) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
