/*
 * Copyright (C) 2023 Axenix Innovations LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pro.axenix_innovation.axenapi.codegen;

import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.SpringCodegen;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.meta.Stability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.axenix_innovation.axenapi.codegen.helper.JmsHelper;
import pro.axenix_innovation.axenapi.codegen.helper.KafkaHelper;
import pro.axenix_innovation.axenapi.codegen.helper.LibHelper;
import pro.axenix_innovation.axenapi.codegen.helper.RabbitHelper;

import java.util.*;
import java.util.stream.Collectors;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class KafkaCodegenGenerator extends SpringCodegen {
    private static final String MODEL_TEMPLATE_NAME = "model.mustache";
    public static final String CAMEL_REST_COMPONENT = "camelRestComponent";
    public static final String CAMEL_REST_BINDING_MODE = "camelRestBindingMode";
    public static final String CAMEL_REST_CLIENT_REQUEST_VALIDATION = "camelRestClientRequestValidation";
    public static final String CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR = "camelUseDefaultValidationErrorProcessor";
    public static final String CAMEL_VALIDATION_ERROR_PROCESSOR = "camelValidationErrorProcessor";
    public static final String CAMEL_SECURITY_DEFINITIONS = "camelSecurityDefinitions";
    public static final String CAMEL_DATAFORMAT_PROPERTIES = "camelDataformatProperties";
    public static final String RESULT_WRAPPER = "resultWrapper";
    public static final String SECURITY_ANNOTATION = "securityAnnotation";
    public static final String SEND_BYTES = "sendBytes";
    public static final String MESSAGE_ID_NAME = "messageIdName";
    public static final String CORRELATION_ID_NAME = "correlationIdName";
    public static final String GENERATE_MESSAGE_ID = "generateMessageId";
    public static final String GENERATE_CORRELATION_ID = "generateCorrelationId";

    private static final String IS_KAFKA_CLIENT = "kafkaClient";

    private String camelRestComponent = "servlet";
    private String camelRestBindingMode = "auto";
    private boolean camelRestClientRequestValidation = false;
    private boolean camelUseDefaultValidationErrorProcessor = true;
    private String camelValidationErrorProcessor = "validationErrorProcessor";
    private boolean camelSecurityDefinitions = true;
    private String camelDataformatProperties = "";

    private String resultWrapper = null;

    private String securityAnnotation = "";

    private boolean sendBytes = true;

    private boolean fromAxenAPIPlugin = false;
    private String messageIdName = "kafka_messageId";
    private String correlationIdName = "kafka_correlationId";
    private Boolean generateMessageId = true;
    private Boolean generateCorrelationId = true;

    private LibHelper libHelper;

    private boolean useAutoConfig = true;


    public boolean isKafkaClient() {
        return isKafkaClient;
    }

    public void setKafkaClient(boolean kafkaClient) {
        isKafkaClient = kafkaClient;
    }

    private boolean isKafkaClient = false;

    public String getResultWrapper() {
        return resultWrapper;
    }

    public void setResultWrapper(String resultWrapper) {
        this.resultWrapper = resultWrapper;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCodegenGenerator.class);

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "kafka-codegen";
    }

    @Override
    public String getHelp() {
        return "Generates message communication (e.g. Kafka) participants (listeners/producers)";
    }

    public KafkaCodegenGenerator() {
        super();
        templateDir = "templates";
        addCliOptions();
        artifactId = "kafka-codegen";
        super.library = "";
        fromAxenAPIPlugin = false;
    }

    @Override
    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultListener";
        }
        name = sanitizeName(name);

        if (isKafkaClient) return camelize(name) + "Producer";

        return camelize(name);
    }

    private void addCliOptions() {
        cliOptions.add(new CliOption(CAMEL_REST_COMPONENT, "name of the Camel component to use as the REST consumer").defaultValue(camelRestComponent));
        cliOptions.add(new CliOption(CAMEL_REST_BINDING_MODE, "binding mode to be used by the REST consumer").defaultValue(camelRestBindingMode));
        cliOptions.add(CliOption.newBoolean(CAMEL_REST_CLIENT_REQUEST_VALIDATION, "enable validation of the client request to check whether the Content-Type and Accept headers from the client is supported by the Rest-DSL configuration", camelRestClientRequestValidation));
        cliOptions.add(CliOption.newBoolean(CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR, "generate default validation error processor", camelUseDefaultValidationErrorProcessor));
        cliOptions.add(new CliOption(CAMEL_VALIDATION_ERROR_PROCESSOR, "validation error processor bean name").defaultValue(camelValidationErrorProcessor));
        cliOptions.add(CliOption.newBoolean(CAMEL_SECURITY_DEFINITIONS, "generate camel security definitions", camelSecurityDefinitions));
        cliOptions.add(new CliOption(CAMEL_DATAFORMAT_PROPERTIES, "list of dataformat properties separated by comma (propertyName1=propertyValue2,...").defaultValue(camelDataformatProperties));

        cliOptions.add(new CliOption("listenerPackage", "Yes\tNo default value\tPackage, in which client/listeners will be generated."));
        cliOptions.add(new CliOption("modelPackage", "Package, in wich models will be generated (Data Transfer Object)."));
        cliOptions.add(CliOption.newBoolean("useSpring3", "If true, then code will be generated for springboot 3.1. If false, then code will be generated for spring boot 2.7.", false));
        cliOptions.add(CliOption.newBoolean(IS_KAFKA_CLIENT, "If true, client code(producer) will be generated, if false - server code(consumer).", false));
        cliOptions.add(CliOption.newBoolean("interfaceOnly", "Affects only client generation. If true - Kafka consumer implemenation classes will be generated, if false - only iterfaces.", true));
        cliOptions.add(CliOption.newString(RESULT_WRAPPER, "Class, in which return value will be wrapped. Full path to that class must be specified.").defaultValue(""));
        cliOptions.add(CliOption.newString(SECURITY_ANNOTATION, "Annotation class which will be used in consumer code generation if consumer authorization is implemented. If this parameter is not specified, security annotations will not be generated.").defaultValue(""));
        cliOptions.add(CliOption.newBoolean("generateSupportingFiles", "generate camel security definitions", camelSecurityDefinitions));
        cliOptions.add(CliOption.newBoolean(SEND_BYTES, "If true, then headers with types mapped by header names will not be used. If false, then types will be mapped.", false));
        cliOptions.add(CliOption.newBoolean( "useAutoconfig", "If true, then autoconfiguation files will be generated alongside clients.", true));
        cliOptions.add(CliOption.newString(MESSAGE_ID_NAME, "Name of the header, in which messageId value will be stored. If generateMessageId = true").defaultValue("kafka_messageId"));
        cliOptions.add(CliOption.newString(CORRELATION_ID_NAME, "Name of the header, in which correlationId value will be stored. If generateCorrelationId = true").defaultValue("kafka_correlationId"));
        cliOptions.add(CliOption.newBoolean(GENERATE_MESSAGE_ID, "If true, then generated clients will use header kafka_messageId by default. Header value will be random UUID.", true));
        cliOptions.add(CliOption.newBoolean(GENERATE_CORRELATION_ID, "If true, then generated clients will use header kafka_correlationId by default. Header value will be random UUID.", true));
    }

    @Override
    public void processOpts() {
        determineLib();

        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata)
                .stability(Stability.BETA)
                .build();

        if (!additionalProperties.containsKey(DATE_LIBRARY)) {
            additionalProperties.put(DATE_LIBRARY, "legacy");
        }
        super.processOpts();
        if(!fromAxenAPIPlugin) {
            manageAdditionalProperties();
        }
        //super.apiTemplateFiles.remove("apiController.mustache");
        LOGGER.info("***** Additional properties after  manageAdditionalProperties(); *****");
        logAdditionalProperties();

        setTemplates();

        Map<String, String> dataFormatProperties = new HashMap<>();
        if (!"off".equals(camelRestBindingMode)) {
            Arrays.stream(camelDataformatProperties.split(",")).forEach(property -> {
                String[] dataFormatProperty = property.split("=");
                if (dataFormatProperty.length == 2) {
                    dataFormatProperties.put(dataFormatProperty[0].trim(), dataFormatProperty[1].trim());
                }
            });
        }
        additionalProperties.put(CAMEL_DATAFORMAT_PROPERTIES, dataFormatProperties.entrySet());
    }

    private void determineLib() {
        String libPrefix = null;
        String path = null;
        var pathEntryOpt = openAPI.getPaths().entrySet().stream().findFirst();
        if (pathEntryOpt.isPresent()) {
            path = pathEntryOpt.get().getKey();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            var pathElements = Arrays.asList(path.split("/"));
            if (!pathElements.isEmpty()) {
                libPrefix = pathElements.get(0);
            }
        }
        LOGGER.info("prefix = " + libPrefix);
        if (KafkaHelper.PREFIX.equals(libPrefix)) {
            libHelper = KafkaHelper.getInstance();
        } else if (RabbitHelper.PREFIX.equals(libPrefix)) {
            libHelper = RabbitHelper.getInstance();
        } else if (JmsHelper.PREFIX.equals(libPrefix)) {
            libHelper = JmsHelper.getInstance();
        }

        if (path != null && libHelper == null) {
            var exc = new RuntimeException(String.format("Path does not conform to the requirements. (%s)", path));
            LOGGER.error(exc.getMessage());
            throw exc;
        }
    }

    private void logAdditionalProperties() {
        LOGGER.info("Additional properties:");
        additionalProperties.forEach(
                (key, value) -> LOGGER.info("{}: {}", key, value)
        );
    }

    private void manageAdditionalProperties() {
        camelRestComponent = manageAdditionalProperty(CAMEL_REST_COMPONENT, camelRestComponent);
        camelRestBindingMode = manageAdditionalProperty(CAMEL_REST_BINDING_MODE, camelRestBindingMode);
        camelRestClientRequestValidation = manageAdditionalProperty(CAMEL_REST_CLIENT_REQUEST_VALIDATION, camelRestClientRequestValidation);
        camelUseDefaultValidationErrorProcessor = manageAdditionalProperty(CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR, camelUseDefaultValidationErrorProcessor);
        camelValidationErrorProcessor = manageAdditionalProperty(CAMEL_VALIDATION_ERROR_PROCESSOR, camelValidationErrorProcessor);
        camelSecurityDefinitions = manageAdditionalProperty(CAMEL_SECURITY_DEFINITIONS, camelSecurityDefinitions);
        camelDataformatProperties = manageAdditionalProperty(CAMEL_DATAFORMAT_PROPERTIES, camelDataformatProperties);

        isKafkaClient = manageAdditionalProperty(IS_KAFKA_CLIENT, isKafkaClient);
        resultWrapper = manageAdditionalProperty(RESULT_WRAPPER, resultWrapper);
        securityAnnotation = manageAdditionalProperty(SECURITY_ANNOTATION, securityAnnotation);
        sendBytes = manageAdditionalProperty(SEND_BYTES, sendBytes);
        generateMessageId = manageAdditionalProperty(GENERATE_MESSAGE_ID, generateMessageId);
        generateCorrelationId = manageAdditionalProperty(GENERATE_CORRELATION_ID, generateCorrelationId);
        messageIdName = manageAdditionalProperty(MESSAGE_ID_NAME, messageIdName);
        correlationIdName = manageAdditionalProperty(CORRELATION_ID_NAME, correlationIdName);
        apiPackage = manageAdditionalProperty("apiPackage", "pro.axenix_innovation.axenapi.listener");
        modelPackage = manageAdditionalProperty("modelPackage", "pro.axenix_innovation.axenapi.model");
    }

    private <T> T manageAdditionalProperty(String propertyName, T defaultValue) {
        if (additionalProperties.containsKey(propertyName)) {
            Object propertyValue = additionalProperties.get(propertyName);
            if (defaultValue instanceof Boolean && !(propertyValue instanceof Boolean)) {
                return (T) manageBooleanAdditionalProperty((String) propertyValue);
            }
            return (T) additionalProperties.get(propertyName);
        }
        additionalProperties.put(propertyName, defaultValue);
        return defaultValue;
    }

    private Boolean manageBooleanAdditionalProperty(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }

    private void setTemplates() {
        supportingFiles.clear();
        apiTemplateFiles.clear();

        modelTemplateFiles.put(MODEL_TEMPLATE_NAME, ".java");

        libHelper.setTemplates(this, interfaceOnly);
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        String operationId = null;
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }

        ArrayList<HashMap<String, String>> xTags = (ArrayList<HashMap<String, String>>) operation.getExtensions().get("x-tags");

        String tags = xTags.stream().map(m ->
                m.entrySet().stream()
                        .filter(e -> e.getKey().equals("tag"))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.joining("\", \"", "\"", "\""))
        ).collect(Collectors.joining(", "));

        co.vendorExtensions.put("tags", tags);

        operationId = libHelper.addOperationInfo(tag, basePath, operation, co, operations);

        operations.computeIfAbsent(operationId, k -> new ArrayList<>());
        operations.get(operationId).add(co);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        return libHelper.apiFilename(templateName, tag, this);
    }

    public String getSecurityAnnotation() {
        return securityAnnotation;
    }

    public void setSecurityAnnotation(String securityAnnotation) {
        this.securityAnnotation = securityAnnotation;
    }

    public boolean isSendBytes() {
        return sendBytes;
    }

    public void setSendBytes(boolean sendBytes) {
        this.sendBytes = sendBytes;
    }

    public void setUseAutoConfig(boolean useAutoConfig) {
        this.useAutoConfig = useAutoConfig;
    }

    public String getMessageIdName() {
        return messageIdName;
    }

    public void setMessageIdName(String messageIdName) {
        this.messageIdName = messageIdName;
    }

    public String getCorrelationIdName() {
        return correlationIdName;
    }

    public void setCorrelationIdName(String correlationIdName) {
        this.correlationIdName = correlationIdName;
    }

    public Boolean getGenerateMessageId() {
        return generateMessageId;
    }

    public void setGenerateMessageId(Boolean generateMessageId) {
        this.generateMessageId = generateMessageId;
    }

    public Boolean getGenerateCorrelationId() {
        return generateCorrelationId;
    }

    public void setGenerateCorrelationId(Boolean generateCorrelationId) {
        this.generateCorrelationId = generateCorrelationId;
    }

    public void setFromAxenAPIPlugin( boolean fromAxenAPIPlugin) {
        this.fromAxenAPIPlugin = fromAxenAPIPlugin;
    }
}
