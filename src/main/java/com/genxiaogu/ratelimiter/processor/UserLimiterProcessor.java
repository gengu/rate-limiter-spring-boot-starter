package com.genxiaogu.ratelimiter.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.genxiaogu.ratelimiter.annotation.Limiter;
import com.genxiaogu.ratelimiter.annotation.UserLimiter;
import org.springframework.util.StringUtils;

/**
 * 编译阶段检查
 * @author genxiaogu
 */
@SupportedAnnotationTypes({"com.genxiaogu.ratelimiter.annotation.UserLimiter"})
public class UserLimiterProcessor extends AbstractProcessor {

    /**
     * 参数拦截
     */
    public static String userLimiterName = "com.genxiaogu.ratelimiter.annotation.UserLimiter" ;

    /**
     * 检查注解的适用方法
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement currentAnnotation : annotations) {
            Name qualifiedName = currentAnnotation.getQualifiedName();
            if (qualifiedName.contentEquals(userLimiterName)) {
                Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(currentAnnotation);
                for (Element element : annotatedElements) {
                    UserLimiter limiter = element.getAnnotation(UserLimiter.class);
                    String router = limiter.route();
                    int limit = limiter.limit();
                    if (limit <= 0 ) {
                        String errMsg = "UserLimiter limit argument cannot be negative. limit = " + limit ;
                        Messager messager = this.processingEnv.getMessager();
                        messager.printMessage(Diagnostic.Kind.ERROR , errMsg , element);
                        return true ;
                    }else if(StringUtils.isEmpty(router)){
                        String errMsg  = "UserLimiter route argument cannot be empty ." ;
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
