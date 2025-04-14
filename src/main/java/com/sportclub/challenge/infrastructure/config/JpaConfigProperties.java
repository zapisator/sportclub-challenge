package com.sportclub.challenge.infrastructure.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class JpaConfigProperties {

    private Boolean showSql;
    private Map<String, String> properties = new HashMap<>();

    public Map<String, String> buildHibernateProperties() {
        final Map<String, String> hibernateProps = new HashMap<>(this.properties);

        if (this.showSql != null) {
            hibernateProps.put("hibernate.show_sql", String.valueOf(this.showSql));
        }
        return hibernateProps;
    }
}
