# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean

# Build and install in one step
./gradlew installDebug
```

There are no automated tests in this project.

## Architecture

This is an Android shopping app that manages products across three "baskets" (virtual locations), backed by a REST/JSON API at `http://fimblefowl.co.uk:8080/json`.

**Entry point:** `AndroidTabAndListView` (extends deprecated `TabActivity`) renders a 3-tab UI:
- **Cupboard** (curBasket=1) → `CupboardActivity`
- **List** (curBasket=2) → `ListActivity`
- **Basket** (curBasket=3) → `BasketActivity`

**Data flow pattern:** Each tab activity:
1. On `onResume`, fires a `LoadURL` (AsyncTask using OkHttp) with a `?cmd=dumpFiltered&curBasket=N` URL
2. Implements `AsyncTaskCompleteListener<String>` to receive the JSON array response
3. Parses JSON into `ArrayList<HashMap<String, String>>` (keys: `description`, `quantity`, `barcode`, `id`)
4. Binds the list via `CupboardListAdapter` (a `BaseAdapter` with ViewHolder pattern)

**Moving items between baskets:** Tapping a list item fires `LoadURL` with `?cmd=ubT&newBasket=X&product_id=Y&curBasket=Z`. The activity refreshes automatically on the next `onResume`.

**All API URLs** are centralised in `Constants.java`. The base URL `SHOPPING_URL` must include `:8080`.

**`WebFilteredDownload` and `WebChangeBasketStatus`** are older `HttpURLConnection`-based classes that are no longer used — `LoadURL` (OkHttp) replaced them.

## Key Notes

- `usesCleartextTraffic="true"` is set in the manifest because the backend is plain HTTP.
- `CupboardListAdapter` is reused by all three tab activities despite the name.
- `OutboxActivity`, `ProfileActivity`, `BasketListAdapter`, `ListListAdapter`, `JSONParser`, `NameValuePair`, and `Products` exist in the codebase but are not wired up anywhere active.
- The `update()` method in `CupboardListAdapter` has a bug (double-adds items); activities bypass it and call `setListAdapter` + `notifyDataSetChanged` directly.
