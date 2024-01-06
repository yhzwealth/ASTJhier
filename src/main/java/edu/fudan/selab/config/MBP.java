package edu.fudan.selab.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import edu.fudan.selab.mapper.InitMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MBP {

    public static SqlSession sqlSession = initSqlSessionFactory().openSession(true);

    static {
        sqlSession.getMapper(InitMapper.class).createExtendedTable();
        sqlSession.getMapper(InitMapper.class).createFieldTable();
        sqlSession.getMapper(InitMapper.class).createTypeTable();
        sqlSession.getMapper(InitMapper.class).createParameterTable();
        sqlSession.getMapper(InitMapper.class).createImplementedTable();
        sqlSession.getMapper(InitMapper.class).createMethodTable();
        sqlSession.getMapper(InitMapper.class).setLike();
    }

    private static SqlSessionFactory initSqlSessionFactory() {
//        LogFactory.useStdOutLogging();
        Environment environment = new Environment("Dev", new JdbcTransactionFactory(), dataSource());
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        configuration.addMappers("edu.fudan.selab.mapper");
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
//        configuration.setLogImpl(StdOutImpl.class);

        GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
        globalConfig.setSqlInjector(new DefaultSqlInjector());
        globalConfig.setIdentifierGenerator(new DefaultIdentifierGenerator());
        globalConfig.setSuperMapperClass(BaseMapper.class);
        try {
            registryMapperXml(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.sqlite.JDBC.class);

        File file = new File("typeinfo.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        dataSource.setUrl("jdbc:sqlite:typeinfo.db");
//        dataSource.setUsername("");
//        dataSource.setPassword("");
        return dataSource;
    }

    private static void registryMapperXml(MybatisConfiguration configuration) throws IOException {
        ClassLoader classLoader = MBP.class.getClassLoader();
        URL url = classLoader.getResource("mapping");
        if (url.getProtocol().equals("file")) {
            String path = url.getPath();
            File file = new File(path);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    FileInputStream in = new FileInputStream(f);
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, configuration, f.getPath(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                    in.close();
                }
            }
        } else {
            JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
            JarFile jarFile = urlConnection.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().startsWith("mapping") && jarEntry.getName().endsWith(".xml")) {
                    InputStream in = jarFile.getInputStream(jarEntry);
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, configuration, jarEntry.getName(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                    in.close();
                }
            }
        }

    }
}
