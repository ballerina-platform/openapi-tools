import ballerina/http;
import ballerina/data.jsondata;

# The Stripe REST API. Please see https://stripe.com/docs/api for more details.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # <p>Retrieves a PaymentMethod object.</p>
    #
    # + payment_method - Payment Method
    # + headers - Headers to be sent with the request
    # + return - Successful response.
    remote isolated function getPaymentMethodsPaymentMethod(string payment_method, map<string|string[]> headers = {}) returns json|error {
        string resourcePath = string `/v1/payment_methods/${getEncodedUri(payment_method)}`;
        return self.clientEp->get(resourcePath, headers);
    }

    # <p>Creates a new customer object.</p>
    #
    # + customer - Customer ID
    # + headers - Headers to be sent with the request
    # + payload - Customer Details
    # + return - Successful response.
    remote isolated function postCustomers(string customer, customer_customer_body payload, map<string|string[]> headers = {}) returns Customer|error {
        string resourcePath = string `/v1/customer/${getEncodedUri(customer)}`;
        http:Request request = new;
        string encodedRequestBody = createFormURLEncodedRequestBody(check jsondata:toJson(payload).ensureType());
        request.setPayload(encodedRequestBody, "application/x-www-form-urlencoded");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
