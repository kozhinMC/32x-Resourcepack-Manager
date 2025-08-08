package com.stylized_resourcepack_manager.resourcepack_manager_k.cache_tables;

import java.util.*;

public class PackManagerCache {
    // The fingerprint of the resource pack file. We'll use a SHA-256 hash.
    public String fileHash;

    // The sets of categorized paths.
    public Map<String, ArrayList<String>> ResourcePackDataCache;
    public Set<String> scannedNameSpaces;
    public Set<String> ActiveInUseDataCache;
    public Map<String,Boolean> UpdateFlags;

    // A constructor to initialize the sets, preventing null pointer issues.
    public PackManagerCache() {
        ResourcePackDataCache = new HashMap<>();
        scannedNameSpaces = new HashSet<>();
        ActiveInUseDataCache = new HashSet<>();
        UpdateFlags = new HashMap<>();
    }
}
