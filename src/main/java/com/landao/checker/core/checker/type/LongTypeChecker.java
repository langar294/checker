package com.landao.checker.core.checker.type;

import com.landao.checker.annotations.Check;
import com.landao.checker.core.checker.Checker;
import com.landao.checker.model.FeedBack;
import com.landao.checker.model.collection.TypeSet;

import java.lang.reflect.AnnotatedElement;

@Checker
public class LongTypeChecker extends AbstractNumberTypeChecker {

    @Override
    public TypeSet supportedChain(TypeSet set) {
        return set.addChain(Long.class).addChain(long.class);
    }


    @Override
    public FeedBack specialTypeCheck(Check check, String displayName, AnnotatedElement annotatedElement, Object value, String fieldName, Class<?> group) {
        long fieldValue=(Long) value;

        long min = check.min();
        long max = check.max();
        if(fieldValue<min || fieldValue>max){
            return FeedBack.illegal(fieldName,displayName+"必须在"+min+"-"+max+"之间");
        }

        return FeedBack.pass();
    }
}
