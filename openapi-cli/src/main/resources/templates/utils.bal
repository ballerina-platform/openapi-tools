import ballerina/url;

type Encoding record {
    # Defines how multiple values are delimited
    string style = FORM;
    # Specifies whether arrays and objects should generate as separate fields
    boolean explode = true;
};

enum EncodingStyle {
    DEEPOBJECT,
    FORM,
    SPACEDELIMITED,
    PIPEDELIMITED
}

type SimpleBasicType string|boolean|int|float|decimal;

final Encoding & readonly defaultEncoding = {};

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
            int count = 0;
            foreach var recordItem in value {
                if encodingData.style == DEEPOBJECT {
                    payload.push(getDeepObjectStyleRequest(key + "[" + count.toString() + "]", recordItem), "&");
                } else if encodingData.style == FORM && encodingData.explode == false {
                    if count > 0 {
                        payload.push(getFormStyleRequest(key, recordItem, encodingData.explode, true), ",");
                    } else {
                        payload.push(getFormStyleRequest(key, recordItem, encodingData.explode), ",");
                    }
                } else {
                    payload.push(getFormStyleRequest(key, recordItem), "&");
                }
                count = count + 1;
            }
            _ = payload.pop();
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
            int count = 0;
            foreach var recordItem in value {
                recordArray.push(getDeepObjectStyleRequest(nextParent + "[" + count.toString() + "]", recordItem), "&");
                count = count + 1;
            }
            _ = recordArray.pop();
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
# + isNested - Whether the record is inside another record
# + return - Serialized record as a string
isolated function getFormStyleRequest(string parent, record {} anyRecord, boolean explode = true, boolean isNested = false) returns string {
    string[] recordArray = [];
    if explode {
        foreach [string, anydata] [key, value] in anyRecord.entries() {
            if (value is SimpleBasicType) {
                recordArray.push(key, "=", getEncodedUri(value.toString()));
            } else if (value is SimpleBasicType[]) {
                recordArray.push(getSerializedArray(key, value, explode = explode));
            } else if (value is record {}) {
                recordArray.push(getFormStyleRequest(parent, value, explode));
            }
            recordArray.push("&");
        }
        _ = recordArray.pop();
    } else {
        if (!isNested) {
            recordArray.push(parent, "=");
        }
        foreach [string, anydata] [key, value] in anyRecord.entries() {
            if (value is SimpleBasicType) {
                recordArray.push(key, ",", getEncodedUri(value.toString()));
            } else if (value is SimpleBasicType[]) {
                recordArray.push(getSerializedArray(key, value, explode = false));
            } else if (value is record {}) {
                recordArray.push(getFormStyleRequest(parent, value, explode, true));
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
    if (anyArray.length() > 0) {
        if (style == FORM && !explode) {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), ",");
            }
        } else if (style == SPACEDELIMITED && !explode) {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), "%20");
            }
        } else if (style == PIPEDELIMITED && !explode) {
            arrayValues.push(key, "=");
            foreach anydata i in anyArray {
                arrayValues.push(getEncodedUri(i.toString()), "|");
            }
        } else if (style == DEEPOBJECT) {
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

# Get Encoded URI for a given value.
#
# + value - Value to be encoded
# + return - Encoded string
isolated function getEncodedUri(string value) returns string {
    string|error encoded = url:encode(value, "UTF8");
    if (encoded is string) {
        return encoded;
    } else {
        return value;
    }
}

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + encodingMap - Details on serialization mechanism
# + return - Returns generated Path or error at failure of client initialization
isolated function getPathForQueryParam(map<anydata> queryParam, map<Encoding> encodingMap = {}) returns string|error {
    string[] param = [];
    param[param.length()] = "?";
    if (queryParam.length() > 0) {
        param.push("?");
        foreach var [key, value] in queryParam.entries() {
            if value is () {
                _ = queryParam.remove(key);
                continue;
            }
            Encoding encodingData = encodingMap.hasKey(key) ? encodingMap.get(key) : defaultEncoding;
            if (value is SimpleBasicType) {
                param.push(key, "=", getEncodedUri(value.toString()));
            } else if (value is SimpleBasicType[]) {
                param.push(getSerializedArray(key, value, encodingData.style, encodingData.explode));
            } else if (value is record {}) {
                if (encodingData.style == DEEPOBJECT) {
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

# Generate header map for given header values.
#
# + headerParam - Headers  map
# + return - Returns generated map or error at failure of client initialization
isolated function getMapForHeaders(map<any> headerParam) returns map<string|string[]> {
    map<string|string[]> headerMap = {};
    foreach var [key, value] in headerParam.entries() {
        if value is string || value is string[] {
            headerMap[key] = value;
        }
    }
    return headerMap;
}
