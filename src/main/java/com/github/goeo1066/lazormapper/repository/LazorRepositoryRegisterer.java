package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.LazorSqlComposerUtils;
import com.github.goeo1066.lazormapper.composers.delete.LazorDeleteSpec;
import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;
import com.github.goeo1066.lazormapper.composers.upsert.LazorUpsertSpec;
import jakarta.annotation.PostConstruct;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.AsmClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

@Configuration
public class LazorRepositoryRegisterer {
    private static final Logger logger = LoggerFactory.getLogger(LazorRepositoryRegisterer.class);
    private final ApplicationContext applicationContext;
    private final ConfigurableApplicationContext configurableApplicationContext;
    private final NamedParameterJdbcTemplate jdbcTemplate; // todo get instance in init later

    public LazorRepositoryRegisterer(ApplicationContext applicationContext, ConfigurableApplicationContext configurableApplicationContext, NamedParameterJdbcTemplate jdbcTemplate) {
        this.applicationContext = applicationContext;
        this.configurableApplicationContext = configurableApplicationContext;
        this.jdbcTemplate = jdbcTemplate;
    }


    public static <T, S extends T> S proxiedRecord(Class<S> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        RecordComponent[] recordComponents = clazz.getRecordComponents();
        String[] names = new String[recordComponents.length];
        Class<?>[] params = new Class<?>[recordComponents.length];
        Object[] defaults = new Object[recordComponents.length];

        for (int i = 0; i < recordComponents.length; i++) {
            RecordComponent recordComponent = recordComponents[i];
            names[i] = recordComponent.getName();
            params[i] = recordComponent.getType();
        }
        
        try (var make = new ByteBuddy()
                .with(AsmClassReader.Factory.Default.INSTANCE)
                .rebase(clazz)
                .visit(Advice.to(Decorator.class).on(ElementMatchers.namedOneOf(names)))
                .make()) {
            make.load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getConstructor(params)
                    .newInstance(LazorSqlComposerUtils.defaultValues(params));
            System.out.println("SS");
        }
//        var proxied = (S) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Map.class}, (o, method, objects) -> {
//            System.out.println("method name: " + method.getName());
//            return method.invoke(o, objects);
//        });

        return null;

//        RecordComponent[] recordComponents = clazz.getRecordComponents();
//        String[] names = new String[recordComponents.length];
//        Class<?>[] params = new Class<?>[recordComponents.length];
//
//        for (int i = 0; i < recordComponents.length; i++) {
//            RecordComponent recordComponent = recordComponents[i];
//            names[i] = recordComponent.getName();
//            params[i] = recordComponent.getType();
//        }
//
//
//        Object[] values = LazorSqlComposerUtils.defaultValues(params);
//        ByteBuddy byteBuddy = new ByteBuddy();
//        var builder = byteBuddy.redefine(clazz)
//                .visit(Advice.to(MethodInterceptor.class).on(ElementMatchers.namedOneOf(names)));
//        try (var make = builder.make()) {
//            var constructor = make.load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
//                    .getLoaded()
//                    .getConstructor(params);
//
//            return constructor.newInstance(values);
//        }
    }

    public class Decorator {
        static final Set<Object> PROXIES = new HashSet<>();

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static Object enter(
                @Advice.This Object self,
                @Advice.Origin Method method,
                @Advice.AllArguments Object[] arguments) throws Throwable {

            return null;
        }
    }

    public static class MethodInterceptor {
        public static Object intercept(@net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args,
                                       @net.bytebuddy.implementation.bind.annotation.Origin Method method) {
            String string = method.getName();
            System.out.println("method name: " + string);
            return null;
        }

        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Origin String methodName) {
            System.out.println("Entering method: " + methodName);
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return Object returnValue) {
            System.out.println("Exiting method with return: " + returnValue);
        }
    }

    @PostConstruct
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init() throws NoSuchMethodException {
        var list = scanImpls(LazorCrudRepository.class);
        for (WalkClassInfo walkClassInfo : list) {
            LazorCrudRepository t = getInstance(walkClassInfo.subInterface, walkClassInfo.entityClass, jdbcTemplate);
            registerBean(walkClassInfo.subInterface, t);
            logger.info("Bean registered: {}", walkClassInfo.subInterface.getCanonicalName());
        }
        logger.info("Hello, Spring Boot!");
    }

    public <T> void registerBean(Class<T> clazz, T bean) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableApplicationContext.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> bean);
        registry.registerBeanDefinition(clazz.getName(), beanDefinitionBuilder.getBeanDefinition());
    }

    @SuppressWarnings("rawtypes")
    private <T extends LazorCrudRepository> List<WalkClassInfo> scanImpls(Class<T> clazz) {
        String basePackage = getBasePackage();
        if (basePackage == null) {
            throw new RuntimeException("No base package found");
        }
        return findSubClasses(clazz, basePackage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <S extends LazorCrudRepository> List<WalkClassInfo> findSubClasses(Class<S> clazz, String basePackage) {
        List<MetadataReader> metadataReaders = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(walkSuperClasses(clazz, metadataReaders));
        provider.findCandidateComponents(basePackage);

        List<WalkClassInfo> result = new ArrayList<>();
        for (MetadataReader metadataReader : metadataReaders) {
            try {
                Class<S> subclass = (Class<S>) Class.forName(metadataReader.getClassMetadata().getClassName());
                WalkClassInfo walkClassInfo = new WalkClassInfo();
                walkClassInfo.subInterface = subclass;
                if (subclass.getGenericInterfaces().length == 1) {
                    if (subclass.getGenericInterfaces()[0] instanceof ParameterizedType p) {
                        String typeName = p.getActualTypeArguments()[0].getTypeName();
                        walkClassInfo.entityClass = Class.forName(typeName);
                    }
                }
                result.add(walkClassInfo);
            } catch (ClassNotFoundException e) {
                logger.warn("Class not found", e);
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T extends LazorCrudRepository> T getInstance(Class<T> subInterfaceClass, Class<S> entityClass, NamedParameterJdbcTemplate jdbcTemplate) throws NoSuchMethodException {
        LazorCrudRepositoryProxyDelegate<S> delegate = LazorCrudRepositoryProxyDelegate.create(entityClass, "postgresql");
        return (T) Proxy.newProxyInstance(subInterfaceClass.getClassLoader(), new Class[]{subInterfaceClass}, (o, method, objects) -> {
            switch (method.getName()) {
                case "select" -> {
                    return delegate.select(jdbcTemplate, (LazorSelectSpec) objects[0]);
                }
                case "count" -> {
                    return delegate.count(jdbcTemplate, (LazorSelectSpec) objects[0]);
                }
                case "insert" -> {
                    var list = (Collection<S>) objects[0];
                    return delegate.insert(jdbcTemplate, list);
                }
                case "update" -> {
                    var list = (Collection<S>) objects[0];
                    delegate.update(jdbcTemplate, list);
                    return null;
                }
                case "upsert" -> {
                    var list = (Collection<S>) objects[0];
                    var upsertSpec = (LazorUpsertSpec<S>) objects[1];
                    return delegate.upsert(jdbcTemplate, list, upsertSpec);
                }
                case "delete" -> {
                    var deleteSpec = (LazorDeleteSpec) objects[0];
                    delegate.delete(jdbcTemplate, deleteSpec);
                    return null;
                }
            }
            return InvocationHandler.invokeDefault(o, method, objects);
        });
    }

    private <T> TypeFilter walkSuperClasses(Class<T> clazz, List<MetadataReader> out) {
        return new AbstractTypeHierarchyTraversingFilter(true, true) {
            @Override
            public boolean match(@NonNull MetadataReader metadataReader, @NonNull MetadataReaderFactory metadataReaderFactory) throws IOException {
                String[] interfaces = metadataReader.getClassMetadata().getInterfaceNames();
                if (contains(clazz.getCanonicalName(), interfaces)) {
                    out.add(metadataReader);
                }
                return super.match(metadataReader, metadataReaderFactory);
            }
        };
    }

    private boolean contains(String target, String[] array) {
        for (String s : array) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private String getBasePackage() {
        for (Map.Entry<String, Object> stringObjectEntry : applicationContext.getBeansWithAnnotation(ComponentScan.class).entrySet()) {
            return stringObjectEntry.getValue().getClass().getPackageName();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static class WalkClassInfo<S extends LazorCrudRepository> {
        private Class<S> subInterface;
        private Class<?> entityClass;
    }
}
