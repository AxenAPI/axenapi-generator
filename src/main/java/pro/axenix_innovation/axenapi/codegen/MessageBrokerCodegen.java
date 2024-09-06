package pro.axenix_innovation.axenapi.codegen;

import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.languages.SpringCodegen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.axenix_innovation.axenapi.codegen.helper.JmsHelper;
import pro.axenix_innovation.axenapi.codegen.helper.KafkaHelper;
import pro.axenix_innovation.axenapi.codegen.helper.LibHelper;
import pro.axenix_innovation.axenapi.codegen.helper.RabbitHelper;

import java.util.*;
import java.util.stream.Collectors;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class MessageBrokerCodegen extends SpringCodegen implements MyCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBrokerCodegen.class);

    public static final String RESULT_WRAPPER = "resultWrapper";
    public static final String SECURITY_ANNOTATION = "securityAnnotation";
    public static final String SEND_BYTES = "sendBytes";
    public static final String MESSAGE_ID_NAME = "messageIdName";
    public static final String CORRELATION_ID_NAME = "correlationIdName";
    public static final String GENERATE_MESSAGE_ID = "generateMessageId";
    public static final String GENERATE_CORRELATION_ID = "generateCorrelationId";

    private static final String IS_KAFKA_CLIENT = "kafkaClient";


    protected boolean useKafka = false;
    protected boolean useRabbit = false;
    protected boolean useJms = false;
    protected boolean useAxenAPI = true;
    protected String axenAPIVersion = "2.0.0";
    protected boolean interfaceOnly = false;

    private LibHelper libHelper;

    @Override
    public void processOpts() {
        super.processOpts();
        determineLib();
        System.out.println("apiTemplateFiles: " + apiTemplateFiles);
        System.out.println("modelTemplateFiles: " + modelTemplateFiles);
        System.out.println("supportingFiles: " + supportingFiles);
        apiTemplateFiles.clear();
        // find ApiUtil.Java in supportingFiles and remove it
        supportingFiles.removeIf(f -> f.getDestinationFilename().equals("ApiUtil.java"));
        libHelper.setTemplates(this, interfaceOnly);
        if (this.additionalProperties.containsKey("useAxenAPI")) {
            useAxenAPI = this.convertPropertyToBoolean("useAxenAPI");
        }
    }

    @Override
    public String toApiName(String name) {
        if (name.isEmpty()) {
            return "DefaultListener";
        }
        name = sanitizeName(name);
// TODO
//        if (isKafkaClient) {
//            return camelize(name) + "Producer";
//        }

        return camelize(name);
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

    @Override
    public String getName() {
        return "messageBroker";
    }

    public MessageBrokerCodegen() {
        super();
        templateDir = "templates";
        addCliOptions();
    }

    private void addCliOptions() {
        cliOptions.add(CliOption.newBoolean("useAxenAPI", "If true, then AxenApi will be used. If false, then AxenApi will not be used."));
        cliOptions.add(CliOption.newString("axenAPIVersion", "AxenApi version. If not specified, then latest version will be used.").defaultValue(axenAPIVersion));
        cliOptions.add(CliOption.newString("kafkaBootstrap", "List of kafka bootstrap servers (comma separated)."));
        cliOptions.add(CliOption.newString("listenerPackage", "Yes\tNo default value\tPackage, in which client/listeners will be generated."));
        cliOptions.add(CliOption.newString("modelPackage", "Package, in which models will be generated (Data Transfer Object)."));
        cliOptions.add(CliOption.newBoolean("useSpring3", "If true, then code will be generated for springboot 3.1. If false, then code will be generated for spring boot 2.7.", true));
        cliOptions.add(CliOption.newBoolean(IS_KAFKA_CLIENT, "If true, client code(producer) will be generated, if false - server code(consumer).", false));
        cliOptions.add(CliOption.newBoolean("interfaceOnly", "Affects only client generation. If true - Kafka consumer implemenation classes will be generated, if false - only iterfaces.", true));
        cliOptions.add(CliOption.newString(RESULT_WRAPPER, "Class, in which return value will be wrapped. Full path to that class must be specified.").defaultValue(""));
        cliOptions.add(CliOption.newString(SECURITY_ANNOTATION, "Annotation class which will be used in consumer code generation if consumer authorization is implemented. If this parameter is not specified, security annotations will not be generated.").defaultValue(""));
        cliOptions.add(CliOption.newBoolean(SEND_BYTES, "If true, then headers with types mapped by header names will not be used. If false, then types will be mapped.", false));
        cliOptions.add(CliOption.newBoolean( "useAutoconfig", "If true, then autoconfiguation files will be generated alongside clients.", true));
        cliOptions.add(CliOption.newString(MESSAGE_ID_NAME, "Name of the header, in which messageId value will be stored. If generateMessageId = true").defaultValue("kafka_messageId"));
        cliOptions.add(CliOption.newString(CORRELATION_ID_NAME, "Name of the header, in which correlationId value will be stored. If generateCorrelationId = true").defaultValue("kafka_correlationId"));
        cliOptions.add(CliOption.newBoolean(GENERATE_MESSAGE_ID, "If true, then generated clients will use header kafka_messageId by default. Header value will be random UUID.", true));
        cliOptions.add(CliOption.newBoolean(GENERATE_CORRELATION_ID, "If true, then generated clients will use header kafka_correlationId by default. Header value will be random UUID.", true));
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
            useKafka = true;
            libHelper = KafkaHelper.getInstance();
        } else if (RabbitHelper.PREFIX.equals(libPrefix)) {
            useRabbit = true;
            libHelper = RabbitHelper.getInstance();
        } else if (JmsHelper.PREFIX.equals(libPrefix)) {
            useJms = true;
            libHelper = JmsHelper.getInstance();
        }

        if (path != null && libHelper == null) {
            var exc = new RuntimeException(String.format("Path does not conform to the requirements. (%s)", path));
            LOGGER.error(exc.getMessage());
            throw exc;
        }
    }

    @Override
    public boolean isKafkaClient() {
        return false;
    }

    public boolean isUseAxenAPI() {
        return useAxenAPI;
    }

//    public String axenAPIVersion() {
//        return axenAPIVersion;
//    }

    public boolean isInterfaceOnly() {
        return interfaceOnly;
    }
}
