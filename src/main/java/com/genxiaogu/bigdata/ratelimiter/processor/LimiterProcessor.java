package com.genxiaogu.bigdata.ratelimiter.processor;

import com.genxiaogu.bigdata.ratelimiter.annotation.Limiter;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * 编译阶段检查
 * @author genxiaogu
 */
@SupportedAnnotationTypes("Limiter")
public class LimiterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement currentAnnotation : annotations) {
            Name qualifiedName = currentAnnotation.getQualifiedName();
            if (qualifiedName.contentEquals("Limiter")) {
                Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(currentAnnotation);
                for (Element element : annotatedElements) {
                    Limiter limiter = element.getAnnotation(Limiter.class);
                    String router = limiter.route();
                    int limit = limiter.limit();
                    if (limit < 0) {
                        String errMsg = "Limiter cannot be negative. limit = " + limit ;
                        Messager messager = this.processingEnv.getMessager();
                        messager.printMessage(Diagnostic.Kind.ERROR , errMsg , element);
                        return true ;
                    }
                }
            }
        }
        return false;
    }

}
