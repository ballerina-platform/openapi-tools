import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.stripe.com/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # <p>You can list all invoices, or list the invoices for a specific customer. The invoices are returned sorted by creation date, with the most recently created invoices appearing first.</p>
    #
    # + collectionMethod - The collection method of the invoice to retrieve. Either `charge_automatically` or `send_invoice`.
    # + created - A filter on the list based on the object created field. The value can be a string with an integer Unix timestamp, or it can be a dictionary
    # + dueDate - A filter on the list based on the object due_date field. The value can be an integer Unix timestamp, or it can be a dictionary
    # + subscriptions - Only return invoices for the subscription specified by this subscription ID.
    # + return - Response
    remote isolated function listInvoices(string? collectionMethod = (), Created? created = (), DueDate? dueDate = (), string[]? subscriptions = ()) returns json|error {
        string resourcePath = string `/v1/invoices`;
        map<anydata> queryParam = {"collection_method": collectionMethod, "created": created, "due_date": dueDate, "subscriptions": subscriptions};
        map<Encoding> queryParamEncoding = {"created": {style: DEEPOBJECT, explode: true}, "due_date": {style: DEEPOBJECT, explode: true}, "subscriptions": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam, queryParamEncoding);
        json response = check self.clientEp->get(resourcePath);
        return response;
    }
}
