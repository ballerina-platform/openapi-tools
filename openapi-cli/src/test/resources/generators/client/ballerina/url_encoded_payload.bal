import ballerina/http;

# The Stripe REST API. Please see https://stripe.com/docs/api for more details.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, http:ClientConfiguration clientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # <p>Retrieves a PaymentMethod object.</p>
    #
    # + paymentMethod - Payment Method
    # + return - Successful response.
    remote isolated function getPaymentMethodsPaymentMethod(string paymentMethod) returns json|error {
        string resourcePath = string `/v1/payment_methods/${getEncodedUri(paymentMethod)}`;
        json response = check self.clientEp->get(resourcePath);
        return response;
    }
    # <p>Creates a new customer object.</p>
    #
    # + customer - Customer ID
    # + payload - Customer Details
    # + return - Successful response.
    remote isolated function postCustomers(string customer, CustomerCustomerBody payload) returns Customer|error {
        string resourcePath = string `/v1/customer/${getEncodedUri(customer)}`;
        http:Request request = new;
        string encodedRequestBody = createFormURLEncodedRequestBody(payload);
        request.setPayload(encodedRequestBody, "application/x-www-form-urlencoded");
        Customer response = check self.clientEp->post(resourcePath, request);
        return response;
    }
}
