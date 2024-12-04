import ballerina/url;
import ballerina/mime;

type SimpleBasicType string|boolean|int|float|decimal;

# Represents encoding mechanism details.
type Encoding record {
    # Defines how multiple values are delimited
    string style = FORM;
    # Specifies whether arrays and objects should generate as separate fields
    boolean explode = true;
    # Specifies the custom content type
    string contentType?;
    # Specifies the custom headers
    map<any> headers?;
};

enum EncodingStyle {
    DEEPOBJECT, FORM, SPACEDELIMITED, PIPEDELIMITED
}

# Get Encoded URI for a given value.
#
# + value - Value to be encoded
# + return - Encoded string
isolated function getEncodedUri(anydata value) returns string {
    string|error encoded = url:encode(value.toString(), "UTF8");
    if encoded is string {
        return encoded;
    } else {
        return value.toString();
    }
}

isolated function createBodyParts(record {|anydata...;|} anyRecord, map<Encoding> encodingMap = {})
returns mime:Entity[]|error {
    mime:Entity[] entities = [];
    foreach [string, anydata] [key, value] in anyRecord.entries() {
        Encoding encodingData = encodingMap.hasKey(key) ? encodingMap.get(key) : {};
        string contentDisposition = string `form-data; name=${key};`;
        if value is record {byte[] fileContent; string fileName;} {
            string fileContentDisposition = string `${contentDisposition} filename=${value.fileName}`;
            mime:Entity entity = check constructEntity(fileContentDisposition, encodingData,
                    value.fileContent);
            entities.push(entity);
        } else if value is byte[] {
            mime:Entity entity = check constructEntity(contentDisposition, encodingData, value);
            entities.push(entity);
        } else if value is SimpleBasicType {
            mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                    value.toString());
            entities.push(entity);
        } else if value is SimpleBasicType[] {
            if encodingData.explode {
                foreach SimpleBasicType member in value {
                    mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                            member.toString());
                    entities.push(entity);
                }
            } else {
                string[] valueStrArray = from SimpleBasicType val in value
                    select val.toString();
                mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                        string:'join(",", ...valueStrArray));
                entities.push(entity);
            }
        } else if value is record {} {
            mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                    value.toString());
            entities.push(entity);
        } else if value is record {}[] {
            if encodingData.explode {
                foreach record {} member in value {
                    mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                            member.toString());
                    entities.push(entity);
                }
            } else {
                string[] valueStrArray = from record {} val in value
                    select val.toJsonString();
                mime:Entity entity = check constructEntity(contentDisposition, encodingData,
                        string:'join(",", ...valueStrArray));
                entities.push(entity);
            }
        }
    }
    return entities;
}

isolated function constructEntity(string contentDisposition, Encoding encoding,
        string|byte[]|record {} data) returns mime:Entity|error {
    mime:Entity entity = new mime:Entity();
    entity.setContentDisposition(mime:getContentDispositionObject(contentDisposition));
    if data is byte[] {
        entity.setByteArray(data);
    } else if data is string {
        entity.setText(data);
    } else {
        entity.setJson(data.toJson());
    }
    check populateEncodingInfo(entity, encoding);
    return entity;
}

isolated function populateEncodingInfo(mime:Entity entity, Encoding encoding) returns error? {
    if encoding?.contentType is string {
        check entity.setContentType(encoding?.contentType.toString());
    }
    map<any>? headers = encoding?.headers;
    if headers is map<any> {
        foreach var [headerName, headerValue] in headers.entries() {
            if headerValue is SimpleBasicType {
                entity.setHeader(headerName, headerValue.toString());
            }
        }
    }
}
