package pro.axenix_innovation.axenapi.codegen;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.SupportingFile;

import java.util.Collection;
import java.util.Dictionary;

public interface MyCodegen extends CodegenConfig {
    boolean isKafkaClient();

    String getSourceFolder();

    boolean isUseSpringBoot3();
}
