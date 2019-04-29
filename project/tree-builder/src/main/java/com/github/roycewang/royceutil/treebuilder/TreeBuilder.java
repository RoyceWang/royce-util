package com.github.roycewang.royceutil.treebuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class TreeBuilder<T,K>{
    public void build(Collection<T> tree,Iterable<T> items){
        final Map<K,T> cache = new HashMap<>();
        final Iterator<T> cacheIt = items.iterator();
        final Iterator<T> it = items.iterator();
        final Comparator<T> cmp = this.initComparator();
        while(it.hasNext()){
            T item = it.next();
            K pid = this.getParentId(item);
            T pitem = null;
            if(pid != null){
                pitem = this.findParent(cache,cacheIt,pid);
            }
            if(pitem == null){
                if(this.isRoot(item,pid)){
                    tree.add(item);
                }
            }else{
                Collection<T> children = this.getChildren(pitem);
                if(cmp != null && children instanceof List){
                    List<T> list = (List<T>)children;
                    boolean insert = false;
                    int cnt = list.size();
                    for(int i = cnt - 1;i >= 0;i--){
                        T itm = list.get(i);
                        if(cmp.compare(item,itm) >= 0){
                            list.add(i + 1,item);
                            insert = true;
                            break;
                        }
                    }
                    if(!insert){
                        list.add(0,item);
                    }
                }else{
                    children.add(item);
                }
            }
        }
    }
    protected abstract K getId(T item);
    protected abstract K getParentId(T item);
    protected abstract Collection<T> getChildren(T pitem);
    protected abstract boolean isRoot(T itemNoParent,K pid);
    protected Comparator<T> initComparator(){
        return null;
    }
    protected T findParent(Map<K,T> cache,Iterator<T> cacheIt,K pid){
        T pitem = cache.get(pid);
        if(pitem == null){
            while(cacheIt.hasNext()){
                T item = cacheIt.next();
                K id = this.getId(item);
                cache.put(id,item);
                if(pid.equals(id)){
                    pitem = item;
                    break;
                }
            }
        }
        return pitem;
    }
}
