package io.ballerina.openapi.service.mapper.constraint;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.service.mapper.constraint.model.ConstraintAnnotation;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.DATE_CONSTRAINT_ANNOTATION;
import static io.ballerina.openapi.service.mapper.Constants.REGEX_INTERPOLATION_PATTERN;

public class ConstraintMapper {

    private final OpenAPI openAPI;
    private final ModuleMemberVisitor moduleMemberVisitor;
    private final List<OpenAPIMapperDiagnostic> diagnostics;

    public ConstraintMapper(OpenAPI openAPI, ModuleMemberVisitor moduleMemberVisitor,
                            List<OpenAPIMapperDiagnostic> diagnostics) {
        this.openAPI = openAPI;
        this.moduleMemberVisitor = moduleMemberVisitor;
        this.diagnostics = diagnostics;
    }

    public void addMapping() {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            return;
        }
        Map<String, Schema> schemas = components.getSchemas();
        for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
            TypeDefinitionNode typeDefinitionNode = moduleMemberVisitor.getTypeDefinitionNode(schemaEntry.getKey());
            if (Objects.isNull(typeDefinitionNode)) {
                continue;
            }
            if (typeDefinitionNode.metadata().isPresent()) {
                ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder =
                        new ConstraintAnnotation.ConstraintAnnotationBuilder();
                extractedConstraintAnnotation(typeDefinitionNode.metadata().get(), constraintBuilder);
                ConstraintAnnotation constraintAnnot = constraintBuilder.build();
                setConstraintValueToSchema(constraintAnnot, schemaEntry.getValue());
            }
            if (schemaEntry.getValue() instanceof ObjectSchema objectSchema) {
                setObjectConstraintValuesToSchema(objectSchema, typeDefinitionNode);
            }
        }
    }

    /**
     * This util is used to set the constraint values for relevant schema field.
     */
    private void setConstraintValueToSchema(ConstraintAnnotation constraintAnnot, Schema properties) {
        try {
            //Ballerina currently supports only Int, Number (Float, Decimal), String & Array constraints,
            //with plans to extend constraint support in the future.
            if (properties instanceof ArraySchema arraySchema) {
                setArrayConstraintValuesToSchema(constraintAnnot, arraySchema);
            } else if (properties instanceof StringSchema stringSchema) {
                setStringConstraintValuesToSchema(constraintAnnot, stringSchema);
            } else if (properties instanceof IntegerSchema integerSchema) {
                setIntegerConstraintValuesToSchema(constraintAnnot, integerSchema);
            } else if (properties instanceof NumberSchema numberSchema) {
                setNumberConstraintValuesToSchema(constraintAnnot, numberSchema);
            }
        } catch (ParseException parseException) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_114;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                    error.getDescription(), null, parseException.getMessage());
            diagnostics.add(diagnostic);
        }
    }

    /**
     * This util is used to set the integer constraint values for relevant schema field.
     */
    private void setIntegerConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, IntegerSchema properties) {
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        if (constraintAnnot.getMinValue().isPresent()) {
            minimum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMinValue().get()));
        } else if (constraintAnnot.getMinValueExclusive().isPresent()) {
            minimum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMinValueExclusive().get()));
            properties.setExclusiveMinimum(true);
        }

        if (constraintAnnot.getMaxValue().isPresent()) {
            maximum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMaxValue().get()));
        } else if (constraintAnnot.getMaxValueExclusive().isPresent()) {
            maximum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMaxValueExclusive().get()));
            properties.setExclusiveMaximum(true);
        }
        properties.setMinimum(minimum);
        properties.setMaximum(maximum);
    }

    /**
     * This util is used to set the number (float, double) constraint values for relevant schema field.
     */
    private void setNumberConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, NumberSchema properties)
            throws ParseException {
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        if (constraintAnnot.getMinValue().isPresent()) {
            minimum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMinValue().get()).doubleValue()));
        } else if (constraintAnnot.getMinValueExclusive().isPresent()) {
            minimum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMinValueExclusive().get()).doubleValue()));
            properties.setExclusiveMinimum(true);
        }

        if (constraintAnnot.getMaxValue().isPresent()) {
            maximum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMaxValue().get()).doubleValue()));
        } else if (constraintAnnot.getMaxValueExclusive().isPresent()) {
            maximum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMaxValueExclusive().get()).doubleValue()));
            properties.setExclusiveMaximum(true);
        }
        properties.setMinimum(minimum);
        properties.setMaximum(maximum);
    }

    /**
     * This util is used to set the string constraint values for relevant schema field.
     */
    private void setStringConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, StringSchema properties) {
        if (constraintAnnot.getLength().isPresent()) {
            properties.setMinLength(Integer.valueOf(constraintAnnot.getLength().get()));
            properties.setMaxLength(Integer.valueOf(constraintAnnot.getLength().get()));
        } else {
            properties.setMaxLength(constraintAnnot.getMaxLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMaxLength().get()) : null);
            properties.setMinLength(constraintAnnot.getMinLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMinLength().get()) : null);
        }

        if (constraintAnnot.getPattern().isPresent()) {
            String regexPattern = constraintAnnot.getPattern().get();
            properties.setPattern(regexPattern);
        }
    }

    /**
     * This util is used to set the array constraint values for relevant schema field.
     */
    private void setArrayConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, ArraySchema properties) {
        if (constraintAnnot.getLength().isPresent()) {
            properties.setMinItems(Integer.valueOf(constraintAnnot.getLength().get()));
            properties.setMaxItems(Integer.valueOf(constraintAnnot.getLength().get()));
        } else {
            properties.setMaxItems(constraintAnnot.getMaxLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMaxLength().get()) : null);
            properties.setMinItems(constraintAnnot.getMinLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMinLength().get()) : null);
        }
    }

    private void setObjectConstraintValuesToSchema(ObjectSchema typeSchema, TypeDefinitionNode typeDefinitionNode) {
        if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
            NodeList<Node> fieldNodes = ((RecordTypeDescriptorNode) typeDefinitionNode.typeDescriptor()).fields();
            for (Node fieldNode : fieldNodes) {
                RecordFieldNode recordFieldNode = (RecordFieldNode) fieldNode;
                MetadataNode metadata = recordFieldNode.metadata().orElse(null);
                String fieldName = recordFieldNode.fieldName().toString().trim();
                if (Objects.isNull(metadata)) {
                    continue;
                }
                ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder =
                        new ConstraintAnnotation.ConstraintAnnotationBuilder();
                extractedConstraintAnnotation(metadata, constraintBuilder);
                ConstraintAnnotation constraintAnnot = constraintBuilder.build();
                Map<String, Schema> properties = typeSchema.getProperties();
                if (properties.containsKey(fieldName)) {
                    setConstraintValueToSchema(constraintAnnot, properties.get(fieldName));
                }
            }
        }
    }

    /**
     * This util is used to extract the annotation values in `@constraint` and store it in builder.
     */
    private void extractedConstraintAnnotation(MetadataNode metadata,
                                               ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder) {
        NodeList<AnnotationNode> annotations = metadata.annotations();
        annotations.stream()
                .filter(this::isConstraintAnnotation)
                .filter(annotation -> annotation.annotValue().isPresent())
                .forEach(annotation -> {
                    if (isDateConstraint(annotation)) {
                        DiagnosticMessages errorMsg = DiagnosticMessages.OAS_CONVERTOR_120;
                        IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMsg,
                                annotation.location(), annotation.toString());
                        diagnostics.add(error);
                        return;
                    }
                    MappingConstructorExpressionNode annotationValue = annotation.annotValue().orElse(null);
                    if (Objects.isNull(annotationValue)) {
                        return;
                    }
                    annotationValue.fields().stream()
                            .filter(field -> SyntaxKind.SPECIFIC_FIELD.equals(field.kind()))
                            .forEach(field -> {
                                String name = ((SpecificFieldNode) field).fieldName().toString().trim();
                                processConstraintAnnotation((SpecificFieldNode) field, name, constraintBuilder);
                            });
                });
    }

    /**
     * This util is used to check whether an annotation is a constraint annotation.
     */
    private boolean isConstraintAnnotation(AnnotationNode annotation) {
        if (annotation.annotReference() instanceof QualifiedNameReferenceNode qualifiedNameRef) {
            return qualifiedNameRef.modulePrefix().text().equals("constraint");
        }
        return false;
    }

    /*
     * This util is used to check whether an annotation is a constraint:Date annotation.
     * Currently, we don't have support for mapping Date constraints to OAS hence we skip them.
     * {@link <a href="https://github.com/ballerina-platform/ballerina-standard-library/issues/5049">...</a>}
     * Once the above improvement is completed this method should be removed!
     */
    private boolean isDateConstraint(AnnotationNode annotation) {
        return annotation.annotReference().toString().trim().equals(DATE_CONSTRAINT_ANNOTATION);
    }

    /**
     * This util is used to process the content of a constraint annotation.
     */
    private void processConstraintAnnotation(SpecificFieldNode specificFieldNode, String fieldName,
                                             ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder) {
        specificFieldNode.valueExpr()
                .flatMap(this::extractFieldValue)
                .ifPresent(fieldValue -> fillConstraintValue(constraintBuilder, fieldName, fieldValue));
    }

    private Optional<String> extractFieldValue(ExpressionNode exprNode) {
        SyntaxKind syntaxKind = exprNode.kind();
        switch (syntaxKind) {
            case NUMERIC_LITERAL:
                return Optional.of(exprNode.toString().trim());
            case REGEX_TEMPLATE_EXPRESSION:
                String regexContent = ((TemplateExpressionNode) exprNode).content().get(0).toString();
                if (regexContent.matches(REGEX_INTERPOLATION_PATTERN)) {
                    return Optional.of(regexContent);
                } else {
                    DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_119;
                    IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                            exprNode.location(), regexContent);
                    diagnostics.add(error);
                    return Optional.empty();
                }
            case MAPPING_CONSTRUCTOR:
                return ((MappingConstructorExpressionNode) exprNode).fields().stream()
                        .filter(fieldNode -> ((SpecificFieldNode) fieldNode).fieldName().toString().
                                trim().equals("value"))
                        .findFirst()
                        .flatMap(node -> ((SpecificFieldNode) node).valueExpr()
                                .flatMap(this::extractFieldValue));
            default:
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_118;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        exprNode.location(), exprNode.toString());
                diagnostics.add(error);
                return Optional.empty();
        }
    }

    /**
     * This util is used to build the constraint builder with available constraint annotation field value.
     */
    private void fillConstraintValue(ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder,
                                     String name, String constraintValue) {
        switch (name) {
            case "minValue":
                constraintBuilder.withMinValue(constraintValue);
                break;
            case "maxValue":
                constraintBuilder.withMaxValue(constraintValue);
                break;
            case "minValueExclusive":
                constraintBuilder.withMinValueExclusive(constraintValue);
                break;
            case "maxValueExclusive":
                constraintBuilder.withMaxValueExclusive(constraintValue);
                break;
            case "length":
                constraintBuilder.withLength(constraintValue);
                break;
            case "maxLength":
                constraintBuilder.withMaxLength(constraintValue);
                break;
            case "minLength":
                constraintBuilder.withMinLength(constraintValue);
                break;
            case "pattern":
                constraintBuilder.withPattern(constraintValue);
                break;
            default:
                break;
        }
    }
}
