package pro.axenix_innovation.axenapi.codegen;

import io.swagger.v3.oas.models.Operation;
import org.apache.commons.text.CaseUtils;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.SupportingFile;
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

public class RabbitCodegenGenerator extends SpringCodegen {

    private static final String CLIENT_TEMPLATE_NAME = "client.mustache";
    private static final String CLIENT_IMPL_TEMPLATE_NAME = "clientImpl.mustache";
    private static final String RABBIT_SENDER_SERVICE_TEMPLATE_NAME = "rabbitSenderService.mustache";
    private static final String RABBIT_SENDER_SERVICE_TEMPLATE_FILENAME = "RabbitSenderService.java";
    private static final String RABBIT_SENDER_SERVICE_IMPL_TEMPLATE_NAME = "rabbitSenderServiceImpl.mustache";
    private static final String RABBIT_SENDER_SERVICE_IMPL_TEMPLATE_FILENAME = "RabbitSenderServiceImpl.java";
    private static final String RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_NAME = "rabbitSenderServiceConfig.mustache";
    private static final String RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME = "RabbitSenderServiceConfig.java";
    private static final String RABBIT_SENDER_SPRING_FACTORIES_TEMPLATE = "spring.mustache";
    private static final String RABBIT_SENDER_SPRING_FACTORIES_TEMPLATE_FILENAME = "spring.factories";
    private static final String RABBIT_SENDER_SPRING_FACTORIES_3_TEMPLATE = "spring_3_autoconfig.mustache";
    private static final String RABBIT_SENDER_SPRING_FACTORIES_3_TEMPLATE_FILENAME = "org.springframework.boot.autoconfigure.AutoConfiguration.imports";
    private static final String RABBIT_PRODUCER_CONFIG_TEMPLATE_NAME = "rabbitProducerConfig.mustache";
    private static final String RABBIT_PRODUCER_CONFIG_FILE_NAME = "RabbitProducerConfig.java";

    private static final String GROUP_ID = "groupId";
    private static final String QUEUE = "queue";
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_NAME_L = "methodNameL";
    private static final String RABBIT = "rabbit";
    public static final String RESULT_WRAPPER = "resultWrapper";
    public static final String SECURITY_ANNOTATION = "securityAnnotation";
    public static final String SEND_BYTES = "sendBytes";
    public static final String MESSAGE_ID_NAME = "messageIdName";
    public static final String CORRELATION_ID_NAME = "correlationIdName";
    public static final String GENERATE_MESSAGE_ID = "generateMessageId";
    public static final String GENERATE_CORRELATION_ID = "generateCorrelationId";

    private boolean isRabbitClient = false;

    private boolean sendBytes = true;
    private String securityAnnotation = "";
    private boolean fromAxenAPIPlugin = false;
    private String messageIdName = "rabbit_messageId";
    private String correlationIdName = "rabbit_correlationId";
    private Boolean generateMessageId = true;
    private Boolean generateCorrelationId = true;

    private boolean useAutoConfig = true;

    private final Logger LOGGER = LoggerFactory.getLogger(JavaCamelServerCodegen.class);

    public RabbitCodegenGenerator() {
        super();
        templateDir = "rabbit-codegen";
        addCliOptions();
        artifactId = "rabbit-codegen";
        super.library = "";
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

    @Override
    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultListener";
        }
        name = sanitizeName(name);

        if (isRabbitClient) return camelize(name) + "Producer";

        return camelize(name) + "Listener";
    }

    public void setRabbitClient(boolean rabbitClient) {
        isRabbitClient = rabbitClient;
    }

    @Override
    public void processOpts() {
        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata)
                .stability(Stability.BETA)
                .build();
        super.processOpts();
        if(!fromAxenAPIPlugin) {
            manageAdditionalProperties();
        }
        supportingFiles.clear();
        apiTemplateFiles.clear();

        if(isRabbitClient) {
            apiTemplateFiles.put(CLIENT_TEMPLATE_NAME, ".java");
            if (!interfaceOnly) {
                apiTemplateFiles.put(CLIENT_IMPL_TEMPLATE_NAME, ".java");
                supportingFiles.add(new SupportingFile(RABBIT_SENDER_SERVICE_TEMPLATE_NAME, getSourceFolder() + "/service/", RABBIT_SENDER_SERVICE_TEMPLATE_FILENAME));
                supportingFiles.add(new SupportingFile(RABBIT_SENDER_SERVICE_IMPL_TEMPLATE_NAME, getSourceFolder() + "/service/impl", RABBIT_SENDER_SERVICE_IMPL_TEMPLATE_FILENAME));
                supportingFiles.add(new SupportingFile(RABBIT_PRODUCER_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", RABBIT_PRODUCER_CONFIG_FILE_NAME));
                if(this.isUseSpringBoot3()) {
                    supportingFiles.add(new SupportingFile(RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME));
                    supportingFiles.add(new SupportingFile(RABBIT_SENDER_SPRING_FACTORIES_3_TEMPLATE, getSourceFolder() + "/../resources/META-INF/spring", RABBIT_SENDER_SPRING_FACTORIES_3_TEMPLATE_FILENAME));
                } else {
                    supportingFiles.add(new SupportingFile(RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_NAME, getSourceFolder() + "/config/", RABBIT_SENDER_SERVICE_CONFIG_TEMPLATE_FILENAME));
                    supportingFiles.add(new SupportingFile(RABBIT_SENDER_SPRING_FACTORIES_TEMPLATE, getSourceFolder() + "/../resources/META-INF/", RABBIT_SENDER_SPRING_FACTORIES_TEMPLATE_FILENAME));
                }
            }
        } else {
            apiTemplateFiles.put("api.mustache", ".java");
        }
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String suffix = apiTemplateFiles().get(templateName);
        if (templateName.equals(CLIENT_IMPL_TEMPLATE_NAME)) {
            return apiFileFolder() + File.separator + "impl" + File.separator + toApiFilename(tag) + "Impl" + suffix;
        }

        return apiFileFolder() + File.separator + toApiFilename(tag) + suffix;
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

        String queue = "";
        String groupId = "";
        String rabbit = pathElements.get(0);

        if (!rabbit.equalsIgnoreCase(RABBIT)) {
            LOGGER.info("Path does not contain /rabbit/. ({})", resourcePath);
            return;
        }

        if (pathElements.size() == 4) {
            groupId = pathElements.get(1);
            queue = pathElements.get(2);
            co.vendorExtensions.put(GROUP_ID, groupId);
            co.vendorExtensions.put(QUEUE, queue);
            co.vendorExtensions.put(METHOD_NAME, pathElements.get(3));
            co.vendorExtensions.put(METHOD_NAME_L, pathElements.get(3).toLowerCase());
        }

        if (pathElements.size() == 3) {
            queue = pathElements.get(1);
            co.vendorExtensions.put(QUEUE, queue);
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

        if (!queue.isEmpty()) {
//            topic_groupId = topic.substring(0, 1).toUpperCase() + topic.substring(1);
            topic_groupId = queue;
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

    private void addCliOptions() {
        cliOptions.add(new CliOption("listenerPackage", "Yes\tNo default value\tPackage, in which client/listeners will be generated."));
        cliOptions.add(new CliOption("modelPackage", "Package, in wich models will be generated (Data Transfer Object)."));
        cliOptions.add(CliOption.newBoolean("useSpring3", "If true, then code will be generated for springboot 3.1. If false, then code will be generated for spring boot 2.7.", false));
        cliOptions.add(CliOption.newBoolean("rabbitClient", "If true, client code(producer) will be generated, if false - server code(consumer).", false));
        cliOptions.add(CliOption.newBoolean("interfaceOnly", "Affects only client generation. If true - Rabbit consumer implemenation classes will be generated, if false - only iterfaces.", true));
        cliOptions.add(CliOption.newString("resultWrapper", "Class, in which return value will be wrapped. Full path to that class must be specified.").defaultValue(""));
        cliOptions.add(CliOption.newString("securityAnnotation", "Annotation class which will be used in consumer code generation if consumer authorization is implemented. If this parameter is not specified, security annotations will not be generated.").defaultValue(""));
        cliOptions.add(CliOption.newBoolean("sendBytes", "If true, then headers with types mapped by header names will not be used. If false, then types will be mapped.", false));
        cliOptions.add(CliOption.newBoolean( "useAutoconfig", "If true, then autoconfiguation files will be generated alongside clients.", true));
        cliOptions.add(CliOption.newString("messageIdName", "Name of the header, in which messageId value will be stored. If generateMessageId = true").defaultValue("rabbit_messageId"));
        cliOptions.add(CliOption.newString("correlationIdName", "Name of the header, in which correlationId value will be stored. If generateCorrelationId = true").defaultValue("rabbit_correlationId"));
        cliOptions.add(CliOption.newBoolean("generateMessageId", "If true, then generated clients will use header rabbit_messageId by default. Header value will be random UUID.", true));
        cliOptions.add(CliOption.newBoolean("generateCorrelationId", "If true, then generated clients will use header rabbit_correlationId by default. Header value will be random UUID.", true));
    }

    private void manageAdditionalProperties() {
        securityAnnotation = manageAdditionalProperty(SECURITY_ANNOTATION, securityAnnotation);
        sendBytes = manageAdditionalProperty(SEND_BYTES, sendBytes);
        generateMessageId = manageAdditionalProperty(GENERATE_MESSAGE_ID, generateMessageId);
        generateCorrelationId = manageAdditionalProperty(GENERATE_CORRELATION_ID, generateCorrelationId);
        messageIdName = manageAdditionalProperty(MESSAGE_ID_NAME, messageIdName);
        correlationIdName = manageAdditionalProperty(CORRELATION_ID_NAME, correlationIdName);
        apiPackage = manageAdditionalProperty("listenerPackage", "pro.axenix_innovation.axenapi.listener");
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
}
