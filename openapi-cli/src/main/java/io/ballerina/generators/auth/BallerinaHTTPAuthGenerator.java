package io.ballerina.generators.auth;



import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.generators.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.*;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.generators.GeneratorConstants.BallerinaAuthMap;
import static io.ballerina.generators.GeneratorUtils.escapeIdentifier;

/**
 * This class use for generating ballerina client's authentication related codes file according to given yaml file.
 */
public class BallerinaHTTPAuthGenerator {
    private static final List<String> headerApiKeyNameList = new ArrayList<>();
    private static final List<String> queryApiKeyNameList = new ArrayList<>();
    private static boolean isAPIKey = false;
    private static boolean isHttpOROAuth = false;

    public static final String API_KEY = "apikey";
    public static final String API_KEY_CONFIG = "ApiKeysConfig";
    public static final String API_KEY_CONFIG_PARAM = "apiKeyConfig";
    public static final String API_KEY_CONFIG_RECORD_FIELD = "apiKeys";
    public static final String API_KEY_MAP = "map<string|string[]>";
    public static final String AUTH_CONFIG_FILED_NAME = "authConfig";
    public static final String BASIC = "basic";
    public static final String BEARER = "bearer";
    public static final String CONFIG_RECORD_ARG = "clientConfig";
    public static final String CONFIG_RECORD_NAME = "ClientConfig";
    public static final String HTTP = "http";
    public static final String OAUTH2 = "oauth2";
    public static final String SSL_FIELD_NAME = "secureSocketConfig";

    /**
     * This static method is used to generate the Config record for the relavant authentication type.
     */
    public static TypeDefinitionNode getConfigRecord (OpenAPI openAPI) {
        if (openAPI.getComponents().getSecuritySchemes() != null) {
            List<Node> recordFieldList = addItemstoRecordFieldList(openAPI);
            if (recordFieldList != null) {
                Token typeName;
                if (isAPIKey) {
                    typeName = AbstractNodeFactory.createIdentifierToken(API_KEY_CONFIG);
                } else {
                    typeName = AbstractNodeFactory.createIdentifierToken(CONFIG_RECORD_NAME);
                }
                Token visibilityQualifierNode = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.PUBLIC);
                Token typeKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.TYPE);
                Token recordKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.RECORD);
                Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_BRACE);
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLOSE_BRACE);
                RecordTypeDescriptorNode recordTypeDescriptorNode =
                        NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                fieldNodes, null, bodyEndDelimiter);
                Token semicolon = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.SEMICOLON);
                return NodeFactory.createTypeDefinitionNode(null,
                        visibilityQualifierNode, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
            }
        }
        return null;
    }
    /**
     * This static method is used to generate the instance variable for api key map - map<string|string> apiKeys.
     */
    public static List<ObjectFieldNode> getApiKeyMapInstanceVariable() { // return ObjectFieldNode
        List<ObjectFieldNode> apiKeyFieldNodeList = new ArrayList<>();
        if (isAPIKey) {
            NodeList<Token> qualifierList = createEmptyNodeList();
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(API_KEY_MAP));
            IdentifierToken fieldName = createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD);
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
            apiKeyFieldNodeList.add(createObjectFieldNode(metadataNode, null,
                    qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN)));
        }
        return apiKeyFieldNodeList;
    }
    /**
     * This static method is used to generate the config parameters of the client class init method.
     */
    public static List<Node> getConfigParamForClassInit() {
        List<Node> parameters  = new ArrayList<>();
        IdentifierToken equalToken = createIdentifierToken(GeneratorConstants.EQUAL);
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        if (isHttpOROAuth) {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(CONFIG_RECORD_NAME));
            IdentifierToken paramName = createIdentifierToken(CONFIG_RECORD_ARG);
            RequiredParameterNode authConfig = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameters.add(authConfig);
        } else {
            if (isAPIKey) {
                BuiltinSimpleNameReferenceNode apiKeyConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(API_KEY_CONFIG));
                IdentifierToken apiKeyConfigParamName = createIdentifierToken(API_KEY_CONFIG_PARAM);
                RequiredParameterNode apiKeyConfigParamNode = createRequiredParameterNode(annotationNodes,
                        apiKeyConfigTypeName, apiKeyConfigParamName);
                parameters.add(apiKeyConfigParamNode);
                parameters.add(createToken(COMMA_TOKEN));
            }
            BuiltinSimpleNameReferenceNode httpClientonfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken("http:ClientConfiguration"));
            IdentifierToken httpClientConfig = createIdentifierToken(CONFIG_RECORD_ARG);
            BasicLiteralNode emptyexpression = createBasicLiteralNode(null, createIdentifierToken(" {}"));
            DefaultableParameterNode defaultHTTPConfig = createDefaultableParameterNode(annotationNodes,
                    httpClientonfigTypeName,
                    httpClientConfig, equalToken, emptyexpression);
            parameters.add(defaultHTTPConfig);
        }
        return parameters;
    }
    /**
     * This static method is used to generate assignement nodes for api key map assignment.
     */
    public static List<AssignmentStatementNode> getApiKeyAssignemntNodes () {
        List<AssignmentStatementNode> assignmentStatementNodes = new ArrayList<>();
        if (isAPIKey) {
            FieldAccessExpressionNode varRefApiKey = createFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD)));
            // todo add specific node
            SimpleNameReferenceNode expr = createSimpleNameReferenceNode
                    (createIdentifierToken(API_KEY_CONFIG_PARAM +
                            GeneratorConstants.PERIOD + API_KEY_CONFIG_RECORD_FIELD));
            AssignmentStatementNode assignmentStatementNode = createAssignmentStatementNode(varRefApiKey,
                    createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));
            assignmentStatementNodes.add(assignmentStatementNode);
        }

        return assignmentStatementNodes;
    }
    public static VariableDeclarationNode getSecureSocketInitNode () {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        TypeDescriptorNode typeName = createOptionalTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("http:ClientSecureSocket")), createToken(QUESTION_MARK_TOKEN));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken(SSL_FIELD_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeName,
                bindingPattern);
        ExpressionNode configRecordSSLField = createOptionalFieldAccessExpressionNode(createSimpleNameReferenceNode(createIdentifierToken(CONFIG_RECORD_ARG)),
                createIdentifierToken("?."),
                createSimpleNameReferenceNode(createIdentifierToken(SSL_FIELD_NAME)));
        return createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), configRecordSSLField,
                createToken(SEMICOLON_TOKEN));

    }
    /**
     * This static method is used to generate http:client initialization node.
     */
    public static VariableDeclarationNode getClientInitializationNode () {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("http:Client"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        //Expression node
        Token newKeyWord = createIdentifierToken("new");
        Token openParenArg = createIdentifierToken("(");
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.SERVICE_URL)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(",");
        PositionalArgumentNode positionalArgumentNode02;
        if (isHttpOROAuth) {
            // try to create specific node
            positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                    createIdentifierToken(String.format("{ auth: %s.%s, secureSocket: %s }", CONFIG_RECORD_ARG,
                            AUTH_CONFIG_FILED_NAME, SSL_FIELD_NAME))));
            argumentsList.add(comma1);
            argumentsList.add(positionalArgumentNode02);
        } else {
            positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                    createIdentifierToken(CONFIG_RECORD_ARG)));
            argumentsList.add(comma1);
            argumentsList.add(positionalArgumentNode02);
        }
        /**
         * This static method is used to generate http:client initialization node.
         */
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(newKeyWord,
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        return createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer,
                createToken(SEMICOLON_TOKEN));
    }
    /**
     * This static method is used to return the query api keys.
     */
    public static List<String> getQueryApiKeyNameList () {
        return queryApiKeyNameList;
    }
    /**
     * This static method is used to return the header api keys.
     */
    public static List<String> getHeaderApiKeyNameList () {
        return headerApiKeyNameList;
    }
    /**
     * This static method is used generate fields of the config record.
     */
    private static List<Node> addItemstoRecordFieldList (OpenAPI openAPI) {
        List<Node> httpRecordFieldNodes = new ArrayList<>();
        List<Node> apiKeyRecordFieldNodes = new ArrayList<>();

        Token semicolonToken = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.SEMICOLON);
        Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
        List<String> httpFieldTypeNameList = getConfigRecordFieldTypeNames (securitySchemeMap);
        if (!httpFieldTypeNameList.isEmpty())  {
            // add auth config
            Token authFieldType = AbstractNodeFactory.createIdentifierToken(buildConfigRecordFieldTypes
                    (httpFieldTypeNameList).toString());
            IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                    AUTH_CONFIG_FILED_NAME));
            TypeDescriptorNode fieldTypeNode = createBuiltinSimpleNameReferenceNode(null, authFieldType);
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeNode, authFieldName, null, semicolonToken);
            httpRecordFieldNodes.add(recordFieldNode);
            // add socket config
            IdentifierToken sslFieldNameNode = AbstractNodeFactory.createIdentifierToken(SSL_FIELD_NAME);
            TypeDescriptorNode sslfieldTypeNode = createBuiltinSimpleNameReferenceNode(null, AbstractNodeFactory.createIdentifierToken("http:ClientSecureSocket"));
            RecordFieldNode sslRecordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    sslfieldTypeNode, sslFieldNameNode, createToken(QUESTION_MARK_TOKEN), semicolonToken);
            httpRecordFieldNodes.add(sslRecordFieldNode);
            return httpRecordFieldNodes;

        } else if (isAPIKey) {

            Token apiKeyMap = AbstractNodeFactory.createIdentifierToken(API_KEY_MAP);
            IdentifierToken apiKeyMapFieldName = AbstractNodeFactory.createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD);
            TypeDescriptorNode fieldTypeNode = createBuiltinSimpleNameReferenceNode(null, apiKeyMap);
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeNode, apiKeyMapFieldName, null, semicolonToken);
            apiKeyRecordFieldNodes.add(recordFieldNode);
            return apiKeyRecordFieldNodes;

        } else {
            // todo OpenIDConnect, Cookie
            return null;
        }
    }
    /**
     * This static method is used traverse through the security schemas.
     */
    private static List<String> getConfigRecordFieldTypeNames(Map<String, SecurityScheme> securitySchemeMap) {
        List<String> httpFieldTypeNameList = new ArrayList<>();
        // return string
        queryApiKeyNameList.clear();
        headerApiKeyNameList.clear();
        for (Map.Entry<String, SecurityScheme> securitySchemeEntry : securitySchemeMap.entrySet()) {
            SecurityScheme schemaValue = securitySchemeEntry.getValue();
            if (schemaValue != null && schemaValue.getType() != null) {
                String schemaType = schemaValue.getType().name().toLowerCase(Locale.getDefault());
                switch (schemaType) {
                    case HTTP:
                        isHttpOROAuth = true;
                        String scheme = schemaValue.getScheme();
                        if (scheme.equals(BASIC)) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.BASIC.getValue());
                        } else if (scheme.equals(BEARER)) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.BEARER.getValue());
                        }
                        break;
                    case OAUTH2:
                        isHttpOROAuth = true;
                        if (schemaValue.getFlows().getClientCredentials() != null) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.CLIENT_CREDENTIAL.getValue());
                        }
                        if (schemaValue.getFlows().getPassword() != null) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.PASSWORD.getValue());
                        }
                        if (schemaValue.getFlows().getAuthorizationCode() != null) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.BEARER.getValue());
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.REFRESH_TOKEN.getValue());
                        }
                        if (schemaValue.getFlows().getImplicit() != null) {
                            addHttpFieldTypeName(httpFieldTypeNameList, BallerinaAuthMap.BEARER.getValue());
                        }
                        break;
                    case API_KEY:
                        isAPIKey = true;
                        String apiKeyType = schemaValue.getIn().name().toLowerCase(Locale.getDefault());
                        switch (apiKeyType) {
                            case "query":
                                queryApiKeyNameList.add(schemaValue.getName());
                                break;
                            case "header":
                                headerApiKeyNameList.add(schemaValue.getName());
                                break;
                        }
                }
            }
        }
        return httpFieldTypeNameList;
    }
    /**
     * This static method is used update auth type list avoiding duplicates.
     */
    private static void addHttpFieldTypeName (List<String> httpFieldTypeNameList, String authType) {
        if (!httpFieldTypeNameList.contains(authType)) {
            httpFieldTypeNameList.add(authType);
        }
    }
    /**
     * This static method is used concat the config record authConfig field type.
     */
    private static StringBuilder buildConfigRecordFieldTypes(List<String> fieldTypeList) {
        StringBuilder httpAuthFieldTypes = new StringBuilder();
        if (!fieldTypeList.isEmpty()) {
            for (String fieldType: fieldTypeList) {
                if (httpAuthFieldTypes.length() != 0) {
                    httpAuthFieldTypes.append("|").append(fieldType);
                } else {
                    httpAuthFieldTypes.append(fieldType);
                }
            }
        }
        return httpAuthFieldTypes;
    }
}
