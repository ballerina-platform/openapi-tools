# [WIP]Proposal: Bring Operation ID Support to Ballerina Client Resource Function Generation

_Owners_: @lnash94  
_Reviewers_: @daneshk    
_Created_: 2025/12/18
_Updated_: - 
_Issue_: -

## Summary

This proposal introduces Operation ID support to the BI and MI connector space by promoting the OpenAPI operationId as first-class metadata in Ballerina resource functions. This enhancement improves low-code usability in BI and enables reliable conversion from Ballerina connectors to MI connectors.

## Motivation

Currently, most Ballerina connectors are generated with resource functions derived from protocol-level definitions (OpenAPI). While technically correct, these resource names are often not human-friendly and negatively impact:

- Low-code user experience in BI
- Connector operation discoverability and usability
- Ballerina → MI connector conversion

OpenAPI already provides operationId as a unique, descriptive identifier, but it is not leveraged effectively across the connector lifecycle.

### Problem Statement

1\. Resource function names in Ballerina connectors are:
  - Protocol-driven
  - Not descriptive for low-code users

Example resource function:
```ballerina
resource isolated function post api/'3/issue(IssueUpdateDetails payload, map<string|string[]> headers = {}, *CreateIssueQueries queries) returns CreatedIssue|error {
        ...
        return self.clientEp->post(resourcePath, request, headers);
    }
```

2\. In BI:
- Operations appear unclear and not intuitive
- Users struggle to understand connector capabilities

3\. In MI connector conversion:
- MI connectors require explicit, human-readable operation names
- Mapping Ballerina resource functions to MI operations is challenging

There is no structured metadata to reliably represent an operation’s intent.

## Goals

- Enable clear and reliable operation identification in BI and MI by embedding OpenAPI operation metadata into generated Ballerina resource functions.

## Description

In [an earlier implementation](https://github.com/ballerina-platform/openapi-tools/pull/1731#:~:text=As%20the%20first%20phase%2C%20we%20only%20support%20the%20below%20attribute%20metadata%20in%20suggested%20annotation), **[resource-level annotations](https://github.com/ballerina-platform/openapi-tools/blob/39737116ea05470dc6637af4b3f1a3b6cff021d7/module-ballerina-openapi/annotation.bal#L74)** were introduced to capture OpenAPI metadata (such as operationId, summary, and description) within Ballerina resource methods. 
This capability was originally designed to support the Ballerina code → OpenAPI specification generation flow, allowing OpenAPI metadata to be preserved when exporting a specification from Ballerina services.

```ballerina

//usage
...
@openapi:ResourceInfo {
        operationId: "createIssue"
    }
    resource isolated function post api/'3/issue(IssueUpdateDetails payload, map<string|string[]> headers = {}, *CreateIssueQueries queries) returns CreatedIssue|error {
        ...
        return self.clientEp->post(resourcePath, request, headers);
    }
}
...
```
However, this support is not available when generating Ballerina client code from an OpenAPI specification (OAS → Ballerina).

### Solution

With this proposal, the same resource-level annotation mechanism should be enabled and extended for the client code generation flow.
