package ms.springex.server.config;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jms.core.JmsTemplate;

import ms.springex.server.service.GcdService;

@Configuration
@PropertySource("classpath:application-test.properties")
public class TestConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ServerConfig.class);

  @Value("${mq.connection.url}")
  private String mqConnectionUrl;

  @Value("${mq.queue.name}")
  private String queueName;

  /*
   * Apache Active MQ Connection.
   * 
   */
  private ActiveMQConnectionFactory getActiveMQConnectionFactory() {
    LOG.info("MQ Connection URL: {}", this.mqConnectionUrl);
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(this.mqConnectionUrl);
    return factory;
  }

  /*
   * JMS Template configured to use Active MQ Connection.
   */
  @Bean
  public JmsTemplate getJmsTemplate() {
    JmsTemplate template = new JmsTemplate(getActiveMQConnectionFactory());
    return template;
  }

  /*
   * Queue Name
   */
  @Bean(name = "destination")
  public ActiveMQQueue getDestinationQueue() {
    LOG.info("MQ Queue Name: {}", this.queueName);
    ActiveMQQueue queue = new ActiveMQQueue(this.queueName);
    return queue;
  }

  @Bean
  public DataSource dataSource() throws NamingException {
    LOG.info("INIT Datasource...");
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.H2) 
        .addScript("schema.sql").build();
    return db;
  }

  @Bean
  public JaxWsProxyFactoryBean gcdProxyFactoryBean() {
      JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
      proxyFactory.setServiceClass(GcdService.class);
      proxyFactory.setAddress("http://localhost:8080/cxf-example/services/gcdservice");
      return proxyFactory;
  }
  
  @Bean(name = "gcdClient")
  public Object generateProxy() {
    return gcdProxyFactoryBean().create();
  }
  
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfig() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
