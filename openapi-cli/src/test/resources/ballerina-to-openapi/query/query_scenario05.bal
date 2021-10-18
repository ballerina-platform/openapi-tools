import ballerina/http;

@http:ServiceConfig {
    treatNilableAsOptional: false
}
service /payloadV on new http:Listener(9090) {
    # Mock example
    #
    # + pet - Mock optional query parameter Description
    # + return - Return Value Description
    resource function get pets(decimal? pet) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
