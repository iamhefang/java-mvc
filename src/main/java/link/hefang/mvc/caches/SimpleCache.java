package link.hefang.mvc.caches;

import link.hefang.caches.BaseCache;
import link.hefang.caches.CacheItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import static link.hefang.helpers.CollectionHelper.arrayListOf;

public class SimpleCache extends BaseCache {
    private static final ConcurrentHashMap<String, CacheItem> caches = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public Object get(@NotNull String name) {
        CacheItem item = caches.get(name);
        if (item.getExpireIn() < System.currentTimeMillis()) {
            remove(name);
            return null;
        }
        return item.getValue();
    }

    @Override
    public void set(@NotNull String name, @Nullable Object value, long expireIn) {
        caches.put(name, new CacheItem(value, expireIn));
    }

    @Override
    public Boolean exist(@NotNull String name) {
        return caches.containsKey(name);
    }

    @Override
    public Object remove(@NotNull String name) {
        return caches.remove(name).getValue();
    }

    @Override
    public boolean clean() {
        caches.clear();
        return true;
    }

    @Override
    public Collection<String> names() {
        ArrayList<String> list = arrayListOf();
        Enumeration<String> keys = caches.keys();
        while (keys.hasMoreElements()) {
            list.add(keys.nextElement());
        }
        return list;
    }

    @Override
    public int count() {
        return caches.size();
    }
}
