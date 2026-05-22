package com.quanglewangle.peter.shoppingBeta;

public class Constants {
    public static final String SHOPPING_URL = "http://fimblefowl.co.uk:8080/json";
    public static final String DUMP_CUPBOARD = SHOPPING_URL + "?cmd=dumpFiltered&curBasket=1";
    public static final String DUMP_LIST = SHOPPING_URL + "?cmd=dumpFiltered&curBasket=2";
    public static final String DUMP_LIST_QUICK_SHOP = SHOPPING_URL + "?cmd=dumpFiltered&curBasket=2&quickShopOnly=1";
    public static final String DUMP_BASKET = SHOPPING_URL + "?cmd=dumpFiltered&curBasket=3";

    public static final String CLEAR_ALL_QUICK_SHOP = SHOPPING_URL + "?cmd=clearAllQuickShop";
    public static final String LAST_MODIFIED_URL = SHOPPING_URL + "?cmd=lastModified";
    public static final String PREFS_NAME = "shopping_prefs";
    public static final String PREF_QUICK_SHOP_MODE = "quick_shop_mode";
}