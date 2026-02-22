package mdm_service.masterdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@Getter
@Setter
public class ElasticsearchProperties {

    private String host;
    private int port;

    private Index index = new Index();

    @Getter
    @Setter
    public static class Index {
        private String address;
        private String org;
        private String orgHistory;
    }
}
