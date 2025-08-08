package com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables;

import java.util.Set;

public class BlackListsCache {

    public Set<String> BLOCK_BLACK_LIST;
    public Set<String> ITEM_BLACK_LIST;
    public Set<String> PARTICLE_BLACK_LIST;

    // A constructor to initialize the sets, preventing null pointer issues.
    public BlackListsCache(Set<String> item , Set<String> block, Set<String> par) {
        ITEM_BLACK_LIST = item;
        BLOCK_BLACK_LIST = block;
        PARTICLE_BLACK_LIST = par;
    }
}
