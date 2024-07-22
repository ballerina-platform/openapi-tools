import ballerina/http;
import ballerina/openapi;

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

public type Address record {|
    @openapi:Example {
        value: "10 Downing Street"
    }
    string street;
    @openapi:Example {
        value: "London"
    }
    string city;
    record {|
        @openapi:Example {
            value: "United Kingdom"
        }
        string country;
        @openapi:Example {
            value: "UK"
        }
        string code;
    |} country?;
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

    resource function post users/[@openapi:Example {value: "valid"} string id](
            @openapi:Example {value: "approved"}
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
            } User user) returns User {
        return user;
    }

    resource function get users() returns Users {
        return [];
    }

    resource function get users/[@openapi:Examples {valid: {value: "235340"}, invalid: {value: "23cjac0"}} string id]() returns User {
        return {
            name: "John",
            age: 30,
            address: {
                street: "10 Downing Street",
                city: "London"
            }
        };
    }

    resource function post developers(
            @openapi:Example {
                value: {
                    name: "John",
                    age: 30
                }
            }
            record {|string name; int age;|} user) returns json {
        return user;
    }

    resource function post developers/'new(
            record {|
                @openapi:Example {
                    value: "John"
                }
                string name;
                @openapi:Example {
                    value: 30
                }
                int age;
            |} user) returns json {
        return user;
    }

    resource function get users/[@openapi:Example {value: "2345"} string id]/address() returns Address {
        return {
            street: "10 Downing Street",
            city: "London",
            country: {
                country: "United Kingdom",
                code: "UK"
            }
        };
    }
}
