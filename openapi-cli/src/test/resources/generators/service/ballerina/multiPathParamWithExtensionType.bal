import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get 'field/[string \{id\}\.json]() returns http:Ok|error {
    }

    resource function get v4/spreadsheets/[int spreadsheetId]/sheets/[string \{sheetId\}\:copyTo]() returns http:Ok|ErrorDefault|error {
    }
}
