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
import org.apache.commons.text.CaseUtils;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.JavaCamelServerCodegen;
import org.openapitools.codegen.languages.SpringCodegen;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.meta.Stability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class KafkaCodegenGenerator extends SpringCodegen {
    private static final String CLIENT_IMPL_TEMPLATE_NAME = "clientImpl.mustache";
    private static final String KAFKA_SENDER_SERVICE_TEMPLATE_NAME = "kafkaSenderService.mustache";
    private static final String KAFKA_SENDER_SERVICE_TEMPLATE_FILENAME = "KafkaSenderService.java";
    private static final String KAFKA_SENDER_SERVICE_IMPL_TEMPLATE_NAME = "kafkaSenderServiceImpl.mustache";
    private static final String KAFKA_SENDER_SERVICE_IMPL_TEMPLATE_FILENAME = "KafkaSenderServiceImpl.java";
    private static final String KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_NAME = "kafkaSenderServiceConfig.mustache";
    private static final String KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME = "KafkaSenderServiceConfig.java";
    private static final String KAFKA_SENDER_SPRING_FACTORIES_TEMPLATE = "spring.mustache";
    private static final String KAFKA_SENDER_SPRING_FACTORIES_TEMPLATE_FILENAME = "spring.factories";
    private static final String KAFKA_SENDER_SPRING_FACTORIES_3_TEMPLATE = "spring_3_autoconfig.mustache";
    private static final String KAFKA_SENDER_SPRING_FACTORIES_3_TEMPLATE_FILENAME = "org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private static final String KAFKA_PRODUCER_CONFIG_TEMPLATE_NAME = "kafkaProducerConfig.mustache";
    private static final String KAFKA_PRODUCER_CONFIG_FILE_NAME = "KafkaProducerConfig.java";

    private static final String CLIENT_TEMPLATE_NAME = "client.mustache";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";

    public static final String PROJECT_NAME = "projectName";
    public static final String CAMEL_REST_COMPONENT = "camelRestComponent";
    public static final String CAMEL_REST_BINDING_MODE = "camelRestBindingMode";
    public static final String CAMEL_REST_CLIENT_REQUEST_VALIDATION = "camelRestClientRequestValidation";
    public static final String CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR = "camelUseDefaultValidationErrorProcessor";
    public static final String CAMEL_VALIDATION_ERROR_PROCESSOR = "camelValidationErrorProcessor";
    public static final String CAMEL_SECURITY_DEFINITIONS = "camelSecurityDefinitions";
    public static final String CAMEL_DATAFORMAT_PROPERTIES = "camelDataformatProperties";
    private static final String GROUP_ID = "groupId";
    private static final String TOPIC = "topic";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_NAME_L = "methodNameL";
    private static final String KAFKA = "kafka";
    public static final String RESULT_WRAPPER = "resultWrapper";
    public static final String SECURITY_ANNOTATION = "securityAnnotation";
    public static final String SEND_BYTES = "sendBytes";
    public static final String MESSAGE_ID_NAME = "messageIdName";
    public static final String CORRELATION_ID_NAME = "correlationIdName";
    public static final String GENERATE_MESSAGE_ID = "generateMessageId";
    public static final String GENERATE_CORRELATION_ID = "generateCorrelationId";

    public static final String GENERATE_SUPPORTING_FILES = "generateSupportingFiles";

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

    private String messageIdName = "kafka_messageId";
    private String correlationIdName = "kafka_correlationId";
    private Boolean generateMessageId = true;
    private Boolean generateCorrelationId = true;

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

    private final Logger LOGGER = LoggerFactory.getLogger(JavaCamelServerCodegen.class);

    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    public String getName() {
        return "kafka-codegen";
    }

    public String getHelp() {
        return "Generates a Java Camel server (beta).";
    }

    public KafkaCodegenGenerator() {
        super();
        templateDir = "kafka-codegen";
        addCliOptions();
        artifactId = "kafka-codegen";
        super.library = "";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultListener";
        }
        name = sanitizeName(name);

        if (isKafkaClient) return camelize(name) + "Producer";

        return camelize(name) + "Listener";
    }

    private void addCliOptions() {
        cliOptions.add(new CliOption(CAMEL_REST_COMPONENT, "name of the Camel component to use as the REST consumer").defaultValue(camelRestComponent));
        cliOptions.add(new CliOption(CAMEL_REST_BINDING_MODE, "binding mode to be used by the REST consumer").defaultValue(camelRestBindingMode));
        cliOptions.add(CliOption.newBoolean(CAMEL_REST_CLIENT_REQUEST_VALIDATION, "enable validation of the client request to check whether the Content-Type and Accept headers from the client is supported by the Rest-DSL configuration", camelRestClientRequestValidation));
        cliOptions.add(CliOption.newBoolean(CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR, "generate default validation error processor", camelUseDefaultValidationErrorProcessor));
        cliOptions.add(new CliOption(CAMEL_VALIDATION_ERROR_PROCESSOR, "validation error processor bean name").defaultValue(camelValidationErrorProcessor));
        cliOptions.add(CliOption.newBoolean(CAMEL_SECURITY_DEFINITIONS, "generate camel security definitions", camelSecurityDefinitions));
        cliOptions.add(new CliOption(CAMEL_DATAFORMAT_PROPERTIES, "list of dataformat properties separated by comma (propertyName1=propertyValue2,...").defaultValue(camelDataformatProperties));
    }

    @Override
    public void processOpts() {
        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata)
                .stability(Stability.BETA)
                .build();

        if (!additionalProperties.containsKey(DATE_LIBRARY)) {
            additionalProperties.put(DATE_LIBRARY, "legacy");
        }
        super.processOpts();
//        super.apiTemplateFiles.remove("apiController.mustache");
        LOGGER.info("***** Kafka Generator *****");
        supportingFiles.clear();
        apiTemplateFiles.clear();

        if (isKafkaClient) {
            apiTemplateFiles.put(CLIENT_TEMPLATE_NAME, ".java");
            if (!interfaceOnly) {
                apiTemplateFiles.put(CLIENT_IMPL_TEMPLATE_NAME, ".java");
                supportingFiles.add(new SupportingFile(KAFKA_SENDER_SERVICE_TEMPLATE_NAME, getSourceFolder() + "/service/", KAFKA_SENDER_SERVICE_TEMPLATE_FILENAME));
                supportingFiles.add(new SupportingFile(KAFKA_SENDER_SERVICE_IMPL_TEMPLATE_NAME, getSourceFolder() + "/service/impl", KAFKA_SENDER_SERVICE_IMPL_TEMPLATE_FILENAME));
                supportingFiles.add(new SupportingFile(KAFKA_PRODUCER_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", KAFKA_PRODUCER_CONFIG_FILE_NAME));
                if(this.isUseSpringBoot3()) {
                    supportingFiles.add(new SupportingFile(KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME));
                    supportingFiles.add(new SupportingFile(KAFKA_SENDER_SPRING_FACTORIES_3_TEMPLATE, getSourceFolder() + "/../resources/META-INF/spring", KAFKA_SENDER_SPRING_FACTORIES_3_TEMPLATE_FILENAME));
                } else {
                    supportingFiles.add(new SupportingFile(KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", KAFKA_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME));
                    supportingFiles.add(new SupportingFile(KAFKA_SENDER_SPRING_FACTORIES_TEMPLATE, getSourceFolder() + "/../resources/META-INF/", KAFKA_SENDER_SPRING_FACTORIES_TEMPLATE_FILENAME));
                }
            }
        } else {
            apiTemplateFiles.put("api.mustache", ".java");
        }

        manageAdditionalProperties();

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

    private void manageAdditionalProperties() {
        camelRestComponent = manageAdditionalProperty(CAMEL_REST_COMPONENT, camelRestComponent);
        camelRestBindingMode = manageAdditionalProperty(CAMEL_REST_BINDING_MODE, camelRestBindingMode);
        camelRestClientRequestValidation = manageAdditionalProperty(CAMEL_REST_CLIENT_REQUEST_VALIDATION, camelRestClientRequestValidation);
        camelUseDefaultValidationErrorProcessor = manageAdditionalProperty(CAMEL_USE_DEFAULT_VALIDATION_ERROR_PROCESSOR, camelUseDefaultValidationErrorProcessor);
        camelValidationErrorProcessor = manageAdditionalProperty(CAMEL_VALIDATION_ERROR_PROCESSOR, camelValidationErrorProcessor);
        camelSecurityDefinitions = manageAdditionalProperty(CAMEL_SECURITY_DEFINITIONS, camelSecurityDefinitions);
        camelDataformatProperties = manageAdditionalProperty(CAMEL_DATAFORMAT_PROPERTIES, camelDataformatProperties);
        resultWrapper = manageAdditionalProperty(RESULT_WRAPPER, resultWrapper);
        securityAnnotation = manageAdditionalProperty(SECURITY_ANNOTATION, securityAnnotation);
        sendBytes = manageAdditionalProperty(SEND_BYTES, sendBytes);
        generateMessageId = manageAdditionalProperty(GENERATE_MESSAGE_ID, generateMessageId);
        generateCorrelationId = manageAdditionalProperty(GENERATE_CORRELATION_ID, generateCorrelationId);
        messageIdName = manageAdditionalProperty(MESSAGE_ID_NAME, messageIdName);
        correlationIdName = manageAdditionalProperty(CORRELATION_ID_NAME, correlationIdName);
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

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
//        super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }

        List<String> pathElements = Arrays.asList(basePath.split("/"));

        if (pathElements.size() < 3 || pathElements.size() > 4) {
            LOGGER.error("Path do not conform requirements. ({})", resourcePath);
            throw new RuntimeException("Path do not conform requirements. ({})");
        }

        String topic = "";
        String groupId = "";
        String kafka = pathElements.get(0);

        if (!kafka.equalsIgnoreCase(KAFKA)) {
            LOGGER.info("Path does not contain /kafka/. ({})", resourcePath);
            return;
        }

        if (pathElements.size() == 4) {
            groupId = pathElements.get(1);
            topic = pathElements.get(2);
            co.vendorExtensions.put(GROUP_ID, groupId);
            co.vendorExtensions.put(TOPIC, topic);
            co.vendorExtensions.put(METHOD_NAME, pathElements.get(3));
            co.vendorExtensions.put(METHOD_NAME_L, pathElements.get(3).toLowerCase());
        }

        if (pathElements.size() == 3) {
            topic = pathElements.get(1);
            co.vendorExtensions.put(TOPIC, topic);
            co.vendorExtensions.put(METHOD_NAME, pathElements.get(2));
            co.vendorExtensions.put(METHOD_NAME_L, pathElements.get(2).toLowerCase());
        }

        ArrayList<HashMap<String, String>> xTags = (ArrayList<HashMap<String, String>>) operation.getExtensions().get("x-tags");

        String tags = xTags.stream().map(m ->
             m.entrySet().stream()
                     .filter(e -> e.getKey().equals("tag"))
                     .map(entry -> entry.getValue())
                     .collect(Collectors.joining("\", \"", "\"", "\""))
        ).collect(Collectors.joining(", "));

        co.vendorExtensions.put("tags", tags);
        String topic_groupId = "";

        if (!topic.isEmpty()) {
//            topic_groupId = topic.substring(0, 1).toUpperCase() + topic.substring(1);
            topic_groupId = topic;
        }

       if (!groupId.isEmpty()) {
//            topic_groupId = topic_groupId + groupId.substring(0, 1).toUpperCase() + groupId.substring(1);
           topic_groupId = topic_groupId + ' ' + groupId;
       }

//       topic_groupId.replaceAll("[^a-zA-Zа-яёА-ЯЁ\\d]", "");
        topic_groupId = CaseUtils.toCamelCase(topic_groupId, true, new char[]{'-','_','.',' '}).replaceAll("[^a-zA-Zа-яёА-ЯЁ\\d]", "").replaceAll("-", "");
        operations.computeIfAbsent(topic_groupId, k -> new ArrayList<>());
        operations.get(topic_groupId).add(co);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String suffix = apiTemplateFiles().get(templateName);
        if (templateName.equals(CLIENT_IMPL_TEMPLATE_NAME)) {
            return apiFileFolder() + File.separator + "impl" + File.separator + toApiFilename(tag) + "Impl" + suffix;
        }

        return apiFileFolder() + File.separator + toApiFilename(tag) + suffix;
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
}
