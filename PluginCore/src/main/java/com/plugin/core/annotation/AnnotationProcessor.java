package com.plugin.core.annotation;

public class AnnotationProcessor {

    public static FragmentContainer getFragmentContainer(Class clazz) {
        FragmentContainer fragmentContainer = (FragmentContainer)clazz.getAnnotation(FragmentContainer.class);
        return fragmentContainer;
    }

}
