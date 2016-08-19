package com.plugin.core.annotation;

import java.lang.annotation.Annotation;

public class AnnotationProcessor {

    public static FragmentContainer getFragmentContainer(Class clazz) {
        FragmentContainer fragmentContainer = (FragmentContainer)clazz.getAnnotation(FragmentContainer.class);
        return fragmentContainer;
    }

    public static ComponentContainer getComponentContainer(Class clazz) {
        ComponentContainer componentContainer = (ComponentContainer)clazz.getAnnotation(ComponentContainer.class);
        return componentContainer;
    }

}
