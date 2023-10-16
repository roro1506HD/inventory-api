Inventory API
---

This API can be complex to use and was designed for personal usage.
What should be known is that you must implement one of the InventoryInstances to create an inventory, and implement ItemInstance for items.

To create the full Inventory that can be opened, you must call #createInventory using the InventoryManager.

Everything uses Translations from the Language API. Here are the translations used by this API, and their English value. Note that you must add them to your language files.
```json
{
  "inventory.api.inventory.confirmation.title": "Please confirm",
  "inventory.api.item.confirmation.cancel.name": "<red>Cancel",
  "inventory.api.item.confirmation.confirm.name": "<green>Confirm",
  "inventory.api.item.back.name": "<red>Back",
  "inventory.api.item.close.name": "<red>Close",
  "inventory.api.item.pagination.previous.name": "<white>Previous page <gray>(<previous_page>/<max_page>)",
  "inventory.api.item.pagination.next.name": "<white>Next page <gray>(<next_page>/<max_page>)"
}
```