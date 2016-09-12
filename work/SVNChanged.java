import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aemoney.common.util.StringUtil;

/**
 * Created by ae-mp02 on 2016/9/1.
 *
 * 根据 svn 两个版本的提交记录分组列出改变了哪些文件，并统计出有改动的系统，再细分为有改动的包
 *
 * logs.log 文件是用 svn log -v -r XXX:XXX 导出两个版本日志记录
 */
public class SVNChanged {

    private static final String rootPath = "D:\\code";
    private static final String logFilePath = "D:\\code\\develop\\logs.log";
    private static Set<String> paths;
    private BufferedReader reader;

    @Before
    public void setUp() throws Exception {
        paths = new TreeSet<String>();
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(logFilePath)), "GBK"));
    }

    @After
    public void end() throws Exception {
        reader.close();
    }

    @Test
    public void parse() throws Exception {
        Handler.doHandler(reader);
        display();
    }

    private void display(){
        System.out.println("更改的系统：");
        for(String sys : getChangedSystems()) {
            System.out.println(sys);
        }
        System.out.println("=========================");
        for(Map.Entry<String, Set<String>> e : getTypeFilesMap().entrySet()){
            System.out.println(e.getKey());
            System.out.println("=========================");
            for(String f : e.getValue()) {
                System.out.println("\t" + f);
            }
        }
    }

    private Set<String> getChangedSystems(){
        Set<String> systems = new HashSet<String>();
        for(String p : paths) {
            List<String> ls = path2List(p);
            if(ls.size() > 4) systems.add(path2List(p).get(4));
        }
        return systems;
    }

    private Map<String, Set<String>> getTypeFilesMap(){
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for(String p : paths) {
            String kv[] = getLastPath(p).split("\\.");
            if(2 == kv.length) {
                put(map, kv[1], p);
            }
        }
        return map;
    }

    private Set<String> getChangedModels(Set<String> paths) throws Exception {
        Set<String> set = new HashSet<String>();
        for(String p : paths) {
            List<String> list = path2List(p);
            for(int i = list.size() - 1; i >= 0; --i) {
                File dir = new File(rootPath + list2Path(list.subList(0, i)));
                if(dir.exists()) {
                    for (File f : dir.listFiles()) {
                        if (f.getName().endsWith("pom.xml")) {
                            set.add(parsePomForModelName(f));
                            break;
                        }
                    }
                }
            }
        }
        return set;
    }

    private String parsePomForModelName(File pom) throws Exception {
        List<Element> childElements = new SAXReader().read(pom).getRootElement().elements();
        for(Element e : childElements) {
            if (StringUtil.equals("artifactId", e.getName())) {
                return e.getTextTrim();
            }
        }
        return null;
    }

    private List<String> path2List(String path){
        return Arrays.asList(path.split("/"));
    }

    private String list2Path(List<String> list){
        StringBuilder sb = new StringBuilder();
        for(String l : list) {
            if(StringUtil.isNotBlank(l))
                sb.append("/" + l);
        }
        return sb.toString();
    }

    private String getLastPath(String path){
        List<String> list = path2List(path);
        return list.get(list.size() - 1);
    }

    private <K, V> void put(Map<K, Set<V>> map, K key, V value){
        Set<V> set = map.get(key);
        if(null == set){
            set = new HashSet<V>();
            map.put(key, set);
        }
        set.add(value);
    }

    private enum Handler{
        Split {
            @Override
            boolean check(String line) {
                return line.matches("-+");
            }
        },
        Version {
            @Override
            boolean check(String line) {
                return line.endsWith("line");
            }
        },
        Changed {
            @Override
            boolean check(String line) {
                return line.startsWith("Changed paths:");
            }
        },
        Path {
            @Override
            boolean check(String line) {
                return line.contains("/develop/");
            }
            @Override
            void doSomething(String line) {
                paths.add(line.substring(line.indexOf("/develop/")));
            }
        },
        Message {
            @Override
            boolean check(String line) {
                return !Split.check(line) &&
                        !Version.check(line) &&
                        !Changed.check(line) &&
                        !Path.check(line);
            }
        },
        Null {
            @Override
            boolean check(String line) {
                return StringUtil.isBlank(line);
            }
        };

        abstract boolean check(String line);
        void doSomething(String line){
//            System.out.println(this.name() + line);
        }

        private static void template(Handler that, String line, BufferedReader reader) throws Exception {
            do{
                if(! that.check(line)) break;
                that.doSomething(line);
            }while(null != (line = reader.readLine()));
            if(null == line) return;
            next(that, line, reader);
        }

        private static void next(Handler h, String line, BufferedReader reader) throws Exception {
            switch (h){
                case Split: template(Version, line, reader); break;
                case Version: template(Changed, line, reader); break;
                case Changed: template(Path, line, reader); break;
                case Path: template(Null, line, reader); break;
                case Null: template(Message, line, reader); break;
                case Message: template(Split, line, reader); break;
            }
        }

        public static void doHandler(BufferedReader reader) throws Exception {
            template(Null, null, reader);
        }
    }
}
