/**
 * 
 */
package com.blumenthal.listey;

import static com.blumenthal.listey.JsonFieldNameConstants.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ListInfoJsonAdapter implements JsonDeserializer<ListInfo>, JsonSerializer<ListInfo> {
	boolean doAllFields = false;
	
	public ListInfoJsonAdapter(){}
	public ListInfoJsonAdapter(boolean doAllFields) {
		this.doAllFields = doAllFields;
	}
	
	/**
	 * Deserialize the list info.  Note, the ListInfo uniqueID is actually the key of the
	 * parent map and not included in this at all.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ListInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ListInfo listInfo = new ListInfo();

		JsonObject topMap = json.getAsJsonObject();
		listInfo.setLastUpdate(topMap.get(LAST_UPDATE).getAsLong());
		listInfo.setName(topMap.get(NAME).getAsString());
		listInfo.setStatus(TimeStampedNode.Status.valueOf(topMap.get(STATUS).getAsString()));
		if (topMap.has(UNIQUE_ID)){
			listInfo.setUniqueId(topMap.get(UNIQUE_ID).getAsString());
		}

		if (topMap.has(ITEMS)) {
			JsonArray itemsJson = topMap.get(ITEMS).getAsJsonArray();
			for (JsonElement itemJsonElement : itemsJson) {
				ItemInfo item = context.deserialize(itemJsonElement, ItemInfo.class);

				//Add it to the map.  Note, order is lost, but since it's always alphabetical order it's ok
				listInfo.getItems().put(item.getUniqueId(), item);
			}//for itemsJson
		}//if has items

		if (topMap.has(CATEGORIES)) {
			JsonArray categoriesJson = topMap.get(CATEGORIES).getAsJsonArray();
			//Hack needed to make it deserialize to an ArrayList correctly.
			//http://stackoverflow.com/questions/5554217/google-gson-deserialize-listclass-object-generic-type
			Type listType = new TypeToken<TreeSet<CategoryInfo>>() {
            }.getType();
			listInfo.setCategories((TreeSet<CategoryInfo>)context.deserialize(categoriesJson, listType));
		}//if has categories
		
		if (topMap.has(SELECTED_CATEGORIES)) {
			JsonArray selCatJson = topMap.get(SELECTED_CATEGORIES).getAsJsonArray();
			Type listType = new TypeToken<HashSet<String>>() {
            }.getType();
			listInfo.setSelectedCategories((HashSet<String>) context.deserialize(selCatJson, listType));
		}//if has selectedCategories
		
		if (topMap.has(OTHER_USER_PRIVS)) {
			JsonObject privsJson = topMap.get(OTHER_USER_PRIVS).getAsJsonObject();
			for ( Map.Entry<String,JsonElement> privEntry : privsJson.entrySet()){
				OtherUserPrivOnList privInfo = context.deserialize(privEntry.getValue(), OtherUserPrivOnList.class);
				privInfo.userId = privEntry.getKey();
				listInfo.getOtherUserPrivs().put(privEntry.getKey(), privInfo);
			}//for categoriesJson
		}//if has selectedCategories

		//Note, always ignore changedOnServer when parsing.
		
		return listInfo;
	}//deserialize


	/* (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
	@Override
	public JsonElement serialize(ListInfo listInfo, Type type,
			JsonSerializationContext context) {
		JsonObject rv = new JsonObject();
		rv.addProperty(LAST_UPDATE, listInfo.getLastUpdate());
		rv.addProperty(NAME, listInfo.getName());
		rv.addProperty(STATUS, listInfo.getStatus().toString());
		if (listInfo.getChangedOnServer()) {
			rv.addProperty(CHANGED_ON_SERVER, listInfo.getChangedOnServer());
		}
		if (doAllFields) {
			rv.addProperty(UNIQUE_ID, listInfo.getUniqueId());
		}
		
		if (!listInfo.getItems().isEmpty()){
			JsonArray itemsJson = new JsonArray();
			for (Entry<String, ItemInfo> entry : listInfo.getItems().entrySet()) {
				ItemInfo item = entry.getValue();
				JsonObject itemJson = context.serialize(item).getAsJsonObject();
				itemJson.addProperty(UNIQUE_ID, item.getUniqueId());
				itemsJson.add(itemJson);
			}//for each entry
			rv.add(ITEMS, itemsJson);
		}//if items
		
		if (!listInfo.getCategories().isEmpty()) {
			JsonElement categoriesJson = context.serialize(listInfo.getCategories());
			rv.add(CATEGORIES, categoriesJson);			
		}
		
		if (!listInfo.getSelectedCategories().isEmpty()) {
			JsonElement selectedCategoriesJson = context.serialize(listInfo.getSelectedCategories());
			rv.add(SELECTED_CATEGORIES, selectedCategoriesJson);			
		}
		
		if (!listInfo.getOtherUserPrivs().isEmpty()) {
			JsonElement otherUserPrivsJson = context.serialize(listInfo.getOtherUserPrivs());
			for (Map.Entry<String, JsonElement> privEntry : otherUserPrivsJson.getAsJsonObject().entrySet()) {
				JsonObject jsonObj = privEntry.getValue().getAsJsonObject();
				
				//since userId is in the key, no point in also outputting it in the value
				jsonObj.remove(USER_ID);
				
				//No point in wasting space outputting a false value
				if (!jsonObj.get(CHANGED_ON_SERVER).getAsBoolean()) {
					jsonObj.remove(CHANGED_ON_SERVER);
				}
			}//for
			rv.add(OTHER_USER_PRIVS, otherUserPrivsJson);			
		}
		
		return rv;
	}//serialize
}//ListInfoJsonAdapter