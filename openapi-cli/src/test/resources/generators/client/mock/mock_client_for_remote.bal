
public isolated client class Client {
    # Gets invoked to initialize the `connector`.
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

    # Execute payments
    #
    # + headers - Headers to be sent with the request
    remote isolated function UpdateExecutePayments(payments_execute_payments_body payload, map<string|string[]> headers = {}) returns inline_response_200_4|error {
        return {"success": true, "message": "Payments with the following IDs enqueued for processing: [100, 101, 110, 111, 121]"};
    }

    # Submit a payment to retry cycle
    #
    # + headers - Headers to be sent with the request
    remote isolated function UpdateSubmitPaymentToCycle(payments_submit_failed_payment_body payload, map<string|string[]> headers = {}) returns inline_response_200_8|error {
        return {"success": true, "message": "Payment entered into retry process"};
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

    # Execute account payments
    #
    # + account_id - ID of an account.
    # + headers - Headers to be sent with the request
    remote isolated function executeExecuteAccountPayments(string account_id, map<string|string[]> headers = {}) returns inline_response_200_3|error {
        return {"success": true, "message": "Payments with the following IDs enqueued for processing: [310, 311, 312]"};
    }

    # Execute debit memo payment
    #
    # + debit_memo_id - ID of a debit memo.
    # + headers - Headers to be sent with the request
    remote isolated function executeExecuteDebitMemoPayment(string debit_memo_id, map<string|string[]> headers = {}) returns inline_response_200_2|error {
        return {"success": true, "message": "Payments with the following IDs enqueued for processing: [300]"};
    }

    # Execute invoice payment
    #
    # + invoice_id - ID of an invoice.
    # + headers - Headers to be sent with the request
    remote isolated function executeExecuteInvoicePayment(string invoice_id, map<string|string[]> headers = {}) returns inline_response_200_1|error {
        return {"success": true, "message": "Payments with the following IDs enqueued for processing: [290, 291]"};
    }

    # Get an account
    #
    # + account_id - The account ID.
    # + headers - Headers to be sent with the request
    remote isolated function getAccount(string account_id, map<string|string[]> headers = {}) returns CollectionAccount|error {
        return {"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": true, "Pending": false, "In Dispute": false, "Paid In Full": false}};
    }

    # Get retry cycle history for an account
    #
    # + account_id - ID of an account.
    # + headers - Headers to be sent with the request
    remote isolated function getAccountCycleHistory(string account_id, map<string|string[]> headers = {}) returns GETAccountCycleHistoryResponse|error {
        return {"cycles": [{"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 3, "next_attempt": "2021-03-19T18:53:39.641Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:43:28.670-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bcdfc010671", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fb78532b0f01785a38cede190a", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-03-24T19:34:20.254Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51aae41969", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.316-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
    }

    # Get all accounts
    #
    # + headers - Headers to be sent with the request
    remote isolated function getAccounts(map<string|string[]> headers = {}) returns GETCollectionAccountsResponse|error {
        return {"accounts": [{"name": "Testing Account", "zuora_id": "2c92c0f863f2b1680163f579b7e705da", "in_collections": true, "collections_agent": "2018-01-02T00:00:00.000Z", "account_currency": "CAD", "home_currency": "USD", "amount_due_account_currency": 15540, "amount_due_home_currency": 800.55, "last_open_invoice_date": "2018-06-12", "average_debt_age": "194.4 days", "statuses": {"In Collections": 0, "Pending": 0, "In Dispute": 0, "Paid In Full": 0}}], "pagination": {"page": 1, "page_length": 20, "next_page": "https://collections-window.apps.zuora.com/api/v1/accounts?page=2&page_length=20"}};
    }

    # Get active retry cycles for an account
    #
    # + account_id - ID of an account.
    # + headers - Headers to be sent with the request
    remote isolated function getActiveAccountCycle(string account_id, map<string|string[]> headers = {}) returns GETActiveAccountCycleResponse|error {
        return {"cycles": [{"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "debit_memo_id": "2c92c0fb78532b0001785a38f6427976", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-04-01T19:27:34.473Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a5199a6192d", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:27.521-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "invoice_id": "2c92c0fa7853052701785a38c6622473", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 1, "next_attempt": "2021-04-01T19:27:34.436Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a519d85193b", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:28.161-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
    }

    # Get active retry cycles for a debit memo
    #
    # + debit_memo_id - ID of a debit memo.
    # + headers - Headers to be sent with the request
    remote isolated function getActiveDebitMemoCycle(string debit_memo_id, map<string|string[]> headers = {}) returns GETActiveDebitMemoCycleResponse|error {
        return {"cycles": [{"account_id": "2c92c0f868e161e70168e25eb51d755f", "debit_memo_id": "2c92c0fa7853052701785a38f3bb267f", "payment_method_id": "2c92c0f8774f1afe01775f6e533c4c06", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 2, "next_attempt": "2021-04-01T10:22:57.464Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51af791979", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.380-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c0857881cf3e01788ee263b0331c", "time_of_execution": "2021-04-01T19:21:55.207Z", "source": "PR-00000380", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-04-01T10:22:57.464-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
    }

    # Get active retry cycles for an invoice
    #
    # + invoice_id - ID of an invoice.
    # + headers - Headers to be sent with the request
    remote isolated function getActiveInvoiceCycle(string invoice_id, map<string|string[]> headers = {}) returns GETActiveInvoiceCycleResponse|error {
        return {"cycles": [{"account_id": "2c92c0f96bd69165016bdccdd6ce2f29", "invoice_id": "2c92c0f8778bf8cd017798168cb50e0b", "payment_method_id": "2c92c0f9774f2b3e01775f6f06d87b61", "currency": "USD", "status": "Cycle Incomplete", "current_attempt_number": 2, "next_attempt": "2021-04-01T19:27:34.648Z", "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ca076c21", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-04-01T19:27:34.648-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bce0d0a06d5", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:53:39.845-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
    }

    # Get the Amount Recovered metrics
    #
    # + headers - Headers to be sent with the request
    remote isolated function getAmountRecovered(map<string|string[]> headers = {}) returns GETAmountRecoveredResponse|error {
        return {"currency": {"USD": {"total_amount": "77515.21", "last_30_days": "1100.01"}, "EUR": {"total_amount": "337.19", "last_30_days": "17.17"}, "CAD": {"total_amount": "123954.10", "last_30_days": "5132.87"}}, "success": true};
    }

    # Get baseline metrics
    #
    # + headers - Headers to be sent with the request
    remote isolated function getBaselineMetrics(map<string|string[]> headers = {}) returns GETBaselineMetricsResponse|error {
        return {"retry_success_rate": "11.90", "retry_success_rate_trend": "down", "document_success_rate": "13.54", "document_success_rate_trend": "neutral", "average_days_outstanding": "4.76", "average_days_outstanding_trend": "up", "success": true};
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

    # Get the Customer Group metrics
    #
    # + headers - Headers to be sent with the request
    remote isolated function getCustomerGroupMetrics(map<string|string[]> headers = {}) returns GETCustomerGroupMetricsResponse|error {
        return {"customer_groups": [{"id": 1, "name": "batch22", "smart_retry": false, "document_success_rate": "17.17", "document_success_rate_trend": "up", "retry_success_rate": "21.76", "average_attempts": "4.11"}, {"id": 2, "name": "Smart Retry", "smart_retry": true, "document_success_rate": "74.17", "document_success_rate_trend": "down", "retry_success_rate": "81.21", "average_attempts": "1.32"}, {"id": 4, "name": "All Remaining Customers", "smart_retry": false, "document_success_rate": "16.35", "document_success_rate_trend": "up", "retry_success_rate": "15.21", "average_attempts": "3.32"}], "success": true};
    }

    # Get retry cycle history for a debit memo
    #
    # + debit_memo_id - ID of a debit memo.
    # + headers - Headers to be sent with the request
    remote isolated function getDebitMemoCycleHistory(string debit_memo_id, map<string|string[]> headers = {}) returns GETDebitMemoCycleHistoryResponse|error {
        return {"cycles": [{"account_id": "2c92c0f868e161e70168e25eb51d755f", "debit_memo_id": "2c92c0fa7853052701785a38f3bb267f", "payment_method_id": "2c92c0f8774f1afe01775f6e533c4c06", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 2, "next_attempt": null, "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c085785305e201785a51af791979", "time_of_execution": "2021-03-23T16:50:18.878Z", "source": "PR-00000376", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-23T07:51:30.380-09:00", "criteria": "incremental_time", "api_updated_retry_time": "2021-03-26T14:27:21.107-04:00"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c0857881cf3e01788ee263b0331c", "time_of_execution": "2021-04-01T19:21:55.207Z", "source": "PR-00000378", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
    }

    # Get the Document Success Rate metrics by customer group
    #
    # + headers - Headers to be sent with the request
    remote isolated function getDocumentSuccessRateByCustomerGroup(map<string|string[]> headers = {}) returns GETDocumentSuccessRateByCustomerGroupResponse|error {
        return {"customer_groups": {"1": {"05_21": "17.53", "04_21": "13.21", "03_21": "14.92", "02_21": "8.99", "01_21": "34.25", "12_20": "12.30"}, "2": {"05_21": "11.11", "04_21": "7.87", "03_21": "26.00", "02_21": "11.06", "01_21": "13.43", "12_20": "17.92"}, "4": {"05_21": "11.13", "04_21": "9.17", "03_21": "17.20", "02_21": "19.06", "01_21": "12.43", "12_20": "15.92"}}, "success": true};
    }

    # Get retry cycle history for an invoice
    #
    # + invoice_id - ID of an invoice.
    # + headers - Headers to be sent with the request
    remote isolated function getInvoiceCycleHistory(string invoice_id, map<string|string[]> headers = {}) returns GETInvoiceCycleHistoryResponse|error {
        return {"cycles": [{"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 1, "next_attempt": null, "customer_group": "Testing Group", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-04-01T19:11:21.639Z", "source": "PR-00000370", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 5}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}, {"account_id": "2c92c0f96bd69165016bdcbf55ad5e62", "invoice_id": "2c92c0fa7849b3ff01784bc5e8ee18b5", "payment_method_id": "2c92c0f9774f2b3e01775f6cf2fb726a", "currency": "USD", "status": "Cycle Complete", "current_attempt_number": 2, "next_attempt": null, "customer_group": "All Remaining Customers", "attempts": [{"attempt_number": 1, "zuora_payment_id": "2c92c0867849d42301784bc9ce806c31", "time_of_execution": "2021-03-19T18:42:20.103Z", "source": "PR-00000371", "cpr_generated": false, "success": false, "amount_collected": "0.0", "action_info": {"action": "Retry"}, "retry_info": {"next": "2021-03-19T09:43:28.670-09:00", "criteria": "incremental_time"}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}, {"attempt_number": 2, "zuora_payment_id": "2c92c09c7849d3c101784bcdfc010671", "time_of_execution": "2021-03-19T18:52:24.137Z", "source": "PR-00000372", "cpr_generated": true, "success": false, "amount_collected": "0.0", "action_info": {"action": "Stop"}, "retry_info": {}, "mapping_info": {"label": "Hard Decline", "level": "code", "customer_group_id": 1}, "gateway_info": {"id": "2c92c0f85e2d19af015e3a61d8947e5d", "code": "insufficient_funds", "response": "Your card has insufficient funds."}}]}]};
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

    # Remove an account from retry cycle
    #
    # + account_id - ID of an account.
    # + headers - Headers to be sent with the request
    remote isolated function removeAccountFromCycle(string account_id, map<string|string[]> headers = {}) returns inline_response_200_7|error {
        return {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [310, 311, 312]"};
    }

    # Remove a debit memo from retry cycle
    #
    # + debit_memo_id - ID of a debit memo.
    # + headers - Headers to be sent with the request
    remote isolated function removeDebitMemoFromCycle(string debit_memo_id, map<string|string[]> headers = {}) returns inline_response_200_6|error {
        return {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [301]"};
    }

    # Remove an invoice from retry cycle
    #
    # + invoice_id - ID of an invoice.
    # + headers - Headers to be sent with the request
    remote isolated function removeRemoveInoviceFromCycle(string invoice_id, map<string|string[]> headers = {}) returns inline_response_200_5|error {
        return {"success": true, "message": "Payments with the following IDs have been removed from the retry cycle: [290, 291]"};
    }

    # Update a payment run schedule
    #
    # + schedule_id - The schedule ID
    # + headers - Headers to be sent with the request
    remote isolated function updatePaymentRunSchedule(int schedule_id, PUTPaymentRunSchedule payload, map<string|string[]> headers = {}) returns POSTPaymentRunScheduleResponse|error {
        return {"id": 6, "success": true};
    }
}
