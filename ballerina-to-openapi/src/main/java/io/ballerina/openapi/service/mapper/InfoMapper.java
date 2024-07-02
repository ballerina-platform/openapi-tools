/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.openapi.service.mapper.model.OpenAPIInfo;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.CONTACT_NAME;
import static io.ballerina.openapi.service.mapper.Constants.CONTACT_URL;
import static io.ballerina.openapi.service.mapper.Constants.CONTRACT;
import static io.ballerina.openapi.service.mapper.Constants.DESCRIPTION;
import static io.ballerina.openapi.service.mapper.Constants.EMAIL;
import static io.ballerina.openapi.service.mapper.Constants.LICENSE_NAME;
import static io.ballerina.openapi.service.mapper.Constants.LICENSE_URL;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_ANNOTATION;
import static io.ballerina.openapi.service.mapper.Constants.SLASH;
import static io.ballerina.openapi.service.mapper.Constants.SPECIAL_CHAR_REGEX;
import static io.ballerina.openapi.service.mapper.Constants.TERMS_OF_SERVICE;
import static io.ballerina.openapi.service.mapper.Constants.TITLE;
import static io.ballerina.openapi.service.mapper.Constants.VERSION;

/**
 * This {@link InfoMapper} class represents the info mapper.
 * This class provides functionalities for mapping the Ballerina service information to OpenAPI info section.
 *
 * @since 1.0.0
 */
public final class InfoMapper {

    /**
     * This function is for completing the OpenAPI info section with package details and annotation details.
     * First check the given service node has metadata with annotation details with `openapi:serviceInfo`,
     * if it is there, then {@link #parseServiceInfoAnnotationAttachmentDetails(List, AnnotationNode, Path)}
     * function extracts the annotation details and store details in {@code OpenAPIInfo} model using
     * {@link #updateOpenAPIInfoModel(SeparatedNodeList)} function. If the annotation contains the valid contract
     * path then we complete given OpenAPI specification using annotation details. if not we create new OpenAPI
     * specification and fill openAPI info sections.
     * If the annotation is not in the given service, then we filled the OpenAPI specification info section using
     * package details and title with service base path.
     * After completing these two process we normalized the OpenAPI specification by checking all the info
     * details are completed, if in case not completed, we complete empty fields with default values.
     *
     * @param serviceNode   Service node for relevant service.
     * @param semanticModel Semantic model for relevant project.
     * @param openapiFileName OpenAPI generated file name.
     * @param ballerinaFilePath Ballerina file path.
     * @return {@code OASResult}
     */
    static OASResult getOASResultWithInfo(ServiceNode serviceNode, SemanticModel semanticModel, String openapiFileName,
                                          Path ballerinaFilePath) {
        Optional<MetadataNode> metadata = serviceNode.metadata();
        List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
        OpenAPI openAPI = new OpenAPI();
        String currentServiceName = serviceNode.absoluteResourcePath();
        // 01. Set openAPI inFo section wit package details
        String version = getContractVersion(serviceNode, semanticModel);
        if (metadata.isPresent() && !metadata.get().annotations().isEmpty()) {
            MetadataNode metadataNode = metadata.get();
            NodeList<AnnotationNode> annotations = metadataNode.annotations();
            for (AnnotationNode annotation : annotations) {
                if (annotation.annotReference().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode ref = (QualifiedNameReferenceNode) annotation.annotReference();
                    String annotationName = ref.modulePrefix().text() + ":" + ref.identifier().text();
                    if (annotationName.equals(OPENAPI_ANNOTATION)) {
                        OASResult oasResult = parseServiceInfoAnnotationAttachmentDetails(diagnostics, annotation,
                                ballerinaFilePath);
                        return normalizeInfoSection(openapiFileName, currentServiceName, version, oasResult);
                    } else {
                        setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
                    }
                } else {
                    setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
                }
            }
        } else {
            setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
        }

        return new OASResult(openAPI, diagnostics);
    }

    /**
     * Generates openAPI Info section when the service base path is absent or `/`.
     */
    private static void setInfoDetailsIfServiceNameAbsent(String openapiFileName, OpenAPI openAPI,
                                                          String currentServiceName, String version) {
        if (currentServiceName.equals(SLASH) || currentServiceName.isBlank()) {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(openapiFileName)));
        } else {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(currentServiceName)));
        }
    }

    // Finalize the openAPI info section
    private static OASResult normalizeInfoSection(String openapiFileName, String currentServiceName, String version,
                                                  OASResult oasResult) {
        if (oasResult.getOpenAPI().isPresent()) {
            OpenAPI openAPI = oasResult.getOpenAPI().get();
            if (openAPI.getInfo() == null) {
                String title = normalizeTitle(currentServiceName);
                if (currentServiceName.equals(SLASH)) {
                    title = normalizeTitle(openapiFileName);
                }
                openAPI.setInfo(new Info().title(title).version(version));
            } else {
                if (openAPI.getInfo().getTitle() == null) {
                    openAPI.getInfo().setTitle(normalizeTitle(currentServiceName));
                } else if (openAPI.getInfo().getTitle() != null && openAPI.getInfo().getTitle().equals(SLASH)) {
                    openAPI.getInfo().setTitle(normalizeTitle(openapiFileName));
                } else if (openAPI.getInfo().getTitle().isBlank()) {
                    openAPI.getInfo().setTitle(normalizeTitle(currentServiceName));
                } else if (openAPI.getInfo().getTitle() == null && currentServiceName.equals(SLASH)) {
                    openAPI.getInfo().setTitle(normalizeTitle(openapiFileName));
                }
                if (openAPI.getInfo().getVersion() == null || openAPI.getInfo().getVersion().isBlank()) {
                    openAPI.getInfo().setVersion(version);
                }
            }
            return new OASResult(openAPI, oasResult.getDiagnostics());
        } else {
            return oasResult;
        }
    }

    // Set contract version by default using package version.
    private static String getContractVersion(ServiceNode serviceDefinition, SemanticModel semanticModel) {
        Optional<Symbol> symbol = serviceDefinition.getSymbol(semanticModel);
        String version = "1.0.0";
        if (symbol.isPresent()) {
            Symbol serviceSymbol = symbol.get();
            Optional<ModuleSymbol> module = serviceSymbol.getModule();
            if (module.isPresent()) {
                version = module.get().id().version();
            }
        }
        return version;
    }

    private static String normalizeTitle(String title) {
        if (title != null) {
            String[] splits = (title.replaceFirst(SLASH, "")).split(SPECIAL_CHAR_REGEX);
            StringBuilder stringBuilder = new StringBuilder();
            if (splits.length > 1) {
                for (String piece : splits) {
                    if (piece.isBlank()) {
                        continue;
                    }
                    stringBuilder.append(piece.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(piece.substring(1));
                    stringBuilder.append(" ");
                }
                title = stringBuilder.toString().trim();
            } else if (splits.length == 1 && !splits[0].isBlank()) {
                stringBuilder.append(splits[0].substring(0, 1).toUpperCase(Locale.ENGLISH))
                        .append(splits[0].substring(1));
                title = stringBuilder.toString().trim();
            }
            return title;
        }
        return null;
    }

    // Set annotation details  for info section.
    private static OASResult parseServiceInfoAnnotationAttachmentDetails(List<OpenAPIMapperDiagnostic> diagnostics,
                                                                         AnnotationNode annotation,
                                                                         Path ballerinaFilePath) {

        Location location = annotation.location();
        OpenAPI openAPI = new OpenAPI();
        Optional<MappingConstructorExpressionNode> svcInfoAnnotationValue = annotation.annotValue();

        if (svcInfoAnnotationValue.isEmpty()) {
            return new OASResult(openAPI, diagnostics);
        }

        SeparatedNodeList<MappingFieldNode> fields = svcInfoAnnotationValue.get().fields();
        if (fields.isEmpty()) {
            return new OASResult(openAPI, diagnostics);
        }

        OpenAPIInfo openAPIInfo = updateOpenAPIInfoModel(fields);

        // Check for contract path and existing Ballerina file path
        if (openAPIInfo.getContractPath().isPresent() && ballerinaFilePath != null) {
            return updateExistingContractOpenAPI(diagnostics, location, openAPIInfo, ballerinaFilePath);
        }
        populateOASInfo(openAPI, openAPIInfo);
        return new OASResult(openAPI, diagnostics);
    }

    private static void populateOASInfo(OpenAPI openAPI, OpenAPIInfo openAPIInfo) {
        // Populate OpenAPI Info object
        Info info = openAPI.getInfo() == null ? new Info() : openAPI.getInfo();
        openAPIInfo.getTitle().ifPresent(title -> info.setTitle(normalizeTitle(title)));
        openAPIInfo.getVersion().ifPresent(info::setVersion);
        openAPIInfo.getEmail().ifPresent(email -> {
            Contact contact = (info.getContact() != null) ? info.getContact() : new Contact();
            contact.setEmail(email);
            info.setContact(contact);
        });
        openAPIInfo.getContactName().ifPresent(name -> {
            Contact contact = (info.getContact() != null) ? info.getContact() : new Contact();
            contact.setName(name);
            info.setContact(contact);
        });
        openAPIInfo.getContactURL().ifPresent(url -> {
            Contact contact = (info.getContact() != null) ? info.getContact() : new Contact();
            contact.setUrl(url);
            info.setContact(contact);
        });
        openAPIInfo.getLicenseURL().ifPresent(url -> {
            License license = (info.getLicense() != null) ? info.getLicense() : new License();
            license.setUrl(url);
            info.setLicense(license);
        });
        openAPIInfo.getLicenseName().ifPresent(name -> {
            License license = (info.getLicense() != null) ? info.getLicense() : new License();
            license.setName(name);
            info.setLicense(license);
        });
        openAPIInfo.getTermsOfService().ifPresent(info::setTermsOfService);
        openAPIInfo.getDescription().ifPresent(info::setDescription);
        openAPI.setInfo(info);
    }


    private static OASResult updateExistingContractOpenAPI(List<OpenAPIMapperDiagnostic> diagnostics,
                                                           Location location, OpenAPIInfo openAPIInfo,
                                                           Path ballerinaFilePath) {

        OASResult oasResult = resolveContractPath(diagnostics, location, openAPIInfo, ballerinaFilePath);
        Optional<OpenAPI> contract = oasResult.getOpenAPI();
        if (contract.isEmpty()) {
            return oasResult;
        }
        OpenAPI openAPI = contract.get();
        diagnostics.addAll(oasResult.getDiagnostics());
        populateOASInfo(openAPI, openAPIInfo);
        return new OASResult(openAPI, diagnostics);
    }

    private static OpenAPIInfo updateOpenAPIInfoModel(SeparatedNodeList<MappingFieldNode> fields) {
        OpenAPIInfo.OpenAPIInfoBuilder infoBuilder = new OpenAPIInfo.OpenAPIInfoBuilder();
        for (MappingFieldNode field: fields) {
            String fieldName = ((SpecificFieldNode) field).fieldName().toString().trim();
            Optional<ExpressionNode> value = ((SpecificFieldNode) field).valueExpr();
            String fieldValue;
            if (value.isPresent()) {
                ExpressionNode expressionNode = value.get();
                if (!expressionNode.toString().trim().isBlank()) {
                    fieldValue = expressionNode.toString().trim().replaceAll("\"", "");
                    if (fieldValue.isBlank()) {
                        continue;
                    }
                    switch (fieldName) {
                        case CONTRACT -> infoBuilder.contractPath(fieldValue);
                        case TITLE -> infoBuilder.title(fieldValue);
                        case VERSION -> infoBuilder.version(fieldValue);
                        case EMAIL -> infoBuilder.email(fieldValue);
                        case DESCRIPTION -> infoBuilder.description(fieldValue);
                        case CONTACT_NAME -> infoBuilder.contactName(fieldValue);
                        case CONTACT_URL -> infoBuilder.contactURL(fieldValue);
                        case TERMS_OF_SERVICE -> infoBuilder.termsOfService(fieldValue);
                        case LICENSE_NAME -> infoBuilder.licenseName(fieldValue);
                        case LICENSE_URL -> infoBuilder.licenseURL(fieldValue);
                        default -> {
                        }
                    }
                }
            }
        }
        return infoBuilder.build();
    }

    private static OASResult resolveContractPath(List<OpenAPIMapperDiagnostic> diagnostics, Location location,
                                                 OpenAPIInfo openAPIInfo, Path ballerinaFilePath) {
        OASResult oasResult;
        OpenAPI openAPI = null;
        Path openapiPath = Paths.get(openAPIInfo.getContractPath().get().replaceAll("\"", "").trim());
        Path relativePath = null;
        if (openapiPath.toString().isBlank()) {
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_110, location);
            diagnostics.add(diagnostic);
        } else {
            Path path = Paths.get(openapiPath.toString());
            if (path.isAbsolute()) {
                relativePath = path;
            } else {
                File file = new File(ballerinaFilePath.toString());
                File parentFolder = new File(file.getParent());
                File openapiContract = new File(parentFolder, openapiPath.toString());
                try {
                    relativePath = Paths.get(openapiContract.getCanonicalPath());
                } catch (IOException e) {
                    ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_108,
                            location, e.toString());
                    diagnostics.add(diagnostic);
                }
            }
        }
        if (relativePath != null && Files.exists(relativePath)) {
            oasResult = MapperCommonUtils.parseOpenAPIFile(relativePath.toString());
            if (oasResult.getOpenAPI().isPresent()) {
                openAPI = oasResult.getOpenAPI().get();
            }
            diagnostics.addAll(oasResult.getDiagnostics());
        }
        return new OASResult(openAPI, diagnostics);
    }
}
