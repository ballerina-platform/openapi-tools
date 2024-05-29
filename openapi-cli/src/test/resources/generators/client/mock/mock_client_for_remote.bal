import ballerina/http;

@display {label: "Zuora Collection", iconPath: "icon.png"}
public isolated client class Client {
    final http:Client clientEp;
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

    # Update account status
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    remote isolated function UpdateAccount(string account_id, map<string|string[]> headers = {}, *UpdateAccountQueries queries) returns inline_response_200|error {
        return {"success": true};
    }

    # Update account agent
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    remote isolated function UpdateAccountAgent(string account_id, map<string|string[]> headers = {}, *UpdateAccountAgentQueries queries) returns inline_response_200|error {
        return {"success": true};
    }

    # Create an ad hoc statement run
    #
    # + headers - Headers to be sent with the request
    remote isolated function createAdhocStatementRun(POSTAdhocStatementRun payload, map<string|string[]> headers = {}) returns POSTAdhocStatementRunResponse|error {
        return {"success": true, "code": 200, "message": "Success", "statement_number": "STA00000008"};
    }

    # Create a custom statement run
    #
    # + headers - Headers to be sent with the request
    remote isolated function createCustomStatementRun(POSTCustomStatementRun payload, map<string|string[]> headers = {}) returns POSTCustomStatementRunResponse|error {
        return {"success": true, "code": 200, "message": "Success", "execution_number": "2", "report_file": "<aws_s3_link>"};
    }

    # Create a payment run
    #
    # + headers - Headers to be sent with the request
    remote isolated function createPaymentRun(POSTPaymentRun payload, map<string|string[]> headers = {}) returns POSTPaymentRunResponse|error {
        return {"id": 6, "success": "true"};
    }

    # Create a payment run schedule
    #
    # + headers - Headers to be sent with the request
    remote isolated function createPaymentRunSchedule(POSTPaymentRunSchedule payload, map<string|string[]> headers = {}) returns POSTPaymentRunScheduleResponse|error {
        return {"id": 6, "success": "true"};
    }

    # Cancel a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    remote isolated function deletePaymentRunSchedule(int schedule_id, map<string|string[]> headers = {}) returns DELETEPaymentRunScheduleResponse|error {
        return {"success": true};
    }

    # Get an account
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    remote isolated function getAccount(string account_id, map<string|string[]> headers = {}) returns CollectionAccount|error {
        return {"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": true, "Pending": false, "In Dispute": false, "Paid In Full": false}};
    }

    # Get all accounts
    #
    # + headers - Headers to be sent with the request
    remote isolated function getAccounts(map<string|string[]> headers = {}) returns GETCollectionAccountsResponse|error {
        return {"accounts": [{"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": 0, "Pending": 0, "In Dispute": 0, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/accounts?page=2&page_length=20"}};
    }

    # Get a collections agent by email
    #
    # + email - The email of the collections agent.
    # + headers - Headers to be sent with the request
    remote isolated function getCollectionsAgent(string email, map<string|string[]> headers = {}) returns CollectionAgent|error {
        return {"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}};
    }

    # Get all collections agents
    #
    # + headers - Headers to be sent with the request
    remote isolated function getCollectionsAgents(map<string|string[]> headers = {}) returns GETCollectionAgentsResponse|error {
        return {"accounts": [{"name": "Testing User", "email": "test@zuora.com", "zuora_identity_id": "2c92c0f96178a7a901619b10f5d12345", "amount_in_collections": 800.55, "accounts": 2, "account_statuses": {"In Collections": 1, "Pending": 0, "In Dispute": 1, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/users?page=2&page_length=20"}};
    }

    # Get an overview of collections
    #
    # + headers - Headers to be sent with the request
    remote isolated function getCollectionsInfo(map<string|string[]> headers = {}) returns CollectionsInfo|error {
        return {"accounts_in_collections": 24, "home_currency": "USD", "total_debt": "8379.78", "largest_debts": ["Test Account - 12438.00 USD", "Jimmy John - 8000.00 USD", "James Smith - 2450.55 USD", "Bob Roberts - 1000.00 USD", "Jim Reynolds - 829.00 USD"], "oldest_debts": ["Test Account - 2662 days", "Jimbo - 1494 days", "Steve Smith - 942 days", "Jason Williams - 678 days", "Will Jasons - 365 days"], "statuses": {"In Collections": 24, "Pending": 2, "In Dispute": 5, "Paid in Full": 0}};
    }

    # Get a payment run
    #
    # + payment_run_id - The payment run ID. A payment run id in Advanced Payment Manager is different from a payment run id in Zuora.
    # + headers - Headers to be sent with the request
    remote isolated function getPaymentRun(int payment_run_id, map<string|string[]> headers = {}) returns GETPaymentRunResponse|error {
        return {"success": true, "id": 6, "status": "Complete", "target_date": "2018-01-02T00:00:00.000Z", "filter": "Account.Currency = 'CAD'", "payment_run_schedule_id": "Adhoc", "invoices_held": {}, "metrics": {"documents": 0, "payments": 0, "failed": 0, "skipped": 0, "amount": 0, "credit": 0}};
    }

    # Get a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    remote isolated function getPaymentRunSchedule(int schedule_id, map<string|string[]> headers = {}) returns GETPaymentRunScheduleResponse|error {
        return {"success": true, "id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"};
    }

    # Get all payment run schedules
    #
    # + headers - Headers to be sent with the request
    remote isolated function getPaymentRunSchedules(map<string|string[]> headers = {}) returns GETPaymentRunSchedulesResponse|error {
        return {"success": true, "size": 3, "schedules": [{"id": 6, "status": "Active", "filter": "Account.BillCycleDay = 8", "schedule": "At 6:00 AM, only on Monday and Tuesday"}]};
    }

    # Get Statement Generator settings
    #
    # + headers - Headers to be sent with the request
    remote isolated function getSettings(map<string|string[]> headers = {}) returns GETStatementSettingsResponse|error {
        return {"success": true, "templates": [{"name": "Default Template"}, {"name": "Template for end consumers"}], "default_template": "Default Template", "default_cycle": "Month"};
    }

    # Update a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    remote isolated function updatePaymentRunSchedule(int schedule_id, PUTPaymentRunSchedule payload, map<string|string[]> headers = {}) returns POSTPaymentRunScheduleResponse|error {
        return {"id": 6, "success": true};
    }
}
