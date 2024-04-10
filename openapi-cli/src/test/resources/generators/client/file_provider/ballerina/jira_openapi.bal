import  ballerina/http;


public type ApplicationPropertyArr ApplicationProperty[];

public type ApplicationRoleArr ApplicationRole[];

public type TimeTrackingProviderArr TimeTrackingProvider[];

public type FieldDetailsArr FieldDetails[];

public type FilterArr Filter[];

public type ColumnItemArr ColumnItem[];

public type SharePermissionArr SharePermission[];

public type AttachmentArr Attachment[];

type IssueTypeDetailsArr IssueTypeDetails[];

type PriorityArr Priority[];

type ProjectArr Project[];

public type ProjectTypeArr ProjectType[];

public type ComponentArr Component[];

public type ProjectRoleDetailsArr ProjectRoleDetails[];

public type IssueTypeWithStatusArr IssueTypeWithStatus[];

public type VersionArr Version[];

public type ProjectCategoryArr ProjectCategory[];

public type ResolutionArr Resolution[];

public type ProjectRoleArr ProjectRole[];

public type ScreenableFieldArr ScreenableField[];

public type ScreenableTabArr ScreenableTab[];

public type StatusDetailsArr StatusDetails[];

public type StatusCategoryArr StatusCategory[];

public type UserArr User[];

public type UserMigrationBeanArr UserMigrationBean[];

public type GroupNameArr GroupName[];

public type DeprecatedWorkflowArr DeprecatedWorkflow[];

public type WorklogArr Worklog[];

public isolated client class Client {
    final http:Client clientEp;
    public isolated function init(string serviceUrl = "https://your-domain.atlassian.com", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function updateCustomFieldValue(string fieldIdOrKey, CustomFieldValueUpdateRequest payload) returns json|error {
        string resourcePath = string `/rest/api/2/app/field/${fieldIdOrKey}/value`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getApplicationProperty(string? 'key, string? permissionLevel, string? keyFilter) returns ApplicationPropertyArr|error {
        string resourcePath = string `/rest/api/2/application-properties`;
        map<anydata> queryParam = {'key: 'key, permissionLevel: permissionLevel, keyFilter: keyFilter};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAdvancedSettings() returns ApplicationPropertyArr|error {
        string resourcePath = string `/rest/api/2/application-properties/advanced-settings`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setApplicationProperty(string id, SimpleApplicationPropertyBean payload) returns ApplicationProperty|error {
        string resourcePath = string `/rest/api/2/application-properties/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getAllApplicationRoles() returns ApplicationRoleArr|error {
        string resourcePath = string `/rest/api/2/applicationrole`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getApplicationRole(string 'key) returns ApplicationRole|error {
        string resourcePath = string `/rest/api/2/applicationrole/${'key}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAttachmentMeta() returns AttachmentSettings|error {
        string resourcePath = string `/rest/api/2/attachment/meta`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAttachment(string id) returns AttachmentMetadata|error {
        string resourcePath = string `/rest/api/2/attachment/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function removeAttachment(string id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/attachment/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function expandAttachmentForHumans(string id) returns AttachmentArchiveMetadataReadable|error {
        string resourcePath = string `/rest/api/2/attachment/${id}/expand/human`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function expandAttachmentForMachines(string id) returns AttachmentArchiveImpl|error {
        string resourcePath = string `/rest/api/2/attachment/${id}/expand/raw`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAuditRecords(int? offset, int? 'limit, string? filter, string? 'from, string? to) returns AuditRecords|error {
        string resourcePath = string `/rest/api/2/auditing/record`;
        map<anydata> queryParam = {offset: offset, 'limit: 'limit, filter: filter, 'from: 'from, to: to};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllSystemAvatars(string 'type) returns SystemAvatars|error {
        string resourcePath = string `/rest/api/2/avatar/${'type}/system`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getCommentsByIds(string? expand, IssueCommentListRequestBean payload) returns PageBeanComment|error {
        string resourcePath = string `/rest/api/2/comment/list`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getCommentPropertyKeys(string commentId) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/comment/${commentId}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getCommentProperty(string commentId, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setCommentProperty(string commentId, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteCommentProperty(string commentId, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function createComponent(Component payload) returns Component|error {
        string resourcePath = string `/rest/api/2/component`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getComponent(string id) returns Component|error {
        string resourcePath = string `/rest/api/2/component/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateComponent(string id, Component payload) returns Component|error {
        string resourcePath = string `/rest/api/2/component/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteComponent(string id, string? moveIssuesTo) returns http:Response | error {
        string resourcePath = string `/rest/api/2/component/${id}`;
        map<anydata> queryParam = {moveIssuesTo: moveIssuesTo};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getComponentRelatedIssues(string id) returns ComponentIssuesCount|error {
        string resourcePath = string `/rest/api/2/component/${id}/relatedIssueCounts`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getConfiguration() returns Configuration|error {
        string resourcePath = string `/rest/api/2/configuration`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getSelectedTimeTrackingImplementation() returns TimeTrackingProvider|error {
        string resourcePath = string `/rest/api/2/configuration/timetracking`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function selectTimeTrackingImplementation(TimeTrackingProvider payload) returns json|error {
        string resourcePath = string `/rest/api/2/configuration/timetracking`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getAvailableTimeTrackingImplementations() returns TimeTrackingProviderArr|error {
        string resourcePath = string `/rest/api/2/configuration/timetracking/list`;
        return self.clientEp->get(resourcePath);
    }
    remote isolated function getSharedTimeTrackingConfiguration() returns TimeTrackingConfiguration|error {
        string resourcePath = string `/rest/api/2/configuration/timetracking/options`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setSharedTimeTrackingConfiguration(TimeTrackingConfiguration payload) returns TimeTrackingConfiguration|error {
        string resourcePath = string `/rest/api/2/configuration/timetracking/options`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getOptionsForField(int fieldId, int? startAt, int? maxResults) returns PageBeanCustomFieldOptionDetails|error {
        string resourcePath = string `/rest/api/2/customField/${fieldId}/option`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateCustomFieldOptions(int fieldId, UpdateCustomFieldOption payload) returns json|error {
        string resourcePath = string `/rest/api/2/customField/${fieldId}/option`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function createCustomFieldOptions(int fieldId, BulkCreateCustomFieldOptionRequest payload) returns json|error {
        string resourcePath = string `/rest/api/2/customField/${fieldId}/option`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getCustomFieldOption(string id) returns CustomFieldOption|error {
        string resourcePath = string `/rest/api/2/customFieldOption/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllDashboards(string? filter, int? startAt, int? maxResults) returns PageOfDashboards|error {
        string resourcePath = string `/rest/api/2/dashboard`;
        map<anydata> queryParam = {filter: filter, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createDashboard(DashboardDetails payload) returns Dashboard|error {
        string resourcePath = string `/rest/api/2/dashboard`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getDashboardsPaginated(string? dashboardName, string? accountId, string? owner, string? groupname, int? projectId, string? orderBy, int? startAt, int? maxResults, string? expand) returns PageBeanDashboard|error {
        string resourcePath = string `/rest/api/2/dashboard/search`;
        map<anydata> queryParam = {dashboardName: dashboardName, accountId: accountId, owner: owner, groupname: groupname, projectId: projectId, orderBy: orderBy, startAt: startAt, maxResults: maxResults, expand: expand};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getDashboardItemPropertyKeys(string dashboardId, string itemId) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getDashboardItemProperty(string dashboardId, string itemId, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setDashboardItemProperty(string dashboardId, string itemId, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteDashboardItemProperty(string dashboardId, string itemId, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getDashboard(string id) returns Dashboard|error {
        string resourcePath = string `/rest/api/2/dashboard/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateDashboard(string id, DashboardDetails payload) returns Dashboard|error {
        string resourcePath = string `/rest/api/2/dashboard/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteDashboard(string id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/dashboard/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function copyDashboard(string id, DashboardDetails payload) returns Dashboard|error {
        string resourcePath = string `/rest/api/2/dashboard/${id}/copy`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function analyseExpression(string? 'check, JiraExpressionForAnalysis payload) returns JiraExpressionsAnalysis|error {
        string resourcePath = string `/rest/api/2/expression/analyse`;
        map<anydata> queryParam = {'check: 'check};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function evaluateJiraExpression(string? expand, JiraExpressionEvalRequestBean payload) returns JiraExpressionResult|error {
        string resourcePath = string `/rest/api/2/expression/eval`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getFields() returns FieldDetailsArr|error {
        string resourcePath = string `/rest/api/2/field`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createCustomField(CustomFieldDefinitionJsonBean payload) returns FieldDetails|error {
        string resourcePath = string `/rest/api/2/field`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getFieldsPaginated(int? startAt, int? maxResults, string[]? 'type, string[]? id, string? query, string? orderBy, string? expand) returns PageBeanField|error {
        string resourcePath = string `/rest/api/2/field/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, 'type: 'type, id: id, query: query, orderBy: orderBy, expand: expand};
        resourcePath = resourcePath + check check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateCustomField(string fieldId, UpdateCustomFieldDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getContextsForField(string fieldId, boolean? isAnyIssueType, boolean? isGlobalContext, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContext|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context`;
        map<anydata> queryParam = {isAnyIssueType: isAnyIssueType, isGlobalContext: isGlobalContext, contextId: contextId, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createCustomFieldContext(string fieldId, CreateCustomFieldContext payload) returns CreateCustomFieldContext|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getDefaultValues(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContextDefaultValue|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/defaultValue`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setDefaultValues(string fieldId, CustomFieldContextDefaultValueUpdate payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/defaultValue`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getIssueTypeMappingsForContexts(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanIssueTypeToContextMapping|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/issuetypemapping`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getCustomFieldContextsForProjectsAndIssueTypes(string fieldId, int? startAt, int? maxResults, ProjectIssueTypeMappings payload) returns PageBeanContextForProjectAndIssueType|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getProjectContextMapping(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContextProjectMapping|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/projectmapping`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateCustomFieldContext(string fieldId, int contextId, CustomFieldContextUpdateDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteCustomFieldContext(string fieldId, int contextId) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function addIssueTypesToContext(string fieldId, int contextId, IssueTypeIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/issuetype`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removeIssueTypesFromContext(string fieldId, int contextId, IssueTypeIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/issuetype/remove`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getOptionsForContext(string fieldId, int contextId, int? optionId, boolean? onlyOptions, int? startAt, int? maxResults) returns PageBeanCustomFieldContextOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        map<anydata> queryParam = {optionId: optionId, onlyOptions: onlyOptions, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateCustomFieldOption(string fieldId, int contextId, BulkCustomFieldOptionUpdateRequest payload) returns CustomFieldUpdatedContextOptionsList|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function createCustomFieldOption(string fieldId, int contextId, BulkCustomFieldOptionCreateRequest payload) returns CustomFieldCreatedContextOptionsList|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function reorderCustomFieldOptions(string fieldId, int contextId, OrderOfCustomFieldOptions payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/option/move`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteCustomFieldOption(string fieldId, int contextId, int optionId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/option/${optionId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function assignProjectsToCustomFieldContext(string fieldId, int contextId, ProjectIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removeCustomFieldContextFromProjects(string fieldId, int contextId, ProjectIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/context/${contextId}/project/remove`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getContextsForFieldDeprecated(string fieldId, int? startAt, int? maxResults) returns PageBeanContext|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/contexts`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getScreensForField(string fieldId, int? startAt, int? maxResults, string? expand) returns PageBeanScreenWithTab|error {
        string resourcePath = string `/rest/api/2/field/${fieldId}/screens`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllIssueFieldOptions(int? startAt, int? maxResults, string fieldKey) returns PageBeanIssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueFieldOption(string fieldKey, IssueFieldOptionCreateBean payload) returns IssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getSelectableIssueFieldOptions(int? startAt, int? maxResults, int? projectId, string fieldKey) returns PageBeanIssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/suggestions/edit`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getVisibleIssueFieldOptions(int? startAt, int? maxResults, int? projectId, string fieldKey) returns PageBeanIssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/suggestions/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueFieldOption(string fieldKey, int optionId) returns IssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateIssueFieldOption(string fieldKey, int optionId, IssueFieldOption payload) returns IssueFieldOption|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueFieldOption(string fieldKey, int optionId) returns json|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function replaceIssueFieldOption(int? replaceWith, string? jql, string fieldKey, int optionId) returns TaskProgressBeanRemoveOptionFromIssuesResult|error {
        string resourcePath = string `/rest/api/2/field/${fieldKey}/option/${optionId}/issue`;
        map<anydata> queryParam = {replaceWith: replaceWith, jql: jql};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getAllFieldConfigurations(int? startAt, int? maxResults, int[]? id, boolean? isDefault, string? query) returns PageBeanFieldConfiguration|error {
        string resourcePath = string `/rest/api/2/fieldconfiguration`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id, isDefault: isDefault, query: query};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getFieldConfigurationItems(int id, int? startAt, int? maxResults) returns PageBeanFieldConfigurationItem|error {
        string resourcePath = string `/rest/api/2/fieldconfiguration/${id}/fields`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllFieldConfigurationSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanFieldConfigurationScheme|error {
        string resourcePath = string `/rest/api/2/fieldconfigurationscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getFieldConfigurationSchemeMappings(int? startAt, int? maxResults, int[]? fieldConfigurationSchemeId) returns PageBeanFieldConfigurationIssueTypeItem|error {
        string resourcePath = string `/rest/api/2/fieldconfigurationscheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, fieldConfigurationSchemeId: fieldConfigurationSchemeId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getFieldConfigurationSchemeProjectMapping(int? startAt, int? maxResults, int[] projectId) returns PageBeanFieldConfigurationSchemeProjects|error {
        string resourcePath = string `/rest/api/2/fieldconfigurationscheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function assignFieldConfigurationSchemeToProject(FieldConfigurationSchemeProjectAssociation payload) returns json|error {
        string resourcePath = string `/rest/api/2/fieldconfigurationscheme/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getFilters(string? expand) returns FilterArr|error {
        string resourcePath = string `/rest/api/2/filter`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createFilter(string? expand, Filter payload) returns Filter|error {
        string resourcePath = string `/rest/api/2/filter`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getDefaultShareScope() returns DefaultShareScope|error {
        string resourcePath = string `/rest/api/2/filter/defaultShareScope`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setDefaultShareScope(DefaultShareScope payload) returns DefaultShareScope|error {
        string resourcePath = string `/rest/api/2/filter/defaultShareScope`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getFavouriteFilters(string? expand) returns FilterArr|error {
        string resourcePath = string `/rest/api/2/filter/favourite`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getMyFilters(string? expand, boolean? includeFavourites) returns FilterArr|error {
        string resourcePath = string `/rest/api/2/filter/my`;
        map<anydata> queryParam = {expand: expand, includeFavourites: includeFavourites};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getFiltersPaginated(string? filterName, string? accountId, string? owner, string? groupname, int? projectId, int[]? id, string? orderBy, int? startAt, int? maxResults, string? expand) returns PageBeanFilterDetails|error {
        string resourcePath = string `/rest/api/2/filter/search`;
        map<anydata> queryParam = {filterName: filterName, accountId: accountId, owner: owner, groupname: groupname, projectId: projectId, id: id, orderBy: orderBy, startAt: startAt, maxResults: maxResults, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getFilter(int id, string? expand) returns Filter|error {
        string resourcePath = string `/rest/api/2/filter/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateFilter(int id, string? expand, Filter payload) returns Filter|error {
        string resourcePath = string `/rest/api/2/filter/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteFilter(int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/filter/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getColumns(int id) returns ColumnItemArr|error {
        string resourcePath = string `/rest/api/2/filter/${id}/columns`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setColumns(int id, [] payload) returns json|error {
        string resourcePath = string `/rest/api/2/filter/${id}/columns`;
        http:Request request = new;
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function resetColumns(int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/filter/${id}/columns`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function setFavouriteForFilter(int id, string? expand) returns Filter|error {
        string resourcePath = string `/rest/api/2/filter/${id}/favourite`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> put(resourcePath, request);
    }
    remote isolated function deleteFavouriteForFilter(int id, string? expand) returns Filter|error {
        string resourcePath = string `/rest/api/2/filter/${id}/favourite`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getSharePermissions(int id) returns SharePermissionArr|error {
        string resourcePath = string `/rest/api/2/filter/${id}/permission`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addSharePermission(int id, SharePermissionInputBean payload) returns SharePermissionArr|error {
        string resourcePath = string `/rest/api/2/filter/${id}/permission`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getSharePermission(int id, int permissionId) returns SharePermission|error {
        string resourcePath = string `/rest/api/2/filter/${id}/permission/${permissionId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deleteSharePermission(int id, int permissionId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/filter/${id}/permission/${permissionId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getGroup(string groupname, string? expand) returns Group|error {
        string resourcePath = string `/rest/api/2/group`;
        map<anydata> queryParam = {groupname: groupname, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createGroup(AddGroupBean payload) returns Group|error {
        string resourcePath = string `/rest/api/2/group`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function removeGroup(string groupname, string? swapGroup) returns http:Response | error {
        string resourcePath = string `/rest/api/2/group`;
        map<anydata> queryParam = {groupname: groupname, swapGroup: swapGroup};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function bulkGetGroups(int? startAt, int? maxResults, string[]? groupId, string[]? groupName) returns PageBeanGroupDetails|error {
        string resourcePath = string `/rest/api/2/group/bulk`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, groupId: groupId, groupName: groupName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUsersFromGroup(string groupname, boolean? includeInactiveUsers, int? startAt, int? maxResults) returns PageBeanUserDetails|error {
        string resourcePath = string `/rest/api/2/group/member`;
        map<anydata> queryParam = {groupname: groupname, includeInactiveUsers: includeInactiveUsers, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addUserToGroup(string groupname, UpdateUserToGroupBean payload) returns Group|error {
        string resourcePath = string `/rest/api/2/group/user`;
        map<anydata> queryParam = {groupname: groupname};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function removeUserFromGroup(string groupname, string? username, string accountId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/group/user`;
        map<anydata> queryParam = {groupname: groupname, username: username, accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function findGroups(string? accountId, string? query, string[]? exclude, int? maxResults, string? userName) returns FoundGroups|error {
        string resourcePath = string `/rest/api/2/groups/picker`;
        map<anydata> queryParam = {accountId: accountId, query: query, exclude: exclude, maxResults: maxResults, userName: userName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUsersAndGroups(string query, int? maxResults, boolean? showAvatar, string? fieldId, string[]? projectId, string[]? issueTypeId, string? avatarSize, boolean? caseInsensitive, boolean? excludeConnectAddons) returns FoundUsersAndGroups|error {
        string resourcePath = string `/rest/api/2/groupuserpicker`;
        map<anydata> queryParam = {query: query, maxResults: maxResults, showAvatar: showAvatar, fieldId: fieldId, projectId: projectId, issueTypeId: issueTypeId, avatarSize: avatarSize, caseInsensitive: caseInsensitive, excludeConnectAddons: excludeConnectAddons};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getLicense() returns License|error {
        string resourcePath = string `/rest/api/2/instance/license`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssue(boolean? updateHistory, IssueUpdateDetails payload) returns CreatedIssue|error {
        string resourcePath = string `/rest/api/2/issue`;
        map<anydata> queryParam = {updateHistory: updateHistory};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function createIssues(IssuesUpdateBean payload) returns CreatedIssues|error {
        string resourcePath = string `/rest/api/2/issue/bulk`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getCreateIssueMeta(string[]? projectIds, string[]? projectKeys, string[]? issuetypeIds, string[]? issuetypeNames, string? expand) returns IssueCreateMetadata|error {
        string resourcePath = string `/rest/api/2/issue/createmeta`;
        map<anydata> queryParam = {projectIds: projectIds, projectKeys: projectKeys, issuetypeIds: issuetypeIds, issuetypeNames: issuetypeNames, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssuePickerResource(string? query, string? currentJQL, string? currentIssueKey, string? currentProjectId, boolean? showSubTasks, boolean? showSubTaskParent) returns IssuePickerSuggestions|error {
        string resourcePath = string `/rest/api/2/issue/picker`;
        map<anydata> queryParam = {query: query, currentJQL: currentJQL, currentIssueKey: currentIssueKey, currentProjectId: currentProjectId, showSubTasks: showSubTasks, showSubTaskParent: showSubTaskParent};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function bulkSetIssuesProperties(IssueEntityProperties payload) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/properties`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function bulkSetIssueProperty(string propertyKey, BulkIssuePropertyUpdateRequest payload) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function bulkDeleteIssueProperty(string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIssue(string issueIdOrKey, string[]? fields, boolean? fieldsByKeys, string? expand, string[]? properties, boolean? updateHistory) returns IssueBean|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {fields: fields, fieldsByKeys: fieldsByKeys, expand: expand, properties: properties, updateHistory: updateHistory};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function editIssue(string issueIdOrKey, boolean? notifyUsers, boolean? overrideScreenSecurity, boolean? overrideEditableFlag, IssueUpdateDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, overrideScreenSecurity: overrideScreenSecurity, overrideEditableFlag: overrideEditableFlag};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssue(string issueIdOrKey, string? deleteSubtasks) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {deleteSubtasks: deleteSubtasks};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function assignIssue(string issueIdOrKey, User payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/assignee`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function addAttachment(string issueIdOrKey, string payload) returns AttachmentArr|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/attachments`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getChangeLogs(string issueIdOrKey, int? startAt, int? maxResults) returns PageBeanChangelog|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/changelog`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getComments(string issueIdOrKey, int? startAt, int? maxResults, string? orderBy, string? expand) returns PageOfComments|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/comment`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addComment(string issueIdOrKey, string? expand, Comment payload) returns Comment|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/comment`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getComment(string issueIdOrKey, string id, string? expand) returns Comment|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateComment(string issueIdOrKey, string id, string? expand, Comment payload) returns Comment|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteComment(string issueIdOrKey, string id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getEditIssueMeta(string issueIdOrKey, boolean? overrideScreenSecurity, boolean? overrideEditableFlag) returns IssueUpdateMetadata|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/editmeta`;
        map<anydata> queryParam = {overrideScreenSecurity: overrideScreenSecurity, overrideEditableFlag: overrideEditableFlag};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function notify(string issueIdOrKey, Notification payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/notify`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssuePropertyKeys(string issueIdOrKey) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueProperty(string issueIdOrKey, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setIssueProperty(string issueIdOrKey, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueProperty(string issueIdOrKey, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getRemoteIssueLinks(string issueIdOrKey, string? globalId) returns RemoteIssueLink|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        map<anydata> queryParam = {globalId: globalId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createOrUpdateRemoteIssueLink(string issueIdOrKey, RemoteIssueLinkRequest payload) returns RemoteIssueLinkIdentifies|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteRemoteIssueLinkByGlobalId(string issueIdOrKey, string globalId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        map<anydata> queryParam = {globalId: globalId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getRemoteIssueLinkById(string issueIdOrKey, string linkId) returns RemoteIssueLink|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateRemoteIssueLink(string issueIdOrKey, string linkId, RemoteIssueLinkRequest payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteRemoteIssueLinkById(string issueIdOrKey, string linkId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getTransitions(string issueIdOrKey, string? expand, string? transitionId, boolean? skipRemoteOnlyCondition, boolean? includeUnavailableTransitions, boolean? sortByOpsBarAndStatus) returns Transitions|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/transitions`;
        map<anydata> queryParam = {expand: expand, transitionId: transitionId, skipRemoteOnlyCondition: skipRemoteOnlyCondition, includeUnavailableTransitions: includeUnavailableTransitions, sortByOpsBarAndStatus: sortByOpsBarAndStatus};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function doTransition(string issueIdOrKey, IssueUpdateDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/transitions`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getVotes(string issueIdOrKey) returns Votes|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addVote(string issueIdOrKey) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function removeVote(string issueIdOrKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIssueWatchers(string issueIdOrKey) returns Watchers|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addWatcher(string issueIdOrKey, string payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function removeWatcher(string issueIdOrKey, string? username, string? accountId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        map<anydata> queryParam = {username: username, accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIssueWorklog(string issueIdOrKey, int? startAt, int? maxResults, int? startedAfter, string? expand) returns PageOfWorklogs|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, startedAfter: startedAfter, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addWorklog(string issueIdOrKey, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? reduceBy, string? expand, boolean? overrideEditableFlag, Worklog payload) returns Worklog|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, reduceBy: reduceBy, expand: expand, overrideEditableFlag: overrideEditableFlag};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getWorklog(string issueIdOrKey, string id, string? expand) returns Worklog|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorklog(string issueIdOrKey, string id, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? expand, boolean? overrideEditableFlag, Worklog payload) returns Worklog|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, expand: expand, overrideEditableFlag: overrideEditableFlag};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorklog(string issueIdOrKey, string id, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? increaseBy, boolean? overrideEditableFlag) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, increaseBy: increaseBy, overrideEditableFlag: overrideEditableFlag};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getWorklogPropertyKeys(string issueIdOrKey, string worklogId) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function linkIssues(LinkIssueRequestJsonBean payload) returns json|error {
        string resourcePath = string `/rest/api/2/issueLink`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueLink(string linkId) returns IssueLink|error {
        string resourcePath = string `/rest/api/2/issueLink/${linkId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deleteIssueLink(string linkId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issueLink/${linkId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIssueLinkTypes() returns IssueLinkTypes|error {
        string resourcePath = string `/rest/api/2/issueLinkType`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueLinkType(IssueLinkType payload) returns IssueLinkType|error {
        string resourcePath = string `/rest/api/2/issueLinkType`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueLinkType(string issueLinkTypeId) returns IssueLinkType|error {
        string resourcePath = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateIssueLinkType(string issueLinkTypeId, IssueLinkType payload) returns IssueLinkType|error {
        string resourcePath = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueLinkType(string issueLinkTypeId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIssueSecuritySchemes() returns SecuritySchemes|error {
        string resourcePath = string `/rest/api/2/issuesecurityschemes`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueSecurityScheme(int id) returns SecurityScheme|error {
        string resourcePath = string `/rest/api/2/issuesecurityschemes/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueSecurityLevelMembers(int issueSecuritySchemeId, int? startAt, int? maxResults, int[]? issueSecurityLevelId, string? expand) returns PageBeanIssueSecurityLevelMember|error {
        string resourcePath = string `/rest/api/2/issuesecurityschemes/${issueSecuritySchemeId}/members`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueSecurityLevelId: issueSecurityLevelId, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueAllTypes() returns IssueTypeDetailsArr|error {
        string resourcePath = string `/rest/api/2/issuetype`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueType(IssueTypeCreateBean payload) returns IssueTypeDetails|error {
        string resourcePath = string `/rest/api/2/issuetype`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueType(string id) returns IssueTypeDetails|error {
        string resourcePath = string `/rest/api/2/issuetype/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateIssueType(string id, IssueTypeUpdateBean payload) returns IssueTypeDetails|error {
        string resourcePath = string `/rest/api/2/issuetype/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueType(string id, string? alternativeIssueTypeId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issuetype/${id}`;
        map<anydata> queryParam = {alternativeIssueTypeId: alternativeIssueTypeId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getAlternativeIssueTypes(string id) returns IssueTypeDetailsArr|error {
        string resourcePath = string `/rest/api/2/issuetype/${id}/alternatives`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueTypeAvatar(string id, int? x, int? y, int size, json payload) returns Avatar|error {
        string resourcePath = string `/rest/api/2/issuetype/${id}/avatar2`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueTypePropertyKeys(string issueTypeId) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/issuetype/${issueTypeId}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueTypeProperty(string issueTypeId, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setIssueTypeProperty(string issueTypeId, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueTypeProperty(string issueTypeId, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getAllIssueTypeSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanIssueTypeScheme|error {
        string resourcePath = string `/rest/api/2/issuetypescheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueTypeScheme(IssueTypeSchemeDetails payload) returns IssueTypeSchemeID|error {
        string resourcePath = string `/rest/api/2/issuetypescheme`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueTypeSchemesMapping(int? startAt, int? maxResults, int[]? issueTypeSchemeId) returns PageBeanIssueTypeSchemeMapping|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueTypeSchemeId: issueTypeSchemeId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueTypeSchemeForProjects(int? startAt, int? maxResults, int[] projectId) returns PageBeanIssueTypeSchemeProjects|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function assignIssueTypeSchemeToProject(IssueTypeSchemeProjectAssociation payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function updateIssueTypeScheme(int issueTypeSchemeId, IssueTypeSchemeUpdateDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueTypeScheme(int issueTypeSchemeId) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function addIssueTypesToIssueTypeScheme(int issueTypeSchemeId, IssueTypeIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function reorderIssueTypesInIssueTypeScheme(int issueTypeSchemeId, OrderOfIssueTypes payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype/move`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removeIssueTypeFromIssueTypeScheme(int issueTypeSchemeId, int issueTypeId) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype/${issueTypeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getIssueTypeScreenSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanIssueTypeScreenScheme|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createIssueTypeScreenScheme(IssueTypeScreenSchemeDetails payload) returns IssueTypeScreenSchemeId|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueTypeScreenSchemeMappings(int? startAt, int? maxResults, int[]? issueTypeScreenSchemeId) returns PageBeanIssueTypeScreenSchemeItem|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueTypeScreenSchemeId: issueTypeScreenSchemeId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueTypeScreenSchemeProjectAssociations(int? startAt, int? maxResults, int[] projectId) returns PageBeanIssueTypeScreenSchemesProjects|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function assignIssueTypeScreenSchemeToProject(IssueTypeScreenSchemeProjectAssociation payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function updateIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeScreenSchemeUpdateDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteIssueTypeScreenScheme(string issueTypeScreenSchemeId) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function appendMappingsForIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeScreenSchemeMappingDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function updateDefaultScreenScheme(string issueTypeScreenSchemeId, UpdateDefaultScreenScheme payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping/default`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removeMappingsFromIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeIds payload) returns json|error {
        string resourcePath = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping/remove`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getAutoComplete() returns JQLReferenceData|error {
        string resourcePath = string `/rest/api/2/jql/autocompletedata`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAutoCompletePost(SearchAutoCompleteFilter payload) returns JQLReferenceData|error {
        string resourcePath = string `/rest/api/2/jql/autocompletedata`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getFieldAutoCompleteForQueryString(string? fieldName, string? fieldValue, string? predicateName, string? predicateValue) returns AutoCompleteSuggestions|error {
        string resourcePath = string `/rest/api/2/jql/autocompletedata/suggestions`;
        map<anydata> queryParam = {fieldName: fieldName, fieldValue: fieldValue, predicateName: predicateName, predicateValue: predicateValue};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function matchIssues(IssuesAndJQLQueries payload) returns IssueMatches|error {
        string resourcePath = string `/rest/api/2/jql/match`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function parseJqlQueries(string? validation, JqlQueriesToParse payload) returns ParsedJqlQueries|error {
        string resourcePath = string `/rest/api/2/jql/parse`;
        map<anydata> queryParam = {validation: validation};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function migrateQueries(JQLPersonalDataMigrationRequest payload) returns ConvertedJQLQueries|error {
        string resourcePath = string `/rest/api/2/jql/pdcleaner`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getAllLabels(int? startAt, int? maxResults) returns PageBeanString|error {
        string resourcePath = string `/rest/api/2/label`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getMyPermissions(string? projectKey, string? projectId, string? issueKey, string? issueId, string? permissions, string? projectUuid, string? projectConfigurationUuid) returns Permissions|error {
        string resourcePath = string `/rest/api/2/mypermissions`;
        map<anydata> queryParam = {projectKey: projectKey, projectId: projectId, issueKey: issueKey, issueId: issueId, permissions: permissions, projectUuid: projectUuid, projectConfigurationUuid: projectConfigurationUuid};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getPreference(string 'key) returns string|error {
        string resourcePath = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setPreference(string 'key, string payload) returns json|error {
        string resourcePath = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removePreference(string 'key) returns http:Response | error {
        string resourcePath = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getLocale() returns Locale|error {
        string resourcePath = string `/rest/api/2/mypreferences/locale`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setLocale(Locale payload) returns json|error {
        string resourcePath = string `/rest/api/2/mypreferences/locale`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteLocale() returns json|error {
        string resourcePath = string `/rest/api/2/mypreferences/locale`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getCurrentUser(string? expand) returns User|error {
        string resourcePath = string `/rest/api/2/myself`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getNotificationSchemes(int? startAt, int? maxResults, string? expand) returns PageBeanNotificationScheme|error {
        string resourcePath = string `/rest/api/2/notificationscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getNotificationScheme(int id, string? expand) returns NotificationScheme|error {
        string resourcePath = string `/rest/api/2/notificationscheme/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllPermissions() returns Permissions|error {
        string resourcePath = string `/rest/api/2/permissions`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getBulkPermissions(BulkPermissionsRequestBean payload) returns BulkPermissionGrants|error {
        string resourcePath = string `/rest/api/2/permissions/check`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getPermittedProjects(PermissionsKeysBean payload) returns PermittedProjects|error {
        string resourcePath = string `/rest/api/2/permissions/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getAllPermissionSchemes(string? expand) returns PermissionSchemes|error {
        string resourcePath = string `/rest/api/2/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createPermissionScheme(string? expand, PermissionScheme payload) returns PermissionScheme|error {
        string resourcePath = string `/rest/api/2/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getPermissionScheme(int schemeId, string? expand) returns PermissionScheme|error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updatePermissionScheme(int schemeId, string? expand, PermissionScheme payload) returns PermissionScheme|error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deletePermissionScheme(int schemeId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getPermissionSchemeGrants(int schemeId, string? expand) returns PermissionGrants|error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}/permission`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createPermissionGrant(int schemeId, string? expand, PermissionGrant payload) returns PermissionGrant|error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}/permission`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getPermissionSchemeGrant(int schemeId, int permissionId, string? expand) returns PermissionGrant|error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}/permission/${permissionId}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deletePermissionSchemeEntity(int schemeId, int permissionId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/permissionscheme/${schemeId}/permission/${permissionId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getPriorities() returns PriorityArr|error {
        string resourcePath = string `/rest/api/2/priority`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getPriority(string id) returns Priority|error {
        string resourcePath = string `/rest/api/2/priority/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllProjects(string? expand, int? recent, string[]? properties) returns ProjectArr|error {
        string resourcePath = string `/rest/api/2/project`;
        map<anydata> queryParam = {expand: expand, recent: recent, properties: properties};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createProject(ProjectInputBean payload) returns ProjectIdentifiers|error {
        string resourcePath = string `/rest/api/2/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function searchProjects(int? startAt, int? maxResults, string? orderBy, string? query, string? typeKey, int? categoryId, string? action, string? expand, string[]? status, StringList[]? properties, string? propertyQuery) returns PageBeanProject|error {
        string resourcePath = string `/rest/api/2/project/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query, typeKey: typeKey, categoryId: categoryId, action: action, expand: expand, status: status, properties: properties, propertyQuery: propertyQuery};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllProjectTypes() returns ProjectTypeArr|error {
        string resourcePath = string `/rest/api/2/project/type`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllAccessibleProjectTypes() returns ProjectTypeArr|error {
        string resourcePath = string `/rest/api/2/project/type/accessible`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectTypeByKey(string projectTypeKey) returns ProjectType|error {
        string resourcePath = string `/rest/api/2/project/type/${projectTypeKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAccessibleProjectTypeByKey(string projectTypeKey) returns ProjectType|error {
        string resourcePath = string `/rest/api/2/project/type/${projectTypeKey}/accessible`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProject(string projectIdOrKey, string? expand, string[]? properties) returns Project|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {expand: expand, properties: properties};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateProject(string projectIdOrKey, string? expand, ProjectInputBean payload) returns Project|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteProject(string projectIdOrKey, boolean? enableUndo) returns http:Response | error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {enableUndo: enableUndo};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function archiveProject(string projectIdOrKey) returns json|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/archive`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function updateProjectAvatar(string projectIdOrKey, Avatar payload) returns json|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/avatar`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteProjectAvatar(string projectIdOrKey, int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/avatar/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function createProjectAvatar(string projectIdOrKey, int? x, int? y, int? size, json payload) returns Avatar|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/avatar2`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getAllProjectAvatars(string projectIdOrKey) returns ProjectAvatars|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/avatars`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectComponentsPaginated(string projectIdOrKey, int? startAt, int? maxResults, string? orderBy, string? query) returns PageBeanComponentWithIssueCount|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/component`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectComponents(string projectIdOrKey) returns ComponentArr|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/components`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deleteProjectAsynchronously(string projectIdOrKey) returns TaskProgressBeanObject|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/delete`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function getFeaturesForProject(string projectIdOrKey) returns ProjectFeaturesResponse|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/features`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function toggleFeatureForProject(string projectIdOrKey, string featureKey, ProjectFeatureToggleRequest payload) returns ProjectFeaturesResponse|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/features/${featureKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getProjectPropertyKeys(string projectIdOrKey) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectProperty(string projectIdOrKey, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setProjectProperty(string projectIdOrKey, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteProjectProperty(string projectIdOrKey, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function restore(string projectIdOrKey) returns Project|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/restore`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function getProjectRoles(string projectIdOrKey) returns record|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/role`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectRole(string projectIdOrKey, int id) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setActors(string projectIdOrKey, int id, ProjectRoleActorsUpdateBean payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function addActorUsers(string projectIdOrKey, int id, ActorsMap payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteActor(string projectIdOrKey, int id, string? user, string? 'group) returns http:Response | error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        map<anydata> queryParam = {user: user, 'group: 'group};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getProjectRoleDetails(string projectIdOrKey, boolean? currentMember, boolean? excludeConnectAddons) returns ProjectRoleDetailsArr|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/roledetails`;
        map<anydata> queryParam = {currentMember: currentMember, excludeConnectAddons: excludeConnectAddons};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllStatuses(string projectIdOrKey) returns IssueTypeWithStatusArr|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/statuses`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateProjectType(string projectIdOrKey, string newProjectTypeKey) returns Project|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/type/${newProjectTypeKey}`;
        http:Request request = new;
        return self.clientEp-> put(resourcePath, request);
    }
    remote isolated function getProjectVersionsPaginated(string projectIdOrKey, int? startAt, int? maxResults, string? orderBy, string? query, string? status, string? expand) returns PageBeanVersion|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/version`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query, status: status, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectVersions(string projectIdOrKey, string? expand) returns VersionArr|error {
        string resourcePath = string `/rest/api/2/project/${projectIdOrKey}/versions`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectEmail(int projectId) returns ProjectEmailAddress|error {
        string resourcePath = string `/rest/api/2/project/${projectId}/email`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateProjectEmail(int projectId, ProjectEmailAddress payload) returns json|error {
        string resourcePath = string `/rest/api/2/project/${projectId}/email`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getHierarchy(int projectId) returns ProjectIssueTypeHierarchy|error {
        string resourcePath = string `/rest/api/2/project/${projectId}/hierarchy`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getProjectIssueSecurityScheme(string projectKeyOrId) returns SecurityScheme|error {
        string resourcePath = string `/rest/api/2/project/${projectKeyOrId}/issuesecuritylevelscheme`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getNotificationSchemeForProject(string projectKeyOrId, string? expand) returns NotificationScheme|error {
        string resourcePath = string `/rest/api/2/project/${projectKeyOrId}/notificationscheme`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAssignedPermissionScheme(string projectKeyOrId, string? expand) returns PermissionScheme|error {
        string resourcePath = string `/rest/api/2/project/${projectKeyOrId}/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function assignPermissionScheme(string projectKeyOrId, string? expand, IdBean payload) returns PermissionScheme|error {
        string resourcePath = string `/rest/api/2/project/${projectKeyOrId}/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getSecurityLevelsForProject(string projectKeyOrId) returns ProjectIssueSecurityLevels|error {
        string resourcePath = string `/rest/api/2/project/${projectKeyOrId}/securitylevel`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllProjectCategories() returns ProjectCategoryArr|error {
        string resourcePath = string `/rest/api/2/projectCategory`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createProjectCategory(ProjectCategory payload) returns ProjectCategory|error {
        string resourcePath = string `/rest/api/2/projectCategory`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getProjectCategoryById(int id) returns ProjectCategory|error {
        string resourcePath = string `/rest/api/2/projectCategory/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateProjectCategory(int id, ProjectCategory payload) returns UpdatedProjectCategory|error {
        string resourcePath = string `/rest/api/2/projectCategory/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function removeProjectCategory(int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/projectCategory/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function validateProjectKey(string? 'key) returns ErrorCollection|error {
        string resourcePath = string `/rest/api/2/projectvalidate/key`;
        map<anydata> queryParam = {'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getValidProjectKey(string? 'key) returns string|error {
        string resourcePath = string `/rest/api/2/projectvalidate/validProjectKey`;
        map<anydata> queryParam = {'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getValidProjectName(string name) returns string|error {
        string resourcePath = string `/rest/api/2/projectvalidate/validProjectName`;
        map<anydata> queryParam = {name: name};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getResolutions() returns ResolutionArr|error {
        string resourcePath = string `/rest/api/2/resolution`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getResolution(string id) returns Resolution|error {
        string resourcePath = string `/rest/api/2/resolution/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllProjectRoles() returns ProjectRoleArr|error {
        string resourcePath = string `/rest/api/2/role`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createProjectRole(CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getProjectRoleById(int id) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function fullyUpdateProjectRole(int id, CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function partialUpdateProjectRole(int id, CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteProjectRole(int id, int? swap) returns http:Response | error {
        string resourcePath = string `/rest/api/2/role/${id}`;
        map<anydata> queryParam = {swap: swap};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getProjectRoleActorsForRole(int id) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}/actors`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addProjectRoleActorsToRole(int id, ActorInputBean payload) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}/actors`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteProjectRoleActorsFromRole(int id, string? user, string? 'group) returns ProjectRole|error {
        string resourcePath = string `/rest/api/2/role/${id}/actors`;
        map<anydata> queryParam = {user: user, 'group: 'group};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getScreens(int? startAt, int? maxResults, int[]? id) returns PageBeanScreen|error {
        string resourcePath = string `/rest/api/2/screens`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createScreen(ScreenDetails payload) returns Screen|error {
        string resourcePath = string `/rest/api/2/screens`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function addFieldToDefaultScreen(string fieldId) returns json|error {
        string resourcePath = string `/rest/api/2/screens/addToDefault/${fieldId}`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function updateScreen(int screenId, UpdateScreenDetails payload) returns Screen|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteScreen(int screenId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/screens/${screenId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getAvailableScreenFields(int screenId) returns ScreenableFieldArr|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/availableFields`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllScreenTabs(int screenId, string? projectKey) returns ScreenableTabArr|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs`;
        map<anydata> queryParam = {projectKey: projectKey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addScreenTab(int screenId, ScreenableTab payload) returns ScreenableTab|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function renameScreenTab(int screenId, int tabId, ScreenableTab payload) returns ScreenableTab|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteScreenTab(int screenId, int tabId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getAllScreenTabFields(int screenId, int tabId, string? projectKey) returns ScreenableFieldArr|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields`;
        map<anydata> queryParam = {projectKey: projectKey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function addScreenTabField(int screenId, int tabId, AddFieldBean payload) returns ScreenableField|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function removeScreenTabField(int screenId, int tabId, string id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function moveScreenTabField(int screenId, int tabId, string id, MoveFieldBean payload) returns json|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields/${id}/move`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function moveScreenTab(int screenId, int tabId, int pos) returns json|error {
        string resourcePath = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/move/${pos}`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function getScreenSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanScreenScheme|error {
        string resourcePath = string `/rest/api/2/screenscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createScreenScheme(ScreenSchemeDetails payload) returns ScreenSchemeId|error {
        string resourcePath = string `/rest/api/2/screenscheme`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function updateScreenScheme(string screenSchemeId, UpdateScreenSchemeDetails payload) returns json|error {
        string resourcePath = string `/rest/api/2/screenscheme/${screenSchemeId}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteScreenScheme(string screenSchemeId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/screenscheme/${screenSchemeId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function searchForIssuesUsingJql(string? jql, int? startAt, int? maxResults, string? validateQuery, string[]? fields, string? expand, string[]? properties, boolean? fieldsByKeys) returns SearchResults|error {
        string resourcePath = string `/rest/api/2/search`;
        map<anydata> queryParam = {jql: jql, startAt: startAt, maxResults: maxResults, validateQuery: validateQuery, fields: fields, expand: expand, properties: properties, fieldsByKeys: fieldsByKeys};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function searchForIssuesUsingJqlPost(SearchRequestBean payload) returns SearchResults|error {
        string resourcePath = string `/rest/api/2/search`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIssueSecurityLevel(string id) returns SecurityLevel|error {
        string resourcePath = string `/rest/api/2/securitylevel/${id}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getServerInfo() returns ServerInformation|error {
        string resourcePath = string `/rest/api/2/serverInfo`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getIssueNavigatorDefaultColumns() returns ColumnItemArr|error {
        string resourcePath = string `/rest/api/2/settings/columns`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setIssueNavigatorDefaultColumns([] payload) returns json|error {
        string resourcePath = string `/rest/api/2/settings/columns`;
        http:Request request = new;
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getStatuses() returns StatusDetailsArr|error {
        string resourcePath = string `/rest/api/2/status`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getStatus(string idOrName) returns StatusDetails|error {
        string resourcePath = string `/rest/api/2/status/${idOrName}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getStatusCategories() returns StatusCategoryArr|error {
        string resourcePath = string `/rest/api/2/statuscategory`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getStatusCategory(string idOrKey) returns StatusCategory|error {
        string resourcePath = string `/rest/api/2/statuscategory/${idOrKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getTask(string taskId) returns TaskProgressBeanObject|error {
        string resourcePath = string `/rest/api/2/task/${taskId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function cancelTask(string taskId) returns json|error {
        string resourcePath = string `/rest/api/2/task/${taskId}/cancel`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function getAvatars(string 'type, string entityId) returns Avatars|error {
        string resourcePath = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${entityId}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function storeAvatar(string 'type, string entityId, int? x, int? y, int size, json payload) returns Avatar|error {
        string resourcePath = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${entityId}`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteAvatar(string 'type, string owningObjectId, int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${owningObjectId}/avatar/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getUser(string? accountId, string? username, string? 'key, string? expand) returns User|error {
        string resourcePath = string `/rest/api/2/user`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createUser(UserWriteBean payload) returns User|error {
        string resourcePath = string `/rest/api/2/user`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function removeUser(string accountId, string? username, string? 'key) returns http:Response | error {
        string resourcePath = string `/rest/api/2/user`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function findBulkAssignableUsers(string? query, string? username, string? accountId, string projectKeys, int? startAt, int? maxResults) returns UserArr|error {
        string resourcePath = string `/rest/api/2/user/assignable/multiProjectSearch`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, projectKeys: projectKeys, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findAssignableUsers(string? query, string? sessionId, string? username, string? accountId, string? project, string? issueKey, int? startAt, int? maxResults, int? actionDescriptorId, boolean? recommend) returns UserArr|error {
        string resourcePath = string `/rest/api/2/user/assignable/search`;
        map<anydata> queryParam = {query: query, sessionId: sessionId, username: username, accountId: accountId, project: project, issueKey: issueKey, startAt: startAt, maxResults: maxResults, actionDescriptorId: actionDescriptorId, recommend: recommend};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function bulkGetUsers(int? startAt, int? maxResults, string[]? username, string[]? 'key, string[] accountId) returns PageBeanUser|error {
        string resourcePath = string `/rest/api/2/user/bulk`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, username: username, 'key: 'key, accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function bulkGetUsersMigration(int? startAt, int? maxResults, string[]? username, string[]? 'key) returns UserMigrationBeanArr|error {
        string resourcePath = string `/rest/api/2/user/bulk/migration`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, username: username, 'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUserDefaultColumns(string? accountId, string? username) returns ColumnItemArr|error {
        string resourcePath = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setUserColumns(string? accountId, [] payload) returns json|error {
        string resourcePath = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function resetUserColumns(string? accountId, string? username) returns http:Response | error {
        string resourcePath = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getUserEmail(string accountId) returns UnrestrictedUserEmail|error {
        string resourcePath = string `/rest/api/2/user/email`;
        map<anydata> queryParam = {accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUserEmailBulk(string[] accountId) returns UnrestrictedUserEmail|error {
        string resourcePath = string `/rest/api/2/user/email/bulk`;
        map<anydata> queryParam = {accountId: accountId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUserGroups(string accountId, string? username, string? 'key) returns GroupNameArr|error {
        string resourcePath = string `/rest/api/2/user/groups`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUsersWithAllPermissions(string? query, string? username, string? accountId, string permissions, string? issueKey, string? projectKey, int? startAt, int? maxResults) returns UserArr|error {
        string resourcePath = string `/rest/api/2/user/permission/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, permissions: permissions, issueKey: issueKey, projectKey: projectKey, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUsersForPicker(string query, int? maxResults, boolean? showAvatar, string[]? exclude, string[]? excludeAccountIds, string? avatarSize, boolean? excludeConnectUsers) returns FoundUsers|error {
        string resourcePath = string `/rest/api/2/user/picker`;
        map<anydata> queryParam = {query: query, maxResults: maxResults, showAvatar: showAvatar, exclude: exclude, excludeAccountIds: excludeAccountIds, avatarSize: avatarSize, excludeConnectUsers: excludeConnectUsers};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUserPropertyKeys(string? accountId, string? userKey, string? username) returns PropertyKeys|error {
        string resourcePath = string `/rest/api/2/user/properties`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getUserProperty(string? accountId, string? userKey, string? username, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setUserProperty(string? accountId, string? userKey, string? username, string propertyKey, json payload) returns json|error {
        string resourcePath = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteUserProperty(string? accountId, string? userKey, string? username, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function findUsers(string? query, string? username, string? accountId, int? startAt, int? maxResults, string? property) returns UserArr|error {
        string resourcePath = string `/rest/api/2/user/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, startAt: startAt, maxResults: maxResults, property: property};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUsersByQuery(string query, int? startAt, int? maxResults) returns PageBeanUser|error {
        string resourcePath = string `/rest/api/2/user/search/query`;
        map<anydata> queryParam = {query: query, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUserKeysByQuery(string query, int? startAt, int? maxResults) returns PageBeanUserKey|error {
        string resourcePath = string `/rest/api/2/user/search/query/key`;
        map<anydata> queryParam = {query: query, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function findUsersWithBrowsePermission(string? query, string? username, string? accountId, string? issueKey, string? projectKey, int? startAt, int? maxResults) returns UserArr|error {
        string resourcePath = string `/rest/api/2/user/viewissue/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, issueKey: issueKey, projectKey: projectKey, startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllUsersDefault(int? startAt, int? maxResults) returns UserArr|error {
        string resourcePath = string `/rest/api/2/users`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getAllUsers(int? startAt, int? maxResults) returns UserArr|error {
        string resourcePath = string `/rest/api/2/users/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createVersion(Version payload) returns Version|error {
        string resourcePath = string `/rest/api/2/version`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getVersion(string id, string? expand) returns Version|error {
        string resourcePath = string `/rest/api/2/version/${id}`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateVersion(string id, Version payload) returns Version|error {
        string resourcePath = string `/rest/api/2/version/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteVersion(string id, string? moveFixIssuesTo, string? moveAffectedIssuesTo) returns http:Response | error {
        string resourcePath = string `/rest/api/2/version/${id}`;
        map<anydata> queryParam = {moveFixIssuesTo: moveFixIssuesTo, moveAffectedIssuesTo: moveAffectedIssuesTo};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function mergeVersions(string id, string moveIssuesTo) returns json|error {
        string resourcePath = string `/rest/api/2/version/${id}/mergeto/${moveIssuesTo}`;
        http:Request request = new;
        return self.clientEp-> put(resourcePath, request);
    }
    remote isolated function moveVersion(string id, VersionMoveBean payload) returns Version|error {
        string resourcePath = string `/rest/api/2/version/${id}/move`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getVersionRelatedIssues(string id) returns VersionIssueCounts|error {
        string resourcePath = string `/rest/api/2/version/${id}/relatedIssueCounts`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deleteAndReplaceVersion(string id, DeleteAndReplaceVersionBean payload) returns json|error {
        string resourcePath = string `/rest/api/2/version/${id}/removeAndSwap`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getVersionUnresolvedIssues(string id) returns VersionUnresolvedIssuesCount|error {
        string resourcePath = string `/rest/api/2/version/${id}/unresolvedIssueCount`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getDynamicWebhooksForApp(int? startAt, int? maxResults) returns PageBeanWebhook|error {
        string resourcePath = string `/rest/api/2/webhook`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function registerDynamicWebhooks(WebhookRegistrationDetails payload) returns ContainerForRegisteredWebhooks|error {
        string resourcePath = string `/rest/api/2/webhook`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteWebhookById() returns http:Response | error {
        string resourcePath = string `/rest/api/2/webhook`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getFailedWebhooks(int? maxResults, int? after) returns FailedWebhooks|error {
        string resourcePath = string `/rest/api/2/webhook/failed`;
        map<anydata> queryParam = {maxResults: maxResults, after: after};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function refreshWebhooks(ContainerForWebhookIDs payload) returns WebhooksExpirationDate|error {
        string resourcePath = string `/rest/api/2/webhook/refresh`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getAllWorkflows(string? workflowName) returns DeprecatedWorkflowArr|error {
        string resourcePath = string `/rest/api/2/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createWorkflow(CreateWorkflowDetails payload) returns WorkflowIDs|error {
        string resourcePath = string `/rest/api/2/workflow`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getWorkflowTransitionRuleConfigurations(int? startAt, int? maxResults, string[] types, string[]? keys, string? expand) returns PageBeanWorkflowTransitionRules|error {
        string resourcePath = string `/rest/api/2/workflow/rule/config`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, types: types, keys: keys, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorkflowTransitionRuleConfigurations(WorkflowTransitionRulesUpdate payload) returns WorkflowTransitionRulesUpdateErrors|error {
        string resourcePath = string `/rest/api/2/workflow/rule/config`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getWorkflowsPaginated(int? startAt, int? maxResults, string[]? workflowName, string? expand) returns PageBeanWorkflow|error {
        string resourcePath = string `/rest/api/2/workflow/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, workflowName: workflowName, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getWorkflowTransitionProperties(int transitionId, boolean? includeReservedKeys, string? 'key, string workflowName, string? workflowMode) returns WorkflowTransitionProperty|error {
        string resourcePath = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {includeReservedKeys: includeReservedKeys, 'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode, WorkflowTransitionProperty payload) returns WorkflowTransitionProperty|error {
        string resourcePath = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function createWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode, WorkflowTransitionProperty payload) returns WorkflowTransitionProperty|error {
        string resourcePath = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function deleteWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function deleteInactiveWorkflow(string entityId) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflow/${entityId}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getAllWorkflowSchemes(int? startAt, int? maxResults) returns PageBeanWorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createWorkflowScheme(WorkflowScheme payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getWorkflowSchemeProjectAssociations(int[] projectId) returns ContainerOfWorkflowSchemeAssociations|error {
        string resourcePath = string `/rest/api/2/workflowscheme/project`;
        map<anydata> queryParam = {projectId: projectId};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function assignSchemeToProject(WorkflowSchemeProjectAssociation payload) returns json|error {
        string resourcePath = string `/rest/api/2/workflowscheme/project`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function getWorkflowScheme(int id, boolean? returnDraftIfExists) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorkflowScheme(int id, WorkflowScheme payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorkflowScheme(int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function createWorkflowSchemeDraftFromParent(int id) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/createdraft`;
        http:Request request = new;
        return self.clientEp-> post(resourcePath, request);
    }
    remote isolated function getDefaultWorkflow(int id, boolean? returnDraftIfExists) returns DefaultWorkflow|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/default`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateDefaultWorkflow(int id, DefaultWorkflow payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/default`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteDefaultWorkflow(int id, boolean? updateDraftIfNeeded) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/default`;
        map<anydata> queryParam = {updateDraftIfNeeded: updateDraftIfNeeded};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getWorkflowSchemeDraft(int id) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorkflowSchemeDraft(int id, WorkflowScheme payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorkflowSchemeDraft(int id) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getDraftDefaultWorkflow(int id) returns DefaultWorkflow|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateDraftDefaultWorkflow(int id, DefaultWorkflow payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteDraftDefaultWorkflow(int id) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getWorkflowSchemeDraftIssueType(int id, string issueType) returns IssueTypeWorkflowMapping|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setWorkflowSchemeDraftIssueType(int id, string issueType, IssueTypeWorkflowMapping payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorkflowSchemeDraftIssueType(int id, string issueType) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getDraftWorkflow(int id, string? workflowName) returns IssueTypesWorkflowMapping|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateDraftWorkflowMapping(int id, string workflowName, IssueTypesWorkflowMapping payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteDraftWorkflowMapping(int id, string workflowName) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getWorkflowSchemeIssueType(int id, string issueType, boolean? returnDraftIfExists) returns IssueTypeWorkflowMapping|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function setWorkflowSchemeIssueType(int id, string issueType, IssueTypeWorkflowMapping payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorkflowSchemeIssueType(int id, string issueType, boolean? updateDraftIfNeeded) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        map<anydata> queryParam = {updateDraftIfNeeded: updateDraftIfNeeded};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function getWorkflow(int id, string? workflowName, boolean? returnDraftIfExists) returns IssueTypesWorkflowMapping|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName, returnDraftIfExists: returnDraftIfExists};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function updateWorkflowMapping(int id, string workflowName, IssueTypesWorkflowMapping payload) returns WorkflowScheme|error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function deleteWorkflowMapping(int id, string workflowName, boolean? updateDraftIfNeeded) returns http:Response | error {
        string resourcePath = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName, updateDraftIfNeeded: updateDraftIfNeeded};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function getIdsOfWorklogsDeletedSince(int? since) returns ChangedWorklogs|error {
        string resourcePath = string `/rest/api/2/worklog/deleted`;
        map<anydata> queryParam = {since: since};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function getWorklogsForIds(string? expand, WorklogIdsRequestBean payload) returns WorklogArr|error {
        string resourcePath = string `/rest/api/2/worklog/list`;
        map<anydata> queryParam = {expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getIdsOfWorklogsModifiedSince(int? since, string? expand) returns ChangedWorklogs|error {
        string resourcePath = string `/rest/api/2/worklog/updated`;
        map<anydata> queryParam = {since: since, expand: expand};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function 'AddonPropertiesResource\.getAddonProperties\_get(string addonKey) returns PropertyKeys|error {
        string resourcePath = string `/rest/atlassian-connect/1/addons/${addonKey}/properties`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function 'AddonPropertiesResource\.getAddonProperty\_get(string addonKey, string propertyKey) returns EntityProperty|error {
        string resourcePath = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function 'AddonPropertiesResource\.putAddonProperty\_put(string addonKey, string propertyKey, json payload) returns OperationMessage|error {
        string resourcePath = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    remote isolated function 'AddonPropertiesResource\.deleteAddonProperty\_delete(string addonKey, string propertyKey) returns http:Response | error {
        string resourcePath = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
    remote isolated function 'DynamicModulesResource\.getModules\_get() returns ConnectModules|error {
        string resourcePath = string `/rest/atlassian-connect/1/app/module/dynamic`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function 'DynamicModulesResource\.registerModules\_post(ConnectModules payload) returns http:Response | error {
        string resourcePath = string `/rest/atlassian-connect/1/app/module/dynamic`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function 'DynamicModulesResource\.removeModules\_delete(string[]? moduleKey) returns http:Response | error {
        string resourcePath = string `/rest/atlassian-connect/1/app/module/dynamic`;
        map<anydata> queryParam = {moduleKey: moduleKey};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request );
    }
}

isolated function getPathForQueryParam(map<anydata>   queryParam)  returns  string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach  var [key, value] in  queryParam.entries() {
        if  value  is  () {
            _ = queryParam.remove(key);
        } else {
            if  string:startsWith( key, "'") {
                 param[param.length()] = string:substring(key, 1, key.length());
            } else {
                param[param.length()] = key;
            }
            param[param.length()] = "=";
            if  value  is  string {
                string updateV =  check url:encode(value, "UTF-8");
                param[param.length()] = updateV;
            } else {
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length()-1);
    if  param.length() ==  1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}
