package com.genxiaogu.ratelimiter.processor;

import com.genxiaogu.ratelimiter.annotation.Limiter;
import org.springframework.util.StringUtils;

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
@SupportedAnnotationTypes("com.genxiaogu.ratelimiter.annotation.Limiter")
public class LimiterProcessor extends AbstractProcessor {

    /**
     * 检查注解的适用方法
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("hello world==========" ) ;
        for (TypeElement currentAnnotation : annotations) {
            Name qualifiedName = currentAnnotation.getQualifiedName();
            if (qualifiedName.contentEquals("Limiter")) {
                Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(currentAnnotation);
                for (Element element : annotatedElements) {
                    Limiter limiter = element.getAnnotation(Limiter.class);
                    String router = limiter.route();
                    int limit = limiter.limit();
                    if (limit <= 0 ) {

                        String errMsg = "Limiter cannot be negative. limit = " + limit ;
                        Messager messager = this.processingEnv.getMessager();
                        messager.printMessage(Diagnostic.Kind.ERROR , errMsg , element);
                        return true ;
                    }else if(StringUtils.isEmpty(router)){
                        String errMsg  = "router cannot be empty ." ;
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
