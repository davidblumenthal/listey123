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

public class ListeyDataOneUserJsonAdapter implements JsonDeserializer<ListeyDataOneUser>, JsonSerializer<ListeyDataOneUser>{
	boolean doAllFields = false;
	
	public ListeyDataOneUserJsonAdapter(){}
	public ListeyDataOneUserJsonAdapter(boolean doAllFields) {
		this.doAllFields = doAllFields;
	}
	
	@Override
	public ListeyDataOneUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ListeyDataOneUser userInfo = new ListeyDataOneUser();

		JsonObject userJson = json.getAsJsonObject();
		if (userJson.has(ListeyDataOneUser.USER_EMAIL)) {
			userInfo.setUniqueId(userJson.get(ListeyDataOneUser.USER_EMAIL).getAsString());
		}
		if (userJson.has(ListeyDataOneUser.LISTS)){
			JsonObject listsJson = userJson.get(ListeyDataOneUser.LISTS).getAsJsonObject();
			for ( Map.Entry<String,JsonElement> listEntry : listsJson.entrySet()){
				ListInfo listInfo = context.deserialize(listEntry.getValue(), ListInfo.class);
				listInfo.setUniqueId(listEntry.getKey());
				userInfo.lists.put(listEntry.getKey(), listInfo);
			}//for categoriesJson
		}//if USER_DATA
		
		return userInfo;
	}//deserialize

	/* (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
	@Override
	public JsonElement serialize(ListeyDataOneUser oneUser, Type arg1,
			JsonSerializationContext context) {
		JsonObject rv = new JsonObject();
		if (doAllFields){
			rv.addProperty(ListeyDataOneUser.USER_EMAIL, oneUser.getUniqueId());
		}
		if (!oneUser.lists.isEmpty()){
			JsonElement listsJson = context.serialize(oneUser.lists);
			rv.add(ListeyDataOneUser.LISTS, listsJson);
		}//if categories
		return rv;
	}
}//ListeyDataMultipleUsersJsonAdapter