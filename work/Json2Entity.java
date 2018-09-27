package com.glbpay.pgw.service;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单从接口文档定义（json）转换为java模型
 */
public class Json2Entity {

    private static final String DATA;
    
    @Test
    public void create() throws Exception {
        StringReader reader = new StringReader(DATA);
        JsonObject json = parseJsonObject(reader);
        String entity = dumpJson("Root", json);
        System.out.println(entity);
    }

    // region 导出Entity
    public String dumpJson(String name, JsonObject json) throws Exception {
        StringBuilder buf = new StringBuilder(String.format("private Class %s {\n", toCamel(name, true)));
        for (Peer p : json.values) {
            buf.append(dumpPeer(p));
        }
        buf.append("}\n");
        return buf.toString();
    }

    public String dumpArray(String name, JsonArray array) throws Exception {
        MyJson i = array.values.get(0);
        if (i instanceof JsonObject) {
            String template =
                    "\t@JSONField(name=\"%s\")\n" +     // 原始Key
                    "\tList<%s> %s;\n" +                // 转换后的Key， 转换后的Key
                    "%s";                               // 类
            return String.format(template, name, toCamel(name, true), toCamel(name), dumpJson(name, (JsonObject) i));
        } else if (i instanceof Atom) {
            Atom a = (Atom) i;
            String className = a.value.getClass().getSimpleName();
            String template =
                    "\t@JSONField(name=\"%s\")\n" +     // 原始Key
                    "\tList<%s> %s;\n";                 // 转换后的Key， 转换后的Key
            return String.format(template, name, className, toCamel(name));
        } else {
            throw new Exception("不支持的类型解析" + i.getClass());
        }
    }

    private String dumpPeer(Peer peer) throws Exception {
        String annotation = (null == peer.annotation) ? "" : ("\t/*" + peer.annotation.trim() + "*/\n");
        if (peer.value instanceof JsonObject) {
            String template =
                    "%s" +  // 注释
                    "%s";   // 类详情
            return String.format(template, annotation, dumpJson(peer.key, (JsonObject) peer.value));
        } else if (peer.value instanceof JsonArray) {
            String template = dumpArray(peer.key, (JsonArray) peer.value);
            return String.format(template, toCamel(peer.key));
        } else if (peer.value instanceof Atom) {
            String template =
                    "\n\t@JSONField(name=\"%s\")\n" +   // Peer原始Key
                    "%s" +                            // Peer的注释
                    "\tprivate %s %s;\n";             // ClassType， Peer转换后的Key， Atom的注释
            String className = dumpAtom((Atom) peer.value);
            return String.format(template, peer.key, annotation, className, toCamel(peer.key));
        } else {
            throw new Exception("不支持的类型解析" + peer.value.getClass());
        }
    }

    private String dumpAtom(Atom atom) throws Exception {
        return atom.value.getClass().getSimpleName();
    }
    // endregion

    // region 自定义json类
    public interface MyJson {}

    public class JsonObject implements MyJson {
        List<Peer> values;
    }

    public class JsonArray implements MyJson {
        List<MyJson> values;
    }

    public class Peer implements MyJson {
        String key;
        MyJson value;
        String annotation;
    }

    public class Atom implements MyJson {
        Object value;
    }
    // endregion

    // region json 解析
    private static final String BLANK_CHARS = "\u00A0 \n\t";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String QUOTATION_CHARS = "“”\"";
    public JsonObject parseJsonObject(StringReader reader) throws Exception {
        JsonObject json = new JsonObject();
        char c = reader.ignore(BLANK_CHARS);
        if ('{' != c) throw new Exception("解析失败：" + c);

        List<Peer> peers = new ArrayList<>();
        while (true) {
            peers.add(parsePeer(reader));
            c = reader.ignore(BLANK_CHARS);
            if ('}' == c) {
                json.values = peers;
                return json;
            } else {
                reader.backspace();
            }
        }
    }

    public JsonArray parseJsonArray(StringReader reader) throws Exception {
        JsonArray array = new JsonArray();
        char c = reader.ignore(BLANK_CHARS);
        if ('[' != c) throw new Exception("解析失败：" + c);

        List<MyJson> list = new ArrayList<>();
        while (true) {
            c = reader.ignore(BLANK_CHARS);
            if (QUOTATION_CHARS.contains(c+"")) {
                reader.backspace();
                list.add(parseAtom(reader));
            } else if ('{' == c) {
                reader.backspace();
                list.add(parseJsonObject(reader));
            } else if (']' == c) {
                array.values = list;
                return array;
            } else if ("/,".contains(c+"")) {
                reader.until("\n");     // 做下兼容，忽略至行尾
            } else {
                throw new Exception("解析失败：" + c);
            }
        }
    }

    private Peer parsePeer(StringReader reader) throws Exception {
        Peer peer = new Peer();
        char c = reader.ignore(BLANK_CHARS);
        if (!QUOTATION_CHARS.contains(c+"")) throw new Exception("解析失败：" + c);
        peer.key = reader.until(QUOTATION_CHARS);

        c = reader.ignore(BLANK_CHARS);
        if (':' != c) throw new Exception("解析失败：" + c);

        c = reader.ignore(BLANK_CHARS);
        reader.backspace();
        if ('{' == c) {
            peer.value = parseJsonObject(reader);
        } else if ('[' == c) {
            peer.value = parseJsonArray(reader);
        } else {
            peer.value = parseAtom(reader);
        }

        String end = reader.until("\n");
        if (end.contains("//")) {
            peer.annotation = end.substring(end.indexOf("//")+2).trim();
        }
        return peer;
    }

    private Atom parseAtom(StringReader reader) {
        Atom atom = new Atom();
        char c = reader.ignore(BLANK_CHARS);
        if (QUOTATION_CHARS.contains(c+"")) {
            String str = reader.until(QUOTATION_CHARS);
            atom.value = str;
        } else if (DIGIT_CHARS.contains(c+"")){
            reader.backspace();
            String intStr = reader.untilNot(DIGIT_CHARS);
            atom.value = Integer.valueOf(intStr);
        }
        return atom;
    }
    // endregion

    // region 辅助工具
    private class StringReader {
        int index;
        char[] data;

        public StringReader(String data) {
            this.index = 0;
            this.data = data.toCharArray();
        }

        public char readChar() {
            char c = data[index];
            index++;
            return c;
        }

        public void backspace() {
            index--;
        }

        public char ignore(String chars) {
            char c;
            do {
                c = readChar();
            } while (chars.contains(c+""));
            return c;
        }

        public String until(String chars) {
            StringBuilder buf = new StringBuilder();
            while(true) {
                char c = readChar();
                if (chars.contains(c + "")) {
                    return buf.toString();
                }
                buf.append(c);
            }
        }

        public String untilNot(String chars) {
            StringBuilder buf = new StringBuilder();
            while(true) {
                char c = readChar();
                if (!chars.contains(c + "")) {
                    return buf.toString();
                }
                buf.append(c);
            }
        }
    }

    private String toCamel(String str) {
        return toCamel(str, false);
    }

    private String toCamel(String str, boolean firstUpper) {
        StringBuilder buf = new StringBuilder();
        boolean isUpper = firstUpper;
        for (char c : str.toCharArray()) {
            if ('_' == c) {
                isUpper = true;
            } else {
                buf.append(isUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                if (isUpper) {
                    isUpper = false;
                }
            }
        }
        return buf.toString();
    }
    // endregion
}
