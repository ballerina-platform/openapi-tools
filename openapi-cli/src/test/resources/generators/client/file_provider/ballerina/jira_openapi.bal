import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

type ApplicationPropertyArr ApplicationProperty[];

type ApplicationRoleArr ApplicationRole[];

type TimeTrackingProviderArr TimeTrackingProvider[];

type FieldDetailsArr FieldDetails[];

type FilterArr Filter[];

type ColumnItemArr ColumnItem[];

type SharePermissionArr SharePermission[];

type AttachmentArr Attachment[];

type IssueTypeDetailsArr IssueTypeDetails[];

type PriorityArr Priority[];

type ProjectArr Project[];

type ProjectTypeArr ProjectType[];

type ComponentArr Component[];

type ProjectRoleDetailsArr ProjectRoleDetails[];

type IssueTypeWithStatusArr IssueTypeWithStatus[];

type VersionArr Version[];

type ProjectCategoryArr ProjectCategory[];

type ResolutionArr Resolution[];

type ProjectRoleArr ProjectRole[];

type ScreenableFieldArr ScreenableField[];

type ScreenableTabArr ScreenableTab[];

type StatusDetailsArr StatusDetails[];

type StatusCategoryArr StatusCategory[];

type UserArr User[];

type UserMigrationBeanArr UserMigrationBean[];

type GroupNameArr GroupName[];

type DeprecatedWorkflowArr DeprecatedWorkflow[];

type WorklogArr Worklog[];

public client class Client {
    http:Client clientEp;
    public isolated function init(string serviceUrl = "https://your-domain.atlassian.com", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function updateCustomFieldValue(string fieldIdOrKey, CustomFieldValueUpdateRequest payload) returns json|error {
        string  path = string `/rest/api/2/app/field/${fieldIdOrKey}/value`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getApplicationProperty(string? 'key, string? permissionLevel, string? keyFilter) returns ApplicationPropertyArr|error {
        string  path = string `/rest/api/2/application-properties`;
        map<anydata> queryParam = {'key: 'key, permissionLevel: permissionLevel, keyFilter: keyFilter};
        path = path + getPathForQueryParam(queryParam);
        ApplicationPropertyArr response = check self.clientEp-> get(path, targetType = ApplicationPropertyArr);
        return response;
    }
    remote isolated function getAdvancedSettings() returns ApplicationPropertyArr|error {
        string  path = string `/rest/api/2/application-properties/advanced-settings`;
        ApplicationPropertyArr response = check self.clientEp-> get(path, targetType = ApplicationPropertyArr);
        return response;
    }
    remote isolated function setApplicationProperty(string id, SimpleApplicationPropertyBean payload) returns ApplicationProperty|error {
        string  path = string `/rest/api/2/application-properties/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ApplicationProperty response = check self.clientEp->put(path, request, targetType=ApplicationProperty);
        return response;
    }
    remote isolated function getAllApplicationRoles() returns ApplicationRoleArr|error {
        string  path = string `/rest/api/2/applicationrole`;
        ApplicationRoleArr response = check self.clientEp-> get(path, targetType = ApplicationRoleArr);
        return response;
    }
    remote isolated function getApplicationRole(string 'key) returns ApplicationRole|error {
        string  path = string `/rest/api/2/applicationrole/${'key}`;
        ApplicationRole response = check self.clientEp-> get(path, targetType = ApplicationRole);
        return response;
    }
    remote isolated function getAttachmentMeta() returns AttachmentSettings|error {
        string  path = string `/rest/api/2/attachment/meta`;
        AttachmentSettings response = check self.clientEp-> get(path, targetType = AttachmentSettings);
        return response;
    }
    remote isolated function getAttachment(string id) returns AttachmentMetadata|error {
        string  path = string `/rest/api/2/attachment/${id}`;
        AttachmentMetadata response = check self.clientEp-> get(path, targetType = AttachmentMetadata);
        return response;
    }
    remote isolated function removeAttachment(string id) returns http:Response | error {
        string  path = string `/rest/api/2/attachment/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function expandAttachmentForHumans(string id) returns AttachmentArchiveMetadataReadable|error {
        string  path = string `/rest/api/2/attachment/${id}/expand/human`;
        AttachmentArchiveMetadataReadable response = check self.clientEp-> get(path, targetType = AttachmentArchiveMetadataReadable);
        return response;
    }
    remote isolated function expandAttachmentForMachines(string id) returns AttachmentArchiveImpl|error {
        string  path = string `/rest/api/2/attachment/${id}/expand/raw`;
        AttachmentArchiveImpl response = check self.clientEp-> get(path, targetType = AttachmentArchiveImpl);
        return response;
    }
    remote isolated function getAuditRecords(int? offset, int? 'limit, string? filter, string? 'from, string? to) returns AuditRecords|error {
        string  path = string `/rest/api/2/auditing/record`;
        map<anydata> queryParam = {offset: offset, 'limit: 'limit, filter: filter, 'from: 'from, to: to};
        path = path + getPathForQueryParam(queryParam);
        AuditRecords response = check self.clientEp-> get(path, targetType = AuditRecords);
        return response;
    }
    remote isolated function getAllSystemAvatars(string 'type) returns SystemAvatars|error {
        string  path = string `/rest/api/2/avatar/${'type}/system`;
        SystemAvatars response = check self.clientEp-> get(path, targetType = SystemAvatars);
        return response;
    }
    remote isolated function getCommentsByIds(string? expand, IssueCommentListRequestBean payload) returns PageBeanComment|error {
        string  path = string `/rest/api/2/comment/list`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PageBeanComment response = check self.clientEp->post(path, request, targetType=PageBeanComment);
        return response;
    }
    remote isolated function getCommentPropertyKeys(string commentId) returns PropertyKeys|error {
        string  path = string `/rest/api/2/comment/${commentId}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getCommentProperty(string commentId, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setCommentProperty(string commentId, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteCommentProperty(string commentId, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/comment/${commentId}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function createComponent(Component payload) returns Component|error {
        string  path = string `/rest/api/2/component`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Component response = check self.clientEp->post(path, request, targetType=Component);
        return response;
    }
    remote isolated function getComponent(string id) returns Component|error {
        string  path = string `/rest/api/2/component/${id}`;
        Component response = check self.clientEp-> get(path, targetType = Component);
        return response;
    }
    remote isolated function updateComponent(string id, Component payload) returns Component|error {
        string  path = string `/rest/api/2/component/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Component response = check self.clientEp->put(path, request, targetType=Component);
        return response;
    }
    remote isolated function deleteComponent(string id, string? moveIssuesTo) returns http:Response | error {
        string  path = string `/rest/api/2/component/${id}`;
        map<anydata> queryParam = {moveIssuesTo: moveIssuesTo};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getComponentRelatedIssues(string id) returns ComponentIssuesCount|error {
        string  path = string `/rest/api/2/component/${id}/relatedIssueCounts`;
        ComponentIssuesCount response = check self.clientEp-> get(path, targetType = ComponentIssuesCount);
        return response;
    }
    remote isolated function getConfiguration() returns Configuration|error {
        string  path = string `/rest/api/2/configuration`;
        Configuration response = check self.clientEp-> get(path, targetType = Configuration);
        return response;
    }
    remote isolated function getSelectedTimeTrackingImplementation() returns TimeTrackingProvider|error {
        string  path = string `/rest/api/2/configuration/timetracking`;
        TimeTrackingProvider response = check self.clientEp-> get(path, targetType = TimeTrackingProvider);
        return response;
    }
    remote isolated function selectTimeTrackingImplementation(TimeTrackingProvider payload) returns json|error {
        string  path = string `/rest/api/2/configuration/timetracking`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getAvailableTimeTrackingImplementations() returns TimeTrackingProviderArr|error {
        string  path = string `/rest/api/2/configuration/timetracking/list`;
        TimeTrackingProviderArr response = check self.clientEp-> get(path, targetType = TimeTrackingProviderArr);
        return response;
    }
    remote isolated function getSharedTimeTrackingConfiguration() returns TimeTrackingConfiguration|error {
        string  path = string `/rest/api/2/configuration/timetracking/options`;
        TimeTrackingConfiguration response = check self.clientEp-> get(path, targetType = TimeTrackingConfiguration);
        return response;
    }
    remote isolated function setSharedTimeTrackingConfiguration(TimeTrackingConfiguration payload) returns TimeTrackingConfiguration|error {
        string  path = string `/rest/api/2/configuration/timetracking/options`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        TimeTrackingConfiguration response = check self.clientEp->put(path, request, targetType=TimeTrackingConfiguration);
        return response;
    }
    remote isolated function getOptionsForField(int fieldId, int? startAt, int? maxResults) returns PageBeanCustomFieldOptionDetails|error {
        string  path = string `/rest/api/2/customField/${fieldId}/option`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanCustomFieldOptionDetails response = check self.clientEp-> get(path, targetType = PageBeanCustomFieldOptionDetails);
        return response;
    }
    remote isolated function updateCustomFieldOptions(int fieldId, UpdateCustomFieldOption payload) returns json|error {
        string  path = string `/rest/api/2/customField/${fieldId}/option`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function createCustomFieldOptions(int fieldId, BulkCreateCustomFieldOptionRequest payload) returns json|error {
        string  path = string `/rest/api/2/customField/${fieldId}/option`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getCustomFieldOption(string id) returns CustomFieldOption|error {
        string  path = string `/rest/api/2/customFieldOption/${id}`;
        CustomFieldOption response = check self.clientEp-> get(path, targetType = CustomFieldOption);
        return response;
    }
    remote isolated function getAllDashboards(string? filter, int? startAt, int? maxResults) returns PageOfDashboards|error {
        string  path = string `/rest/api/2/dashboard`;
        map<anydata> queryParam = {filter: filter, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageOfDashboards response = check self.clientEp-> get(path, targetType = PageOfDashboards);
        return response;
    }
    remote isolated function createDashboard(DashboardDetails payload) returns Dashboard|error {
        string  path = string `/rest/api/2/dashboard`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Dashboard response = check self.clientEp->post(path, request, targetType=Dashboard);
        return response;
    }
    remote isolated function getDashboardsPaginated(string? dashboardName, string? accountId, string? owner, string? groupname, int? projectId, string? orderBy, int? startAt, int? maxResults, string? expand) returns PageBeanDashboard|error {
        string  path = string `/rest/api/2/dashboard/search`;
        map<anydata> queryParam = {dashboardName: dashboardName, accountId: accountId, owner: owner, groupname: groupname, projectId: projectId, orderBy: orderBy, startAt: startAt, maxResults: maxResults, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanDashboard response = check self.clientEp-> get(path, targetType = PageBeanDashboard);
        return response;
    }
    remote isolated function getDashboardItemPropertyKeys(string dashboardId, string itemId) returns PropertyKeys|error {
        string  path = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getDashboardItemProperty(string dashboardId, string itemId, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setDashboardItemProperty(string dashboardId, string itemId, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteDashboardItemProperty(string dashboardId, string itemId, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/dashboard/${dashboardId}/items/${itemId}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getDashboard(string id) returns Dashboard|error {
        string  path = string `/rest/api/2/dashboard/${id}`;
        Dashboard response = check self.clientEp-> get(path, targetType = Dashboard);
        return response;
    }
    remote isolated function updateDashboard(string id, DashboardDetails payload) returns Dashboard|error {
        string  path = string `/rest/api/2/dashboard/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Dashboard response = check self.clientEp->put(path, request, targetType=Dashboard);
        return response;
    }
    remote isolated function deleteDashboard(string id) returns http:Response | error {
        string  path = string `/rest/api/2/dashboard/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function copyDashboard(string id, DashboardDetails payload) returns Dashboard|error {
        string  path = string `/rest/api/2/dashboard/${id}/copy`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Dashboard response = check self.clientEp->post(path, request, targetType=Dashboard);
        return response;
    }
    remote isolated function analyseExpression(string? 'check, JiraExpressionForAnalysis payload) returns JiraExpressionsAnalysis|error {
        string  path = string `/rest/api/2/expression/analyse`;
        map<anydata> queryParam = {'check: 'check};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        JiraExpressionsAnalysis response = check self.clientEp->post(path, request, targetType=JiraExpressionsAnalysis);
        return response;
    }
    remote isolated function evaluateJiraExpression(string? expand, JiraExpressionEvalRequestBean payload) returns JiraExpressionResult|error {
        string  path = string `/rest/api/2/expression/eval`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        JiraExpressionResult response = check self.clientEp->post(path, request, targetType=JiraExpressionResult);
        return response;
    }
    remote isolated function getFields() returns FieldDetailsArr|error {
        string  path = string `/rest/api/2/field`;
        FieldDetailsArr response = check self.clientEp-> get(path, targetType = FieldDetailsArr);
        return response;
    }
    remote isolated function createCustomField(CustomFieldDefinitionJsonBean payload) returns FieldDetails|error {
        string  path = string `/rest/api/2/field`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        FieldDetails response = check self.clientEp->post(path, request, targetType=FieldDetails);
        return response;
    }
    remote isolated function getFieldsPaginated(int? startAt, int? maxResults, string[]? 'type, string[]? id, string? query, string? orderBy, string? expand) returns PageBeanField|error {
        string  path = string `/rest/api/2/field/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, 'type: 'type, id: id, query: query, orderBy: orderBy, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanField response = check self.clientEp-> get(path, targetType = PageBeanField);
        return response;
    }
    remote isolated function updateCustomField(string fieldId, UpdateCustomFieldDetails payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getContextsForField(string fieldId, boolean? isAnyIssueType, boolean? isGlobalContext, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContext|error {
        string  path = string `/rest/api/2/field/${fieldId}/context`;
        map<anydata> queryParam = {isAnyIssueType: isAnyIssueType, isGlobalContext: isGlobalContext, contextId: contextId, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanCustomFieldContext response = check self.clientEp-> get(path, targetType = PageBeanCustomFieldContext);
        return response;
    }
    remote isolated function createCustomFieldContext(string fieldId, CreateCustomFieldContext payload) returns CreateCustomFieldContext|error {
        string  path = string `/rest/api/2/field/${fieldId}/context`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        CreateCustomFieldContext response = check self.clientEp->post(path, request, targetType=CreateCustomFieldContext);
        return response;
    }
    remote isolated function getDefaultValues(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContextDefaultValue|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/defaultValue`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanCustomFieldContextDefaultValue response = check self.clientEp-> get(path, targetType = PageBeanCustomFieldContextDefaultValue);
        return response;
    }
    remote isolated function setDefaultValues(string fieldId, CustomFieldContextDefaultValueUpdate payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/defaultValue`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getIssueTypeMappingsForContexts(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanIssueTypeToContextMapping|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/issuetypemapping`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeToContextMapping response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeToContextMapping);
        return response;
    }
    remote isolated function getCustomFieldContextsForProjectsAndIssueTypes(string fieldId, int? startAt, int? maxResults, ProjectIssueTypeMappings payload) returns PageBeanContextForProjectAndIssueType|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PageBeanContextForProjectAndIssueType response = check self.clientEp->post(path, request, targetType=PageBeanContextForProjectAndIssueType);
        return response;
    }
    remote isolated function getProjectContextMapping(string fieldId, int[]? contextId, int? startAt, int? maxResults) returns PageBeanCustomFieldContextProjectMapping|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/projectmapping`;
        map<anydata> queryParam = {contextId: contextId, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanCustomFieldContextProjectMapping response = check self.clientEp-> get(path, targetType = PageBeanCustomFieldContextProjectMapping);
        return response;
    }
    remote isolated function updateCustomFieldContext(string fieldId, int contextId, CustomFieldContextUpdateDetails payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteCustomFieldContext(string fieldId, int contextId) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function addIssueTypesToContext(string fieldId, int contextId, IssueTypeIds payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/issuetype`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function removeIssueTypesFromContext(string fieldId, int contextId, IssueTypeIds payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/issuetype/remove`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getOptionsForContext(string fieldId, int contextId, int? optionId, boolean? onlyOptions, int? startAt, int? maxResults) returns PageBeanCustomFieldContextOption|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        map<anydata> queryParam = {optionId: optionId, onlyOptions: onlyOptions, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanCustomFieldContextOption response = check self.clientEp-> get(path, targetType = PageBeanCustomFieldContextOption);
        return response;
    }
    remote isolated function updateCustomFieldOption(string fieldId, int contextId, BulkCustomFieldOptionUpdateRequest payload) returns CustomFieldUpdatedContextOptionsList|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        CustomFieldUpdatedContextOptionsList response = check self.clientEp->put(path, request, targetType=CustomFieldUpdatedContextOptionsList);
        return response;
    }
    remote isolated function createCustomFieldOption(string fieldId, int contextId, BulkCustomFieldOptionCreateRequest payload) returns CustomFieldCreatedContextOptionsList|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/option`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        CustomFieldCreatedContextOptionsList response = check self.clientEp->post(path, request, targetType=CustomFieldCreatedContextOptionsList);
        return response;
    }
    remote isolated function reorderCustomFieldOptions(string fieldId, int contextId, OrderOfCustomFieldOptions payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/option/move`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteCustomFieldOption(string fieldId, int contextId, int optionId) returns http:Response | error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/option/${optionId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function assignProjectsToCustomFieldContext(string fieldId, int contextId, ProjectIds payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function removeCustomFieldContextFromProjects(string fieldId, int contextId, ProjectIds payload) returns json|error {
        string  path = string `/rest/api/2/field/${fieldId}/context/${contextId}/project/remove`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getContextsForFieldDeprecated(string fieldId, int? startAt, int? maxResults) returns PageBeanContext|error {
        string  path = string `/rest/api/2/field/${fieldId}/contexts`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanContext response = check self.clientEp-> get(path, targetType = PageBeanContext);
        return response;
    }
    remote isolated function getScreensForField(string fieldId, int? startAt, int? maxResults, string? expand) returns PageBeanScreenWithTab|error {
        string  path = string `/rest/api/2/field/${fieldId}/screens`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanScreenWithTab response = check self.clientEp-> get(path, targetType = PageBeanScreenWithTab);
        return response;
    }
    remote isolated function getAllIssueFieldOptions(int? startAt, int? maxResults, string fieldKey) returns PageBeanIssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueFieldOption response = check self.clientEp-> get(path, targetType = PageBeanIssueFieldOption);
        return response;
    }
    remote isolated function createIssueFieldOption(string fieldKey, IssueFieldOptionCreateBean payload) returns IssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueFieldOption response = check self.clientEp->post(path, request, targetType=IssueFieldOption);
        return response;
    }
    remote isolated function getSelectableIssueFieldOptions(int? startAt, int? maxResults, int? projectId, string fieldKey) returns PageBeanIssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/suggestions/edit`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueFieldOption response = check self.clientEp-> get(path, targetType = PageBeanIssueFieldOption);
        return response;
    }
    remote isolated function getVisibleIssueFieldOptions(int? startAt, int? maxResults, int? projectId, string fieldKey) returns PageBeanIssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/suggestions/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueFieldOption response = check self.clientEp-> get(path, targetType = PageBeanIssueFieldOption);
        return response;
    }
    remote isolated function getIssueFieldOption(string fieldKey, int optionId) returns IssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        IssueFieldOption response = check self.clientEp-> get(path, targetType = IssueFieldOption);
        return response;
    }
    remote isolated function updateIssueFieldOption(string fieldKey, int optionId, IssueFieldOption payload) returns IssueFieldOption|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueFieldOption response = check self.clientEp->put(path, request, targetType=IssueFieldOption);
        return response;
    }
    remote isolated function deleteIssueFieldOption(string fieldKey, int optionId) returns json|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/${optionId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function replaceIssueFieldOption(int? replaceWith, string? jql, string fieldKey, int optionId) returns TaskProgressBeanRemoveOptionFromIssuesResult|error {
        string  path = string `/rest/api/2/field/${fieldKey}/option/${optionId}/issue`;
        map<anydata> queryParam = {replaceWith: replaceWith, jql: jql};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        TaskProgressBeanRemoveOptionFromIssuesResult response = check self.clientEp-> delete(path, request, targetType = TaskProgressBeanRemoveOptionFromIssuesResult);
        return response;
    }
    remote isolated function getAllFieldConfigurations(int? startAt, int? maxResults, int[]? id, boolean? isDefault, string? query) returns PageBeanFieldConfiguration|error {
        string  path = string `/rest/api/2/fieldconfiguration`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id, isDefault: isDefault, query: query};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFieldConfiguration response = check self.clientEp-> get(path, targetType = PageBeanFieldConfiguration);
        return response;
    }
    remote isolated function getFieldConfigurationItems(int id, int? startAt, int? maxResults) returns PageBeanFieldConfigurationItem|error {
        string  path = string `/rest/api/2/fieldconfiguration/${id}/fields`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFieldConfigurationItem response = check self.clientEp-> get(path, targetType = PageBeanFieldConfigurationItem);
        return response;
    }
    remote isolated function getAllFieldConfigurationSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanFieldConfigurationScheme|error {
        string  path = string `/rest/api/2/fieldconfigurationscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFieldConfigurationScheme response = check self.clientEp-> get(path, targetType = PageBeanFieldConfigurationScheme);
        return response;
    }
    remote isolated function getFieldConfigurationSchemeMappings(int? startAt, int? maxResults, int[]? fieldConfigurationSchemeId) returns PageBeanFieldConfigurationIssueTypeItem|error {
        string  path = string `/rest/api/2/fieldconfigurationscheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, fieldConfigurationSchemeId: fieldConfigurationSchemeId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFieldConfigurationIssueTypeItem response = check self.clientEp-> get(path, targetType = PageBeanFieldConfigurationIssueTypeItem);
        return response;
    }
    remote isolated function getFieldConfigurationSchemeProjectMapping(int? startAt, int? maxResults, int[] projectId) returns PageBeanFieldConfigurationSchemeProjects|error {
        string  path = string `/rest/api/2/fieldconfigurationscheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFieldConfigurationSchemeProjects response = check self.clientEp-> get(path, targetType = PageBeanFieldConfigurationSchemeProjects);
        return response;
    }
    remote isolated function assignFieldConfigurationSchemeToProject(FieldConfigurationSchemeProjectAssociation payload) returns json|error {
        string  path = string `/rest/api/2/fieldconfigurationscheme/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getFilters(string? expand) returns FilterArr|error {
        string  path = string `/rest/api/2/filter`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        FilterArr response = check self.clientEp-> get(path, targetType = FilterArr);
        return response;
    }
    remote isolated function createFilter(string? expand, Filter payload) returns Filter|error {
        string  path = string `/rest/api/2/filter`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Filter response = check self.clientEp->post(path, request, targetType=Filter);
        return response;
    }
    remote isolated function getDefaultShareScope() returns DefaultShareScope|error {
        string  path = string `/rest/api/2/filter/defaultShareScope`;
        DefaultShareScope response = check self.clientEp-> get(path, targetType = DefaultShareScope);
        return response;
    }
    remote isolated function setDefaultShareScope(DefaultShareScope payload) returns DefaultShareScope|error {
        string  path = string `/rest/api/2/filter/defaultShareScope`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        DefaultShareScope response = check self.clientEp->put(path, request, targetType=DefaultShareScope);
        return response;
    }
    remote isolated function getFavouriteFilters(string? expand) returns FilterArr|error {
        string  path = string `/rest/api/2/filter/favourite`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        FilterArr response = check self.clientEp-> get(path, targetType = FilterArr);
        return response;
    }
    remote isolated function getMyFilters(string? expand, boolean? includeFavourites) returns FilterArr|error {
        string  path = string `/rest/api/2/filter/my`;
        map<anydata> queryParam = {expand: expand, includeFavourites: includeFavourites};
        path = path + getPathForQueryParam(queryParam);
        FilterArr response = check self.clientEp-> get(path, targetType = FilterArr);
        return response;
    }
    remote isolated function getFiltersPaginated(string? filterName, string? accountId, string? owner, string? groupname, int? projectId, int[]? id, string? orderBy, int? startAt, int? maxResults, string? expand) returns PageBeanFilterDetails|error {
        string  path = string `/rest/api/2/filter/search`;
        map<anydata> queryParam = {filterName: filterName, accountId: accountId, owner: owner, groupname: groupname, projectId: projectId, id: id, orderBy: orderBy, startAt: startAt, maxResults: maxResults, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanFilterDetails response = check self.clientEp-> get(path, targetType = PageBeanFilterDetails);
        return response;
    }
    remote isolated function getFilter(int id, string? expand) returns Filter|error {
        string  path = string `/rest/api/2/filter/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        Filter response = check self.clientEp-> get(path, targetType = Filter);
        return response;
    }
    remote isolated function updateFilter(int id, string? expand, Filter payload) returns Filter|error {
        string  path = string `/rest/api/2/filter/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Filter response = check self.clientEp->put(path, request, targetType=Filter);
        return response;
    }
    remote isolated function deleteFilter(int id) returns http:Response | error {
        string  path = string `/rest/api/2/filter/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getColumns(int id) returns ColumnItemArr|error {
        string  path = string `/rest/api/2/filter/${id}/columns`;
        ColumnItemArr response = check self.clientEp-> get(path, targetType = ColumnItemArr);
        return response;
    }
    remote isolated function setColumns(int id, [] payload) returns json|error {
        string  path = string `/rest/api/2/filter/${id}/columns`;
        http:Request request = new;
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function resetColumns(int id) returns http:Response | error {
        string  path = string `/rest/api/2/filter/${id}/columns`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function setFavouriteForFilter(int id, string? expand) returns Filter|error {
        string  path = string `/rest/api/2/filter/${id}/favourite`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        Filter response = check self.clientEp-> put(path, request, targetType = Filter);
        return response;
    }
    remote isolated function deleteFavouriteForFilter(int id, string? expand) returns Filter|error {
        string  path = string `/rest/api/2/filter/${id}/favourite`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        Filter response = check self.clientEp-> delete(path, request, targetType = Filter);
        return response;
    }
    remote isolated function getSharePermissions(int id) returns SharePermissionArr|error {
        string  path = string `/rest/api/2/filter/${id}/permission`;
        SharePermissionArr response = check self.clientEp-> get(path, targetType = SharePermissionArr);
        return response;
    }
    remote isolated function addSharePermission(int id, SharePermissionInputBean payload) returns SharePermissionArr|error {
        string  path = string `/rest/api/2/filter/${id}/permission`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        SharePermissionArr response = check self.clientEp->post(path, request, targetType=SharePermissionArr);
        return response;
    }
    remote isolated function getSharePermission(int id, int permissionId) returns SharePermission|error {
        string  path = string `/rest/api/2/filter/${id}/permission/${permissionId}`;
        SharePermission response = check self.clientEp-> get(path, targetType = SharePermission);
        return response;
    }
    remote isolated function deleteSharePermission(int id, int permissionId) returns http:Response | error {
        string  path = string `/rest/api/2/filter/${id}/permission/${permissionId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getGroup(string groupname, string? expand) returns Group|error {
        string  path = string `/rest/api/2/group`;
        map<anydata> queryParam = {groupname: groupname, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        Group response = check self.clientEp-> get(path, targetType = Group);
        return response;
    }
    remote isolated function createGroup(AddGroupBean payload) returns Group|error {
        string  path = string `/rest/api/2/group`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Group response = check self.clientEp->post(path, request, targetType=Group);
        return response;
    }
    remote isolated function removeGroup(string groupname, string? swapGroup) returns http:Response | error {
        string  path = string `/rest/api/2/group`;
        map<anydata> queryParam = {groupname: groupname, swapGroup: swapGroup};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function bulkGetGroups(int? startAt, int? maxResults, string[]? groupId, string[]? groupName) returns PageBeanGroupDetails|error {
        string  path = string `/rest/api/2/group/bulk`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, groupId: groupId, groupName: groupName};
        path = path + getPathForQueryParam(queryParam);
        PageBeanGroupDetails response = check self.clientEp-> get(path, targetType = PageBeanGroupDetails);
        return response;
    }
    remote isolated function getUsersFromGroup(string groupname, boolean? includeInactiveUsers, int? startAt, int? maxResults) returns PageBeanUserDetails|error {
        string  path = string `/rest/api/2/group/member`;
        map<anydata> queryParam = {groupname: groupname, includeInactiveUsers: includeInactiveUsers, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanUserDetails response = check self.clientEp-> get(path, targetType = PageBeanUserDetails);
        return response;
    }
    remote isolated function addUserToGroup(string groupname, UpdateUserToGroupBean payload) returns Group|error {
        string  path = string `/rest/api/2/group/user`;
        map<anydata> queryParam = {groupname: groupname};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Group response = check self.clientEp->post(path, request, targetType=Group);
        return response;
    }
    remote isolated function removeUserFromGroup(string groupname, string? username, string accountId) returns http:Response | error {
        string  path = string `/rest/api/2/group/user`;
        map<anydata> queryParam = {groupname: groupname, username: username, accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function findGroups(string? accountId, string? query, string[]? exclude, int? maxResults, string? userName) returns FoundGroups|error {
        string  path = string `/rest/api/2/groups/picker`;
        map<anydata> queryParam = {accountId: accountId, query: query, exclude: exclude, maxResults: maxResults, userName: userName};
        path = path + getPathForQueryParam(queryParam);
        FoundGroups response = check self.clientEp-> get(path, targetType = FoundGroups);
        return response;
    }
    remote isolated function findUsersAndGroups(string query, int? maxResults, boolean? showAvatar, string? fieldId, string[]? projectId, string[]? issueTypeId, string? avatarSize, boolean? caseInsensitive, boolean? excludeConnectAddons) returns FoundUsersAndGroups|error {
        string  path = string `/rest/api/2/groupuserpicker`;
        map<anydata> queryParam = {query: query, maxResults: maxResults, showAvatar: showAvatar, fieldId: fieldId, projectId: projectId, issueTypeId: issueTypeId, avatarSize: avatarSize, caseInsensitive: caseInsensitive, excludeConnectAddons: excludeConnectAddons};
        path = path + getPathForQueryParam(queryParam);
        FoundUsersAndGroups response = check self.clientEp-> get(path, targetType = FoundUsersAndGroups);
        return response;
    }
    remote isolated function getLicense() returns License|error {
        string  path = string `/rest/api/2/instance/license`;
        License response = check self.clientEp-> get(path, targetType = License);
        return response;
    }
    remote isolated function createIssue(boolean? updateHistory, IssueUpdateDetails payload) returns CreatedIssue|error {
        string  path = string `/rest/api/2/issue`;
        map<anydata> queryParam = {updateHistory: updateHistory};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        CreatedIssue response = check self.clientEp->post(path, request, targetType=CreatedIssue);
        return response;
    }
    remote isolated function createIssues(IssuesUpdateBean payload) returns CreatedIssues|error {
        string  path = string `/rest/api/2/issue/bulk`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        CreatedIssues response = check self.clientEp->post(path, request, targetType=CreatedIssues);
        return response;
    }
    remote isolated function getCreateIssueMeta(string[]? projectIds, string[]? projectKeys, string[]? issuetypeIds, string[]? issuetypeNames, string? expand) returns IssueCreateMetadata|error {
        string  path = string `/rest/api/2/issue/createmeta`;
        map<anydata> queryParam = {projectIds: projectIds, projectKeys: projectKeys, issuetypeIds: issuetypeIds, issuetypeNames: issuetypeNames, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        IssueCreateMetadata response = check self.clientEp-> get(path, targetType = IssueCreateMetadata);
        return response;
    }
    remote isolated function getIssuePickerResource(string? query, string? currentJQL, string? currentIssueKey, string? currentProjectId, boolean? showSubTasks, boolean? showSubTaskParent) returns IssuePickerSuggestions|error {
        string  path = string `/rest/api/2/issue/picker`;
        map<anydata> queryParam = {query: query, currentJQL: currentJQL, currentIssueKey: currentIssueKey, currentProjectId: currentProjectId, showSubTasks: showSubTasks, showSubTaskParent: showSubTaskParent};
        path = path + getPathForQueryParam(queryParam);
        IssuePickerSuggestions response = check self.clientEp-> get(path, targetType = IssuePickerSuggestions);
        return response;
    }
    remote isolated function bulkSetIssuesProperties(IssueEntityProperties payload) returns http:Response | error {
        string  path = string `/rest/api/2/issue/properties`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response  response = check self.clientEp->post(path, request, targetType=http:Response );
        return response;
    }
    remote isolated function bulkSetIssueProperty(string propertyKey, BulkIssuePropertyUpdateRequest payload) returns http:Response | error {
        string  path = string `/rest/api/2/issue/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response  response = check self.clientEp->put(path, request, targetType=http:Response );
        return response;
    }
    remote isolated function bulkDeleteIssueProperty(string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/issue/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIssue(string issueIdOrKey, string[]? fields, boolean? fieldsByKeys, string? expand, string[]? properties, boolean? updateHistory) returns IssueBean|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {fields: fields, fieldsByKeys: fieldsByKeys, expand: expand, properties: properties, updateHistory: updateHistory};
        path = path + getPathForQueryParam(queryParam);
        IssueBean response = check self.clientEp-> get(path, targetType = IssueBean);
        return response;
    }
    remote isolated function editIssue(string issueIdOrKey, boolean? notifyUsers, boolean? overrideScreenSecurity, boolean? overrideEditableFlag, IssueUpdateDetails payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, overrideScreenSecurity: overrideScreenSecurity, overrideEditableFlag: overrideEditableFlag};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteIssue(string issueIdOrKey, string? deleteSubtasks) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}`;
        map<anydata> queryParam = {deleteSubtasks: deleteSubtasks};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function assignIssue(string issueIdOrKey, User payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/assignee`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function addAttachment(string issueIdOrKey, string payload) returns AttachmentArr|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/attachments`;
        http:Request request = new;
        AttachmentArr response = check self.clientEp->post(path, request, targetType=AttachmentArr);
        return response;
    }
    remote isolated function getChangeLogs(string issueIdOrKey, int? startAt, int? maxResults) returns PageBeanChangelog|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/changelog`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanChangelog response = check self.clientEp-> get(path, targetType = PageBeanChangelog);
        return response;
    }
    remote isolated function getComments(string issueIdOrKey, int? startAt, int? maxResults, string? orderBy, string? expand) returns PageOfComments|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/comment`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageOfComments response = check self.clientEp-> get(path, targetType = PageOfComments);
        return response;
    }
    remote isolated function addComment(string issueIdOrKey, string? expand, Comment payload) returns Comment|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/comment`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Comment response = check self.clientEp->post(path, request, targetType=Comment);
        return response;
    }
    remote isolated function getComment(string issueIdOrKey, string id, string? expand) returns Comment|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        Comment response = check self.clientEp-> get(path, targetType = Comment);
        return response;
    }
    remote isolated function updateComment(string issueIdOrKey, string id, string? expand, Comment payload) returns Comment|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Comment response = check self.clientEp->put(path, request, targetType=Comment);
        return response;
    }
    remote isolated function deleteComment(string issueIdOrKey, string id) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/comment/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getEditIssueMeta(string issueIdOrKey, boolean? overrideScreenSecurity, boolean? overrideEditableFlag) returns IssueUpdateMetadata|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/editmeta`;
        map<anydata> queryParam = {overrideScreenSecurity: overrideScreenSecurity, overrideEditableFlag: overrideEditableFlag};
        path = path + getPathForQueryParam(queryParam);
        IssueUpdateMetadata response = check self.clientEp-> get(path, targetType = IssueUpdateMetadata);
        return response;
    }
    remote isolated function notify(string issueIdOrKey, Notification payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/notify`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getIssuePropertyKeys(string issueIdOrKey) returns PropertyKeys|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getIssueProperty(string issueIdOrKey, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setIssueProperty(string issueIdOrKey, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteIssueProperty(string issueIdOrKey, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getRemoteIssueLinks(string issueIdOrKey, string? globalId) returns RemoteIssueLink|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        map<anydata> queryParam = {globalId: globalId};
        path = path + getPathForQueryParam(queryParam);
        RemoteIssueLink response = check self.clientEp-> get(path, targetType = RemoteIssueLink);
        return response;
    }
    remote isolated function createOrUpdateRemoteIssueLink(string issueIdOrKey, RemoteIssueLinkRequest payload) returns RemoteIssueLinkIdentifies|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        RemoteIssueLinkIdentifies response = check self.clientEp->post(path, request, targetType=RemoteIssueLinkIdentifies);
        return response;
    }
    remote isolated function deleteRemoteIssueLinkByGlobalId(string issueIdOrKey, string globalId) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink`;
        map<anydata> queryParam = {globalId: globalId};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getRemoteIssueLinkById(string issueIdOrKey, string linkId) returns RemoteIssueLink|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        RemoteIssueLink response = check self.clientEp-> get(path, targetType = RemoteIssueLink);
        return response;
    }
    remote isolated function updateRemoteIssueLink(string issueIdOrKey, string linkId, RemoteIssueLinkRequest payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteRemoteIssueLinkById(string issueIdOrKey, string linkId) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/remotelink/${linkId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getTransitions(string issueIdOrKey, string? expand, string? transitionId, boolean? skipRemoteOnlyCondition, boolean? includeUnavailableTransitions, boolean? sortByOpsBarAndStatus) returns Transitions|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/transitions`;
        map<anydata> queryParam = {expand: expand, transitionId: transitionId, skipRemoteOnlyCondition: skipRemoteOnlyCondition, includeUnavailableTransitions: includeUnavailableTransitions, sortByOpsBarAndStatus: sortByOpsBarAndStatus};
        path = path + getPathForQueryParam(queryParam);
        Transitions response = check self.clientEp-> get(path, targetType = Transitions);
        return response;
    }
    remote isolated function doTransition(string issueIdOrKey, IssueUpdateDetails payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/transitions`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getVotes(string issueIdOrKey) returns Votes|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        Votes response = check self.clientEp-> get(path, targetType = Votes);
        return response;
    }
    remote isolated function addVote(string issueIdOrKey) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> post(path, request, targetType = json);
        return response;
    }
    remote isolated function removeVote(string issueIdOrKey) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/votes`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIssueWatchers(string issueIdOrKey) returns Watchers|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        Watchers response = check self.clientEp-> get(path, targetType = Watchers);
        return response;
    }
    remote isolated function addWatcher(string issueIdOrKey, string payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function removeWatcher(string issueIdOrKey, string? username, string? accountId) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/watchers`;
        map<anydata> queryParam = {username: username, accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIssueWorklog(string issueIdOrKey, int? startAt, int? maxResults, int? startedAfter, string? expand) returns PageOfWorklogs|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, startedAfter: startedAfter, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageOfWorklogs response = check self.clientEp-> get(path, targetType = PageOfWorklogs);
        return response;
    }
    remote isolated function addWorklog(string issueIdOrKey, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? reduceBy, string? expand, boolean? overrideEditableFlag, Worklog payload) returns Worklog|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, reduceBy: reduceBy, expand: expand, overrideEditableFlag: overrideEditableFlag};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Worklog response = check self.clientEp->post(path, request, targetType=Worklog);
        return response;
    }
    remote isolated function getWorklog(string issueIdOrKey, string id, string? expand) returns Worklog|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        Worklog response = check self.clientEp-> get(path, targetType = Worklog);
        return response;
    }
    remote isolated function updateWorklog(string issueIdOrKey, string id, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? expand, boolean? overrideEditableFlag, Worklog payload) returns Worklog|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, expand: expand, overrideEditableFlag: overrideEditableFlag};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Worklog response = check self.clientEp->put(path, request, targetType=Worklog);
        return response;
    }
    remote isolated function deleteWorklog(string issueIdOrKey, string id, boolean? notifyUsers, string? adjustEstimate, string? newEstimate, string? increaseBy, boolean? overrideEditableFlag) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${id}`;
        map<anydata> queryParam = {notifyUsers: notifyUsers, adjustEstimate: adjustEstimate, newEstimate: newEstimate, increaseBy: increaseBy, overrideEditableFlag: overrideEditableFlag};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getWorklogPropertyKeys(string issueIdOrKey, string worklogId) returns PropertyKeys|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteWorklogProperty(string issueIdOrKey, string worklogId, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/issue/${issueIdOrKey}/worklog/${worklogId}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function linkIssues(LinkIssueRequestJsonBean payload) returns json|error {
        string  path = string `/rest/api/2/issueLink`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getIssueLink(string linkId) returns IssueLink|error {
        string  path = string `/rest/api/2/issueLink/${linkId}`;
        IssueLink response = check self.clientEp-> get(path, targetType = IssueLink);
        return response;
    }
    remote isolated function deleteIssueLink(string linkId) returns http:Response | error {
        string  path = string `/rest/api/2/issueLink/${linkId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIssueLinkTypes() returns IssueLinkTypes|error {
        string  path = string `/rest/api/2/issueLinkType`;
        IssueLinkTypes response = check self.clientEp-> get(path, targetType = IssueLinkTypes);
        return response;
    }
    remote isolated function createIssueLinkType(IssueLinkType payload) returns IssueLinkType|error {
        string  path = string `/rest/api/2/issueLinkType`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueLinkType response = check self.clientEp->post(path, request, targetType=IssueLinkType);
        return response;
    }
    remote isolated function getIssueLinkType(string issueLinkTypeId) returns IssueLinkType|error {
        string  path = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        IssueLinkType response = check self.clientEp-> get(path, targetType = IssueLinkType);
        return response;
    }
    remote isolated function updateIssueLinkType(string issueLinkTypeId, IssueLinkType payload) returns IssueLinkType|error {
        string  path = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueLinkType response = check self.clientEp->put(path, request, targetType=IssueLinkType);
        return response;
    }
    remote isolated function deleteIssueLinkType(string issueLinkTypeId) returns http:Response | error {
        string  path = string `/rest/api/2/issueLinkType/${issueLinkTypeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIssueSecuritySchemes() returns SecuritySchemes|error {
        string  path = string `/rest/api/2/issuesecurityschemes`;
        SecuritySchemes response = check self.clientEp-> get(path, targetType = SecuritySchemes);
        return response;
    }
    remote isolated function getIssueSecurityScheme(int id) returns SecurityScheme|error {
        string  path = string `/rest/api/2/issuesecurityschemes/${id}`;
        SecurityScheme response = check self.clientEp-> get(path, targetType = SecurityScheme);
        return response;
    }
    remote isolated function getIssueSecurityLevelMembers(int issueSecuritySchemeId, int? startAt, int? maxResults, int[]? issueSecurityLevelId, string? expand) returns PageBeanIssueSecurityLevelMember|error {
        string  path = string `/rest/api/2/issuesecurityschemes/${issueSecuritySchemeId}/members`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueSecurityLevelId: issueSecurityLevelId, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueSecurityLevelMember response = check self.clientEp-> get(path, targetType = PageBeanIssueSecurityLevelMember);
        return response;
    }
    remote isolated function getIssueAllTypes() returns IssueTypeDetailsArr|error {
        string  path = string `/rest/api/2/issuetype`;
        IssueTypeDetailsArr response = check self.clientEp-> get(path, targetType = IssueTypeDetailsArr);
        return response;
    }
    remote isolated function createIssueType(IssueTypeCreateBean payload) returns IssueTypeDetails|error {
        string  path = string `/rest/api/2/issuetype`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueTypeDetails response = check self.clientEp->post(path, request, targetType=IssueTypeDetails);
        return response;
    }
    remote isolated function getIssueType(string id) returns IssueTypeDetails|error {
        string  path = string `/rest/api/2/issuetype/${id}`;
        IssueTypeDetails response = check self.clientEp-> get(path, targetType = IssueTypeDetails);
        return response;
    }
    remote isolated function updateIssueType(string id, IssueTypeUpdateBean payload) returns IssueTypeDetails|error {
        string  path = string `/rest/api/2/issuetype/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueTypeDetails response = check self.clientEp->put(path, request, targetType=IssueTypeDetails);
        return response;
    }
    remote isolated function deleteIssueType(string id, string? alternativeIssueTypeId) returns http:Response | error {
        string  path = string `/rest/api/2/issuetype/${id}`;
        map<anydata> queryParam = {alternativeIssueTypeId: alternativeIssueTypeId};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getAlternativeIssueTypes(string id) returns IssueTypeDetailsArr|error {
        string  path = string `/rest/api/2/issuetype/${id}/alternatives`;
        IssueTypeDetailsArr response = check self.clientEp-> get(path, targetType = IssueTypeDetailsArr);
        return response;
    }
    remote isolated function createIssueTypeAvatar(string id, int? x, int? y, int size, json payload) returns Avatar|error {
        string  path = string `/rest/api/2/issuetype/${id}/avatar2`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        Avatar response = check self.clientEp->post(path, request, targetType=Avatar);
        return response;
    }
    remote isolated function getIssueTypePropertyKeys(string issueTypeId) returns PropertyKeys|error {
        string  path = string `/rest/api/2/issuetype/${issueTypeId}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getIssueTypeProperty(string issueTypeId, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setIssueTypeProperty(string issueTypeId, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteIssueTypeProperty(string issueTypeId, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/issuetype/${issueTypeId}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getAllIssueTypeSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanIssueTypeScheme|error {
        string  path = string `/rest/api/2/issuetypescheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeScheme response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeScheme);
        return response;
    }
    remote isolated function createIssueTypeScheme(IssueTypeSchemeDetails payload) returns IssueTypeSchemeID|error {
        string  path = string `/rest/api/2/issuetypescheme`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueTypeSchemeID response = check self.clientEp->post(path, request, targetType=IssueTypeSchemeID);
        return response;
    }
    remote isolated function getIssueTypeSchemesMapping(int? startAt, int? maxResults, int[]? issueTypeSchemeId) returns PageBeanIssueTypeSchemeMapping|error {
        string  path = string `/rest/api/2/issuetypescheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueTypeSchemeId: issueTypeSchemeId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeSchemeMapping response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeSchemeMapping);
        return response;
    }
    remote isolated function getIssueTypeSchemeForProjects(int? startAt, int? maxResults, int[] projectId) returns PageBeanIssueTypeSchemeProjects|error {
        string  path = string `/rest/api/2/issuetypescheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeSchemeProjects response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeSchemeProjects);
        return response;
    }
    remote isolated function assignIssueTypeSchemeToProject(IssueTypeSchemeProjectAssociation payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function updateIssueTypeScheme(int issueTypeSchemeId, IssueTypeSchemeUpdateDetails payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteIssueTypeScheme(int issueTypeSchemeId) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function addIssueTypesToIssueTypeScheme(int issueTypeSchemeId, IssueTypeIds payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function reorderIssueTypesInIssueTypeScheme(int issueTypeSchemeId, OrderOfIssueTypes payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype/move`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function removeIssueTypeFromIssueTypeScheme(int issueTypeSchemeId, int issueTypeId) returns json|error {
        string  path = string `/rest/api/2/issuetypescheme/${issueTypeSchemeId}/issuetype/${issueTypeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function getIssueTypeScreenSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanIssueTypeScreenScheme|error {
        string  path = string `/rest/api/2/issuetypescreenscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeScreenScheme response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeScreenScheme);
        return response;
    }
    remote isolated function createIssueTypeScreenScheme(IssueTypeScreenSchemeDetails payload) returns IssueTypeScreenSchemeId|error {
        string  path = string `/rest/api/2/issuetypescreenscheme`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueTypeScreenSchemeId response = check self.clientEp->post(path, request, targetType=IssueTypeScreenSchemeId);
        return response;
    }
    remote isolated function getIssueTypeScreenSchemeMappings(int? startAt, int? maxResults, int[]? issueTypeScreenSchemeId) returns PageBeanIssueTypeScreenSchemeItem|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/mapping`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, issueTypeScreenSchemeId: issueTypeScreenSchemeId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeScreenSchemeItem response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeScreenSchemeItem);
        return response;
    }
    remote isolated function getIssueTypeScreenSchemeProjectAssociations(int? startAt, int? maxResults, int[] projectId) returns PageBeanIssueTypeScreenSchemesProjects|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/project`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanIssueTypeScreenSchemesProjects response = check self.clientEp-> get(path, targetType = PageBeanIssueTypeScreenSchemesProjects);
        return response;
    }
    remote isolated function assignIssueTypeScreenSchemeToProject(IssueTypeScreenSchemeProjectAssociation payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function updateIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeScreenSchemeUpdateDetails payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteIssueTypeScreenScheme(string issueTypeScreenSchemeId) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function appendMappingsForIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeScreenSchemeMappingDetails payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function updateDefaultScreenScheme(string issueTypeScreenSchemeId, UpdateDefaultScreenScheme payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping/default`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function removeMappingsFromIssueTypeScreenScheme(string issueTypeScreenSchemeId, IssueTypeIds payload) returns json|error {
        string  path = string `/rest/api/2/issuetypescreenscheme/${issueTypeScreenSchemeId}/mapping/remove`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getAutoComplete() returns JQLReferenceData|error {
        string  path = string `/rest/api/2/jql/autocompletedata`;
        JQLReferenceData response = check self.clientEp-> get(path, targetType = JQLReferenceData);
        return response;
    }
    remote isolated function getAutoCompletePost(SearchAutoCompleteFilter payload) returns JQLReferenceData|error {
        string  path = string `/rest/api/2/jql/autocompletedata`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        JQLReferenceData response = check self.clientEp->post(path, request, targetType=JQLReferenceData);
        return response;
    }
    remote isolated function getFieldAutoCompleteForQueryString(string? fieldName, string? fieldValue, string? predicateName, string? predicateValue) returns AutoCompleteSuggestions|error {
        string  path = string `/rest/api/2/jql/autocompletedata/suggestions`;
        map<anydata> queryParam = {fieldName: fieldName, fieldValue: fieldValue, predicateName: predicateName, predicateValue: predicateValue};
        path = path + getPathForQueryParam(queryParam);
        AutoCompleteSuggestions response = check self.clientEp-> get(path, targetType = AutoCompleteSuggestions);
        return response;
    }
    remote isolated function matchIssues(IssuesAndJQLQueries payload) returns IssueMatches|error {
        string  path = string `/rest/api/2/jql/match`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        IssueMatches response = check self.clientEp->post(path, request, targetType=IssueMatches);
        return response;
    }
    remote isolated function parseJqlQueries(string? validation, JqlQueriesToParse payload) returns ParsedJqlQueries|error {
        string  path = string `/rest/api/2/jql/parse`;
        map<anydata> queryParam = {validation: validation};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ParsedJqlQueries response = check self.clientEp->post(path, request, targetType=ParsedJqlQueries);
        return response;
    }
    remote isolated function migrateQueries(JQLPersonalDataMigrationRequest payload) returns ConvertedJQLQueries|error {
        string  path = string `/rest/api/2/jql/pdcleaner`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ConvertedJQLQueries response = check self.clientEp->post(path, request, targetType=ConvertedJQLQueries);
        return response;
    }
    remote isolated function getAllLabels(int? startAt, int? maxResults) returns PageBeanString|error {
        string  path = string `/rest/api/2/label`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanString response = check self.clientEp-> get(path, targetType = PageBeanString);
        return response;
    }
    remote isolated function getMyPermissions(string? projectKey, string? projectId, string? issueKey, string? issueId, string? permissions, string? projectUuid, string? projectConfigurationUuid) returns Permissions|error {
        string  path = string `/rest/api/2/mypermissions`;
        map<anydata> queryParam = {projectKey: projectKey, projectId: projectId, issueKey: issueKey, issueId: issueId, permissions: permissions, projectUuid: projectUuid, projectConfigurationUuid: projectConfigurationUuid};
        path = path + getPathForQueryParam(queryParam);
        Permissions response = check self.clientEp-> get(path, targetType = Permissions);
        return response;
    }
    remote isolated function getPreference(string 'key) returns string|error {
        string  path = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        string response = check self.clientEp-> get(path, targetType = string);
        return response;
    }
    remote isolated function setPreference(string 'key, string payload) returns json|error {
        string  path = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function removePreference(string 'key) returns http:Response | error {
        string  path = string `/rest/api/2/mypreferences`;
        map<anydata> queryParam = {'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getLocale() returns Locale|error {
        string  path = string `/rest/api/2/mypreferences/locale`;
        Locale response = check self.clientEp-> get(path, targetType = Locale);
        return response;
    }
    remote isolated function setLocale(Locale payload) returns json|error {
        string  path = string `/rest/api/2/mypreferences/locale`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteLocale() returns json|error {
        string  path = string `/rest/api/2/mypreferences/locale`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> delete(path, request, targetType = json);
        return response;
    }
    remote isolated function getCurrentUser(string? expand) returns User|error {
        string  path = string `/rest/api/2/myself`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        User response = check self.clientEp-> get(path, targetType = User);
        return response;
    }
    remote isolated function getNotificationSchemes(int? startAt, int? maxResults, string? expand) returns PageBeanNotificationScheme|error {
        string  path = string `/rest/api/2/notificationscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanNotificationScheme response = check self.clientEp-> get(path, targetType = PageBeanNotificationScheme);
        return response;
    }
    remote isolated function getNotificationScheme(int id, string? expand) returns NotificationScheme|error {
        string  path = string `/rest/api/2/notificationscheme/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        NotificationScheme response = check self.clientEp-> get(path, targetType = NotificationScheme);
        return response;
    }
    remote isolated function getAllPermissions() returns Permissions|error {
        string  path = string `/rest/api/2/permissions`;
        Permissions response = check self.clientEp-> get(path, targetType = Permissions);
        return response;
    }
    remote isolated function getBulkPermissions(BulkPermissionsRequestBean payload) returns BulkPermissionGrants|error {
        string  path = string `/rest/api/2/permissions/check`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        BulkPermissionGrants response = check self.clientEp->post(path, request, targetType=BulkPermissionGrants);
        return response;
    }
    remote isolated function getPermittedProjects(PermissionsKeysBean payload) returns PermittedProjects|error {
        string  path = string `/rest/api/2/permissions/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PermittedProjects response = check self.clientEp->post(path, request, targetType=PermittedProjects);
        return response;
    }
    remote isolated function getAllPermissionSchemes(string? expand) returns PermissionSchemes|error {
        string  path = string `/rest/api/2/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PermissionSchemes response = check self.clientEp-> get(path, targetType = PermissionSchemes);
        return response;
    }
    remote isolated function createPermissionScheme(string? expand, PermissionScheme payload) returns PermissionScheme|error {
        string  path = string `/rest/api/2/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PermissionScheme response = check self.clientEp->post(path, request, targetType=PermissionScheme);
        return response;
    }
    remote isolated function getPermissionScheme(int schemeId, string? expand) returns PermissionScheme|error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PermissionScheme response = check self.clientEp-> get(path, targetType = PermissionScheme);
        return response;
    }
    remote isolated function updatePermissionScheme(int schemeId, string? expand, PermissionScheme payload) returns PermissionScheme|error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PermissionScheme response = check self.clientEp->put(path, request, targetType=PermissionScheme);
        return response;
    }
    remote isolated function deletePermissionScheme(int schemeId) returns http:Response | error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getPermissionSchemeGrants(int schemeId, string? expand) returns PermissionGrants|error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}/permission`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PermissionGrants response = check self.clientEp-> get(path, targetType = PermissionGrants);
        return response;
    }
    remote isolated function createPermissionGrant(int schemeId, string? expand, PermissionGrant payload) returns PermissionGrant|error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}/permission`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PermissionGrant response = check self.clientEp->post(path, request, targetType=PermissionGrant);
        return response;
    }
    remote isolated function getPermissionSchemeGrant(int schemeId, int permissionId, string? expand) returns PermissionGrant|error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}/permission/${permissionId}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PermissionGrant response = check self.clientEp-> get(path, targetType = PermissionGrant);
        return response;
    }
    remote isolated function deletePermissionSchemeEntity(int schemeId, int permissionId) returns http:Response | error {
        string  path = string `/rest/api/2/permissionscheme/${schemeId}/permission/${permissionId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getPriorities() returns PriorityArr|error {
        string  path = string `/rest/api/2/priority`;
        PriorityArr response = check self.clientEp-> get(path, targetType = PriorityArr);
        return response;
    }
    remote isolated function getPriority(string id) returns Priority|error {
        string  path = string `/rest/api/2/priority/${id}`;
        Priority response = check self.clientEp-> get(path, targetType = Priority);
        return response;
    }
    remote isolated function getAllProjects(string? expand, int? recent, string[]? properties) returns ProjectArr|error {
        string  path = string `/rest/api/2/project`;
        map<anydata> queryParam = {expand: expand, recent: recent, properties: properties};
        path = path + getPathForQueryParam(queryParam);
        ProjectArr response = check self.clientEp-> get(path, targetType = ProjectArr);
        return response;
    }
    remote isolated function createProject(ProjectInputBean payload) returns ProjectIdentifiers|error {
        string  path = string `/rest/api/2/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectIdentifiers response = check self.clientEp->post(path, request, targetType=ProjectIdentifiers);
        return response;
    }
    remote isolated function searchProjects(int? startAt, int? maxResults, string? orderBy, string? query, string? typeKey, int? categoryId, string? action, string? expand, string[]? status, StringList[]? properties, string? propertyQuery) returns PageBeanProject|error {
        string  path = string `/rest/api/2/project/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query, typeKey: typeKey, categoryId: categoryId, action: action, expand: expand, status: status, properties: properties, propertyQuery: propertyQuery};
        path = path + getPathForQueryParam(queryParam);
        PageBeanProject response = check self.clientEp-> get(path, targetType = PageBeanProject);
        return response;
    }
    remote isolated function getAllProjectTypes() returns ProjectTypeArr|error {
        string  path = string `/rest/api/2/project/type`;
        ProjectTypeArr response = check self.clientEp-> get(path, targetType = ProjectTypeArr);
        return response;
    }
    remote isolated function getAllAccessibleProjectTypes() returns ProjectTypeArr|error {
        string  path = string `/rest/api/2/project/type/accessible`;
        ProjectTypeArr response = check self.clientEp-> get(path, targetType = ProjectTypeArr);
        return response;
    }
    remote isolated function getProjectTypeByKey(string projectTypeKey) returns ProjectType|error {
        string  path = string `/rest/api/2/project/type/${projectTypeKey}`;
        ProjectType response = check self.clientEp-> get(path, targetType = ProjectType);
        return response;
    }
    remote isolated function getAccessibleProjectTypeByKey(string projectTypeKey) returns ProjectType|error {
        string  path = string `/rest/api/2/project/type/${projectTypeKey}/accessible`;
        ProjectType response = check self.clientEp-> get(path, targetType = ProjectType);
        return response;
    }
    remote isolated function getProject(string projectIdOrKey, string? expand, string[]? properties) returns Project|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {expand: expand, properties: properties};
        path = path + getPathForQueryParam(queryParam);
        Project response = check self.clientEp-> get(path, targetType = Project);
        return response;
    }
    remote isolated function updateProject(string projectIdOrKey, string? expand, ProjectInputBean payload) returns Project|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Project response = check self.clientEp->put(path, request, targetType=Project);
        return response;
    }
    remote isolated function deleteProject(string projectIdOrKey, boolean? enableUndo) returns http:Response | error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}`;
        map<anydata> queryParam = {enableUndo: enableUndo};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function archiveProject(string projectIdOrKey) returns json|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/archive`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> post(path, request, targetType = json);
        return response;
    }
    remote isolated function updateProjectAvatar(string projectIdOrKey, Avatar payload) returns json|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/avatar`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteProjectAvatar(string projectIdOrKey, int id) returns http:Response | error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/avatar/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function createProjectAvatar(string projectIdOrKey, int? x, int? y, int? size, json payload) returns Avatar|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/avatar2`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        Avatar response = check self.clientEp->post(path, request, targetType=Avatar);
        return response;
    }
    remote isolated function getAllProjectAvatars(string projectIdOrKey) returns ProjectAvatars|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/avatars`;
        ProjectAvatars response = check self.clientEp-> get(path, targetType = ProjectAvatars);
        return response;
    }
    remote isolated function getProjectComponentsPaginated(string projectIdOrKey, int? startAt, int? maxResults, string? orderBy, string? query) returns PageBeanComponentWithIssueCount|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/component`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query};
        path = path + getPathForQueryParam(queryParam);
        PageBeanComponentWithIssueCount response = check self.clientEp-> get(path, targetType = PageBeanComponentWithIssueCount);
        return response;
    }
    remote isolated function getProjectComponents(string projectIdOrKey) returns ComponentArr|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/components`;
        ComponentArr response = check self.clientEp-> get(path, targetType = ComponentArr);
        return response;
    }
    remote isolated function deleteProjectAsynchronously(string projectIdOrKey) returns TaskProgressBeanObject|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/delete`;
        http:Request request = new;
        //TODO: Update the request as needed;
        TaskProgressBeanObject response = check self.clientEp-> post(path, request, targetType = TaskProgressBeanObject);
        return response;
    }
    remote isolated function getFeaturesForProject(string projectIdOrKey) returns ProjectFeaturesResponse|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/features`;
        ProjectFeaturesResponse response = check self.clientEp-> get(path, targetType = ProjectFeaturesResponse);
        return response;
    }
    remote isolated function toggleFeatureForProject(string projectIdOrKey, string featureKey, ProjectFeatureToggleRequest payload) returns ProjectFeaturesResponse|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/features/${featureKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectFeaturesResponse response = check self.clientEp->put(path, request, targetType=ProjectFeaturesResponse);
        return response;
    }
    remote isolated function getProjectPropertyKeys(string projectIdOrKey) returns PropertyKeys|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getProjectProperty(string projectIdOrKey, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setProjectProperty(string projectIdOrKey, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteProjectProperty(string projectIdOrKey, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function restore(string projectIdOrKey) returns Project|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/restore`;
        http:Request request = new;
        //TODO: Update the request as needed;
        Project response = check self.clientEp-> post(path, request, targetType = Project);
        return response;
    }
    remote isolated function getProjectRoles(string projectIdOrKey) returns record|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/role`;
        record response = check self.clientEp-> get(path, targetType = record);
        return response;
    }
    remote isolated function getProjectRole(string projectIdOrKey, int id) returns ProjectRole|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        ProjectRole response = check self.clientEp-> get(path, targetType = ProjectRole);
        return response;
    }
    remote isolated function setActors(string projectIdOrKey, int id, ProjectRoleActorsUpdateBean payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->put(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function addActorUsers(string projectIdOrKey, int id, ActorsMap payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->post(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function deleteActor(string projectIdOrKey, int id, string? user, string? 'group) returns http:Response | error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/role/${id}`;
        map<anydata> queryParam = {user: user, 'group: 'group};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getProjectRoleDetails(string projectIdOrKey, boolean? currentMember, boolean? excludeConnectAddons) returns ProjectRoleDetailsArr|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/roledetails`;
        map<anydata> queryParam = {currentMember: currentMember, excludeConnectAddons: excludeConnectAddons};
        path = path + getPathForQueryParam(queryParam);
        ProjectRoleDetailsArr response = check self.clientEp-> get(path, targetType = ProjectRoleDetailsArr);
        return response;
    }
    remote isolated function getAllStatuses(string projectIdOrKey) returns IssueTypeWithStatusArr|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/statuses`;
        IssueTypeWithStatusArr response = check self.clientEp-> get(path, targetType = IssueTypeWithStatusArr);
        return response;
    }
    remote isolated function updateProjectType(string projectIdOrKey, string newProjectTypeKey) returns Project|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/type/${newProjectTypeKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        Project response = check self.clientEp-> put(path, request, targetType = Project);
        return response;
    }
    remote isolated function getProjectVersionsPaginated(string projectIdOrKey, int? startAt, int? maxResults, string? orderBy, string? query, string? status, string? expand) returns PageBeanVersion|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/version`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, orderBy: orderBy, query: query, status: status, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanVersion response = check self.clientEp-> get(path, targetType = PageBeanVersion);
        return response;
    }
    remote isolated function getProjectVersions(string projectIdOrKey, string? expand) returns VersionArr|error {
        string  path = string `/rest/api/2/project/${projectIdOrKey}/versions`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        VersionArr response = check self.clientEp-> get(path, targetType = VersionArr);
        return response;
    }
    remote isolated function getProjectEmail(int projectId) returns ProjectEmailAddress|error {
        string  path = string `/rest/api/2/project/${projectId}/email`;
        ProjectEmailAddress response = check self.clientEp-> get(path, targetType = ProjectEmailAddress);
        return response;
    }
    remote isolated function updateProjectEmail(int projectId, ProjectEmailAddress payload) returns json|error {
        string  path = string `/rest/api/2/project/${projectId}/email`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getHierarchy(int projectId) returns ProjectIssueTypeHierarchy|error {
        string  path = string `/rest/api/2/project/${projectId}/hierarchy`;
        ProjectIssueTypeHierarchy response = check self.clientEp-> get(path, targetType = ProjectIssueTypeHierarchy);
        return response;
    }
    remote isolated function getProjectIssueSecurityScheme(string projectKeyOrId) returns SecurityScheme|error {
        string  path = string `/rest/api/2/project/${projectKeyOrId}/issuesecuritylevelscheme`;
        SecurityScheme response = check self.clientEp-> get(path, targetType = SecurityScheme);
        return response;
    }
    remote isolated function getNotificationSchemeForProject(string projectKeyOrId, string? expand) returns NotificationScheme|error {
        string  path = string `/rest/api/2/project/${projectKeyOrId}/notificationscheme`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        NotificationScheme response = check self.clientEp-> get(path, targetType = NotificationScheme);
        return response;
    }
    remote isolated function getAssignedPermissionScheme(string projectKeyOrId, string? expand) returns PermissionScheme|error {
        string  path = string `/rest/api/2/project/${projectKeyOrId}/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PermissionScheme response = check self.clientEp-> get(path, targetType = PermissionScheme);
        return response;
    }
    remote isolated function assignPermissionScheme(string projectKeyOrId, string? expand, IdBean payload) returns PermissionScheme|error {
        string  path = string `/rest/api/2/project/${projectKeyOrId}/permissionscheme`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        PermissionScheme response = check self.clientEp->put(path, request, targetType=PermissionScheme);
        return response;
    }
    remote isolated function getSecurityLevelsForProject(string projectKeyOrId) returns ProjectIssueSecurityLevels|error {
        string  path = string `/rest/api/2/project/${projectKeyOrId}/securitylevel`;
        ProjectIssueSecurityLevels response = check self.clientEp-> get(path, targetType = ProjectIssueSecurityLevels);
        return response;
    }
    remote isolated function getAllProjectCategories() returns ProjectCategoryArr|error {
        string  path = string `/rest/api/2/projectCategory`;
        ProjectCategoryArr response = check self.clientEp-> get(path, targetType = ProjectCategoryArr);
        return response;
    }
    remote isolated function createProjectCategory(ProjectCategory payload) returns ProjectCategory|error {
        string  path = string `/rest/api/2/projectCategory`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectCategory response = check self.clientEp->post(path, request, targetType=ProjectCategory);
        return response;
    }
    remote isolated function getProjectCategoryById(int id) returns ProjectCategory|error {
        string  path = string `/rest/api/2/projectCategory/${id}`;
        ProjectCategory response = check self.clientEp-> get(path, targetType = ProjectCategory);
        return response;
    }
    remote isolated function updateProjectCategory(int id, ProjectCategory payload) returns UpdatedProjectCategory|error {
        string  path = string `/rest/api/2/projectCategory/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        UpdatedProjectCategory response = check self.clientEp->put(path, request, targetType=UpdatedProjectCategory);
        return response;
    }
    remote isolated function removeProjectCategory(int id) returns http:Response | error {
        string  path = string `/rest/api/2/projectCategory/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function validateProjectKey(string? 'key) returns ErrorCollection|error {
        string  path = string `/rest/api/2/projectvalidate/key`;
        map<anydata> queryParam = {'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        ErrorCollection response = check self.clientEp-> get(path, targetType = ErrorCollection);
        return response;
    }
    remote isolated function getValidProjectKey(string? 'key) returns string|error {
        string  path = string `/rest/api/2/projectvalidate/validProjectKey`;
        map<anydata> queryParam = {'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        string response = check self.clientEp-> get(path, targetType = string);
        return response;
    }
    remote isolated function getValidProjectName(string name) returns string|error {
        string  path = string `/rest/api/2/projectvalidate/validProjectName`;
        map<anydata> queryParam = {name: name};
        path = path + getPathForQueryParam(queryParam);
        string response = check self.clientEp-> get(path, targetType = string);
        return response;
    }
    remote isolated function getResolutions() returns ResolutionArr|error {
        string  path = string `/rest/api/2/resolution`;
        ResolutionArr response = check self.clientEp-> get(path, targetType = ResolutionArr);
        return response;
    }
    remote isolated function getResolution(string id) returns Resolution|error {
        string  path = string `/rest/api/2/resolution/${id}`;
        Resolution response = check self.clientEp-> get(path, targetType = Resolution);
        return response;
    }
    remote isolated function getAllProjectRoles() returns ProjectRoleArr|error {
        string  path = string `/rest/api/2/role`;
        ProjectRoleArr response = check self.clientEp-> get(path, targetType = ProjectRoleArr);
        return response;
    }
    remote isolated function createProjectRole(CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/role`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->post(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function getProjectRoleById(int id) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}`;
        ProjectRole response = check self.clientEp-> get(path, targetType = ProjectRole);
        return response;
    }
    remote isolated function fullyUpdateProjectRole(int id, CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->put(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function partialUpdateProjectRole(int id, CreateUpdateRoleRequestBean payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->post(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function deleteProjectRole(int id, int? swap) returns http:Response | error {
        string  path = string `/rest/api/2/role/${id}`;
        map<anydata> queryParam = {swap: swap};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getProjectRoleActorsForRole(int id) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}/actors`;
        ProjectRole response = check self.clientEp-> get(path, targetType = ProjectRole);
        return response;
    }
    remote isolated function addProjectRoleActorsToRole(int id, ActorInputBean payload) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}/actors`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ProjectRole response = check self.clientEp->post(path, request, targetType=ProjectRole);
        return response;
    }
    remote isolated function deleteProjectRoleActorsFromRole(int id, string? user, string? 'group) returns ProjectRole|error {
        string  path = string `/rest/api/2/role/${id}/actors`;
        map<anydata> queryParam = {user: user, 'group: 'group};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        ProjectRole response = check self.clientEp-> delete(path, request, targetType = ProjectRole);
        return response;
    }
    remote isolated function getScreens(int? startAt, int? maxResults, int[]? id) returns PageBeanScreen|error {
        string  path = string `/rest/api/2/screens`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        path = path + getPathForQueryParam(queryParam);
        PageBeanScreen response = check self.clientEp-> get(path, targetType = PageBeanScreen);
        return response;
    }
    remote isolated function createScreen(ScreenDetails payload) returns Screen|error {
        string  path = string `/rest/api/2/screens`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Screen response = check self.clientEp->post(path, request, targetType=Screen);
        return response;
    }
    remote isolated function addFieldToDefaultScreen(string fieldId) returns json|error {
        string  path = string `/rest/api/2/screens/addToDefault/${fieldId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> post(path, request, targetType = json);
        return response;
    }
    remote isolated function updateScreen(int screenId, UpdateScreenDetails payload) returns Screen|error {
        string  path = string `/rest/api/2/screens/${screenId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Screen response = check self.clientEp->put(path, request, targetType=Screen);
        return response;
    }
    remote isolated function deleteScreen(int screenId) returns http:Response | error {
        string  path = string `/rest/api/2/screens/${screenId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getAvailableScreenFields(int screenId) returns ScreenableFieldArr|error {
        string  path = string `/rest/api/2/screens/${screenId}/availableFields`;
        ScreenableFieldArr response = check self.clientEp-> get(path, targetType = ScreenableFieldArr);
        return response;
    }
    remote isolated function getAllScreenTabs(int screenId, string? projectKey) returns ScreenableTabArr|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs`;
        map<anydata> queryParam = {projectKey: projectKey};
        path = path + getPathForQueryParam(queryParam);
        ScreenableTabArr response = check self.clientEp-> get(path, targetType = ScreenableTabArr);
        return response;
    }
    remote isolated function addScreenTab(int screenId, ScreenableTab payload) returns ScreenableTab|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ScreenableTab response = check self.clientEp->post(path, request, targetType=ScreenableTab);
        return response;
    }
    remote isolated function renameScreenTab(int screenId, int tabId, ScreenableTab payload) returns ScreenableTab|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ScreenableTab response = check self.clientEp->put(path, request, targetType=ScreenableTab);
        return response;
    }
    remote isolated function deleteScreenTab(int screenId, int tabId) returns http:Response | error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getAllScreenTabFields(int screenId, int tabId, string? projectKey) returns ScreenableFieldArr|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields`;
        map<anydata> queryParam = {projectKey: projectKey};
        path = path + getPathForQueryParam(queryParam);
        ScreenableFieldArr response = check self.clientEp-> get(path, targetType = ScreenableFieldArr);
        return response;
    }
    remote isolated function addScreenTabField(int screenId, int tabId, AddFieldBean payload) returns ScreenableField|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ScreenableField response = check self.clientEp->post(path, request, targetType=ScreenableField);
        return response;
    }
    remote isolated function removeScreenTabField(int screenId, int tabId, string id) returns http:Response | error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function moveScreenTabField(int screenId, int tabId, string id, MoveFieldBean payload) returns json|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/fields/${id}/move`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function moveScreenTab(int screenId, int tabId, int pos) returns json|error {
        string  path = string `/rest/api/2/screens/${screenId}/tabs/${tabId}/move/${pos}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> post(path, request, targetType = json);
        return response;
    }
    remote isolated function getScreenSchemes(int? startAt, int? maxResults, int[]? id) returns PageBeanScreenScheme|error {
        string  path = string `/rest/api/2/screenscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, id: id};
        path = path + getPathForQueryParam(queryParam);
        PageBeanScreenScheme response = check self.clientEp-> get(path, targetType = PageBeanScreenScheme);
        return response;
    }
    remote isolated function createScreenScheme(ScreenSchemeDetails payload) returns ScreenSchemeId|error {
        string  path = string `/rest/api/2/screenscheme`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ScreenSchemeId response = check self.clientEp->post(path, request, targetType=ScreenSchemeId);
        return response;
    }
    remote isolated function updateScreenScheme(string screenSchemeId, UpdateScreenSchemeDetails payload) returns json|error {
        string  path = string `/rest/api/2/screenscheme/${screenSchemeId}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteScreenScheme(string screenSchemeId) returns http:Response | error {
        string  path = string `/rest/api/2/screenscheme/${screenSchemeId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function searchForIssuesUsingJql(string? jql, int? startAt, int? maxResults, string? validateQuery, string[]? fields, string? expand, string[]? properties, boolean? fieldsByKeys) returns SearchResults|error {
        string  path = string `/rest/api/2/search`;
        map<anydata> queryParam = {jql: jql, startAt: startAt, maxResults: maxResults, validateQuery: validateQuery, fields: fields, expand: expand, properties: properties, fieldsByKeys: fieldsByKeys};
        path = path + getPathForQueryParam(queryParam);
        SearchResults response = check self.clientEp-> get(path, targetType = SearchResults);
        return response;
    }
    remote isolated function searchForIssuesUsingJqlPost(SearchRequestBean payload) returns SearchResults|error {
        string  path = string `/rest/api/2/search`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        SearchResults response = check self.clientEp->post(path, request, targetType=SearchResults);
        return response;
    }
    remote isolated function getIssueSecurityLevel(string id) returns SecurityLevel|error {
        string  path = string `/rest/api/2/securitylevel/${id}`;
        SecurityLevel response = check self.clientEp-> get(path, targetType = SecurityLevel);
        return response;
    }
    remote isolated function getServerInfo() returns ServerInformation|error {
        string  path = string `/rest/api/2/serverInfo`;
        ServerInformation response = check self.clientEp-> get(path, targetType = ServerInformation);
        return response;
    }
    remote isolated function getIssueNavigatorDefaultColumns() returns ColumnItemArr|error {
        string  path = string `/rest/api/2/settings/columns`;
        ColumnItemArr response = check self.clientEp-> get(path, targetType = ColumnItemArr);
        return response;
    }
    remote isolated function setIssueNavigatorDefaultColumns([] payload) returns json|error {
        string  path = string `/rest/api/2/settings/columns`;
        http:Request request = new;
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getStatuses() returns StatusDetailsArr|error {
        string  path = string `/rest/api/2/status`;
        StatusDetailsArr response = check self.clientEp-> get(path, targetType = StatusDetailsArr);
        return response;
    }
    remote isolated function getStatus(string idOrName) returns StatusDetails|error {
        string  path = string `/rest/api/2/status/${idOrName}`;
        StatusDetails response = check self.clientEp-> get(path, targetType = StatusDetails);
        return response;
    }
    remote isolated function getStatusCategories() returns StatusCategoryArr|error {
        string  path = string `/rest/api/2/statuscategory`;
        StatusCategoryArr response = check self.clientEp-> get(path, targetType = StatusCategoryArr);
        return response;
    }
    remote isolated function getStatusCategory(string idOrKey) returns StatusCategory|error {
        string  path = string `/rest/api/2/statuscategory/${idOrKey}`;
        StatusCategory response = check self.clientEp-> get(path, targetType = StatusCategory);
        return response;
    }
    remote isolated function getTask(string taskId) returns TaskProgressBeanObject|error {
        string  path = string `/rest/api/2/task/${taskId}`;
        TaskProgressBeanObject response = check self.clientEp-> get(path, targetType = TaskProgressBeanObject);
        return response;
    }
    remote isolated function cancelTask(string taskId) returns json|error {
        string  path = string `/rest/api/2/task/${taskId}/cancel`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> post(path, request, targetType = json);
        return response;
    }
    remote isolated function getAvatars(string 'type, string entityId) returns Avatars|error {
        string  path = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${entityId}`;
        Avatars response = check self.clientEp-> get(path, targetType = Avatars);
        return response;
    }
    remote isolated function storeAvatar(string 'type, string entityId, int? x, int? y, int size, json payload) returns Avatar|error {
        string  path = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${entityId}`;
        map<anydata> queryParam = {x: x, y: y, size: size};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        Avatar response = check self.clientEp->post(path, request, targetType=Avatar);
        return response;
    }
    remote isolated function deleteAvatar(string 'type, string owningObjectId, int id) returns http:Response | error {
        string  path = string `/rest/api/2/universal_avatar/'type/${'type}/owner/${owningObjectId}/avatar/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getUser(string? accountId, string? username, string? 'key, string? expand) returns User|error {
        string  path = string `/rest/api/2/user`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        User response = check self.clientEp-> get(path, targetType = User);
        return response;
    }
    remote isolated function createUser(UserWriteBean payload) returns User|error {
        string  path = string `/rest/api/2/user`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        User response = check self.clientEp->post(path, request, targetType=User);
        return response;
    }
    remote isolated function removeUser(string accountId, string? username, string? 'key) returns http:Response | error {
        string  path = string `/rest/api/2/user`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function findBulkAssignableUsers(string? query, string? username, string? accountId, string projectKeys, int? startAt, int? maxResults) returns UserArr|error {
        string  path = string `/rest/api/2/user/assignable/multiProjectSearch`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, projectKeys: projectKeys, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function findAssignableUsers(string? query, string? sessionId, string? username, string? accountId, string? project, string? issueKey, int? startAt, int? maxResults, int? actionDescriptorId, boolean? recommend) returns UserArr|error {
        string  path = string `/rest/api/2/user/assignable/search`;
        map<anydata> queryParam = {query: query, sessionId: sessionId, username: username, accountId: accountId, project: project, issueKey: issueKey, startAt: startAt, maxResults: maxResults, actionDescriptorId: actionDescriptorId, recommend: recommend};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function bulkGetUsers(int? startAt, int? maxResults, string[]? username, string[]? 'key, string[] accountId) returns PageBeanUser|error {
        string  path = string `/rest/api/2/user/bulk`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, username: username, 'key: 'key, accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        PageBeanUser response = check self.clientEp-> get(path, targetType = PageBeanUser);
        return response;
    }
    remote isolated function bulkGetUsersMigration(int? startAt, int? maxResults, string[]? username, string[]? 'key) returns UserMigrationBeanArr|error {
        string  path = string `/rest/api/2/user/bulk/migration`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, username: username, 'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        UserMigrationBeanArr response = check self.clientEp-> get(path, targetType = UserMigrationBeanArr);
        return response;
    }
    remote isolated function getUserDefaultColumns(string? accountId, string? username) returns ColumnItemArr|error {
        string  path = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId, username: username};
        path = path + getPathForQueryParam(queryParam);
        ColumnItemArr response = check self.clientEp-> get(path, targetType = ColumnItemArr);
        return response;
    }
    remote isolated function setUserColumns(string? accountId, [] payload) returns json|error {
        string  path = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function resetUserColumns(string? accountId, string? username) returns http:Response | error {
        string  path = string `/rest/api/2/user/columns`;
        map<anydata> queryParam = {accountId: accountId, username: username};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getUserEmail(string accountId) returns UnrestrictedUserEmail|error {
        string  path = string `/rest/api/2/user/email`;
        map<anydata> queryParam = {accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        UnrestrictedUserEmail response = check self.clientEp-> get(path, targetType = UnrestrictedUserEmail);
        return response;
    }
    remote isolated function getUserEmailBulk(string[] accountId) returns UnrestrictedUserEmail|error {
        string  path = string `/rest/api/2/user/email/bulk`;
        map<anydata> queryParam = {accountId: accountId};
        path = path + getPathForQueryParam(queryParam);
        UnrestrictedUserEmail response = check self.clientEp-> get(path, targetType = UnrestrictedUserEmail);
        return response;
    }
    remote isolated function getUserGroups(string accountId, string? username, string? 'key) returns GroupNameArr|error {
        string  path = string `/rest/api/2/user/groups`;
        map<anydata> queryParam = {accountId: accountId, username: username, 'key: 'key};
        path = path + getPathForQueryParam(queryParam);
        GroupNameArr response = check self.clientEp-> get(path, targetType = GroupNameArr);
        return response;
    }
    remote isolated function findUsersWithAllPermissions(string? query, string? username, string? accountId, string permissions, string? issueKey, string? projectKey, int? startAt, int? maxResults) returns UserArr|error {
        string  path = string `/rest/api/2/user/permission/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, permissions: permissions, issueKey: issueKey, projectKey: projectKey, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function findUsersForPicker(string query, int? maxResults, boolean? showAvatar, string[]? exclude, string[]? excludeAccountIds, string? avatarSize, boolean? excludeConnectUsers) returns FoundUsers|error {
        string  path = string `/rest/api/2/user/picker`;
        map<anydata> queryParam = {query: query, maxResults: maxResults, showAvatar: showAvatar, exclude: exclude, excludeAccountIds: excludeAccountIds, avatarSize: avatarSize, excludeConnectUsers: excludeConnectUsers};
        path = path + getPathForQueryParam(queryParam);
        FoundUsers response = check self.clientEp-> get(path, targetType = FoundUsers);
        return response;
    }
    remote isolated function getUserPropertyKeys(string? accountId, string? userKey, string? username) returns PropertyKeys|error {
        string  path = string `/rest/api/2/user/properties`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        path = path + getPathForQueryParam(queryParam);
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function getUserProperty(string? accountId, string? userKey, string? username, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        path = path + getPathForQueryParam(queryParam);
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function setUserProperty(string? accountId, string? userKey, string? username, string propertyKey, json payload) returns json|error {
        string  path = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function deleteUserProperty(string? accountId, string? userKey, string? username, string propertyKey) returns http:Response | error {
        string  path = string `/rest/api/2/user/properties/${propertyKey}`;
        map<anydata> queryParam = {accountId: accountId, userKey: userKey, username: username};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function findUsers(string? query, string? username, string? accountId, int? startAt, int? maxResults, string? property) returns UserArr|error {
        string  path = string `/rest/api/2/user/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, startAt: startAt, maxResults: maxResults, property: property};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function findUsersByQuery(string query, int? startAt, int? maxResults) returns PageBeanUser|error {
        string  path = string `/rest/api/2/user/search/query`;
        map<anydata> queryParam = {query: query, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanUser response = check self.clientEp-> get(path, targetType = PageBeanUser);
        return response;
    }
    remote isolated function findUserKeysByQuery(string query, int? startAt, int? maxResults) returns PageBeanUserKey|error {
        string  path = string `/rest/api/2/user/search/query/key`;
        map<anydata> queryParam = {query: query, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanUserKey response = check self.clientEp-> get(path, targetType = PageBeanUserKey);
        return response;
    }
    remote isolated function findUsersWithBrowsePermission(string? query, string? username, string? accountId, string? issueKey, string? projectKey, int? startAt, int? maxResults) returns UserArr|error {
        string  path = string `/rest/api/2/user/viewissue/search`;
        map<anydata> queryParam = {query: query, username: username, accountId: accountId, issueKey: issueKey, projectKey: projectKey, startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function getAllUsersDefault(int? startAt, int? maxResults) returns UserArr|error {
        string  path = string `/rest/api/2/users`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function getAllUsers(int? startAt, int? maxResults) returns UserArr|error {
        string  path = string `/rest/api/2/users/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        UserArr response = check self.clientEp-> get(path, targetType = UserArr);
        return response;
    }
    remote isolated function createVersion(Version payload) returns Version|error {
        string  path = string `/rest/api/2/version`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Version response = check self.clientEp->post(path, request, targetType=Version);
        return response;
    }
    remote isolated function getVersion(string id, string? expand) returns Version|error {
        string  path = string `/rest/api/2/version/${id}`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        Version response = check self.clientEp-> get(path, targetType = Version);
        return response;
    }
    remote isolated function updateVersion(string id, Version payload) returns Version|error {
        string  path = string `/rest/api/2/version/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Version response = check self.clientEp->put(path, request, targetType=Version);
        return response;
    }
    remote isolated function deleteVersion(string id, string? moveFixIssuesTo, string? moveAffectedIssuesTo) returns http:Response | error {
        string  path = string `/rest/api/2/version/${id}`;
        map<anydata> queryParam = {moveFixIssuesTo: moveFixIssuesTo, moveAffectedIssuesTo: moveAffectedIssuesTo};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function mergeVersions(string id, string moveIssuesTo) returns json|error {
        string  path = string `/rest/api/2/version/${id}/mergeto/${moveIssuesTo}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        json response = check self.clientEp-> put(path, request, targetType = json);
        return response;
    }
    remote isolated function moveVersion(string id, VersionMoveBean payload) returns Version|error {
        string  path = string `/rest/api/2/version/${id}/move`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        Version response = check self.clientEp->post(path, request, targetType=Version);
        return response;
    }
    remote isolated function getVersionRelatedIssues(string id) returns VersionIssueCounts|error {
        string  path = string `/rest/api/2/version/${id}/relatedIssueCounts`;
        VersionIssueCounts response = check self.clientEp-> get(path, targetType = VersionIssueCounts);
        return response;
    }
    remote isolated function deleteAndReplaceVersion(string id, DeleteAndReplaceVersionBean payload) returns json|error {
        string  path = string `/rest/api/2/version/${id}/removeAndSwap`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->post(path, request, targetType=json);
        return response;
    }
    remote isolated function getVersionUnresolvedIssues(string id) returns VersionUnresolvedIssuesCount|error {
        string  path = string `/rest/api/2/version/${id}/unresolvedIssueCount`;
        VersionUnresolvedIssuesCount response = check self.clientEp-> get(path, targetType = VersionUnresolvedIssuesCount);
        return response;
    }
    remote isolated function getDynamicWebhooksForApp(int? startAt, int? maxResults) returns PageBeanWebhook|error {
        string  path = string `/rest/api/2/webhook`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanWebhook response = check self.clientEp-> get(path, targetType = PageBeanWebhook);
        return response;
    }
    remote isolated function registerDynamicWebhooks(WebhookRegistrationDetails payload) returns ContainerForRegisteredWebhooks|error {
        string  path = string `/rest/api/2/webhook`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        ContainerForRegisteredWebhooks response = check self.clientEp->post(path, request, targetType=ContainerForRegisteredWebhooks);
        return response;
    }
    remote isolated function deleteWebhookById() returns http:Response | error {
        string  path = string `/rest/api/2/webhook`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getFailedWebhooks(int? maxResults, int? after) returns FailedWebhooks|error {
        string  path = string `/rest/api/2/webhook/failed`;
        map<anydata> queryParam = {maxResults: maxResults, after: after};
        path = path + getPathForQueryParam(queryParam);
        FailedWebhooks response = check self.clientEp-> get(path, targetType = FailedWebhooks);
        return response;
    }
    remote isolated function refreshWebhooks(ContainerForWebhookIDs payload) returns WebhooksExpirationDate|error {
        string  path = string `/rest/api/2/webhook/refresh`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WebhooksExpirationDate response = check self.clientEp->put(path, request, targetType=WebhooksExpirationDate);
        return response;
    }
    remote isolated function getAllWorkflows(string? workflowName) returns DeprecatedWorkflowArr|error {
        string  path = string `/rest/api/2/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        path = path + getPathForQueryParam(queryParam);
        DeprecatedWorkflowArr response = check self.clientEp-> get(path, targetType = DeprecatedWorkflowArr);
        return response;
    }
    remote isolated function createWorkflow(CreateWorkflowDetails payload) returns WorkflowIDs|error {
        string  path = string `/rest/api/2/workflow`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowIDs response = check self.clientEp->post(path, request, targetType=WorkflowIDs);
        return response;
    }
    remote isolated function getWorkflowTransitionRuleConfigurations(int? startAt, int? maxResults, string[] types, string[]? keys, string? expand) returns PageBeanWorkflowTransitionRules|error {
        string  path = string `/rest/api/2/workflow/rule/config`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, types: types, keys: keys, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanWorkflowTransitionRules response = check self.clientEp-> get(path, targetType = PageBeanWorkflowTransitionRules);
        return response;
    }
    remote isolated function updateWorkflowTransitionRuleConfigurations(WorkflowTransitionRulesUpdate payload) returns WorkflowTransitionRulesUpdateErrors|error {
        string  path = string `/rest/api/2/workflow/rule/config`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowTransitionRulesUpdateErrors response = check self.clientEp->put(path, request, targetType=WorkflowTransitionRulesUpdateErrors);
        return response;
    }
    remote isolated function getWorkflowsPaginated(int? startAt, int? maxResults, string[]? workflowName, string? expand) returns PageBeanWorkflow|error {
        string  path = string `/rest/api/2/workflow/search`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults, workflowName: workflowName, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        PageBeanWorkflow response = check self.clientEp-> get(path, targetType = PageBeanWorkflow);
        return response;
    }
    remote isolated function getWorkflowTransitionProperties(int transitionId, boolean? includeReservedKeys, string? 'key, string workflowName, string? workflowMode) returns WorkflowTransitionProperty|error {
        string  path = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {includeReservedKeys: includeReservedKeys, 'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        path = path + getPathForQueryParam(queryParam);
        WorkflowTransitionProperty response = check self.clientEp-> get(path, targetType = WorkflowTransitionProperty);
        return response;
    }
    remote isolated function updateWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode, WorkflowTransitionProperty payload) returns WorkflowTransitionProperty|error {
        string  path = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowTransitionProperty response = check self.clientEp->put(path, request, targetType=WorkflowTransitionProperty);
        return response;
    }
    remote isolated function createWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode, WorkflowTransitionProperty payload) returns WorkflowTransitionProperty|error {
        string  path = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowTransitionProperty response = check self.clientEp->post(path, request, targetType=WorkflowTransitionProperty);
        return response;
    }
    remote isolated function deleteWorkflowTransitionProperty(int transitionId, string 'key, string workflowName, string? workflowMode) returns http:Response | error {
        string  path = string `/rest/api/2/workflow/transitions/${transitionId}/properties`;
        map<anydata> queryParam = {'key: 'key, workflowName: workflowName, workflowMode: workflowMode};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function deleteInactiveWorkflow(string entityId) returns http:Response | error {
        string  path = string `/rest/api/2/workflow/${entityId}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getAllWorkflowSchemes(int? startAt, int? maxResults) returns PageBeanWorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme`;
        map<anydata> queryParam = {startAt: startAt, maxResults: maxResults};
        path = path + getPathForQueryParam(queryParam);
        PageBeanWorkflowScheme response = check self.clientEp-> get(path, targetType = PageBeanWorkflowScheme);
        return response;
    }
    remote isolated function createWorkflowScheme(WorkflowScheme payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->post(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function getWorkflowSchemeProjectAssociations(int[] projectId) returns ContainerOfWorkflowSchemeAssociations|error {
        string  path = string `/rest/api/2/workflowscheme/project`;
        map<anydata> queryParam = {projectId: projectId};
        path = path + getPathForQueryParam(queryParam);
        ContainerOfWorkflowSchemeAssociations response = check self.clientEp-> get(path, targetType = ContainerOfWorkflowSchemeAssociations);
        return response;
    }
    remote isolated function assignSchemeToProject(WorkflowSchemeProjectAssociation payload) returns json|error {
        string  path = string `/rest/api/2/workflowscheme/project`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        json response = check self.clientEp->put(path, request, targetType=json);
        return response;
    }
    remote isolated function getWorkflowScheme(int id, boolean? returnDraftIfExists) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        path = path + getPathForQueryParam(queryParam);
        WorkflowScheme response = check self.clientEp-> get(path, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function updateWorkflowScheme(int id, WorkflowScheme payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteWorkflowScheme(int id) returns http:Response | error {
        string  path = string `/rest/api/2/workflowscheme/${id}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function createWorkflowSchemeDraftFromParent(int id) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/createdraft`;
        http:Request request = new;
        //TODO: Update the request as needed;
        WorkflowScheme response = check self.clientEp-> post(path, request, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function getDefaultWorkflow(int id, boolean? returnDraftIfExists) returns DefaultWorkflow|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/default`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        path = path + getPathForQueryParam(queryParam);
        DefaultWorkflow response = check self.clientEp-> get(path, targetType = DefaultWorkflow);
        return response;
    }
    remote isolated function updateDefaultWorkflow(int id, DefaultWorkflow payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/default`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteDefaultWorkflow(int id, boolean? updateDraftIfNeeded) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/default`;
        map<anydata> queryParam = {updateDraftIfNeeded: updateDraftIfNeeded};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        WorkflowScheme response = check self.clientEp-> delete(path, request, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function getWorkflowSchemeDraft(int id) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft`;
        WorkflowScheme response = check self.clientEp-> get(path, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function updateWorkflowSchemeDraft(int id, WorkflowScheme payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteWorkflowSchemeDraft(int id) returns http:Response | error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getDraftDefaultWorkflow(int id) returns DefaultWorkflow|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        DefaultWorkflow response = check self.clientEp-> get(path, targetType = DefaultWorkflow);
        return response;
    }
    remote isolated function updateDraftDefaultWorkflow(int id, DefaultWorkflow payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteDraftDefaultWorkflow(int id) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/default`;
        http:Request request = new;
        //TODO: Update the request as needed;
        WorkflowScheme response = check self.clientEp-> delete(path, request, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function getWorkflowSchemeDraftIssueType(int id, string issueType) returns IssueTypeWorkflowMapping|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        IssueTypeWorkflowMapping response = check self.clientEp-> get(path, targetType = IssueTypeWorkflowMapping);
        return response;
    }
    remote isolated function setWorkflowSchemeDraftIssueType(int id, string issueType, IssueTypeWorkflowMapping payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteWorkflowSchemeDraftIssueType(int id, string issueType) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/issuetype/${issueType}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        WorkflowScheme response = check self.clientEp-> delete(path, request, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function getDraftWorkflow(int id, string? workflowName) returns IssueTypesWorkflowMapping|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        path = path + getPathForQueryParam(queryParam);
        IssueTypesWorkflowMapping response = check self.clientEp-> get(path, targetType = IssueTypesWorkflowMapping);
        return response;
    }
    remote isolated function updateDraftWorkflowMapping(int id, string workflowName, IssueTypesWorkflowMapping payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteDraftWorkflowMapping(int id, string workflowName) returns http:Response | error {
        string  path = string `/rest/api/2/workflowscheme/${id}/draft/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getWorkflowSchemeIssueType(int id, string issueType, boolean? returnDraftIfExists) returns IssueTypeWorkflowMapping|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        map<anydata> queryParam = {returnDraftIfExists: returnDraftIfExists};
        path = path + getPathForQueryParam(queryParam);
        IssueTypeWorkflowMapping response = check self.clientEp-> get(path, targetType = IssueTypeWorkflowMapping);
        return response;
    }
    remote isolated function setWorkflowSchemeIssueType(int id, string issueType, IssueTypeWorkflowMapping payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteWorkflowSchemeIssueType(int id, string issueType, boolean? updateDraftIfNeeded) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/issuetype/${issueType}`;
        map<anydata> queryParam = {updateDraftIfNeeded: updateDraftIfNeeded};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        WorkflowScheme response = check self.clientEp-> delete(path, request, targetType = WorkflowScheme);
        return response;
    }
    remote isolated function getWorkflow(int id, string? workflowName, boolean? returnDraftIfExists) returns IssueTypesWorkflowMapping|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName, returnDraftIfExists: returnDraftIfExists};
        path = path + getPathForQueryParam(queryParam);
        IssueTypesWorkflowMapping response = check self.clientEp-> get(path, targetType = IssueTypesWorkflowMapping);
        return response;
    }
    remote isolated function updateWorkflowMapping(int id, string workflowName, IssueTypesWorkflowMapping payload) returns WorkflowScheme|error {
        string  path = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorkflowScheme response = check self.clientEp->put(path, request, targetType=WorkflowScheme);
        return response;
    }
    remote isolated function deleteWorkflowMapping(int id, string workflowName, boolean? updateDraftIfNeeded) returns http:Response | error {
        string  path = string `/rest/api/2/workflowscheme/${id}/workflow`;
        map<anydata> queryParam = {workflowName: workflowName, updateDraftIfNeeded: updateDraftIfNeeded};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function getIdsOfWorklogsDeletedSince(int? since) returns ChangedWorklogs|error {
        string  path = string `/rest/api/2/worklog/deleted`;
        map<anydata> queryParam = {since: since};
        path = path + getPathForQueryParam(queryParam);
        ChangedWorklogs response = check self.clientEp-> get(path, targetType = ChangedWorklogs);
        return response;
    }
    remote isolated function getWorklogsForIds(string? expand, WorklogIdsRequestBean payload) returns WorklogArr|error {
        string  path = string `/rest/api/2/worklog/list`;
        map<anydata> queryParam = {expand: expand};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        WorklogArr response = check self.clientEp->post(path, request, targetType=WorklogArr);
        return response;
    }
    remote isolated function getIdsOfWorklogsModifiedSince(int? since, string? expand) returns ChangedWorklogs|error {
        string  path = string `/rest/api/2/worklog/updated`;
        map<anydata> queryParam = {since: since, expand: expand};
        path = path + getPathForQueryParam(queryParam);
        ChangedWorklogs response = check self.clientEp-> get(path, targetType = ChangedWorklogs);
        return response;
    }
    remote isolated function 'AddonPropertiesResource\.getAddonProperties\_get(string addonKey) returns PropertyKeys|error {
        string  path = string `/rest/atlassian-connect/1/addons/${addonKey}/properties`;
        PropertyKeys response = check self.clientEp-> get(path, targetType = PropertyKeys);
        return response;
    }
    remote isolated function 'AddonPropertiesResource\.getAddonProperty\_get(string addonKey, string propertyKey) returns EntityProperty|error {
        string  path = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        EntityProperty response = check self.clientEp-> get(path, targetType = EntityProperty);
        return response;
    }
    remote isolated function 'AddonPropertiesResource\.putAddonProperty\_put(string addonKey, string propertyKey, json payload) returns OperationMessage|error {
        string  path = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        OperationMessage response = check self.clientEp->put(path, request, targetType=OperationMessage);
        return response;
    }
    remote isolated function 'AddonPropertiesResource\.deleteAddonProperty\_delete(string addonKey, string propertyKey) returns http:Response | error {
        string  path = string `/rest/atlassian-connect/1/addons/${addonKey}/properties/${propertyKey}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
    remote isolated function 'DynamicModulesResource\.getModules\_get() returns ConnectModules|error {
        string  path = string `/rest/atlassian-connect/1/app/module/dynamic`;
        ConnectModules response = check self.clientEp-> get(path, targetType = ConnectModules);
        return response;
    }
    remote isolated function 'DynamicModulesResource\.registerModules\_post(ConnectModules payload) returns http:Response | error {
        string  path = string `/rest/atlassian-connect/1/app/module/dynamic`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response  response = check self.clientEp->post(path, request, targetType=http:Response );
        return response;
    }
    remote isolated function 'DynamicModulesResource\.removeModules\_delete(string[]? moduleKey) returns http:Response | error {
        string  path = string `/rest/atlassian-connect/1/app/module/dynamic`;
        map<anydata> queryParam = {moduleKey: moduleKey};
        path = path + getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(path, request, targetType = http:Response );
        return response;
    }
}

isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string {
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
                string updateV =  checkpanic url:encode(value, "UTF-8");
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
