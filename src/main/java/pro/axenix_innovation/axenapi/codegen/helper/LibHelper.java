package pro.axenix_innovation.axenapi.codegen.helper;

import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.CodegenOperation;
import pro.axenix_innovation.axenapi.codegen.KafkaCodegenGenerator;

import java.util.List;
import java.util.Map;

public interface LibHelper {
    String CLIENT_TEMPLATE_NAME = "client.mustache";
    String LISTENER_SERVICE_TEMPLATE_NAME = "listenerService.mustache";
    String SPRING_2_AUTOCONFIG_FILENAME = "spring.factories";
    String SPRING_3_AUTOCONFIG_FILENAME = "org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    String MODEL_NAME = "modelName";
    String MODEL_NAME_CAMEL = "modelNameCamel";

    void setTemplates(KafkaCodegenGenerator gen, boolean isInterfaceOnly);

    String addOperationInfo(String tag, String path, Operation operation, CodegenOperation co,
                             Map<String, List<CodegenOperation>> operations);

    String apiFilename(String templateName, String tag, KafkaCodegenGenerator gen);
}
