package pro.axenix_innovation.axenapi.codegen.helper;

import io.swagger.v3.oas.models.Operation;
import org.apache.commons.text.CaseUtils;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.utils.CamelizeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.axenix_innovation.axenapi.codegen.KafkaCodegenGenerator;

import java.io.File;
import java.util.*;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class KafkaHelper implements LibHelper {
    public static final String PREFIX = "kafka";

    private static final String CLIENT_IMPL_TEMPLATE_NAME = PREFIX + File.separator + "clientImpl.mustache";
    private static final String SENDER_SERVICE_TEMPLATE_NAME = PREFIX + File.separator + "senderService.mustache";
    private static final String SENDER_SERVICE_FILENAME = "KafkaSenderService.java";
    private static final String SENDER_SERVICE_IMPL_TEMPLATE_NAME = PREFIX + File.separator + "senderServiceImpl.mustache";
    private static final String SENDER_SERVICE_IMPL_FILENAME = "KafkaSenderServiceImpl.java";
    private static final String SENDER_SERVICE_CONFIG_TEMPLATE_NAME = PREFIX + File.separator + "senderServiceConfig.mustache";
    private static final String SENDER_SERVICE_CONFIG_FILENAME = "KafkaSenderServiceConfig.java";
    private static final String SPRING_2_AUTOCONFIG_TEMPLATE_NAME = PREFIX + File.separator + "spring_2_autoconfig.mustache";
    private static final String SPRING_3_AUTOCONFIG_TEMPLATE_NAME = PREFIX + File.separator + "spring_3_autoconfig.mustache";

    private static final String PRODUCER_CONFIG_TEMPLATE_NAME = PREFIX + File.separator + "producerConfig.mustache";
    private static final String PRODUCER_CONFIG_FILENAME = "KafkaProducerConfig.java";

    private static final String LISTENER_TEMPLATE_NAME = PREFIX + File.separator + "listener.mustache";

    private static final String GROUP_ID = "groupId";
    private static final String TOPIC = "topic";

    private final Logger LOGGER = LoggerFactory.getLogger(KafkaHelper.class);

    private static LibHelper instance;

    public static LibHelper getInstance() {
        if (instance == null) {
            instance = new KafkaHelper();
        }
        return instance;
    }

    @Override
    public void setTemplates(KafkaCodegenGenerator gen, boolean isInterfaceOnly) {
        if (gen.isKafkaClient()) {
            gen.apiTemplateFiles().put(CLIENT_TEMPLATE_NAME, ".java");
            if (!isInterfaceOnly) {
                gen.apiTemplateFiles().put(CLIENT_IMPL_TEMPLATE_NAME, ".java");
                gen.supportingFiles().add(new SupportingFile(SENDER_SERVICE_TEMPLATE_NAME,
                        gen.getSourceFolder() + File.separator + "service", SENDER_SERVICE_FILENAME));
                gen.supportingFiles().add(new SupportingFile(SENDER_SERVICE_IMPL_TEMPLATE_NAME,
                        gen.getSourceFolder() + File.separator + "service" + File.separator + "impl", SENDER_SERVICE_IMPL_FILENAME));
                gen.supportingFiles().add(new SupportingFile(PRODUCER_CONFIG_TEMPLATE_NAME,
                        gen.getSourceFolder() + File.separator + "config", PRODUCER_CONFIG_FILENAME));
                if (gen.isUseSpringBoot3()) {
                    gen.supportingFiles().add(new SupportingFile(SENDER_SERVICE_CONFIG_TEMPLATE_NAME,
                            gen.getSourceFolder() + File.separator + "config", SENDER_SERVICE_CONFIG_FILENAME));
                    gen.supportingFiles().add(new SupportingFile(SPRING_3_AUTOCONFIG_TEMPLATE_NAME, // /../resources/META-INF/spring
                            gen.getSourceFolder() + File.separator + ".." + File.separator + "resources" +
                                    File.separator + "META-INF" + File.separator + "spring", SPRING_3_AUTOCONFIG_FILENAME));
                } else {
                    gen.supportingFiles().add(new SupportingFile(SENDER_SERVICE_CONFIG_TEMPLATE_NAME,
                            gen.getSourceFolder() + File.separator + "config", SENDER_SERVICE_CONFIG_FILENAME));
                    gen.supportingFiles().add(new SupportingFile(SPRING_2_AUTOCONFIG_TEMPLATE_NAME, // /../resources/META-INF
                            gen.getSourceFolder() + File.separator + ".." + File.separator +
                                    "resources" + File.separator + "META-INF", SPRING_2_AUTOCONFIG_FILENAME));
                }
            }
        } else {
            gen.apiTemplateFiles().put(LISTENER_TEMPLATE_NAME, ".java");
            gen.apiTemplateFiles().put(LISTENER_SERVICE_TEMPLATE_NAME, ".java");
        }
    }

    @Override
    public String addOperationInfo(String tag, String path, Operation operation, CodegenOperation co,
                                    Map<String, List<CodegenOperation>> operations) {
        String topic = "";
        String groupId = "";
        String topic_groupId = "";

        List<String> pathElements = Arrays.asList(path.split("/"));

        if (pathElements.size() < 3 || pathElements.size() > 4) {
            var exc = new RuntimeException(String.format("Path does not conform to either patterns /kafka/<group_id>/<topic_id>/<model>" +
                    " and /kafka/<topic_id>/<model>. (%s)", path));
            LOGGER.error(exc.getMessage());
            throw exc;
        }

        if (pathElements.size() == 4) {
            groupId = pathElements.get(1);
            topic = pathElements.get(2);
            co.vendorExtensions.put(GROUP_ID, groupId);
            co.vendorExtensions.put(TOPIC, topic);
            co.vendorExtensions.put(MODEL_NAME, pathElements.get(3));
            co.vendorExtensions.put(MODEL_NAME_CAMEL, camelize(pathElements.get(3), CamelizeOption.LOWERCASE_FIRST_CHAR));
        }

        if (pathElements.size() == 3) {
            topic = pathElements.get(1);
            co.vendorExtensions.put(TOPIC, topic);
            co.vendorExtensions.put(MODEL_NAME, pathElements.get(2));
            co.vendorExtensions.put(MODEL_NAME_CAMEL, camelize(pathElements.get(2), CamelizeOption.LOWERCASE_FIRST_CHAR));
        }

        if (!topic.isEmpty()) {
            topic_groupId = topic;
        }

        if (!groupId.isEmpty()) {
            topic_groupId = topic_groupId + ' ' + groupId;
        }

        topic_groupId = CaseUtils.toCamelCase(topic_groupId, true, new char[]{'-','_','.',' '}).replaceAll("[^a-zA-Zа-яёА-ЯЁ\\d]", "").replaceAll("-", "");
        return topic_groupId;
    }

    @Override
    public String apiFilename(String templateName, String tag, KafkaCodegenGenerator gen) {
        String suffix = gen.apiTemplateFiles().get(templateName);
        if (templateName.equals(CLIENT_IMPL_TEMPLATE_NAME)) {
            return gen.apiFileFolder() + File.separator + "impl" + File.separator +
                    gen.toApiFilename(tag) + "Impl" + suffix;
        }

        String listenerQualifier = "";
        String listenerInnerPackage = "";
        if (templateName.equals(LISTENER_TEMPLATE_NAME)) {
            listenerQualifier = "Listener";
        } else if (templateName.equals(LISTENER_SERVICE_TEMPLATE_NAME)) {
            listenerQualifier = "Service";
            listenerInnerPackage = "service" + File.separator;
        }
        if (!listenerQualifier.isEmpty()) {
            return gen.apiFileFolder() + File.separator + listenerInnerPackage +
                    gen.toApiFilename(tag) + listenerQualifier + suffix;
        }

        return gen.apiFileFolder() + File.separator + gen.toApiFilename(tag) + suffix;
    }
}
