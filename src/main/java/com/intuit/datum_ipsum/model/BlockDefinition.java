package com.intuit.datum_ipsum.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** A set of characters used to define a block. */
public class BlockDefinition implements Serializable {

    private Set<Character> characters = new HashSet();


    /** @return the characters in this definition */
    public Set<Character> getCharacters() {
        return characters;
    }
    /** Check if this definition contains a specific character.
     *
     * @param character the character to check for
     * @return if this block definition contains the character
     */
    public Boolean contains(Character character) {
        return characters.contains(character);
    }


    /** Create an empty block definition */
    public BlockDefinition() {
        super();
    }

    /** Create a block definition from a set of characters.
     *
     * @param characterCollection the characters to use in the definition
     */
    public BlockDefinition(Collection<Character> characterCollection) {
        super();
        characters.addAll(characterCollection);
    }


    /** Serialize this block definition to a JSON string.
     *
     * @return the JSON string
     * @throws JSONException
     */
    public String toJSONString() throws JSONException {
        JSONWriter stringer = new JSONStringer();
        appendToJSON(stringer);
        return stringer.toString();
    }
    void appendToJSON(JSONWriter json) throws JSONException {
        json.object(); // Outer object
        json.key("characters").array();
        for (Character character: characters) {
            json.value(character);
        }
        json.endArray();
        json.endObject(); // End outer
    }

    /** Deserialize a block definition from a JSON string
     *
     * @param jsonString the JSON string
     * @return a block definition object represented by the string
     * @throws JSONException
     */
    static public BlockDefinition fromJSONString(String jsonString) throws JSONException {
        return fromJSON(new JSONObject(jsonString));
    }
    static BlockDefinition fromJSON(JSONObject json) throws JSONException {
        JSONArray charactersJSON = json.optJSONArray("characters");
        if (charactersJSON == null) {
            throw new JSONException("Missing array for key \"characters\".");
        }
        Set<Character> inputCharacters = new HashSet();
        String characterString;
        for (Integer index = 0; index < charactersJSON.length(); index ++) {
            characterString = charactersJSON.getString(index);
            if (characterString != null) {
                if (characterString.length() > 1) {
                    throw new JSONException("Character value in \"characters\" has length > 1.");
                } else {
                    inputCharacters.add(characterString.charAt(0));
                }
            }
        }
        return new BlockDefinition(inputCharacters);
    }

    @Override
    public String toString() {
        return "BlockDefinition [characters=" + characters.toString()
                + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(characters).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof BlockDefinition)) return false;
        BlockDefinition other = (BlockDefinition) obj;
        return new EqualsBuilder().append(characters, other.getCharacters()).isEquals();
    }
}
