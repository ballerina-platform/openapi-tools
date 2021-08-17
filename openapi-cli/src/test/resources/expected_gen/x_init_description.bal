import ballerina/http;

# With the Movie Reviews API, you can search New York Times movie reviews by keyword and get lists of NYT Critics' Picks. This is a feature given by new york times.
# Please visit [NYTimes](https://developer.nytimes.com/accounts/login) for more details
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    # Client initialization required API credentials and service URL.
    # The service URL may set to the default value. You can override if required.
    # Create [NYTimes](https://developer.nytimes.com/accounts/login) Developer Account.
    # Log into NYTimes Developer Portal by visiting https://developer.nytimes.com/accounts/login.
    # Register an app and obtain the API Key following the process summarized [here](https://developer.nytimes.com/get-started).
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.nytimes.com/svc/movies/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # Get movie reviews that are critics' picks. You can either specify the reviewer name or use "all", "full-time", or "part-time".
    #
    # + return - An array of Movie Critics
    remote isolated function criticsPicks() returns InlineResponse200|error {
        string  path = string `/`;
        InlineResponse200 response = check self.clientEp-> get(path, targetType = InlineResponse200);
        return response;
    }
}
