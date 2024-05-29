import ballerina/http;
import ballerina/jballerina.java;

function setModule() = @java:Method {'class: "io.ballerina.openapi.client.ModuleUtils"} external;

function init() {
    setModule();
}

type ClientMethodImpl record {|
    string name;
|};

annotation ClientMethodImpl MethodImpl on function;

type ClientMethodInvocationError http:ClientError;

function setModule() = @java:Method {'class: "io.ballerina.openapi.client.ModuleUtils"} external;

function init() {
    setModule();
}

type ClientMethodImpl record {|
    string name;
|};

annotation ClientMethodImpl MethodImpl on function;

type ClientMethodInvocationError http:ClientError;

@display {label: "Zuora Collection", iconPath: "icon.png"}
public isolated client class Client {
    final http:StatusCodeClient clientEp;
    # Gets invoked to initialize the `connector`.
    # The connector initialization requires setting the API credentials.
    # Create a [Zuora account](https://www.zuora.com/) and obtain tokens by following [this guide](https://www.zuora.com/developer/collect-api/#section/Authentication).
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config, string serviceUrl) returns error? {
        return;
    }

    # Update account agent
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    @MethodImpl {name: "UpdateAccountAgentImpl"}
    resource isolated function post api/v1/accounts/[string account_id]/update_agent(map<string|string[]> headers = {}, typedesc<OkInline_response_200> targetType = <>, *UpdateAccountAgentQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Update account status
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    @MethodImpl {name: "UpdateAccountImpl"}
    resource isolated function post api/v1/accounts/[string account_id]/update_status(map<string|string[]> headers = {}, typedesc<OkInline_response_200> targetType = <>, *UpdateAccountQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Create an ad hoc statement run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createAdhocStatementRunImpl"}
    resource isolated function post api/v1/run(POSTAdhocStatementRun payload, map<string|string[]> headers = {}, typedesc<OkPOSTAdhocStatementRunResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a custom statement run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createCustomStatementRunImpl"}
    resource isolated function post api/v1/execute(POSTCustomStatementRun payload, map<string|string[]> headers = {}, typedesc<OkPOSTCustomStatementRunResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a payment run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createPaymentRunImpl"}
    resource isolated function post api/v1/subscription_payment_runs(POSTPaymentRun payload, map<string|string[]> headers = {}, typedesc<OkPostpaymentrunresponseJson> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a payment run schedule
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createPaymentRunScheduleImpl"}
    resource isolated function post api/v1/payment_run_schedules(POSTPaymentRunSchedule payload, map<string|string[]> headers = {}, typedesc<OkPostpaymentrunscheduleresponseJson> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Cancel a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "deletePaymentRunScheduleImpl"}
    resource isolated function delete api/v1/payment_run_schedules/[int schedule_id](map<string|string[]> headers = {}, typedesc<OkDELETEPaymentRunScheduleResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get an account
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getAccountImpl"}
    resource isolated function get api/v1/accounts/[string account_id](map<string|string[]> headers = {}, typedesc<OkCollectionAccount> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all accounts
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getAccountsImpl"}
    resource isolated function get api/v1/accounts(map<string|string[]> headers = {}, typedesc<OkGETCollectionAccountsResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get a collections agent by email
    #
    # + email - The email of the collections agent.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsAgentImpl"}
    resource isolated function get api/v1/users/[string email](map<string|string[]> headers = {}, typedesc<OkCollectionAgent> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all collections agents
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsAgentsImpl"}
    resource isolated function get api/v1/users(map<string|string[]> headers = {}, typedesc<OkGETCollectionAgentsResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get an overview of collections
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsInfoImpl"}
    resource isolated function get api/v1/collections_info(map<string|string[]> headers = {}, typedesc<OkCollectionsInfo> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get a payment run
    #
    # + payment_run_id - The payment run ID. A payment run id in Advanced Payment Manager is different from a payment run id in Zuora.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunImpl"}
    resource isolated function get api/v1/subscription_payment_runs/[int payment_run_id](map<string|string[]> headers = {}, typedesc<OkGETPaymentRunResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunScheduleImpl"}
    resource isolated function get api/v1/payment_run_schedules/[int schedule_id](map<string|string[]> headers = {}, typedesc<OkGETPaymentRunScheduleResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all payment run schedules
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunSchedulesImpl"}
    resource isolated function get api/v1/payment_run_schedules(map<string|string[]> headers = {}, typedesc<OkGETPaymentRunSchedulesResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get Statement Generator settings
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getSettingsImpl"}
    resource isolated function get api/v1/fetch_settings(map<string|string[]> headers = {}, typedesc<OkGETStatementSettingsResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Update a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "updatePaymentRunScheduleImpl"}
    resource isolated function put api/v1/payment_run_schedules/[int schedule_id](PUTPaymentRunSchedule payload, map<string|string[]> headers = {}, typedesc<OkPOSTPaymentRunScheduleResponse> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    private isolated function UpdateAccountAgentImpl(string account_id, map<string|string[]> headers, typedesc<OkInline_response_200> targetType, *UpdateAccountAgentQueries queries) returns OkInline_response_200|error {
        return {
            body: {"success": true},
            headers: {}
        };
    }

    private isolated function UpdateAccountImpl(string account_id, map<string|string[]> headers, typedesc<OkInline_response_200> targetType, *UpdateAccountQueries queries) returns OkInline_response_200|error {
        return {
            body: {"success": true},
            headers: {}
        };
    }

    private isolated function createAdhocStatementRunImpl(POSTAdhocStatementRun payload, map<string|string[]> headers, typedesc<OkPOSTAdhocStatementRunResponse> targetType) returns OkPOSTAdhocStatementRunResponse|error {
        return {
            body: {"success": true, "code": 200, "message": "Success", "statement_number": "STA00000008"},
            headers: {}
        };
    }

    private isolated function createCustomStatementRunImpl(POSTCustomStatementRun payload, map<string|string[]> headers, typedesc<OkPOSTCustomStatementRunResponse> targetType) returns OkPOSTCustomStatementRunResponse|error {
        return {
            body: {"success": true, "code": 200, "message": "Success", "execution_number": "2", "report_file": "<aws_s3_link>"},
            headers: {}
        };
    }

    private isolated function createPaymentRunImpl(POSTPaymentRun payload, map<string|string[]> headers, typedesc<OkPostpaymentrunresponseJson> targetType) returns OkPostpaymentrunresponseJson|error {
        return {
            body: {"id": 6, "success": "true"},
            headers: {}
        };
    }

    private isolated function createPaymentRunScheduleImpl(POSTPaymentRunSchedule payload, map<string|string[]> headers, typedesc<OkPostpaymentrunscheduleresponseJson> targetType) returns OkPostpaymentrunscheduleresponseJson|error {
        return {
            body: {"id": 6, "success": "true"},
            headers: {}
        };
    }

    private isolated function deletePaymentRunScheduleImpl(int schedule_id, map<string|string[]> headers, typedesc<OkDELETEPaymentRunScheduleResponse> targetType) returns OkDELETEPaymentRunScheduleResponse|error {
        return {
            body: {"success": true},
            headers: {}
        };
    }

    private isolated function getAccountImpl(string account_id, map<string|string[]> headers, typedesc<OkCollectionAccount> targetType) returns OkCollectionAccount|error {
        return {
            body: {"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": true, "Pending": false, "In Dispute": false, "Paid In Full": false}},
            headers: {}
        };
    }

    private isolated function getAccountsImpl(map<string|string[]> headers, typedesc<OkGETCollectionAccountsResponse> targetType) returns OkGETCollectionAccountsResponse|error {
        return {
            body: {"accounts": [{"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": 0, "Pending": 0, "In Dispute": 0, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/accounts?page=2&page_length=20"}},
            headers: {}
        };
    }

    private isolated function getCollectionsAgentImpl(string email, map<string|string[]> headers, typedesc<OkCollectionAgent> targetType) returns OkCollectionAgent|error {
        return {
            body: {"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}},
            headers: {}
        };
    }

    private isolated function getCollectionsAgentsImpl(map<string|string[]> headers, typedesc<OkGETCollectionAgentsResponse> targetType) returns OkGETCollectionAgentsResponse|error {
        return {
            body: {"accounts": [{"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/users?page=2&page_length=20"}},
            headers: {}
        };
    }

    private isolated function getCollectionsInfoImpl(map<string|string[]> headers, typedesc<OkCollectionsInfo> targetType) returns OkCollectionsInfo|error {
        return {
            body: {"accounts_in_collections": 24, "home_currency": "USD", "total_debt": "8379.78", "largest_debts": ["Test Account - 12438.00 USD", "Jimmy John - 8000.00 USD", "James Smith - 2450.55 USD", "Bob Roberts - 1000.00 USD", "Jim Reynolds - 829.00 USD"], "oldest_debts": ["Test Account - 2662 days", "Jimbo - 1494 days", "Steve Smith - 942 days", "Jason Williams - 678 days", "Will Jasons - 365 days"], "statuses": {"In Collections": 24, "Pending": 2, "In Dispute": 5, "Paid in Full": 0}},
            headers: {}
        };
    }

    private isolated function getPaymentRunImpl(int payment_run_id, map<string|string[]> headers, typedesc<OkGETPaymentRunResponse> targetType) returns OkGETPaymentRunResponse|error {
        return {
            body: {"success": true, "id": 6, "status": "Complete", "target_date": "2018-01-02T00:00:00.000Z", "filter": "Account.Currency = 'CAD'", "payment_run_schedule_id": "Adhoc", "invoices_held": {}, "metrics": {"documents": 0, "payments": 0, "failed": 0, "skipped": 0, "amount": 0, "credit": 0}},
            headers: {}
        };
    }

    private isolated function getPaymentRunScheduleImpl(int schedule_id, map<string|string[]> headers, typedesc<OkGETPaymentRunScheduleResponse> targetType) returns OkGETPaymentRunScheduleResponse|error {
        return {
            body: {"success": true, "id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"},
            headers: {}
        };
    }

    private isolated function getPaymentRunSchedulesImpl(map<string|string[]> headers, typedesc<OkGETPaymentRunSchedulesResponse> targetType) returns OkGETPaymentRunSchedulesResponse|error {
        return {
            body: {"success": true, "size": 3, "schedules": [{"id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"}]},
            headers: {}
        };
    }

    private isolated function getSettingsImpl(map<string|string[]> headers, typedesc<OkGETStatementSettingsResponse> targetType) returns OkGETStatementSettingsResponse|error {
        return {
            body: {"success": true, "templates": [{"name": "Default Template"}, {"name": "Template for end consumers"}], "default_template": "Default Template", "default_cycle": "Month"},
            headers: {}
        };
    }

    private isolated function updatePaymentRunScheduleImpl(int schedule_id, PUTPaymentRunSchedule payload, map<string|string[]> headers, typedesc<OkPOSTPaymentRunScheduleResponse> targetType) returns OkPOSTPaymentRunScheduleResponse|error {
        return {
            body: {"id": 6, "success": true},
            headers: {}
        };
    }
}
