/**
 * 
 */
package com.blumenthal.listey;

import java.lang.reflect.Type;
import java.util.Map;

import com.blumenthal.listey.ListeyDataOneUser.ItemCategoryStatus;
import com.blumenthal.listey.ListeyDataOneUser.ItemStatus;
import com.blumenthal.listey.ListeyDataOneUser.ListInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ListInfoDeserializer implements JsonDeserializer<ListInfo> {
	@Override
	public ListInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ListInfo listInfo = new ListInfo();

		JsonObject topMap = json.getAsJsonObject();
		listInfo.lastUpdate = topMap.get("lastUpdate").getAsLong();
		listInfo.uniqueId = topMap.get("uniqueId").getAsString();
		listInfo.name = topMap.get("name").getAsString();

		if (topMap.has("items")) {
			JsonArray itemsJson = topMap.get("items").getAsJsonArray();
			for (JsonElement itemJsonElement : itemsJson) {
				JsonObject itemJson = itemJsonElement.getAsJsonObject();
				ItemInfo item = new ItemInfo();
				item.uniqueId = itemJson.get("uniqueId").getAsString();
				if (itemJson.has("count")) {
					item.count = itemJson.getAsInt();
				}
				item.name = itemJson.get("name").getAsString();
				item.status = ItemStatus.valueOf(itemJson.getAsString());
				item.lastUpdate = itemJson.get("lastUpdate").getAsLong();
				if (itemJson.has("categories")) {
					JsonObject categoriesJson = itemJson.get("categories").getAsJsonObject();
					for ( Map.Entry<String,JsonElement> catEntry : categoriesJson.entrySet()){
						ItemCategoryInfo catInfo = new ItemCategoryInfo();
						catInfo.uniqueCategoryId = catEntry.getKey();
						JsonObject catJson = catEntry.getValue().getAsJsonObject();
						catInfo.lastUpdate = catJson.get("lastUpdate").getAsLong();
						catInfo.status = ItemCategoryStatus.valueOf(catJson.get("status").getAsString());
						item.categories.put(catEntry.getKey(), catInfo);
					}//for categoriesJson
				}//if has categories

				//Add it to the map.  Note, order is lost, but since it's always alphabetical order it's ok
				listInfo.items.put(item.uniqueId, item);
			}//for itemsJson
		}//if has items

		if (topMap.has("categories")) {
			JsonArray categoriesJson = topMap.get("categories").getAsJsonArray();
			for ( JsonElement catElement : categoriesJson){
				JsonObject catJson = catElement.getAsJsonObject();
				CategoryInfo catInfo = new CategoryInfo();
				catInfo.name = catJson.get("name").getAsString();
				catInfo.lastUpdate = catJson.get("lastUpdate").getAsLong();
				catInfo.uniqueId = catJson.get("uniqueId").getAsString();
				listInfo.categories.add(catInfo);
			}//for categoriesJson
		}//if has categories

		return listInfo;
	}//deserialize
}//ListInfoDeserializer