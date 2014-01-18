/**
 * 
 */
package com.blumenthal.listey;

import static com.blumenthal.listey.JsonFieldNameConstants.*;
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
	public ItemInfoJsonAdapter(){}
	public ItemInfoJsonAdapter(boolean doAllFields) {
		this.doAllFields = doAllFields;
	}
	boolean doAllFields = false;
	
	@Override
	public ItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {			
		JsonObject itemJson = json.getAsJsonObject();
		ItemInfo item = new ItemInfo();
		item.setUniqueId(itemJson.get(UNIQUE_ID).getAsString());
		if (itemJson.has(COUNT)) {
			item.setCount(itemJson.get(COUNT).getAsLong());
		}
		item.setName(itemJson.get(NAME).getAsString());
		if (itemJson.has(STATUS)) {
			String statusString = itemJson.get(STATUS).getAsString();
			item.setStatus(TimeStampedNode.Status.valueOf(statusString));
		}
		else {
			item.setStatus(TimeStampedNode.Status.ACTIVE);
		}
		
		item.setLastUpdate(itemJson.get(LAST_UPDATE).getAsLong());
		if (itemJson.has(CATEGORIES)) {
			JsonObject categoriesJson = itemJson.get(CATEGORIES).getAsJsonObject();
			for ( Map.Entry<String,JsonElement> catEntry : categoriesJson.entrySet()){
				ItemCategoryInfo catInfo = context.deserialize(catEntry.getValue(), ItemCategoryInfo.class);
				catInfo.setUniqueId(catEntry.getKey());
				item.getCategories().put(catEntry.getKey(), catInfo);
				//We never want this set on read
				catInfo.setChangedOnServer(false);
			}//for categoriesJson
		}//if has categories
		
		//Note, always ignore changedOnServer when parsing.

		return item;
	}//deserialize

	/* (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
	@Override
	public JsonElement serialize(ItemInfo item, Type type,
			JsonSerializationContext context) {
		JsonObject rv = new JsonObject();
		rv.addProperty(COUNT, item.getCount());
		rv.addProperty(LAST_UPDATE, item.getLastUpdate());
		rv.addProperty(NAME, item.getName());
		rv.addProperty(STATUS, item.getStatus().toString());
		rv.addProperty(UNIQUE_ID, item.getUniqueId());
		if (item.getChangedOnServer()) {
			rv.addProperty(CHANGED_ON_SERVER, item.getChangedOnServer());
		}
		
		if (!item.getCategories().isEmpty()){
			JsonElement categoriesJson = context.serialize(item.getCategories());
			for (Map.Entry<String, JsonElement> categoryEntry : categoriesJson.getAsJsonObject().entrySet()) {
				JsonObject catJsonObj = categoryEntry.getValue().getAsJsonObject();
				//Since UNIQUE_ID is in the map key, don't also put it in the value
				catJsonObj.remove(UNIQUE_ID);
				
				//It's just wasted space to send a false value for this, so remove it
				if (!catJsonObj.get(CHANGED_ON_SERVER).getAsBoolean()) {
					catJsonObj.remove(CHANGED_ON_SERVER);
				}
			}//for
			rv.add(CATEGORIES, categoriesJson);
		}//if categories
		return rv;
	}//serialize
}//ItemInfoJsonAdapter