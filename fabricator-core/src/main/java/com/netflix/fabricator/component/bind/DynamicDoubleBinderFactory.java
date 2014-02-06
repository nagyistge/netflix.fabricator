package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import com.google.common.base.Supplier;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class DynamicDoubleBinderFactory implements PropertyBinderFactory {
    private final static DynamicDoubleBinderFactory instance = new DynamicDoubleBinderFactory();
    
    public static DynamicDoubleBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        if (!clazz.isAssignableFrom(Supplier.class) && !clazz.isAssignableFrom(ListenableSupplier.class)) {
            return null;
        }
        
        ParameterizedType supplierType = (ParameterizedType)method.getGenericParameterTypes()[0];
        final Class<?> argType = (Class<?>)supplierType.getActualTypeArguments()[0];
        if (!argType.isAssignableFrom(Double.class) &&
            !argType.equals(double.class)) {
            return null;
        }
        
        return new PropertyBinder() {
                @Override
                public boolean bind(Object obj, ComponentConfiguration config) throws Exception {
                    Supplier<?> supplier = config.getDynamicValue(propertyName, Double.class);
                    if (supplier != null) {
                        //invoke method only when property exists. Otherwise, let builder
                        //plug-in default values
                        if (supplier.get() != null) {
                            method.invoke(obj, supplier);
                        }
                        return true;
                    }
                    else {
                        //Shouldn't happen
                        return false;
                    }
                }
            };    
    }
}
