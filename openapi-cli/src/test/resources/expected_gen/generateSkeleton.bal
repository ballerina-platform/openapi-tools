
service /v1 on ep0 {
    resource function get pets( int? 'limit) returns Pets|Error {
    }
    resource function post pets() returns http:|Error {
    }
    resource function get /pets/[string petId]() returns Pets|Error {
    }
}
