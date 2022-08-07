package org.lengyu.algorithm;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import org.lengyu.algorithm.common.ListNode;
import org.lengyu.algorithm.common.TreeNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestCaseParser {
    private static TestCaseParser instance = new TestCaseParser();

    private TestCaseParser() {}

    public static TestCaseParser getInstance() {
        return instance;
    }

    public Object parsePara(String s, Type type) {
        return s == null || s.length()==0 ? null : parsePara(Json.parse(s), type);
    }

    public Object parsePara(JsonValue value, Type type) {
        if(type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rType = pType.getRawType();
            Type gType;
            if(rType == List.class) {
                gType = pType.getActualTypeArguments()[0];
                Class<?> clazz = gType instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) gType).getRawType() :
                        (Class<?>) gType;
                return parseList(value, clazz, gType);
            }else if(rType == Map.class) {
                Type[] gTypes = pType.getActualTypeArguments();
                final Type gTypeK = gTypes[0];
                final Type gTypeV = gTypes[1];

                final Class<?> clazzK = gTypeK instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) gTypeK).getRawType() :
                        (Class<?>) gTypeK;
                final Class<?> clazzV = gTypeV instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) gTypeV).getRawType() :
                        (Class<?>) gTypeV;

                return parseMap(value, clazzK, clazzV, gTypeK, gTypeV);
            }
        }else if(type instanceof Class<?>) {
            if(isBasicType(type)) {
                return parseBasicType(value,type);
            }else if(((Class<?>) type).isArray()) {
                return parseArray(value,type);
            }else if(type == ListNode.class) {
                return parseListNode(value);
            }else if(type == TreeNode.class) {
                return parseTreeNode(value);
            }
        }
        return null;
    }

    /**
     * 判断是否是“基本类型”，即 java 的8个基本类型及其包装类，再加上 String
     * @param type
     * @return
     */
    public boolean isBasicType(Type type) {
        return type instanceof Class<?> &&
                (type == int.class || type == Integer.class || type == String.class ||
                        type == long.class || type == Long.class || type == double.class || type == Double.class ||
                        type == float.class || type == Float.class || type == boolean.class || type == Boolean.class ||
                        type == char.class || type == Character.class || type == short.class || type == Short.class ||
                        type == byte.class || type == Byte.class);
    }

    /**
     * 解析 isBasicType 方法定义的"基本类型"
     * @param value
     * @param type
     * @return
     */
    public Object parseBasicType(JsonValue value, Type type) {
        Object res = null;
        if(type == int.class || type == Integer.class) {
            res = value.asInt();
        }else if(type == String.class) {
            res = value.asString();
        }else if(type == long.class || type == Long.class) {
            res = value.asLong();
        }else if(type == double.class || type == Double.class) {
            res = value.asDouble();
        }else if(type == char.class || type == Character.class) {
            res = value.asString().charAt(0);
        }else if(type == float.class || type == Float.class) {
            res = value.asFloat();
        }else if(type == boolean.class || type == Boolean.class) {
            res = value.asBoolean();
        }else if(type == short.class || type == Short.class) {
            int tmp = value.asInt();
            if(tmp > Short.MAX_VALUE) tmp = Short.MAX_VALUE;
            else if(tmp < Short.MIN_VALUE) tmp = Short.MIN_VALUE;
            return (short)tmp;
        }else if(type == byte.class || type == Byte.class) {
            int tmp = value.asInt();
            if(tmp > Byte.MAX_VALUE) tmp = Byte.MAX_VALUE;
            else if(tmp < Byte.MIN_VALUE) tmp = Byte.MIN_VALUE;
            return (byte)tmp;
        }
        return res;
    }

    public ListNode parseListNode(String s) {
        return parseListNode(Json.parse(s));
    }

    /**
     * 将 value 解析为 ListNode
     * @param value
     * @return
     */
    public ListNode parseListNode(JsonValue value) {
        JsonArray arr = value.asArray();
        if(arr.size() == 0 || arr.get(0).isNull()) return null;
        ListNode head = new ListNode(arr.get(0).asInt());
        ListNode node = head;
        for(int j = 1; j < arr.size(); j++) {
            node.next = new ListNode(arr.get(j).asInt());
            node = node.next;
        }
        return head;
    }

    public ListNode parseListNode(int[] a) {
        if(a.length == 0) return null;
        ListNode head = new ListNode(a[0]);
        ListNode node = head;
        for(int j = 1; j < a.length; j++) {
            node.next = new ListNode(a[j]);
            node = node.next;
        }
        return head;
    }

    public TreeNode parseTreeNode(String s) {
        return parseTreeNode(Json.parse(s));
    }

    /**
     * 将 value 解析为 TreeNode
     * @param value
     * @return
     */
    public TreeNode parseTreeNode(JsonValue value) {
        JsonArray arr = value.asArray();
        if(arr.size() == 0 || arr.get(0).isNull()) return null;
        TreeNode root = new TreeNode(arr.get(0).asInt());
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        boolean left = true;
        for(int j = 1; j < arr.size(); j++) {
            JsonValue jv = arr.get(j);
            TreeNode t = null;
            if(!jv.isNull()) {
                t = new TreeNode(jv.asInt());
                TreeNode t0 = queue.peek();
                if(left) t0.left = t;
                else t0.right = t;
                queue.add(t);
            }
            if(!left) queue.poll();
            left = !left;
        }
        return root;
    }

    /**
     * 将 JsonValue 解析为指定类型的数组，注意此方法支持的数组类型有限
     * @param value
     * @param type
     * @return
     */
    public Object parseArray(JsonValue value, Type type) {
        JsonArray arr = value.asArray();
        int n = arr.size();
        Object res = null;
        if(type == int[].class) {
            int[] a = new int[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asInt();}
            res = a;
        }else if(type == String[].class) {
            String[] a = new String[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asString();}
            res = a;
        }else if(type == int[][].class) {
            if(n == 0) return new int[0][0];
            JsonArray arr1 = arr.get(0).asArray();
            int m = arr1.size();
            int[][] a = new int[n][m];
            for(int i = 0; i < n; i++) {
                arr1 = arr.get(i).asArray();
                for(int j = 0; j < m; j++) {a[i][j] = arr1.get(j).asInt();}
            }
            res = a;
        }else if(type == double[].class) {
            double[] a = new double[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asDouble();}
            res = a;
        }else if(type == char[].class) {
            char[] a = new char[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asString().charAt(0);}
            res = a;
        }else if(type == long[].class) {
            long[] a = new long[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asLong();}
            res = a;
        }else if(type == ListNode[].class) {
            ListNode[] a = new ListNode[n];
            for(int i = 0; i < a.length; i++) {a[i] = parseListNode(arr.get(i));}
            res = a;
        }else if(type == TreeNode[].class) {
            TreeNode[] a = new TreeNode[n];
            for(int i = 0; i < a.length; i++) {a[i] = parseTreeNode(arr.get(i));}
            res = a;
        }else if(type == char[][].class) {
            if(n == 0) return new char[0][0];
            JsonArray arr1 = arr.get(0).asArray();
            int m = arr1.size();
            char[][] a = new char[n][m];
            for(int i = 0; i < n; i++) {
                arr1 = arr.get(i).asArray();
                for(int j = 0; j < m; j++) {a[i][j] = arr1.get(j).asString().charAt(0);}
            }
            res = a;
        }else if(type == double[][].class) {
            if(n == 0) return new double[0][0];
            JsonArray arr1 = arr.get(0).asArray();
            int m = arr1.size();
            double[][] a = new double[n][m];
            for(int i = 0; i < n; i++) {
                arr1 = arr.get(i).asArray();
                for(int j = 0; j < m; j++) {a[i][j] = arr1.get(j).asDouble();}
            }
            res = a;
        }else if(type == String[][].class) {
            if(n == 0) return new String[0][0];
            JsonArray arr1 = arr.get(0).asArray();
            int m = arr1.size();
            String[][] a = new String[n][m];
            for(int i = 0; i < n; i++) {
                arr1 = arr.get(i).asArray();
                for(int j = 0; j < m; j++) {a[i][j] = arr1.get(j).asString();}
            }
            res = a;
        }else if(type == long[][].class) {
            if(n == 0) return new long[0][0];
            JsonArray arr1 = arr.get(0).asArray();
            int m = arr1.size();
            long[][] a = new long[n][m];
            for(int i = 0; i < n; i++) {
                arr1 = arr.get(i).asArray();
                for(int j = 0; j < m; j++) {a[i][j] = arr1.get(j).asLong();}
            }
            res = a;
        }else if(type == float[].class) {
            float[] a = new float[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asFloat();}
            res = a;
        }else if(type == byte[].class) {
            byte[] a = new byte[n];
            for(int i = 0; i < a.length; i++) {a[i] = (byte)(arr.get(i).asInt());}
            res = a;
        }else if(type == short[].class) {
            short[] a = new short[n];
            for(int i = 0; i < a.length; i++) {a[i] = (short)(arr.get(i).asInt());}
            res = a;
        }else if(type == boolean[].class) {
            boolean[] a = new boolean[n];
            for(int i = 0; i < a.length; i++) {a[i] = arr.get(i).asBoolean();}
            res = a;
        }

        return res;
    }

    /**
     * 将 JsonValue 对象解析为 List<E>，可以解析嵌套类型，注意 clazz 和 type 都是 E，不是 List 本身
     * @param value
     * @param clazz
     * @param cType
     * @return
     * @param <E>
     */
    public <E> List<E> parseList(JsonValue value, Class<E> clazz, Type cType) {
        List<E> list = new ArrayList<>();
        value.asArray().forEach((v)->{list.add((E) parsePara(v,cType));});
        return list;
        /*
        JsonArray arr = value.asArray();
        List<E> list = new ArrayList<>();
        if(cType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) cType;
            Type rType = pType.getRawType();
            if(rType == List.class) {
                final Type cType1 = pType.getActualTypeArguments()[0];

                final Class<?> clazz1 = cType1 instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) cType1).getRawType() :
                        (Class<?>) cType1;

                arr.forEach((v)->{list.add((E) (Object) (parseList(v, clazz1, cType1)));});
            }else if(rType == Map.class){
                Type[] gTypes = pType.getActualTypeArguments();
                final Type gTypeK = gTypes[0];
                final Type gTypeV = gTypes[1];

                final Class<?> clazzK = gTypeK instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) gTypeK).getRawType() :
                        (Class<?>) gTypeK;
                final Class<?> clazzV = gTypeV instanceof ParameterizedType ?
                        (Class<?>) ((ParameterizedType) gTypeV).getRawType() :
                        (Class<?>) gTypeV;

                arr.forEach((v)->{list.add((E) (Object) (parseMapNaive(v, clazzK, clazzV, gTypeK, gTypeV)));});
            }
        }else if(cType == Integer.class){
            arr.forEach((v)->{list.add((E) (Object) v.asInt());});
        }else if(cType == String.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asString());});
        }else if(cType == Double.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asDouble());});
        }else if(cType == Long.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asDouble());});
        }else if(cType == Float.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asFloat());});
        }else if(cType == Boolean.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asBoolean());});
        }else if(cType == Character.class) {
            arr.forEach((v)->{list.add((E) (Object) v.asString().charAt(0));});
        }else if(cType == Short.class) {
            arr.forEach((v)->{list.add((E) (Object) (short) v.asInt());});
        }else if(cType == Byte.class) {
            arr.forEach((v)->{list.add((E) (Object) (byte) v.asInt());});
        }else {
            arr.forEach((v)->{list.add((E) (Object) parsePara(v,cType));});
        }*/
    }


    /**
     * 将 JsonValue 对象解析为 Map<K,V>，可以解析嵌套类型
     * @param value
     * @param clazzK
     * @param clazzV
     * @param typeK
     * @param typeV
     * @return
     * @param <K>
     * @param <V>
     */
    public <K,V> Map<K,V> parseMap(JsonValue value, Class<K> clazzK, Class<V> clazzV, Type typeK, Type typeV) {
        Map<K,V> map = new HashMap<>();
        value.asArray().forEach((v)->{
            map.put((K)parsePara(v.asArray().get(0), typeK),
                    (V)parsePara(v.asArray().get(1), typeV));
        });
        return map;
    }
}
