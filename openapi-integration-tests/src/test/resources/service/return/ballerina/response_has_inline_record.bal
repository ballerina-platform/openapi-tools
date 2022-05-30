import ballerina/http;

    public type BadRequestInlineResponse400 record {|
        *http:BadRequest;
        InlineResponse400 body;
    |};

    public type User record {
        string userName;
        string firstName?;
        string lastName?;
    };

    public type PetForm record {
        string userName;
        string firstName?;
        string lastName?;
    };

    public type InlineResponse400 record {
        # The error ID.
        int id?;
        # The error name.
        string errorType?;
    };

    public type Pet record {
        string userName;
        string firstName?;
        string lastName?;
    };
