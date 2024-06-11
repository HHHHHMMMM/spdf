package stirling.software.SPDF.config.security;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:mysql")
public class JasyptConfig {
    @Value("${jasypt.encryptor.password}")
    private String password;

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        return encryptor;
    }
}
