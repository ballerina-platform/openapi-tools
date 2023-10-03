import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + spreadsheetId - The id of the pet to retrieve
    # + sheetidCopyto - The id of the pet to retrieve
    # + return - returns can be any of following types
    # http:Ok (Expected response to a valid request)
    # http:Response (unexpected error)
    resource function get v4/spreadsheets/[int spreadsheetId]/sheets/[string sheetidCopyto]() returns http:Ok|http:Response|error {
        if !sheetidCopyto.endsWith(":copyTo") {
            return error("bad URL");
        }
        string sheetId = sheetidCopyto.substring(0, sheetidCopyto.length() - 6);
    }
    # Get the details of the specified field
    #
    # + idJson - Field ID
    # + return - Successful response
    resource function get 'field/[string idJson]() returns http:Ok|error {
        if !idJson.endsWith(".json") {
            return error("bad URL");
        }
        string id = idJson.substring(0, idJson.length() - 4);
    }
}
