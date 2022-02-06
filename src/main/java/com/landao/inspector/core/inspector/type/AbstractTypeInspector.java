package com.landao.inspector.core.inspector.type;

import com.landao.inspector.annotations.special.group.AddIgnore;
import com.landao.inspector.annotations.special.group.Id;
import com.landao.inspector.annotations.special.group.UpdateIgnore;
import com.landao.inspector.model.FeedBack;
import com.landao.inspector.model.collection.TypeSet;
import com.landao.inspector.model.exception.InspectorException;
import com.landao.inspector.utils.InspectUtils;
import com.landao.inspector.utils.InspectorManager;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;


/**
 * 做一些必要的初始化工作
 */
public abstract class AbstractTypeInspector implements TypeInspector {

    @Override
    public TypeSet supportClasses() {
        return supportedChain(new TypeSet());
    }

    public abstract TypeSet supportedChain(TypeSet set);

    @Override
    public FeedBack inspect(AnnotatedElement annotatedElement, Object value, String beanName, String fieldName, Class<?> group) {
        if (group != null) {
            if (isAddGroup(group)) {//就处理这两种,其他放行
                if (requireSetNull(annotatedElement)) {
                    return FeedBack.pass(null);
                }
            } else if (isUpdateGroup(group)) {
                UpdateIgnore updateIgnore = AnnotationUtils.findAnnotation(annotatedElement, UpdateIgnore.class);
                if (updateIgnore != null) {
                    return FeedBack.pass(null);
                }
                Id id = AnnotationUtils.findAnnotation(annotatedElement, Id.class);
                if (id != null) {
                    String idName=getIdName(fieldName);
                    if(value==null){
                        return FeedBack.illegal(fieldName,"修改"+beanName+"时必须指明"+idName);
                    }
                    Class<?> valueType = value.getClass();
                    if (InspectorManager.isLong(valueType)) {
                        if ( (Long) value <= 0) {
                            return FeedBack.illegal(fieldName,beanName+idName+"不合法");
                        }
                    } else if (InspectorManager.isInteger(valueType)) {
                        if ((Integer)value <= 0) {
                            return FeedBack.illegal(fieldName,beanName+idName+"不合法");
                        }
                    } else if (InspectorManager.isString(valueType)) {
                        if (!StringUtils.hasText((String) value)) {
                            return FeedBack.illegal(fieldName,beanName+idName+"不合法");
                        }
                    }else {
                        throw new InspectorException("不推荐以"+valueType.getName()+"作为id");
                    }
                }
            }
        }
        Nullable nullable = AnnotationUtils.findAnnotation(annotatedElement, Nullable.class);

        if (nullable != null && value == null) {
            //没有标注的我不能报错,因为用户可能想自己检查这些未标注的字段
            return FeedBack.pass();
        }
        //能走到这里的,有两种可能,没有标注nullable，字段是否为null不清楚或者字段不为null,所以下面需要注意非空判断
        return specialInspect(annotatedElement, value, beanName, fieldName, group);
    }

    public abstract FeedBack specialInspect(AnnotatedElement annotatedElement, Object value, String beanName, String fieldName, Class<?> group);

    protected String getDisplayName(String beanName,String fieldName){
        if(StringUtils.hasText(beanName)){
            return beanName+"的"+fieldName;
        }else {
            return fieldName;
        }
    }

    private String getIdName(String fieldName){
        return fieldName.split("\\.")[1];
    }

    //简化工具类,直接内部调用
    protected final boolean isAddGroup(Class<?> group) {
        return InspectUtils.isAddGroup(group);
    }

    protected final boolean isUpdateGroup(Class<?> group) {
        return InspectUtils.isUpdateGroup(group);
    }

    private boolean requireSetNull(AnnotatedElement field) {
        Id id = AnnotationUtils.findAnnotation(field, Id.class);
        if (id != null) {
            return true;
        }
        AddIgnore addIgnore = AnnotationUtils.findAnnotation(field, AddIgnore.class);
        if (addIgnore != null) {
            return true;
        }
        return false;
    }



}