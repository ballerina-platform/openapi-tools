import ballerina/url;
import ballerina/mime;
import ballerina/http;

# Represents encoding mechanism details.
type Encoding record {
    # Defines how multiple values are delimited
    string style = FORM;
    # Specifies whether arrays and objects should generate as separate fields
    boolean explode = true;
    # Specifies the custom content type
    string contentType?;
    # Specifies the custom headers
    map<string> headers?;
};

enum EncodingStyle {
    DEEPOBJECT,
    FORM,
    SPACEDELIMITED,
    PIPEDELIMITED
}

final Encoding & readonly defaultEncoding = {};

type SimpleBasicType string|boolean|int|float|decimal;

# Generate client request when the media type is given as application/x-www-form-urlencoded.
#
# + encodingMap - Includes the information about the encoding mechanism
# + anyRecord - Record to be serialized
# + return - Serialized request body or query parameter as a string
isolated function createFormURLEncodedRequestBody(record {|anydata...; |} anyRecord, map<Encoding> encodingMap = {}) returns string {
    string[] payload = [];
    foreach [string, anydata] [key, value] in anyRecord.entries() {
        Encoding encodingData = encodingMap.hasKey(key) ? encodingMap.get(key) : defaultEncoding;
        if value is SimpleBasicType {
            payload.push(key, "=", getEncodedUri(value.toString()));
        } else if value is SimpleBasicType[] {
            payload.push(getSerializedArray(key, value, encodingData.style, encodingData.explode));
        } else if (value is record {}) {
            if encodingData.style == DEEPOBJECT {
                payload.push(getDeepObjectStyleRequest(key, value));
            } else {
                payload.push(getFormStyleRequest(key, value));
            }
        } else if (value is record {}[]) {
            payload.push(getSerializedRecordArray(key, value, encodingData.style, encodingData.explode));
        }
        payload.push("&");
    }
    _ = payload.pop();
    return string:'join("", ...payload);
}

# Serialize the record according to the deepObject style.
#
# + parent - Parent record name
# + anyRecord - Record to be serialized
# + return - Serialized record as a string
isolated function getDeepObjectStyleRequest(string parent, record {} anyRecord) returns string {
    string[] recordArray = [];
    foreach [string, anydata] [key, value] in anyRecord.entries() {
        if value is SimpleBasicType {
            recordArray.push(parent + "[" + key + "]" + "=" + getEncodedUri(value.toString()));
        } else if value is SimpleBasicType[] {
            recordArray.push(getSerializedArray(parent + "[" + key + "]" + "[]", value, DEEPOBJECT, true));
        } else if value is record {} {
            string nextParent = parent + "[" + key + "]";
            recordArray.push(getDeepObjectStyleRequest(nextParent, value));
        } else if value is record {}[] {
            string nextParent = parent + "[" + key + "]";
            recordArray.push(getSerializedRecordArray(nextParent, value, DEEPOBJECT));
        }
        recordArray.push("&");
    }
    _ = recordArray.pop();
    return string:'join("", ...recordArray);
}

# Serialize the record according to the form style.
#
# + parent - Parent record name
# + anyRecord - Record to be serialized
# + explode - Specifies whether arrays and objects should generate separate parameters
# + return - Serialized record as a string
isolated function getFormStyleRequest(string parent, record {} anyRecord, boolean explode = true) returns string {
    string[] recordArray = [];
    if explode {
        foreach [string, anydata] [key, value] in anyRecord.entries() {
            if value is SimpleBasicType {
                recordArray.push(key, "=", getEncodedUri(value.toString()));
            } else if value is SimpleBasicType[] {
                recordArray.push(getSerializedArray(key, value, explode = explode));
            } else if value is record {} {
                recordArray.push(getFormStyleRequest(parent, value, explode));
            }
            recordArray.push("&");
        }
        _ = recordArray.pop();
    } else {
        foreach [string, anydata] [key, value] in anyRecord.entries() {
            if value is SimpleBasicType {
                recordArray.push(key, ",", getEncodedUri(value.toString()));
            } else if value is SimpleBasicType[] {
                recordArray.push(getSerializedArray(key, value, explode = false));
            } else if value is record {} {
                recordArray.push(getFormStyleRequest(parent, value, explode));
            }
            recordArray.push(",");
        }
        _ = recordArray.pop();
    }
    return string:'join("", ...recordArray);
}

# Serialize arrays.
#
# + arrayName - Name of the field with arrays
# + anyArray - Array to be serialized
# + style - Defines how multiple values are delimited
# + explode - Specifies whether arrays and objects should generate separate parameters
# + return - Serialized array as a string
isolated function getSerializedArray(string arrayName, anydata[] anyArray, string style = "form", boolean explode = true) returns string {
    string key = arrayName;
    string[] arrayValues = [];
    if anyArray.length() > 0 {
        if style == FORM && !explode {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), ",");
            }
        } else if style == SPACEDELIMITED && !explode {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), "%20");
            }
        } else if style == PIPEDELIMITED && !explode {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), "|");
            }
        } else if style == DEEPOBJECT {
            foreach anydata i in anyArray {
                arrayValues.push(key, "[]", "=", getEncodedUri(i.toString()), "&");
            }
        } else {
            foreach anydata i in anyArray {
                arrayValues.push(key, "=", getEncodedUri(i.toString()), "&");
            }
        }
        _ = arrayValues.pop();
    }
    return string:'join("", ...arrayValues);
}

# Serialize the array of records according to the form style.
#
# + parent - Parent record name
# + value - Array of records to be serialized
# + style - Defines how multiple values are delimited
# + explode - Specifies whether arrays and objects should generate separate parameters
# + return - Serialized record as a string
isolated function getSerializedRecordArray(string parent, record {}[] value, string style = FORM, boolean explode = true) returns string{
    string[] serializedArray = [];
    if style == DEEPOBJECT {
        int arayIndex = 0;
        foreach var recordItem in value {
            serializedArray.push(getDeepObjectStyleRequest(parent + "[" + arayIndex.toString() + "]", recordItem), "&");
            arayIndex = arayIndex + 1;
        }
    } else {
        if !explode {
            serializedArray.push(parent, "=");
        }
        foreach var recordItem in value {
            serializedArray.push(getFormStyleRequest(parent, recordItem, explode), ",");
        }
    }
    _ = serializedArray.pop();
    return string:'join("", ...serializedArray);
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

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + encodingMap - Details on serialization mechanism
# + return - Returns generated Path or error at failure of client initialization
isolated function getPathForQueryParam(map<anydata> queryParam, map<Encoding> encodingMap = {}) returns string|error {
    map<anydata> queriesMap = http:getQueryMap(queryParam);
    string[] param = [];
    if queriesMap.length() > 0 {
        param.push("?");
        foreach var [key, value] in queriesMap.entries() {
            if value is () {
                _ = queriesMap.remove(key);
                continue;
            }
            Encoding encodingData = encodingMap.hasKey(key) ? encodingMap.get(key) : defaultEncoding;
            if value is SimpleBasicType {
                param.push(key, "=", getEncodedUri(value.toString()));
            } else if value is SimpleBasicType[] {
                param.push(getSerializedArray(key, value, encodingData.style, encodingData.explode));
            } else if value is record {} {
                if encodingData.style == DEEPOBJECT {
                    param.push(getDeepObjectStyleRequest(key, value));
                } else {
                    param.push(getFormStyleRequest(key, value, encodingData.explode));
                }
            } else {
                param.push(key, "=", value.toString());
            }
            param.push("&");
        }
        _ = param.pop();
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
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

isolated function getValidatedResponseForDefaultMapping(http:StatusCodeResponse|error response, int[] nonDefaultStatusCodes) returns http:StatusCodeResponse|error {
    if response is error {
        if response is http:StatusCodeResponseDataBindingError {
            http:StatusCodeBindingErrorDetail detail = response.detail();
            if nonDefaultStatusCodes.indexOf(detail.statusCode) is int && detail.fromDefaultStatusCodeMapping {
                return createStatusCodeResponseBindingError(detail.statusCode, detail.headers, detail.body);
            }
        }
    } else if response is http:DefaultStatusCodeResponse {
        int statusCode = response.status.code;
        map<anydata> headersFromResponse = response.headers ?: {};
        map<string[]> headers = {};
        foreach var [key, value] in headersFromResponse.entries() {
            if value is anydata[] {
                headers[key] = from anydata data in value
                    select data.toString();
            } else {
                headers[key] = [value.toString()];
            }
        }
        if nonDefaultStatusCodes.indexOf(statusCode) is int {
            return createStatusCodeResponseBindingError(statusCode, headers, response?.body);
        }
    }
    return response;
}

isolated function createStatusCodeResponseBindingError(int statusCode, map<string[]> headers, anydata body = ()) returns http:StatusCodeResponseBindingError {
    string reasonPhrase = string `incompatible type found for the response with non-default status code: ${statusCode}`;
    if 100 <= statusCode && statusCode <= 399 {
        return error http:StatusCodeResponseBindingError(reasonPhrase, statusCode = statusCode, headers = headers, body = body, fromDefaultStatusCodeMapping = false);
    } else if 400 <= statusCode && statusCode <= 499 {
        return error http:StatusCodeBindingClientRequestError(reasonPhrase, statusCode = statusCode, headers = headers, body = body, fromDefaultStatusCodeMapping = false);
    } else {
        return error http:StatusCodeBindingRemoteServerError(reasonPhrase, statusCode = statusCode, headers = headers, body = body, fromDefaultStatusCodeMapping = false);
    }
}
