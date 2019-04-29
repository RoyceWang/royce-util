package com.github.roycewang.royceutil.treebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import com.alibaba.fastjson.JSON;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class TreeBuilderTest extends TestCase{
    public static class Item implements Serializable{
        private static final long serialVersionUID = 1L;
        private Integer id;
        private String name;
        private Integer pid;
        private List<Item> children = new ArrayList<>();
        public Integer getId(){
            return this.id;
        }
        public void setId(Integer id){
            this.id = id;
        }
        public String getName(){
            return this.name;
        }
        public void setName(String name){
            this.name = name;
        }
        public Integer getPid(){
            return this.pid;
        }
        public void setPid(Integer pid){
            this.pid = pid;
        }
        public List<Item> getChildren(){
            return this.children;
        }
        public void setChildren(List<Item> children){
            this.children = children;
        }
    }
    private static final String BR = System.getProperty("line.separator","\n");
    /**
     * Create the test case
     * @param testName name of the test case
     */
    public TreeBuilderTest(String testName){
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(TreeBuilderTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testBuild(){
        final String json = this.readFileText("test-data.json");
        final String result = this.readFileText("test-data-result.txt");
        List<Item> list = JSON.parseArray(json,Item.class);
        List<Item> tree = new ArrayList<>();
        TreeBuilder<Item,Integer> builder = new TreeBuilder<Item,Integer>(){
            @Override
            protected boolean isRoot(Item itemNoParent){
                return true;
            }
            @Override
            protected Integer getParentId(Item item){
                return item.getPid();
            }
            @Override
            protected Integer getId(Item item){
                return item.getId();
            }
            @Override
            protected Collection<Item> getChildren(Item pitem){
                List<Item> children = pitem.getChildren();
                if(children == null){
                    children = new ArrayList<>();
                    pitem.setChildren(children);
                }
                return children;
            }
            @Override
            protected Comparator<Item> initComparator(){
                return new Comparator<Item>(){
                    @Override
                    public int compare(Item o1,Item o2){
                        Integer id1 = o1 == null ? null : o1.getId();
                        Integer id2 = o2 == null ? null : o2.getId();
                        if(id1 == id2){
                            return 0;
                        }
                        if(id1 == null && id2 != null){
                            return Integer.MAX_VALUE;
                        }
                        if(id2 == null && id1 != null){
                            return Integer.MIN_VALUE;
                        }
                        return id1.compareTo(id2);
                    }
                };
            }
        };
        builder.build(tree,list);
        String treeStr = this.tree2String(tree,0);
        System.out.println(treeStr);
        assertTrue(result.equals(treeStr));
    }
    protected String readFileText(String fileName){
        StringBuilder buff = new StringBuilder();
        try(
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));){
            String line = reader.readLine();
            while(line != null){
                buff.append(line);
                line = reader.readLine();
                if(line != null){
                    buff.append(BR);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return buff.toString();
    }
    protected String tree2String(List<Item> tree,int lvl){
        StringBuilder buff = new StringBuilder();
        if(tree != null){
            if(tree.isEmpty()){
                buff.append("[]");
            }else{
                String indent = this.buildIndent(lvl);
                buff.append("[");
                buff.append(BR);
                for(Item item : tree){
                    Integer id = item.getId();
                    String name = item.getName();
                    Integer pid = item.getPid();
                    String idStr = id == null ? "null" : id.toString();
                    String nameStr = name == null ? "null" : "\"" + name + "\"";
                    String pidStr = pid == null ? "null" : pid.toString();
                    buff.append(indent + "  {id:" + idStr + ",name:" + nameStr + ",pid:" + pidStr + ",children:");
                    buff.append(this.tree2String(item.getChildren(),lvl + 1));
                    buff.append("},");
                    buff.append(BR);
                }
                buff.append(indent + "]");
            }
        }
        return buff.toString();
    }

    protected String buildIndent(int lvl){
        StringBuilder buff = new StringBuilder();
        for(int i = 0;i < lvl;i++){
            buff.append("  ");
        }
        return buff.toString();
    }
}
