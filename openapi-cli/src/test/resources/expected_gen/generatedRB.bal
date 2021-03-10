
service on ep0 {
    resource function post requestBody(@http:Payload {} User payload) returns http:Ok {
    }
}