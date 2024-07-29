import ballerina/http;
import ballerina/openapi;

@openapi:Example {value: "any"}
public type UserService any;

public type Record record {|
    @openapi:Example {value: "any"}
    any anyValue;
|};

public type User record {|
    @openapi:Example {
        value: "John"
    }
    string name;
    @openapi:Example {
        value: 30
    }
    int age;
    Address address;
    @openapi:Example {
        value: [
            {
                name: "Doe",
                age: 25,
                address: {
                    street: "10 Downing Street",
                    city: "London"
                }
            }
        ]
    }
    User[] friends?;
|};

@openapi:Example {
    value: {
        street: "10 Downing Street",
        city: "London"
    }
}
public type Address record {|
    string street;
    string city;
|};

@openapi:Example {
    value: [
        {
            name: "John",
            age: 30,
            address: {
                street: "10 Downing Street",
                city: "London"
            }
        }
    ]
}
public type Users User[];

service /api on new http:Listener(9090) {

    resource function post users/[
        @openapi:Examples {valid: {value: "valid"}, invalid: {value: "invalid"}}
        @openapi:Example {value: "valid"}
        string id
        ](
            @openapi:Example {value: "approved"}
            @openapi:Examples {approved: {value: "approved"}, pending: {value: "pending"}}
            "approved"|"pending"|"closed"|"new" status,
            @openapi:Examples {
                john: {
                    value: {
                        name: "John",
                        age: 30,
                        address: {
                            street: "10 Downing Street",
                            city: "London"
                        }
                    }
                },
                doe: {
                    value: {
                        name: "Doe",
                        age: 25,
                        address: {
                            street: "13 Buckingham Palace",
                            city: "Canberra"
                        }
                    }
                }
            } User user,
            @openapi:Example {value: "any"}
            http:Request req,
            @openapi:Examples {example: {value: "any"}}
            http:RequestContext ctx) returns User {
        return user;
    }

    resource function get users/[@openapi:Example {value: "any"} string... path]() returns Users {
        return [];
    }

    resource function get [@openapi:Examples {example: {value: "any"}} string... path]() returns Users {
        return [];
    }
}
