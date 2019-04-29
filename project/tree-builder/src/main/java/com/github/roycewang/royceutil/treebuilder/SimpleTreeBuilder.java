package com.github.roycewang.royceutil.treebuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class SimpleTreeBuilder<T,K>extends TreeBuilder<T,K>{
    private static final String GETTER_PRE = "get";
    private static final String SETTER_PRE = "set";
    private final String idField;
    private final String pidField;
    private final String childrenField;
    private final boolean noParentAsRoot;
    private final Comparator<T> sorter;
    private final K rootPid;
    private Method idGetter;
    private Method pidGetter;
    private Method childrenGetter;
    private Class<? extends Collection<T>> childrenType;
    private Method childrenSetter;
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,K rootPid,boolean noParentAsRoot,Comparator<T> sorter){
        super();
        this.idField = idField;
        this.pidField = pidField;
        this.childrenField = childrenField;
        this.noParentAsRoot = noParentAsRoot;
        this.rootPid = rootPid;
        this.sorter = sorter;
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,K rootPid,Comparator<T> sorter){
        this(idField,pidField,childrenField,rootPid,false,sorter);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,boolean noParentAsRoot,Comparator<T> sorter){
        this(idField,pidField,childrenField,null,noParentAsRoot,sorter);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,Comparator<T> sorter){
        this(idField,pidField,childrenField,null,sorter);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,K rootPid,boolean noParentAsRoot){
        this(idField,pidField,childrenField,rootPid,noParentAsRoot,null);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,K rootPid){
        this(idField,pidField,childrenField,rootPid,null);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField,boolean noParentAsRoot){
        this(idField,pidField,childrenField,noParentAsRoot,null);
    }
    public SimpleTreeBuilder(String idField,String pidField,String childrenField){
        this(idField,pidField,childrenField,(Comparator<T>)null);
    }
    @Override
    public void build(Collection<T> tree,Iterable<T> items){
        if(items != null){
            Iterator<T> it = items.iterator();
            if(it.hasNext()){
                T item = it.next();
                if(item != null){
                    Class<? extends Object> cls = item.getClass();
                    try{
                        this.idGetter = cls.getMethod(GETTER_PRE + this.capitalize(idField));
                        this.pidGetter = cls.getMethod(GETTER_PRE + this.capitalize(pidField));
                        this.childrenGetter = cls.getMethod(GETTER_PRE + this.capitalize(childrenField));
                        if(this.childrenGetter != null){
                            @SuppressWarnings("unchecked")
                            Class<? extends Collection<T>> childrenType = (Class<? extends Collection<T>>)this.childrenGetter.getReturnType();
                            this.childrenType = childrenType;
                            this.childrenSetter = cls.getMethod(SETTER_PRE + this.capitalize(childrenField),this.childrenType);
                        }
                    }catch(NoSuchMethodException | SecurityException e){
                        throw new RuntimeException("Failed to retrieve getter method for field '" + this.idField + "'/'" + this.pidField + "'/'" + this.childrenField + "'.",e);
                    }
                }
            }
        }
        super.build(tree,items);
    }
    @Override
    protected K getId(T item){
        K id = null;
        if(this.idGetter != null){
            try{
                @SuppressWarnings("unchecked")
                K ret = (K)this.idGetter.invoke(item);
                id = ret;
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                throw new RuntimeException("Failed to retrieve field '" + this.idField + "'.",e);
            }
        }
        return id;
    }
    @Override
    protected K getParentId(T item){
        K pid = null;
        if(this.pidGetter != null){
            try{
                @SuppressWarnings("unchecked")
                K ret = (K)this.pidGetter.invoke(item);
                pid = ret;
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                throw new RuntimeException("Failed to retrieve field '" + this.pidField + "'.",e);
            }
        }
        return pid;
    }
    @Override
    protected Collection<T> getChildren(T pitem){
        Collection<T> children = null;
        if(this.childrenGetter != null){
            try{
                @SuppressWarnings("unchecked")
                Collection<T> ret = (Collection<T>)this.childrenGetter.invoke(pitem);
                if(ret == null){
                    ret = this.childrenType.newInstance();
                    this.childrenSetter.invoke(pitem,ret);
                }
                children = ret;
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e){
                throw new RuntimeException("Failed to retrieve field '" + this.childrenField + "'.",e);
            }
        }
        return children;
    }
    @Override
    protected boolean isRoot(T item){
        K pid = this.getParentId(item);
        return this.rootPid == pid || this.rootPid != null && this.rootPid.equals(pid);
    }
    @Override
    protected Comparator<T> initComparator(){
        return this.sorter;
    }
    @Override
    protected void itemWithoutParent(Collection<T> tree,T item,K id,K pid){
        if(this.noParentAsRoot){
            tree.add(item);
        }
    }
    protected String capitalize(String str){
        String result = str;
        if(str != null && !str.isEmpty()){
            char first = str.charAt(0);
            if(!Character.isUpperCase(first)){
                result = (new StringBuilder()).append(Character.toUpperCase(first)).append(str.substring(1)).toString();
            }
        }
        return result;
    }
}
