/**
 * 
 */
package com.blumenthal.listey;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ItemInfoJsonAdapter implements JsonDeserializer<ItemInfo>, JsonSerializer<ItemInfo> {
	@Override
	public ItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {			
		JsonObject itemJson = json.getAsJsonObject();
		ItemInfo item = new ItemInfo();
		item.setUniqueId(itemJson.get(ItemInfo.UNIQUE_ID).getAsString());
		if (itemJson.has(ItemInfo.COUNT)) {
			item.setCount(itemJson.get(ItemInfo.COUNT).getAsLong());
		}
		item.setName(itemJson.get(ItemInfo.NAME).getAsString());
		if (itemJson.has(ItemInfo.STATUS)) {
			String statusString = itemJson.get(ItemInfo.STATUS).getAsString();
			item.setStatus(TimeStampedNode.Status.valueOf(statusString));
		}
		else {
			item.setStatus(TimeStampedNode.Status.ACTIVE);
		}
		
		item.setLastUpdate(itemJson.get(ItemInfo.LAST_UPDATE).getAsLong());
		if (itemJson.has(ItemInfo.CATEGORIES)) {
			JsonObject categoriesJson = itemJson.get(ItemInfo.CATEGORIES).getAsJsonObject();
			for ( Map.Entry<String,JsonElement> catEntry : categoriesJson.entrySet()){
				ItemCategoryInfo catInfo = context.deserialize(catEntry.getValue(), ItemCategoryInfo.class);
				catInfo.setUniqueId(catEntry.getKey());
				item.getCategories().put(catEntry.getKey(), catInfo);
			}//for categoriesJson
		}//if has categories

		return item;
	}//deserialize

	/* (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
	@Override
	public JsonElement serialize(ItemInfo item, Type type,
			JsonSerializationContext context) {
		JsonObject rv = new JsonObject();
		rv.addProperty(ItemInfo.COUNT, item.getCount());
		rv.addProperty(ItemInfo.LAST_UPDATE, item.getLastUpdate());
		rv.addProperty(ItemInfo.NAME, item.getName());
		rv.addProperty(ItemInfo.STATUS, item.getStatus().toString());
		rv.addProperty(ItemInfo.UNIQUE_ID, item.getUniqueId());
		
		if (!item.getCategories().isEmpty()){
			JsonElement categoriesJson = context.serialize(item.getCategories());
			for (Map.Entry<String, JsonElement> categoryEntry : categoriesJson.getAsJsonObject().entrySet()) {
				categoryEntry.getValue().getAsJsonObject().remove(ItemCategoryInfo.UNIQUE_ID);
			}
			rv.add(ItemInfo.CATEGORIES, categoriesJson);
		}//if categories
		return rv;
	}//serialize
}//ItemInfoJsonAdapter