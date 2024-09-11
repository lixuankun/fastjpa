package ${configuration.daoPackage};

import ${configuration.domainPackage}.${javaName};
import com.efeichong.jpa.FastJpaRepository;

/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
public interface ${javaName}Dao extends FastJpaRepository<${javaName}, ${pkColumnType}> {

}
