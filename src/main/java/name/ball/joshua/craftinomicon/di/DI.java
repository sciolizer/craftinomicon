package name.ball.joshua.craftinomicon.di;

import java.lang.reflect.*;
import java.util.*;

public class DI {

    private final Map<Class<?>,Provider<?>> providers;
    private Map<Class<?>,Object> instances = new LinkedHashMap<Class<?>, Object>();

    public DI() {
        this(Collections.<Class<?>,Provider<?>>emptyMap());
    }
    public DI(Map<Class<?>, Provider<?>> providers) {
        this.providers = providers;
    }

    public void injectMembers(Object o) {
        try {
            injectMembers(new ArrayList<Class<?>>(), o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // must be called after `injectMembers`
    public Collection<Object> getAllKnownInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    private void injectMembers(List<Class<?>> constructing, Object o) throws Exception {
        for (Map.Entry<Field, Object> entry : buildFieldMap(constructing, o.getClass()).entrySet()) {
            entry.getKey().set(o, entry.getValue());
        }
    }

    private Map<Field,Object> buildFieldMap(List<Class<?>> constructing, Class<?> aClass) throws Exception {
        Map<Field,Object> result = new LinkedHashMap<Field, Object>();
        do {
            Field[] declaredFields = aClass.getDeclaredFields();
            if (declaredFields != null) {
                for (Field field : declaredFields) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        field.setAccessible(true);
                        Object instance = getInstance(add(constructing, aClass), field.getType());
                        result.put(field, instance);
                    }
                }
            }
        } while ((aClass = aClass.getSuperclass()) != Object.class && aClass != null);
        return result;
    }

    private <T> T getInstance(List<Class<?>> constructing, Class<T> t) throws Exception {
        if (instances.containsKey(t)) {
            return (T)instances.get(t);
        }
        if (constructing.contains(t)) {
            throw new IllegalStateException("infinite loop: " + add(constructing, t));
        }
        List<Class<?>> newConstructing = add(constructing, t);
        T instance;
        if (providers.containsKey(t)) {
            instance = fromProvider(newConstructing, t);
        } else if (t.isInterface()) {
            instance = (T)factory(newConstructing, t);
        } else {
            instance = singleton(newConstructing, t);
        }
        instances.put(t, instance);
        if (!t.isInterface()) injectMembers(constructing, instance);
        notifyPropertiesSet(instance);
        return (T)instance;
    }

    private <T> List<Class<?>> add(List<Class<?>> constructing, Class<T> t) {
        List<Class<?>> newConstructing = new ArrayList<Class<?>>(constructing);
        newConstructing.add(t);
        return newConstructing;
    }

    private <T> T singleton(List<Class<?>> newConstructing, Class<?> t) throws Exception {
        Constructor<?>[] constructors = t.getConstructors();
        if (constructors.length != 1) {
            throw new IllegalStateException("more than one constructor: " + t.getName());
        }
        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            arguments[i] = getInstance(newConstructing, parameterType);
        }
        return (T)constructor.newInstance(arguments);
    }

    private Object factory(List<Class<?>> constructing, Class<?> nterface) throws Exception {
        final Map<Method,FactorySchema> injectables = new LinkedHashMap<Method, FactorySchema>();
        Object factory = newProxy(nterface, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                return injectables.get(method).newInstance(objects);
            }
        });
        instances.put(nterface, factory); // allows for circular dependencies
        for (Method method : nterface.getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            Map<Field, Object> fieldMap = buildFieldMap(constructing, returnType);
            FactorySchema factorySchema;
            try {
                factorySchema = new FactorySchema(returnType, fieldMap);
            } catch (Exception e) {
                throw new RuntimeException("Construction chain: " + constructing, e);
            }
            factorySchema.assertMatchesFactoryMethod(method);
            injectables.put(method, factorySchema);
        }
        return factory;
    }

    private <T> T newProxy(Class<T> nterface, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{nterface}, invocationHandler);
    }

    private <T> T fromProvider(List<Class<?>> constructing, Class<T> resultType) throws Exception {
        Provider provider = providers.get(resultType);
        injectMembers(constructing, provider);
        return (T)provider.get();
    }

    private void notifyPropertiesSet(Object o) throws Exception {
        if (o instanceof InitializingBean) {
            ((InitializingBean)o).afterPropertiesSet();
        }
    }


    public interface Provider<T> {
        T get();
    }

    private static class FactorySchema {

        private final Class<?> klass;
        private final Map<Field,Object> fields;
        private final Constructor<?> constructor;

        public FactorySchema(Class<?> klass, Map<Field, Object> fields) {
            this.klass = klass;
            this.fields = fields;
            this.constructor = getOnlyConstructor();
        }

        public void assertMatchesFactoryMethod(Method method) {
            Type[] constructorParameters = this.constructor.getGenericParameterTypes();
            Type[] methodParameters = method.getGenericParameterTypes();
            if (constructorParameters.length != methodParameters.length) {
                throw new IllegalArgumentException("Constructor on "+ klass + " has " + constructorParameters.length + " parameters but " + methodDescriptor(method) + " has " + methodParameters.length + " parameters");
            }
            for (int i = 0; i < constructorParameters.length; i++) {
                Type constructorParameter = constructorParameters[i];
                Type methodParameter = methodParameters[i];
                if (!constructorParameter.equals(methodParameter)) {
                    throw new IllegalArgumentException("Parameter " + i + " of constructor on " + klass + " is " + constructorParameter + " but on factory method " + methodDescriptor(method) + " is " + methodParameter);
                }
            }
        }

        private String methodDescriptor(Method method) {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        private Constructor<Object> getOnlyConstructor() {
            Constructor<?>[] constructors = klass.getConstructors();
            if (constructors.length != 1) {
                throw new IllegalArgumentException("Number of constructors on " + klass.getName() + " unequal to 1");
            }
            return (Constructor<Object>)constructors[0];
        }

        public Object newInstance(Object[] args) throws Exception {
            Object instance;
            try {
                instance = constructor.newInstance(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (Map.Entry<Field, Object> entry : fields.entrySet()) {
                entry.getKey().set(instance, entry.getValue());
            }
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }
            return instance;
        }
    }

}
