package com.plugin.core.annotation;

import java.lang.annotation.Annotation;

public class AnnotationProcessor {

    public static FragmentContainer getFragmentContainer(Class clazz) {
        FragmentContainer fragmentContainer = (FragmentContainer)clazz.getAnnotation(FragmentContainer.class);
        return fragmentContainer;
    }

}
