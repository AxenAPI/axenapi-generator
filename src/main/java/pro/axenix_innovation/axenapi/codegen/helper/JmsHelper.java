package pro.axenix_innovation.axenapi.codegen.helper;

import io.swagger.v3.oas.models.Operation;
import org.apache.commons.text.CaseUtils;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.utils.CamelizeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.axenix_innovation.axenapi.codegen.KafkaCodegenGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class JmsHelper implements LibHelper {
    public static final String PREFIX = "jms";

    private static final String LISTENER_TEMPLATE_NAME = PREFIX + File.separator + "listener.mustache";

    private static final String QUEUE = "queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsHelper.class);

    private static LibHelper instance;

    public static LibHelper getInstance() {
        if (instance == null) {
            instance = new JmsHelper();
        }
        return instance;
    }

    @Override
    public void setTemplates(KafkaCodegenGenerator gen, boolean isInterfaceOnly) {
        if (gen.isKafkaClient()) {

        } else {
            gen.apiTemplateFiles().put(LISTENER_TEMPLATE_NAME, ".java");
            gen.apiTemplateFiles().put(LISTENER_SERVICE_TEMPLATE_NAME, ".java");
        }
    }

    @Override
    public String addOperationInfo(String tag, String path, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        List<String> pathElements = Arrays.asList(path.split("/"));
        String queue = "";

        if (pathElements.size() != 3) {
            var exc = new RuntimeException(String.format("Path does not conform to the pattern /jms/<queue>/<model>. (%s)", path));
            LOGGER.error(exc.getMessage());
            throw exc;
        }

        queue = pathElements.get(1);
        co.vendorExtensions.put(QUEUE, queue);
        co.vendorExtensions.put(MODEL_NAME, pathElements.get(2));
        co.vendorExtensions.put(MODEL_NAME_CAMEL, camelize(pathElements.get(2), CamelizeOption.LOWERCASE_FIRST_CHAR));

        queue = CaseUtils.toCamelCase(queue, true, '-','_','.',' ').replaceAll("[^a-zA-Zа-яёА-ЯЁ\\d]", "").replace("-", "");

        // TODO: temporarily solution - grouping by QUEUE + MODEL_NAME
        // as the library currently works properly only with one JmsListener per class
        return queue + camelize((String) co.vendorExtensions.get(MODEL_NAME_CAMEL), CamelizeOption.UPPERCASE_FIRST_CHAR);
    }

    @Override
    public String apiFilename(String templateName, String tag, KafkaCodegenGenerator gen) {
        String suffix = gen.apiTemplateFiles().get(templateName);
//        if (templateName.equals(CLIENT_IMPL_TEMPLATE_NAME)) {
//            return gen.apiFileFolder() + File.separator + "impl" + File.separator +
//                    gen.toApiFilename(tag) + "Impl" + suffix;
//        }

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
