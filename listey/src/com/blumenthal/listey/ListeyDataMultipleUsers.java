package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This class is a java implementation of the JSON primitive for spec version 2
 * JSON Data Format
   {
  	    "lastUpdate": "1234567890",
  		"userData" : {
    		'<CURRENT_USER_EMAIL' : {
				<SEE ListyDataOneUser>
            },
            '<OTHER_USER_EMAIL>' : {... see <CURRENT_USER_EMAIL> above}
        }//lists
  }//top-level
 */
public class ListeyDataMultipleUsers {
	public Long lastUpdate;
	public Map< String, ListeyDataOneUser> userData;
}//ListeyDataV2
