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

public isolated client class Client {
    # Gets invoked to initialize the `connector`.
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
    resource isolated function post api/v1/accounts/[string account_id]/update_agent(map<string|string[]> headers = {}, typedesc<InlineResponse200Ok> targetType = <>, *UpdateAccountAgentQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Update account status
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    @MethodImpl {name: "UpdateAccountImpl"}
    resource isolated function post api/v1/accounts/[string account_id]/update_status(map<string|string[]> headers = {}, typedesc<InlineResponse200Ok> targetType = <>, *UpdateAccountQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Execute payments
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "UpdateExecutePaymentsImpl"}
    resource isolated function post api/v1/payments/execute_payments(payments_execute_payments_body payload, map<string|string[]> headers = {}, typedesc<InlineResponse2004Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Submit a payment to retry cycle
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "UpdateSubmitPaymentToCycleImpl"}
    resource isolated function post api/v1/payments/submit_failed_payment(payments_submit_failed_payment_body payload, map<string|string[]> headers = {}, typedesc<InlineResponse2008Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create an ad hoc statement run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createAdhocStatementRunImpl"}
    resource isolated function post api/v1/run(POSTAdhocStatementRun payload, map<string|string[]> headers = {}, typedesc<POSTAdhocStatementRunResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a custom statement run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createCustomStatementRunImpl"}
    resource isolated function post api/v1/execute(POSTCustomStatementRun payload, map<string|string[]> headers = {}, typedesc<POSTCustomStatementRunResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a payment run
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createPaymentRunImpl"}
    resource isolated function post api/v1/subscription_payment_runs(POSTPaymentRun payload, map<string|string[]> headers = {}, typedesc<POSTPaymentRunResponseJsonOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Create a payment run schedule
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "createPaymentRunScheduleImpl"}
    resource isolated function post api/v1/payment_run_schedules(POSTPaymentRunSchedule payload, map<string|string[]> headers = {}, typedesc<POSTPaymentRunScheduleResponseJsonOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Cancel a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "deletePaymentRunScheduleImpl"}
    resource isolated function delete api/v1/payment_run_schedules/[int schedule_id](map<string|string[]> headers = {}, typedesc<DELETEPaymentRunScheduleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "executeExecuteAccountPaymentsImpl"}
    resource isolated function put api/v1/payments/execute_account_payments/\<account_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2003Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "executeExecuteDebitMemoPaymentImpl"}
    resource isolated function put api/v1/payments/execute_debit_memo_payment/\<debit_memo_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2002Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "executeExecuteInvoicePaymentImpl"}
    resource isolated function put api/v1/payments/execute_invoice_payment/\<invoice_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2001Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "getAccountCycleHistoryImpl"}
    resource isolated function get api/v1/payments/account_cycle_history/\<account_id\>(map<string|string[]> headers = {}, typedesc<GETAccountCycleHistoryResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get an account
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getAccountImpl"}
    resource isolated function get api/v1/accounts/[string account_id](map<string|string[]> headers = {}, typedesc<CollectionAccountOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all accounts
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getAccountsImpl"}
    resource isolated function get api/v1/accounts(map<string|string[]> headers = {}, typedesc<GETCollectionAccountsResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "getActiveAccountCycleImpl"}
    resource isolated function get api/v1/payments/active_account_cycle_information/\<account_id\>(map<string|string[]> headers = {}, typedesc<GETActiveAccountCycleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "getActiveDebitMemoCycleImpl"}
    resource isolated function get api/v1/payments/active_debit_memo_cycle_information/\<debit_memo_id\>(map<string|string[]> headers = {}, typedesc<GETActiveDebitMemoCycleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "getActiveInvoiceCycleImpl"}
    resource isolated function get api/v1/payments/active_invoice_cycle_information/\<invoice_id\>(map<string|string[]> headers = {}, typedesc<GETActiveInvoiceCycleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get the Amount Recovered metrics
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getAmountRecoveredImpl"}
    resource isolated function get api/v1/metrics/amount_recovered(map<string|string[]> headers = {}, typedesc<GETAmountRecoveredResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get baseline metrics
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getBaselineMetricsImpl"}
    resource isolated function get api/v1/metrics/baseline(map<string|string[]> headers = {}, typedesc<GETBaselineMetricsResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get a collections agent by email
    #
    # + email - The email of the collections agent.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsAgentImpl"}
    resource isolated function get api/v1/users/[string email](map<string|string[]> headers = {}, typedesc<CollectionAgentOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all collections agents
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsAgentsImpl"}
    resource isolated function get api/v1/users(map<string|string[]> headers = {}, typedesc<GETCollectionAgentsResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get an overview of collections
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCollectionsInfoImpl"}
    resource isolated function get api/v1/collections_info(map<string|string[]> headers = {}, typedesc<CollectionsInfoOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get the Customer Group metrics
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getCustomerGroupMetricsImpl"}
    resource isolated function get api/v1/metrics/customer_group(map<string|string[]> headers = {}, typedesc<GETCustomerGroupMetricsResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "getDebitMemoCycleHistoryImpl"}
    resource isolated function get api/v1/payments/debit_memo_cycle_history/\<debit_memo_id\>(map<string|string[]> headers = {}, typedesc<GETDebitMemoCycleHistoryResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get the Document Success Rate metrics by customer group
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getDocumentSuccessRateByCustomerGroupImpl"}
    resource isolated function get api/v1/metrics/customer_group_over_time(map<string|string[]> headers = {}, typedesc<GETDocumentSuccessRateByCustomerGroupResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "getInvoiceCycleHistoryImpl"}
    resource isolated function get api/v1/payments/invoice_cycle_history/\<invoice_id\>(map<string|string[]> headers = {}, typedesc<GETInvoiceCycleHistoryResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get a payment run
    #
    # + payment_run_id - The payment run ID. A payment run id in Advanced Payment Manager is different from a payment run id in Zuora.
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunImpl"}
    resource isolated function get api/v1/subscription_payment_runs/[int payment_run_id](map<string|string[]> headers = {}, typedesc<GETPaymentRunResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunScheduleImpl"}
    resource isolated function get api/v1/payment_run_schedules/[int schedule_id](map<string|string[]> headers = {}, typedesc<GETPaymentRunScheduleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Get all payment run schedules
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getPaymentRunSchedulesImpl"}
    resource isolated function get api/v1/payment_run_schedules(map<string|string[]> headers = {}, typedesc<GETPaymentRunSchedulesResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # Get Statement Generator settings
    #
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "getSettingsImpl"}
    resource isolated function get api/v1/fetch_settings(map<string|string[]> headers = {}, typedesc<GETStatementSettingsResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    @MethodImpl {name: "removeAccountFromCycleImpl"}
    resource isolated function put api/v1/payments/remove_account_from_retry_cycle/\<account_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2007Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "removeDebitMemoFromCycleImpl"}
    resource isolated function put api/v1/payments/remove_debit_memo_from_retry_cycle/\<debit_memo_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2006Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    @MethodImpl {name: "removeRemoveInoviceFromCycleImpl"}
    resource isolated function put api/v1/payments/remove_invoice_from_retry_cycle/\<invoice_id\>(map<string|string[]> headers = {}, typedesc<InlineResponse2005Ok> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # Update a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    @MethodImpl {name: "updatePaymentRunScheduleImpl"}
    resource isolated function put api/v1/payment_run_schedules/[int schedule_id](PUTPaymentRunSchedule payload, map<string|string[]> headers = {}, typedesc<POSTPaymentRunScheduleResponseOk> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    private isolated function UpdateAccountAgentImpl(string account_id, map<string|string[]> headers, typedesc<InlineResponse200Ok> targetType, *UpdateAccountAgentQueries queries) returns http:StatusCodeResponse|error {
        return <InlineResponse200Ok>{
            body: {"success": true}
        };
    }

    private isolated function UpdateAccountImpl(string account_id, map<string|string[]> headers, typedesc<InlineResponse200Ok> targetType, *UpdateAccountQueries queries) returns http:StatusCodeResponse|error {
        return <InlineResponse200Ok>{
            body: {"success": true}
        };
    }

    private isolated function UpdateExecutePaymentsImpl(payments_execute_payments_body payload, map<string|string[]> headers, typedesc<InlineResponse2004Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2004Ok>{
            body: {"success": true, "message": "Payments with the following IDs enqueued for processing: [100, 101, 110, 111, 121]"}
        };
    }

    private isolated function UpdateSubmitPaymentToCycleImpl(payments_submit_failed_payment_body payload, map<string|string[]> headers, typedesc<InlineResponse2008Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2008Ok>{
            body: {"success": true, "message": "Payment entered into retry process"}
        };
    }

    private isolated function createAdhocStatementRunImpl(POSTAdhocStatementRun payload, map<string|string[]> headers, typedesc<POSTAdhocStatementRunResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <POSTAdhocStatementRunResponseOk>{
            body: {"success": true, "code": 200, "message": "Success", "statement_number": "STA00000008"}
        };
    }

    private isolated function createCustomStatementRunImpl(POSTCustomStatementRun payload, map<string|string[]> headers, typedesc<POSTCustomStatementRunResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <POSTCustomStatementRunResponseOk>{
            body: {"success": true, "code": 200, "message": "Success", "execution_number": "2", "report_file": "<aws_s3_link>"}
        };
    }

    private isolated function createPaymentRunImpl(POSTPaymentRun payload, map<string|string[]> headers, typedesc<POSTPaymentRunResponseJsonOk> targetType) returns http:StatusCodeResponse|error {
        return <POSTPaymentRunResponseJsonOk>{
            body: {"id": 6, "success": "true"}
        };
    }

    private isolated function createPaymentRunScheduleImpl(POSTPaymentRunSchedule payload, map<string|string[]> headers, typedesc<POSTPaymentRunScheduleResponseJsonOk> targetType) returns http:StatusCodeResponse|error {
        return <POSTPaymentRunScheduleResponseJsonOk>{
            body: {"id": 6, "success": "true"}
        };
    }

    private isolated function deletePaymentRunScheduleImpl(int schedule_id, map<string|string[]> headers, typedesc<DELETEPaymentRunScheduleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <DELETEPaymentRunScheduleResponseOk>{
            body: {"success": true}
        };
    }

    private isolated function executeExecuteAccountPaymentsImpl(map<string|string[]> headers, typedesc<InlineResponse2003Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2003Ok>{
            body: {"success": true, "message": "Payments with the following IDs enqueued for processing: [310, 311, 312]"}
        };
    }

    private isolated function executeExecuteDebitMemoPaymentImpl(map<string|string[]> headers, typedesc<InlineResponse2002Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2002Ok>{
            body: {"success": true, "message": "Payments with the following IDs enqueued for processing: [300]"}
        };
    }

    private isolated function executeExecuteInvoicePaymentImpl(map<string|string[]> headers, typedesc<InlineResponse2001Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2001Ok>{
            body: {"success": true, "message": "Payments with the following IDs enqueued for processing: [290, 291]"}
        };
    }

    private isolated function getAccountCycleHistoryImpl(map<string|string[]> headers, typedesc<GETAccountCycleHistoryResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETAccountCycleHistoryResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 3, "next_attempt": "2021-03-19T18:53:39.641Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:43:28.670-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bcdfc010671", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fb78532b0f01785a38cede190a", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-03-24T19:34:20.254Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51aae41969", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.316-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getAccountImpl(string account_id, map<string|string[]> headers, typedesc<CollectionAccountOk> targetType) returns http:StatusCodeResponse|error {
        return <CollectionAccountOk>{
            body: {"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": true, "Pending": false, "In Dispute": false, "Paid In Full": false}}
        };
    }

    private isolated function getAccountsImpl(map<string|string[]> headers, typedesc<GETCollectionAccountsResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETCollectionAccountsResponseOk>{
            body: {"accounts": [{"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": 0, "Pending": 0, "In Dispute": 0, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/accounts?page=2&page_length=20"}}
        };
    }

    private isolated function getActiveAccountCycleImpl(map<string|string[]> headers, typedesc<GETActiveAccountCycleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETActiveAccountCycleResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "debit_memo_id": "2c92c0fb78532b0001785a38f6427976", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-04-01T19:27:34.473Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a5199a6192d", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:27.521-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "invoice_id": "2c92c0fa7853052701785a38c6622473", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-04-01T19:27:34.436Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a519d85193b", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:28.161-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getActiveDebitMemoCycleImpl(map<string|string[]> headers, typedesc<GETActiveDebitMemoCycleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETActiveDebitMemoCycleResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f868e161e70168e25eb51d755f", "debit_memo_id": "2c92c0fa7853052701785a38f3bb267f", "payment_method_id": "2c92c0f8774f1afe01775f6e533c4c06", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 2, "next_attempt": "2021-04-01T10:22:57.464Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51af791979", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.380-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c0857881cf3e01788ee263b0331c", "time_of_execution": "2021-04-01T19:21:55.207Z", "source": "PR-00000380", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-04-01T10:22:57.464-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getActiveInvoiceCycleImpl(map<string|string[]> headers, typedesc<GETActiveInvoiceCycleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETActiveInvoiceCycleResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "invoice_id": "2c92c0f8778bf8cd017798168cb50e0b", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 2, "next_attempt": "2021-04-01T19:27:34.648Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ca076c21", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-04-01T19:27:34.648-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bce0d0a06d5", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:53:39.845-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getAmountRecoveredImpl(map<string|string[]> headers, typedesc<GETAmountRecoveredResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETAmountRecoveredResponseOk>{
            body: {"currency": {"USD": {"total_amount": "77515.21", "last_30_days": "1100.01"}, "EUR": {"total_amount": "337.19", "last_30_days": "17.17"}, "CAD": {"total_amount": "123954.10", "last_30_days": "5132.87"}}, "success": true}
        };
    }

    private isolated function getBaselineMetricsImpl(map<string|string[]> headers, typedesc<GETBaselineMetricsResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETBaselineMetricsResponseOk>{
            body: {"retry_success_rate": "11.90", "retry_success_rate_trend": "down", "document_success_rate": "13.54", "document_success_rate_trend": "neutral", "average_days_outstanding": "4.76", "average_days_outstanding_trend": "up", "success": true}
        };
    }

    private isolated function getCollectionsAgentImpl(string email, map<string|string[]> headers, typedesc<CollectionAgentOk> targetType) returns http:StatusCodeResponse|error {
        return <CollectionAgentOk>{
            body: {"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}}
        };
    }

    private isolated function getCollectionsAgentsImpl(map<string|string[]> headers, typedesc<GETCollectionAgentsResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETCollectionAgentsResponseOk>{
            body: {"accounts": [{"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/users?page=2&page_length=20"}}
        };
    }

    private isolated function getCollectionsInfoImpl(map<string|string[]> headers, typedesc<CollectionsInfoOk> targetType) returns http:StatusCodeResponse|error {
        return <CollectionsInfoOk>{
            body: {"accounts_in_collections": 24, "home_currency": "USD", "total_debt": "8379.78", "largest_debts": ["Test Account - 12438.00 USD", "Jimmy John - 8000.00 USD", "James Smith - 2450.55 USD", "Bob Roberts - 1000.00 USD", "Jim Reynolds - 829.00 USD"], "oldest_debts": ["Test Account - 2662 days", "Jimbo - 1494 days", "Steve Smith - 942 days", "Jason Williams - 678 days", "Will Jasons - 365 days"], "statuses": {"In Collections": 24, "Pending": 2, "In Dispute": 5, "Paid in Full": 0}}
        };
    }

    private isolated function getCustomerGroupMetricsImpl(map<string|string[]> headers, typedesc<GETCustomerGroupMetricsResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETCustomerGroupMetricsResponseOk>{
            body: {"customer_groups": [{"id": 1, "name": "batch22", "smart_retry": false, "document_success_rate": "17.17", "document_success_rate_trend": "up", "retry_success_rate": "21.76", "average_attempts": "4.11"}, {"id": 2, "name": "Smart Retry", "smart_retry": true, "document_success_rate": "74.17", "document_success_rate_trend": "down", "retry_success_rate": "81.21", "average_attempts": "1.32"}, {"id": 4, "name": "All Remaining Customers", "smart_retry": false, "document_success_rate": "16.35", "document_success_rate_trend": "up", "retry_success_rate": "15.21", "average_attempts": "3.32"}], "success": true}
        };
    }

    private isolated function getDebitMemoCycleHistoryImpl(map<string|string[]> headers, typedesc<GETDebitMemoCycleHistoryResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETDebitMemoCycleHistoryResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f868e161e70168e25eb51d755f", "debit_memo_id": "2c92c0fa7853052701785a38f3bb267f", "payment_method_id": "2c92c0f8774f1afe01775f6e533c4c06", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 2, "next_attempt": null, "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51af791979", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.380-09:00", "criteria": "incremental_time", "api_updated_retry_time": "2021-03-26T14:27:21.107-04:00"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c0857881cf3e01788ee263b0331c", "time_of_execution": "2021-04-01T19:21:55.207Z", "source": "PR-00000378", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getDocumentSuccessRateByCustomerGroupImpl(map<string|string[]> headers, typedesc<GETDocumentSuccessRateByCustomerGroupResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETDocumentSuccessRateByCustomerGroupResponseOk>{
            body: {"customer_groups": {"1": {"05_21": "17.53", "04_21": "13.21", "03_21": "14.92", "02_21": "8.99", "01_21": "34.25", "12_20": "12.30"}, "2": {"05_21": "11.11", "04_21": "7.87", "03_21": "26.00", "02_21": "11.06", "01_21": "13.43", "12_20": "17.92"}, "4": {"05_21": "11.13", "04_21": "9.17", "03_21": "17.20", "02_21": "19.06", "01_21": "12.43", "12_20": "15.92"}}, "success": true}
        };
    }

    private isolated function getInvoiceCycleHistoryImpl(map<string|string[]> headers, typedesc<GETInvoiceCycleHistoryResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETInvoiceCycleHistoryResponseOk>{
            body: {"cycles": [{"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 1, "next_attempt": null, "customer_group": "Testing Group", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-04-01T19:11:21.639Z", "source": "PR-00000370", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 5}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 2, "next_attempt": null, "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:43:28.670-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bcdfc010671", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]}
        };
    }

    private isolated function getPaymentRunImpl(int payment_run_id, map<string|string[]> headers, typedesc<GETPaymentRunResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETPaymentRunResponseOk>{
            body: {"success": true, "id": 6, "status": "Complete", "target_date": "2018-01-02T00:00:00.000Z", "filter": "Account.Currency = 'CAD'", "payment_run_schedule_id": "Adhoc", "invoices_held": {}, "metrics": {"documents": 0, "payments": 0, "failed": 0, "skipped": 0, "amount": 0, "credit": 0}}
        };
    }

    private isolated function getPaymentRunScheduleImpl(int schedule_id, map<string|string[]> headers, typedesc<GETPaymentRunScheduleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETPaymentRunScheduleResponseOk>{
            body: {"success": true, "id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"}
        };
    }

    private isolated function getPaymentRunSchedulesImpl(map<string|string[]> headers, typedesc<GETPaymentRunSchedulesResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETPaymentRunSchedulesResponseOk>{
            body: {"success": true, "size": 3, "schedules": [{"id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"}]}
        };
    }

    private isolated function getSettingsImpl(map<string|string[]> headers, typedesc<GETStatementSettingsResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <GETStatementSettingsResponseOk>{
            body: {"success": true, "templates": [{"name": "Default Template"}, {"name": "Template for end consumers"}], "default_template": "Default Template", "default_cycle": "Month"}
        };
    }

    private isolated function removeAccountFromCycleImpl(map<string|string[]> headers, typedesc<InlineResponse2007Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2007Ok>{
            body: {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [310, 311, 312]"}
        };
    }

    private isolated function removeDebitMemoFromCycleImpl(map<string|string[]> headers, typedesc<InlineResponse2006Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2006Ok>{
            body: {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [301]"}
        };
    }

    private isolated function removeRemoveInoviceFromCycleImpl(map<string|string[]> headers, typedesc<InlineResponse2005Ok> targetType) returns http:StatusCodeResponse|error {
        return <InlineResponse2005Ok>{
            body: {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [290, 291]"}
        };
    }

    private isolated function updatePaymentRunScheduleImpl(int schedule_id, PUTPaymentRunSchedule payload, map<string|string[]> headers, typedesc<POSTPaymentRunScheduleResponseOk> targetType) returns http:StatusCodeResponse|error {
        return <POSTPaymentRunScheduleResponseOk>{
            body: {"id": 6, "success": true}
        };
    }
}
