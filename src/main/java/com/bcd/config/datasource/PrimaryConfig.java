package com.bcd.config.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        entityManagerFactoryRef="entityManagerFactoryPrimary",
//        transactionManagerRef="transactionManagerPrimary",
//        basePackages = {
//                "com.base",
//                "com.incar.cvp.car.baseinfo",
//                "com.incar.cvp.car.control",
//                "com.incar.cvp.car.device",
//                "com.incar.cvp.car.driver",
//                "com.incar.cvp.car.report",
//                "com.incar.cvp.sys",
//                "com.incar.icudf"
//        },
//        excludeFilters = {
//                //@ComponentScan.Filter(type=FilterType.REGEX,pattern = {"com\\.incar\\.cvp\\.car\\.data\\.{1}.+"}),
//                @ComponentScan.Filter(type=FilterType.ANNOTATION, value=Service.class),
//                @ComponentScan.Filter(type=FilterType.ANNOTATION, value=Controller.class)
//        }
//)
public class PrimaryConfig {

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;

    @Primary
    @Bean(name = "entityManagerPrimary")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryPrimary(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryPrimary")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryPrimary (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource)
                .properties(getVendorProperties(primaryDataSource))
                .packages(
                        "com.base",
                        "com.incar.cvp.car.baseinfo",
                        "com.incar.cvp.car.control",
                        "com.incar.cvp.car.device",
                        "com.incar.cvp.car.driver",
                        "com.incar.cvp.car.report",
                        "com.incar.cvp.sys",
                        "com.incar.icudf"
                ) //设置实体类所在位置
                .persistenceUnit("primaryPersistenceUnit")
                .build();
    }

    private Map<String, String> getVendorProperties(DataSource dataSource) {
        return jpaProperties.getHibernateProperties(dataSource);
    }

    @Primary
    @Bean(name = "transactionManagerPrimary")
    public PlatformTransactionManager transactionManagerPrimary(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryPrimary(builder).getObject());
    }
}