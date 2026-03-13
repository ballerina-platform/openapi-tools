    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Ok
    resource isolated function get albums/[int id](GetAlbumsIdHeaders headers = {}, *GetAlbumsIdQueries queries) returns record {}|error {
