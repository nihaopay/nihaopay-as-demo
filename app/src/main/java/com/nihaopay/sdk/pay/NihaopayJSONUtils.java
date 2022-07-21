package com.nihaopay.sdk.pay;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class NihaopayJSONUtils {  
    /** 
     * JSON字符串转成Object对象<br> 
     * 此方法假设没有把子类当成父类去使用<br> 
     * 不然会出现解析出来的对象没有子类属性<br> 
     * @param json 
     * @param clazz 
     * @return 
     */  
    public static Object toObject(String json, Class<?> clazz) {  
        return toObject(json, clazz, null, null);  
    }  
      
    /** 
     * JSON字符串转成Object对象<br> 
     * 此方法可以把子类当成父类去使用<br> 
     * 但是需要提供一个map<br> 
     * 告诉程序对应的(子)类<br> 
     * @param json 
     * @param map 
     * @return 
     */  
    public static Object toObject(String json, Map<String,String> map) {  
        return toObject(json, null, null, map);  
    }  
      
    /** 
     * JSON字符串转成Object对象<br> 
     * 此方法可以把子类当成父类去使用<br> 
     * 但是需要提供一个基础的basePackage<br> 
     * 并且在生成JSON的时候,也需要传入这个basePackage 
     * @param json 
     * @return 
     */  
    public static Object toObject(String json, String basePackage) {  
        return toObject(json, null, basePackage, null);  
    }  
      
    /** 
     * JSON字符串转成Object对象<br> 
     * 此方法可以把子类当成父类去使用<br> 
     * 但是需要提供一个基础的basePackage<br> 
     * 并且在生成JSON的时候,也需要传入这个basePackage 
     * @param json 
     * @return 
     */  
    public static Object toObject(String json, Class<?> clazz, String basePackage) {  
        return toObject(json, clazz, basePackage, null);  
    }  
      
    /** 
     * Object对象转成JSON字符串<br> 
     * 此方法假设没有把子类当成父类去使用<br> 
     * 不然反转的时候可能会出现解析出来的对象没有子类属性<br> 
     * @param obj 
     * @return 
     */  
    public static String toJson(Object obj) {   
        return toJson(obj, null, false);  
    }  
      
    /** 
     * Object对象转成JSON字符串<br> 
     * 对应反转方法：toObject(String json, Map<String,String> map) 
     * @param obj 
     * @param createClassInfo 
     * @return 
     */  
    public static String toJson(Object obj, boolean createClassInfo) {   
        return toJson(obj, null, createClassInfo);  
    }  
      
    /** 
     * Object对象转成JSON字符串<br> 
     * 对应反转方法：toObject(String json, String basePackage) 
     * @param obj 
     * @return 
     */  
    public static String toJson(Object obj, String basePackage) {   
        return toJson(obj, basePackage, false);  
    }  
      
    public static String format(String json) {  
        String fillStringUnit = "  ";  
        if (json == null || json.trim().length() == 0) {  
            return null;  
        }  
  
        int fixedLenth = 0;  
        ArrayList<String> tokenList = new ArrayList<String>();  
        {  
            String jsonTemp = json;  
            // 预读取  
            while (jsonTemp.length() > 0) {  
                String token = getToken(jsonTemp);  
                jsonTemp = jsonTemp.substring(token.length());  
                token = token.trim();  
                tokenList.add(token);  
            }  
        }  
  
        for (int i = 0; i < tokenList.size(); i++) {  
            String token = tokenList.get(i);  
            int length = token.getBytes().length;  
            if (length > fixedLenth && i < tokenList.size() - 1  
                    && tokenList.get(i + 1).equals(":")) {  
                fixedLenth = length;  
            }  
        }  
  
        StringBuilder buf = new StringBuilder();  
        int count = 0;  
        for (int i = 0; i < tokenList.size(); i++) {  
  
            String token = tokenList.get(i);  
  
            if (token.equals(",")) {  
                buf.append(token);  
                doFill(buf, count, fillStringUnit);  
                continue;  
            }  
            if (token.equals(":")) {  
                buf.append(" ").append(token).append(" ");  
                continue;  
            }  
            if (token.equals("{")) {  
                String nextToken = tokenList.get(i + 1);  
                if (nextToken.equals("}")) {  
                    i++;  
                    buf.append("{ }");  
                } else {  
                    count++;  
                    buf.append(token);  
                    doFill(buf, count, fillStringUnit);  
                }  
                continue;  
            }  
            if (token.equals("}")) {  
                count--;  
                doFill(buf, count, fillStringUnit);  
                buf.append(token);  
                continue;  
            }  
            if (token.equals("[")) {  
                String nextToken = tokenList.get(i + 1);  
                if (nextToken.equals("]")) {  
                    i++;  
                    buf.append("[ ]");  
                } else {  
                    count++;  
                    buf.append(token);  
                    doFill(buf, count, fillStringUnit);  
                }  
                continue;  
            }  
            if (token.equals("]")) {  
                count--;  
                doFill(buf, count, fillStringUnit);  
                buf.append(token);  
                continue;  
            }  
  
            buf.append(token);  
            // 左对齐  
            if (i < tokenList.size() - 1 && tokenList.get(i + 1).equals(":")) {  
                int fillLength = fixedLenth - token.getBytes().length;  
                if (fillLength > 0) {  
                    for (int j = 0; j < fillLength; j++) {  
                        buf.append(" ");  
                    }  
                }  
            }  
        }  
        return buf.toString();  
    }  
     
    /** 
     *  
     * @param json 
     *            JSONObject的字符串形式 
     * @param clazz 
     *            Object的class 
     * @return 
     */  
    private static final String ENTER_REPLACE_STR = "\\n";  
    private static Object toObject(String json, Class<?> clazz, String basePackage, Map<String,String> map) {  
        if (json == null || json.equals("")) {  
            return json;  
        } else {  
            json = json.replace("\r\n", ENTER_REPLACE_STR).replace("\n", ENTER_REPLACE_STR);  
        }  
        Object object = null;  
        try {  
            if (json.charAt(0) == '[') {  
                object = toList(json, clazz, basePackage, map);  
            } else if (json.charAt(0) == '{'){  
                JSONObject infoObject = new JSONObject(json);  
                //  
                //  
                if (infoObject.has("class")) {  
                    String className = infoObject.getString("class");  
                    if (className != null) {  
                        if (map != null && map.get(className) != null) {  
                            clazz = Class.forName(map.get(className));  
                        } else if (basePackage != null) {  
                            clazz = Class.forName(basePackage + className);  
                        }  
                    }  
                }  
                if (clazz == null) {  
                    throw new NullPointerException("clazz, basePackage, map不能全部为空。");  
                }  
                object = clazz.newInstance();  
                //  
                //  
                Iterator<?> iterator = infoObject.keys();  
                while (iterator.hasNext()) {  
                    String key = (String) iterator.next();  
                    Object fieldValue = null;  
                    List<Field> fields = getAllFields(null, clazz);  
                    for (int i = 0; i < fields.size(); i++) {  
                        Field field = fields.get(i);  
                        if (field.getName().equalsIgnoreCase(key)) {  
                            field.setAccessible(true);  
                            fieldValue = getFieldValue(field, infoObject, key, basePackage, map);  
                            if (null != fieldValue) {  
                                field.set(object, fieldValue);  
                            }  
                            field.setAccessible(false);  
                        }  
                    }  
                }  
            } else {  
                return json;  
            }  
        } catch (Exception e) {  
            Logger.error("=====JSON EXCEPTION====="+e.getMessage());  
        }  
        if (object instanceof List<?>) {  
            List<?> list = (List<?>) object;  
            for (int i = 0; i < list.size(); i++) {  
                replaceObjectBr(list.get(i));  
            }  
        } else {  
            replaceObjectBr(object);  
        }  
        return object;  
    }  
      
    /** 
     *  
     * @param json 
     *            JSONArray的字符串形式 
     * @param clazz 
     *            List内对象的class 
     * @return 
     */  
    private static List<?> toList(String json, Class<?> clazz, String basePackage, Map<String,String> map) {  
        List<Object> list = null;  
        try {  
            JSONArray jsonArray = (JSONArray) new JSONArray(json);  
            list = new ArrayList<Object>();  
            for (int i = 0; i < jsonArray.length(); i++) {  
                String jsonvalue = jsonArray.getJSONObject(i).toString();  
                switch (getJSONType(jsonvalue)) {  
                case JSON_TYPE_OBJECT: {  
/*                  JSONObject subObject = new JSONObject(jsonvalue); 
                    Class<?> newClazz = null; 
                    if (subObject.has("class")) { 
                        String className = subObject.getString("class"); 
                        if (className != null) { 
                            if (map != null && map.get(className) != null) { 
                                newClazz = Class.forName(map.get(className)); 
                            } else if (basePackage != null) { 
                                newClazz = Class.forName(basePackage + className); 
                            } 
                        } 
                    }*/  
                    list.add(toObject(jsonvalue, clazz, basePackage, map));  
                    break;  
                }  
                case JSON_TYPE_ARRAY: {  
                    List<?> infoList = toList(jsonvalue, clazz, basePackage, map);  
                    list.add(infoList);  
                    break;  
                }  
                default:  
                    break;  
                }  
            }  
        } catch (Exception e) {  
            Logger.error("=====JSON EXCEPTION====="+e.getMessage());  
        }  
        return list;  
    }  
      
    private static String toJson(Object obj, String basePackage, boolean createClass) {  
        if (obj instanceof String) {  
            return obj.toString();  
        } else {  
            StringBuffer xmlBuffer = new StringBuffer("");  
            if (obj instanceof List<?>) {  
                xmlBuffer.append("[");  
                List<?> list = (List<?>) obj;  
                for (int i = 0; i < list.size(); i++) {  
                    addObjectToBuffer(xmlBuffer, list.get(i), basePackage, createClass);  
                    if (i < list.size() - 1) {  
                        xmlBuffer.append(",");  
                    }  
                }  
                xmlBuffer.append("]");  
            } else {  
                addObjectToBuffer(xmlBuffer, obj, basePackage, createClass);  
            }  
            return xmlBuffer.toString();  
        }  
    }  
      
    /** 
     * 在JSON->Object的时候 
     * 根据JSON的键(key) 
     * 生成Field的值(value) 
     * @param field 
     * @param infoObject 
     * @param key 
     * @param basePackage 
     * @return 
     * @throws Exception 
     */  
    private static Object getFieldValue(Field field, JSONObject infoObject, String key, String basePackage, Map<String,String> map) throws Exception {  
        Object fieldValue = null;  
        Class<?> fieldClass = field.getType();  
        if (fieldClass.getSimpleName().toString().equals("int")  
                || fieldClass.getSimpleName().toString().equals(  
                        "Integer")) {  
            fieldValue = infoObject.getInt(key);  
        } else if (fieldClass.getSimpleName().toString()  
                .equalsIgnoreCase("String")) {  
            fieldValue = infoObject.getString(key);  
        } else if (fieldClass.getSimpleName().toString()  
                .equalsIgnoreCase("double")) {  
            fieldValue = infoObject.getDouble(key);  
        } else if (fieldClass.getSimpleName().toString()  
                .equalsIgnoreCase("boolean")) {  
            fieldValue = infoObject.getBoolean(key);  
        } else if (fieldClass.getSimpleName().toString()  
                .equalsIgnoreCase("long")) {  
            fieldValue = infoObject.getLong(key);  
        } else if (fieldClass.getSimpleName().toString()  
                .equalsIgnoreCase("Date")) {  
//            fieldValue = DateFormatUtil.toDate(infoObject.getString(key));  
        } else {  
            String jsonvalue = infoObject.getString(key);  
            switch (getJSONType(jsonvalue)) {  
            case JSON_TYPE_OBJECT: {  
                fieldValue = toObject(jsonvalue, fieldClass, basePackage, map);  
                break;  
            }  
            case JSON_TYPE_ARRAY: {  
                // 获取泛型的class  
                Type genericFieldType = field.getGenericType();  
                if (genericFieldType instanceof ParameterizedType) {  
                    ParameterizedType aType = (ParameterizedType) genericFieldType;  
                    Type[] fieldArgTypes = aType  
                            .getActualTypeArguments();  
                    for (Type fieldArgType : fieldArgTypes) {  
                        Class<?> fieldArgClass = (Class<?>) fieldArgType;  
//                      if (map != null && map.get(key) != null) {  
//                          fieldArgClass = Class.forName(map.get(key));  
//                      }  
                        fieldValue = toList(jsonvalue, fieldArgClass, basePackage, map);  
                    }  
                }  
                break;  
            }  
            default:  
                break;  
            }  
        }  
        return fieldValue;  
    }  
      
    private static List<Field> getAllFields(List<Field> fields, Class<?> clazz) {  
        if (fields == null) {  
            fields = new ArrayList<Field>();  
        }  
        try {  
            if (clazz.getSuperclass() != null) {  
                Field[] fieldsSelf = clazz.getDeclaredFields();  
                for (Field field : fieldsSelf) {  
                    if (!Modifier.isFinal(field.getModifiers())) {  
                        fields.add(field);  
                    }  
                }  
                getAllFields(fields, clazz.getSuperclass());  
            }  
        } catch (Exception e) {  
            Logger.error("=====JSON Exception====="+e.getMessage());  
        }  
        return fields;  
    }  
      
    private static void addObjectToBuffer(StringBuffer xmlBuffer, Object obj, String basePackage, boolean createClass) {  
        xmlBuffer.append("{");  
        // 获取所有属性  
        List<Field> fields = new ArrayList<Field>();  
        getAllFields(fields, obj.getClass());  
        for (int i = -1; i < fields.size(); i++) {  
            if (i == -1) {  
                if (basePackage != null || createClass) {  
                    // 加入类信息  
                    xmlBuffer.append("\"");  
                    xmlBuffer.append("class");  
                    xmlBuffer.append("\":");  
                    xmlBuffer.append("\"");  
                    if (basePackage != null) {  
                        xmlBuffer.append(obj.getClass().getName().replace(basePackage, ""));  
                    } else {  
                        xmlBuffer.append(obj.getClass().getSimpleName());  
                    }  
                    xmlBuffer.append("\",");  
                }  
                continue;  
            }  
            Field field = fields.get(i);  
            String fieldName = field.getName();  
            // 拼接GET方法名  
            Object fieldValue = null;  
            Method method = null;  
            try {  
                String methodName = "get" + ((char) (fieldName.charAt(0) - 32))  
                        + fieldName.substring(1);  
                // System.out.println("methodName->" + methodName);  
                method = obj.getClass().getMethod(methodName);  
            } catch (Exception e) {  
                String methodName = "is" + ((char) (fieldName.charAt(0) - 32))  
                        + fieldName.substring(1);  
                try {  
                    method = obj.getClass().getMethod(methodName);  
                } catch (Exception e1) {  
                    Logger.error("=====To JSON Object Exception====="+e.getMessage());  
                }  
            }  
            if (method != null) {  
                try {  
                    fieldValue = method.invoke(obj);  
                } catch (Exception e) {  
                    Logger.error("=====To JSON Object Exception====="+e.getMessage());  
                }  
            }  
            if (null != fieldValue) {  
                xmlBuffer.append("\"");  
                xmlBuffer.append(fieldName);  
                xmlBuffer.append("\":");  
                if (fieldValue instanceof Date) {  
                    xmlBuffer.append("\"");  
//                    xmlBuffer.append((DateFormatUtil.toTimeFormat((Date) fieldValue)));  
                    xmlBuffer.append("\"");  
                } else if (fieldValue instanceof Integer  
                        || fieldValue instanceof Double  
                        || fieldValue instanceof Long  
                        || fieldValue instanceof Boolean) {  
                    xmlBuffer.append("\"");  
                    xmlBuffer.append(fieldValue.toString());  
                    xmlBuffer.append("\"");  
                } else if (fieldValue instanceof String) {  
                    xmlBuffer.append("\"");  
                    String str = fieldValue.toString();  
                    xmlBuffer.append(str);  
                    xmlBuffer.append("\"");  
                } else if (fieldValue instanceof List<?>) {  
                    addListToBuffer(xmlBuffer, fieldValue, basePackage, createClass);  
                } else {  
                    addObjectToBuffer(xmlBuffer, fieldValue, basePackage, createClass);  
                }  
                xmlBuffer.append(",");  
            }  
            if (i == fields.size() - 1 && xmlBuffer.charAt(xmlBuffer.length() - 1) == ',') {  
                xmlBuffer.deleteCharAt(xmlBuffer.length() - 1);  
            }  
        }  
        xmlBuffer.append("}");  
    }  
  
    private static void addListToBuffer(StringBuffer xmlBuffer, Object fieldvalue, String basePackage,boolean createClass) {  
        xmlBuffer.append("[");  
        List<?> v_list = (List<?>) fieldvalue;  
        for (int i = 0; i < v_list.size(); i++) {  
            addObjectToBuffer(xmlBuffer, v_list.get(i), basePackage, createClass);  
            if (i < v_list.size() - 1) {  
                xmlBuffer.append(",");  
            }  
        }  
        xmlBuffer.append("]");  
    }  
  
    private static JSON_TYPE getJSONType(String str) {  
        final char[] strChar = str.substring(0, 1).toCharArray();  
        final char firstChar = strChar[0];  
        if (firstChar == '{') {  
            return JSON_TYPE.JSON_TYPE_OBJECT;  
        } else if (firstChar == '[') {  
            return JSON_TYPE.JSON_TYPE_ARRAY;  
        } else {  
            return JSON_TYPE.JSON_TYPE_ERROR;  
        }  
    }  
  
    private static String getToken(String json) {  
        StringBuilder buf = new StringBuilder();  
        boolean isInYinHao = false;  
        while (json.length() > 0) {  
            String token = json.substring(0, 1);  
            json = json.substring(1);  
  
            if (!isInYinHao  
                    && (token.equals(":") || token.equals("{")  
                            || token.equals("}") || token.equals("[")  
                            || token.equals("]") || token.equals(","))) {  
                if (buf.toString().trim().length() == 0) {  
                    buf.append(token);  
                }  
  
                break;  
            }  
  
            if (token.equals("\\")) {  
                buf.append(token);  
                buf.append(json.substring(0, 1));  
                json = json.substring(1);  
                continue;  
            }  
            if (token.equals("\"")) {  
                buf.append(token);  
                if (isInYinHao) {  
                    break;  
                } else {  
                    isInYinHao = true;  
                    continue;  
                }  
            }  
            buf.append(token);  
        }  
        return buf.toString();  
    }  
  
    private static void doFill(StringBuilder buf, int count, String fillStringUnit) {  
        buf.append("\n");  
        for (int i = 0; i < count; i++) {  
            buf.append(fillStringUnit);  
        }  
    }  
      
    /** 
     * 替换掉/n  
     * @param obj 此Object不可以是List 
     */  
    private static void replaceObjectBr(Object obj) {  
        // 获取所有属性  
        Field[] fields = obj.getClass().getDeclaredFields();  
        for (int i = 0; i < fields.length; i++) {  
            Field field = fields[i];  
            if (!Modifier.isFinal(field.getModifiers())) {  
                continue;  
            }  
            field.setAccessible(true);  
            Object fieldValue = null;  
            try {  
                fieldValue = field.get(obj);  
                if (fieldValue instanceof Integer) {  
                } else if (fieldValue instanceof Double) {  
                } else if (fieldValue instanceof Boolean) {  
                } else if (fieldValue instanceof Long) {  
                } else if (fieldValue instanceof Date) {  
                } else if (fieldValue instanceof String) {  
                    if (fieldValue != null) {  
                        String str = fieldValue.toString();  
                        field.set(obj, str.replaceAll(ENTER_REPLACE_STR, "\n"));  
                    }  
                }else if (fieldValue instanceof List<?>) {  
                    if (fieldValue != null) {  
                        List<?> list = (List<?>) fieldValue;  
                        for (int j = 0; j < list.size(); j++) {  
                            replaceObjectBr(list.get(j));  
                        }  
                    }  
                } else {  
                    if (fieldValue != null) {  
                        replaceObjectBr(fieldValue);  
                    }  
                }  
            } catch (Exception e) {  
                Logger.error("=====JSON Exception====="+e.getMessage());  
            } finally {  
                field.setAccessible(false);  
            }  
        }  
    }  
  
    private enum JSON_TYPE {  
        /** JSONObject */  
        JSON_TYPE_OBJECT,  
        /** JSONArray */  
        JSON_TYPE_ARRAY,  
        /** 不是JSON格式的字符串 */  
        JSON_TYPE_ERROR  
    }  
}  