package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.parameters.Parameter;

import java.io.File;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErlangClientCodegen extends DefaultCodegen implements CodegenConfig {
    static Logger LOGGER = LoggerFactory.getLogger(ErlangClientCodegen.class);

    protected String packageName = "swagger";
    protected String packageVersion = "1.0.0";
    protected String apiDocPath = "doc/";
    protected String modelDocPath = "doc/";

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "erlang";
    }

    public String getHelp() {
        return "Generates a Erlang client library (beta).";
    }

    public ErlangClientCodegen() {
        super();
        outputFolder = "generated-code/erlang";
        modelTemplateFiles.put("model.mustache", ".erl");
        apiTemplateFiles.put("api.mustache", ".erl");

        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        templateDir = "erlang";

        setReservedWordsLowerCase(
            Arrays.asList(
                "after",
                "begin",
                "catch",
                "case",
                "cond",
                "end",
                "fun",
                "if",
                "let",
                "of",
                "receive",
                "try",
                "when"
                )
        );

        // defaultIncludes = new HashSet<String>(
        //         Arrays.asList(
        //             )
        //         );

        // languageSpecificPrimitives = new HashSet<String>(
        //     Arrays.asList(
        //         )
        //     );

        instantiationTypes.clear();
        /*instantiationTypes.put("array", "GoArray");
        instantiationTypes.put("map", "GoMap");*/

        typeMapping.clear();
        typeMapping.put("integer", "integer()");
        typeMapping.put("long", "integer()");
        typeMapping.put("number", "number()");
        typeMapping.put("float", "float()");
        typeMapping.put("double", "float()");
        typeMapping.put("boolean", "boolean()");
        typeMapping.put("string", "string()");
        typeMapping.put("date", "calendar:datetime()");
        typeMapping.put("DateTime", "calendar:datetime()");
        typeMapping.put("password", "string()");
        typeMapping.put("File", "file:name()");
        typeMapping.put("file", "file:name()");
        typeMapping.put("binary", "binary()");
        typeMapping.put("ByteArray", "string()");

        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Erlang application name (convention: lowercase).")
                .defaultValue("swagger"));
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_VERSION, "Erlang application version.")
                .defaultValue("1.0.0"));
    }

    @Override
    public void processOpts() {
        //super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        }
        else {
            setPackageName("swagger");
        }

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_VERSION)) {
            setPackageVersion((String) additionalProperties.get(CodegenConstants.PACKAGE_VERSION));
        }
        else {
            setPackageVersion("1.0.0");
        }

        additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        additionalProperties.put(CodegenConstants.PACKAGE_VERSION, packageVersion);

        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        modelPackage = packageName;
        apiPackage = packageName;

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("configuration.mustache", "src", "configuration.erl"));
        supportingFiles.add(new SupportingFile("api_client.mustache", "src", "api_client.erl"));
        supportingFiles.add(new SupportingFile("api_response.mustache", "src", "api_response.erl"));
        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
    }

    @Override
    public String escapeReservedWord(String name) {
        // Can't start with an underscore, as our fields need to start with a
        // LowerLetter so that Erlang treats them as atoms.
        return camelize(name, true) + '_';
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator;
    }

    public String modelFileFolder() {
        return outputFolder + File.separator;
    }

    @Override
    public String toVarName(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_");

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);

        // for reserved word or word starting with number, append _
        if(isReservedWord(name) || name.matches("^\\d.*"))
            name = escapeReservedWord(name);

        return name;
    }

    @Override
    public String toParamName(String name) {
        // params should be upperCamelCase.
        // E.g. "Person Person", instead of "person Person".
        return camelize(toVarName(name), false);
    }

    @Override
    public String toModelName(String name) {
        // camelize the model name
        // phone_number => phoneNumber
        return camelize(toModelFilename(name), false);
    }

    @Override
    public String toModelFilename(String name) {
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        name = sanitizeName(name);

        // model name cannot use reserved keyword, e.g. catch
        if (isReservedWord(name)) {
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. catch => ModelCatch (after camelize)
        }

        return underscore(name);
    }

    @Override
    public String toApiFilename(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // e.g. PetApi.erl => pet_api.erl
        return underscore(name) + "_api";
    }



    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String toModelDocFilename(String name) {
        return toModelName(name);
    }

    @Override
    public String toApiDocFilename(String name) {
        return toApiName(name);
    }

    @Override
    public String getTypeDeclaration(Property p) {
        if(p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return "[" +getTypeDeclaration(inner)+ "]";
        }
        else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();

            //getSwaggerType(p) (= "map", …)
            return "#{string() => " + getTypeDeclaration(inner) + "}";
        }
        return super.getTypeDeclaration(p);
    }

    // @Override
    // public String getSwaggerType(Property p) {
    //     String swaggerType = super.getSwaggerType(p);
    //     String type = null;
    //     if(typeMapping.containsKey(swaggerType)) {
    //         type = typeMapping.get(swaggerType);
    //         if(languageSpecificPrimitives.contains(type))
    //             return (type);
    //     }
    //     else
    //         type = swaggerType;
    //     return type;
    // }

    @Override
    public String toOperationId(String operationId) {
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + camelize(sanitizeName("call_" + operationId)));
            operationId = "call_" + operationId;
        }

        return camelize(operationId, true);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            // http method verb conversion (e.g. PUT => put)
            operation.httpMethod = operation.httpMethod.toLowerCase();
        }

        return objs;
    }

    // @Override
    // protected boolean needToImport(String type) {
    //     return !defaultIncludes.contains(type)
    //         && !languageSpecificPrimitives.contains(type);
    // }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }
}
