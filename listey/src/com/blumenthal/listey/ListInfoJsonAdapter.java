/**
 * 
 */
package com.blumenthal.listey;

import java.lang.reflect.Type;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ListInfoJsonAdapter implements JsonDeserializer<ListInfo>, JsonSerializer<ListInfo> {
	/**
	 * Deserialize the list info.  Note, the ListInfo uniqueID is actually the key of the
	 * parent map and not included in this at all.
	 */
	@Override
	public ListInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ListInfo listInfo = new ListInfo();

		JsonObject topMap = json.getAsJsonObject();
		listInfo.lastUpdate = topMap.get("lastUpdate").getAsLong();
		listInfo.name = topMap.get("name").getAsString();
		listInfo.status = ListInfo.ListInfoStatus.valueOf(topMap.get("status").getAsString());

		if (topMap.has("items")) {
			JsonArray itemsJson = topMap.get("items").getAsJsonArray();
			for (JsonElement itemJsonElement : itemsJson) {
				ItemInfo item = context.deserialize(itemJsonElement, ItemInfo.class);

				//Add it to the map.  Note, order is lost, but since it's always alphabetical order it's ok
				listInfo.items.put(item.uniqueId, item);
			}//for itemsJson
		}//if has items

		if (topMap.has("categories")) {
			JsonArray categoriesJson = topMap.get("categories").getAsJsonArray();
			for ( JsonElement catElement : categoriesJson){
				CategoryInfo catInfo = context.deserialize(catElement,  CategoryInfo.class);
				listInfo.categories.add(catInfo);
			}//for categoriesJson
		}//if has categories
		
		if (topMap.has("selectedCategories")) {
			JsonArray selCatJson = topMap.get("selectedCategories").getAsJsonArray();
			listInfo.selectedCategories = context.deserialize(selCatJson, HashSet.class);
		}//if has selectedCategories

		return listInfo;
	}//deserialize


	/* (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
	@Override
	public JsonElement serialize(ListInfo listInfo, Type type,
			JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}//ListInfoJsonAdapter