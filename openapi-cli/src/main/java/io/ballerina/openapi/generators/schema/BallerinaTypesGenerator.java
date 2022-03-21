/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.TypeGenerator;
import io.ballerina.openapi.generators.schema.model.GeneratorMetaData;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;
import static io.ballerina.openapi.generators.GeneratorUtils.isValidSchemaName;

/**
 * This class wraps the {@link Schema} from openapi models inorder to overcome complications
 * while populating syntax tree.
 */
public class BallerinaTypesGenerator {
    private List<TypeDefinitionNode> typeDefinitionNodeList;

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     */
    public BallerinaTypesGenerator(OpenAPI openAPI, boolean isNullable) {
        GeneratorMetaData.createInstance(openAPI, isNullable);
        this.typeDefinitionNodeList = new LinkedList<>();
    }

    /**
     * This public constructor is used to generate record and other relevant data type when the absent of the nullable
     * flag in the openapi command.
     *
     * @param openAPI OAS definition
     */
    public BallerinaTypesGenerator(OpenAPI openAPI) {
        this(openAPI, false);
        GeneratorMetaData.createInstance(openAPI, false);
    }

    /**
     * Set the typeDefinitionNodeList.
     */
    public void setTypeDefinitionNodeList(List<TypeDefinitionNode> typeDefinitionNodeList) {
        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }

    /**
     * Generate syntaxTree for component schema.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
        if (openAPI.getComponents() != null) {
            // Create typeDefinitionNode
            Components components = openAPI.getComponents();
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
                    String schemaKey = schema.getKey().trim();
                    if (isValidSchemaName(schemaKey)) {
                        List<Node> schemaDoc = new ArrayList<>();
                        typeDefinitionNodeList.add(getTypeDefinitionNode
                                (schema.getValue(), schemaKey, schemaDoc));
                    }
                }
            }
        }
        //Create imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                typeDefinitionNodeList.toArray(new TypeDefinitionNode[typeDefinitionNodeList.size()]));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Create Type Definition Node for a given OpenAPI schema.
     *
     * @param schema   OpenAPI schema
     * @param typeName IdentifierToken of the name of the type
     * @return {@link TypeDefinitionNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public TypeDefinitionNode getTypeDefinitionNode(Schema schema, String typeName, List<Node> schemaDocs)
            throws BallerinaOpenApiException {
        IdentifierToken typeNameToken = AbstractNodeFactory.createIdentifierToken(getValidName(
                typeName.trim(), true));
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, getValidName(
                typeName.trim(), true));
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
        TypeGeneratorUtils.getRecordDocs(schemaDocs, schema, typeAnnotations);
        return typeGenerator.generateTypeDefinitionNode(typeNameToken,
                schemaDocs, typeAnnotations);
    }
}
