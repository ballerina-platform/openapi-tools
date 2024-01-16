# Proposal: Map Ballerina constraints to OpenAPI Specification

_Owners_: @SachinAkash01  
_Reviewers_: @lnash94 @TharmiganK    
_Created_: 2023/09/08  
_Updated_: 2023/09/21  
_Issue_: [#4788](https://github.com/ballerina-platform/ballerina-library/issues/4788)

## Summary
Implement the capabilities to support constraint validations in the generated OpenAPI specification of the Ballerina 
OpenAPI tool.

## Goals
- Enhance the Ballerina OpenAPI tool by adding support for constraint validations in the generated OpenAPI 
specification.

## Motivation
OpenAPI is a widely used standard for documenting APIs. It allows developers to specify the structure and data types 
expected in API requests and responses. One of its valuable features is built-in schema validation, which ensures that 
the data exchanged between different software components conforms to the defined structure and types.

Constraints are the OpenAPI schema validations equivalent in the Ballerina programming language. The existing tool for 
generating OpenAPI specification from Ballerina code lacks support for including these constraints in the OpenAPI 
specification. This means that when developers use constraints in their Ballerina code, this information is not properly
reflected in the OpenAPI specification.

Overall, improving the OpenAPI tool to support Ballerina constraints in the generated OpenAPI specification is a 
worthwhile investment that will improve the overall developer experience.

## Description
The feature implementation focuses on integrating Ballerina constraints into generated OpenAPI specifications to 
accurately document the API. Following is the proposed list of Ballerina constraint types and the corresponding 
OpenAPI schema validations.

**1. OpenAPI data type: integer (formats: int32, int64) : (ballerina mapping): int**
<html>
<body>
<!--StartFragment--><b style="font-weight:normal;" id="docs-internal-guid-bad57f3a-7fff-86b7-6a7c-7f776b33c3b8">
<div dir="ltr" style="margin-left:0pt;" align="left">

| Ballerina Mapping                               | Keywords |
|-------------------------------------------------| -- |
| `@constraint:Int { minValue: <value>}`          | minimum |
| `@constraint:Int { maxValue: <value>}`          | maximum |
| `@constraint:Int { minValueExclusive: <value>}` | exclusiveMinimum |
| `@constraint:Int { maxValueExclusive: <value>}` | exclusiveMaximum |
</div></b><!--EndFragment-->
</body>
</html>

1.1. Sample Ballerina Code:
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Int{
    minValue: 5,
    maxValue: 20
}
public type Age int;

type Person record{
    Age age?;
};

service / on new http:Listener(9090){
    resource function post newPerson(Person body) returns error?{
	 return;
    }
}
```

1.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    Age:
        minimum: 5
        maximum: 20
        type: integer
        format: int32
    Person:
        type: object
        properties:
           age:
              $ref: '#/components/schemas/Age'
```

**2. OpenAPI data type: number (format: float) : (ballerina mapping): float**
<html>
<body>
<!--StartFragment--><b style="font-weight:normal;" id="docs-internal-guid-4a8956c7-7fff-41db-f023-fc63d34a953d">
<div dir="ltr" style="margin-left:0pt;" align="left">

| Ballerina Mapping                                 | Keywords |
|---------------------------------------------------| -- |
| `@constraint:Float { minValue: <value>}`          | minimum | 
| `@constraint:Float { maxValue: <value>}`          | maximum |
| `@constraint:Float { minValueExclusive: <value>}` | exclusiveMinimum |
| `@constraint:Float { maxValueExclusive: <value>}` | exclusiveMaximum |
</div></b><!--EndFragment-->
</body>
</html>

2.1. Ballerina Sample Code:
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Float{
    maxValue: 1000.5
}
public type Salary float;

type Person record{
    Salary salary?;
};

service / on new http:Listener(9090){
    resource function post newPerson(Person body) returns error?{
	 return;
    }
}
```

2.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    Salary:
        maximum: 1000.5
        type: number
        format: float
    Person:
        type: object
        properties:
          salary:
            $ref: '#/components/schemas/Salary'
```

**3. OpenAPI data type: number (format: decimal) : (ballerina mapping): decimal**
<html>
<body>
<!--StartFragment--><b style="font-weight:normal;" id="docs-internal-guid-34d6e091-7fff-dd89-fb8b-d1f0040122d8">
<div dir="ltr" style="margin-left:0pt;" align="left">

| Ballerina Mapping                                  | Keywords |
|----------------------------------------------------| -- |
| `@constraint:Number { minValue: <value>}`          | minimum |
| `@constraint:Number { maxValue: <value>}`          | maximum |
| `@constraint:Number { minValueExclusive: <value>}` | exclusiveMinimum |
| `@constraint:Number { maxValueExclusive: <value>}` | exclusiveMaximum |
</div></b><!--EndFragment-->
</body>
</html>

3.1. Sample Ballerina Code:
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:Number{
    maxValueExclusive: 5.1
}
public type Rating decimal;

type Hotel record{
    Rating rate?;
};

service / on new http:Listener(9090){
    resource function post newHotel(Hotel body) returns error?{
	 return;
    }
}
```

3.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    Rating:
        maximum: 5.1
        exclusiveMaximum: true
        type: number
        format: decimal
    Hotel:
        type: object
        properties:
          rate:
            $ref: '#/components/schemas/Rating'
```

**4. OpenAPI data type: string : (ballerina mapping): string**
<html>
<body>
<!--StartFragment--><b style="font-weight:normal;" id="docs-internal-guid-dcae7678-7fff-eb39-ef9b-b373bb19f125">
<div dir="ltr" style="margin-left:0pt;" align="left">

| Ballerina Mapping                          | Keywords |
|--------------------------------------------| -- |
| `@constraint:String { minLength: <value>}` | minLength |
| `@constraint:String { maxLength: <value>}` | maxLength |
| `@constraint:String { length: <value>}`    | minLength: `<value>`, maxLength: `<value>` |
|  `@constraint:String { pattern: <value>}`  | pattern |
</div></b><!--EndFragment-->
</body>
</html>

4.1.1. Sample Ballerina Code (minLength, maxLength, pattern):
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
    minLength: 5,
    maxLength: 20,
    pattern: re `^[a-zA-Z0-9_]+$`
}
public type Username string;

type Person record{
    Username username?;
};

service / on new http:Listener(9090){
    resource function post newUser(Person body) returns error?{
	 return;
    }
}
```

4.1.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    Username:
        minLength: 3
        maxLength: 20
        pattern: "^[a-zA-Z0-9_]+$"
        type: string
    Person:
        type: object
        properties:
          username:
            $ref: '#/components/schemas/Username'
```

4.2.1. Sample Ballerina Code (length):
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
    length: 5
}
public type ID string;

type Employee record{
    ID id;
};

service / on new http:Listener(9090){
    resource function post newEmployee(Employee body) returns error?{
	    return;
    }
}
```

4.2.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    ID:
        minLength: 5
        maxLength: 5
        type: string
    Employee:
        type: object
        properties:
          id:
            $ref: '#/components/schemas/ID'
```

**5. OpenAPI data type: array : (ballerina mapping): array**
<html>
<body>
<!--StartFragment--><b style="font-weight:normal;" id="docs-internal-guid-88367123-7fff-5493-f776-616a4e9df078">
<div dir="ltr" style="margin-left:0pt;" align="left">

| Ballerina Mapping                           | Keywords |
|---------------------------------------------| -- |
| `@constraint:Array { minLength: <value>}`   | minItems |
| `@constraint:Array { maxLength: <value>}`   | maxItems |
| `@constraint:Array { length: <value>}`      | minItems: `<value>`, maxItems: `<value>` |

</div></b><!--EndFragment-->
</body>
</html>

5.1. Sample Ballerina Code:
```ballerina
import ballerina/http;
import ballerina/constraint;

@constraint:String{
    maxLength: 23
}
public type HobbyItemsString string;

@constraint:Array{
    minLength: 2,
    maxLength: 5
}
public type Hobby HobbyItemsString[];

public type person record{
    Hobby hobby?;
};

service / on new http:Listener(9090){
    resource function post hobby(Person body) returns error?{
	 return;
    }
}
```

5.2. Generated OpenAPI Specification:
```yaml
components:
  schemas:
    HobbyItemsString:
        maxLength: 23
        type: string
    Hobby:
        minLength: 2
        maxLength: 5
        type: array
        items:
           $ref: '#/components/schemas/HobbyItemsString'
    Person:
        type: object
        properties:
          hobby:
           $ref: '#/components/schemas/Hobby'
```

## Testing
- **Unit Testing:** evaluate the individual components and core logic of the Ballerina OpenAPI tool, focusing on 
functions, methods, and modules to ensure correctness and constraint handling.
- **Integration Testing:** assess the interactions and collaborations between various modules and components of the tool, 
verifying the seamless integration of data validation constraints and rule generation into the OpenAPI specification.
