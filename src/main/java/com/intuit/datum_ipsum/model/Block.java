package com.intuit.datum_ipsum.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Random;

/** Bottom level object in the string representation.
 *
 * Each wheel contains a set of blocks.
 * A block is comprised of a distribution for the string length (number of characters)
 * and a distribution for the characters themselves (counts of each character).
 * A block can be created from a block definition to initialize the set of characters considered.
 */
public class Block implements Serializable {
    private Map<Character, Integer> characterCounts = new LinkedHashMap(); // number of occurrences of each character
    private Map<Integer, Integer> lengthCounts = new LinkedHashMap(); // number of strings by length
    private Integer blockCount = 0; // total number of strings characterized by this block
    private Boolean defaultBlock = false; // if this block matches all characters


    /** @return distribution of character occurrences */
    public Map<Character, Integer> getCharacterCounts() {
        return characterCounts;
    }
    /** Add a character, incrementing the count for that character.
     *
     * @param character character to add
     * @param count number to increment by
     */
    public void addCharacter(Character character, Integer count) {
        if (characterCounts.keySet().contains(character)) {
            characterCounts.put(character, count + characterCounts.get(character));
        } else {
            characterCounts.put(character, count);
        }
    }
    /** @return sum of counts for all characters */
    public Integer getTotalCharacterCounts() {
        Integer characterTotalCount = 0;
        for (Integer count : characterCounts.values()) {
            characterTotalCount += count;
        }
        return characterTotalCount;
    }

    /** @return a block definition generated from the set of characters in this block */
    public BlockDefinition getDefinition() {
        return new BlockDefinition(characterCounts.keySet());
    }
    /** @return the set of characters in this block */
    public Set<Character> getCharacters() {
        return characterCounts.keySet();
    }

    /** @return distribution of string length occurrences */
    public Map<Integer, Integer> getLengthCounts() {
        return lengthCounts;
    }
    /** Add a length, incrementing the count for that length.
     *
     * @param length the string length to add
     * @param count number to incrment by
     */
    public void addLength(Integer length, Integer count) {
        if (lengthCounts.keySet().contains(length)) {
            lengthCounts.put(length, count + lengthCounts.get(length));
        } else {
            lengthCounts.put(length, count);
        }
    }
    /** @return sum of counts for all string lenghts */
    public Integer getTotalLengthCounts() {
        Integer lengthTotalCount = 0;
        for (Integer count : lengthCounts.values()) {
            lengthTotalCount += count;
        }
        return lengthTotalCount;
    }
    /** @return maximum length with nonzero count */
    public Integer getMaxLength() {
        Integer maxLength = 0;
        for (Integer length : lengthCounts.keySet()) {
            if ((length > maxLength) && (lengthCounts.get(length) > 0)) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    /** @return total number of strings characterized by this block */
    public Integer getBlockCount() {
        return blockCount;
    }
    /** @param count number of additional string to add */
    public void addBlockCount(Integer count) {
        blockCount += count;
    }

    /** @return if this block matches all characters */
    public Boolean isDefaultBlock() {
        return defaultBlock;
    }
    /** Set this block to be a default block or not.
     *  A default block will match all characters,
     *  even those not previously tracked in the character distribution of the block.
     *
     * @param defaultSetting the setting to use
     */
    public void setDefaultBlock(Boolean defaultSetting) {
        defaultBlock = defaultSetting;
    }


    /** Create an empty block. */
    public Block() {
        super();
    }

    /** Create a block from a set of characters.
     *  The character distribution is initialized with these characters, each with zero count.
     *
     * @param characterCollection the set of characters to use
     */
    public Block(Collection<Character> characterCollection) {
        super();
        for (Character character : characterCollection) {
            characterCounts.put(character, 0);
        }
    }

    /** Create a block from a block definition.
     *
     * @param definition the block definition to use
     */
    public Block(BlockDefinition definition) {
        super();
        for (Character character : definition.getCharacters()) {
            characterCounts.put(character, 0);
        }
    }


    /** Combine another block into this one by aggregating character and length counts.
     *
     * @param other the other block to combine
     */
    public void reduce(Block other) {
        if (other != null) {
            // sum block counts
            this.addBlockCount(other.getBlockCount());
            // combine character distributions, summing counts for shared characters
            for (Map.Entry<Character, Integer> characterEntry : other.getCharacterCounts().entrySet()) {
                this.addCharacter(characterEntry.getKey(), characterEntry.getValue());
            }
            // combine length distributions, summing counts for shared lengths
            for (Map.Entry<Integer, Integer> lengthEntry : other.getLengthCounts().entrySet()) {
                this.addLength(lengthEntry.getKey(), lengthEntry.getValue());
            }

            // combine the default property using 'or' logic
            if (other.isDefaultBlock()) {
                this.setDefaultBlock(true);
            }
        }
    }


    /** Generate a random string based on this block.
     *
     * @param randomGenerator random number generator to use
     * @return the random string
     */
    public String generate(Random randomGenerator) {
        Integer characterTotalCount = this.getTotalCharacterCounts();
        Integer lengthTotalCount = this.getTotalLengthCounts();

        // randomize length
        Integer randomInt = randomGenerator.nextInt(lengthTotalCount);
        Integer cumulativeCount = 0;
        Integer randomLength = 0;

        for (Integer length : lengthCounts.keySet()) {
            cumulativeCount += lengthCounts.get(length);
            if (randomInt < cumulativeCount) {
                randomLength = length;
                break;
            }
        }

        // randomize each character
        String result = "";
        StringBuilder builder = new StringBuilder();

        for (Integer position = 0; position < randomLength; position ++) {
            randomInt = randomGenerator.nextInt(characterTotalCount);
            cumulativeCount = 0;
            Character randomCharacter = null;

            for (Character character : characterCounts.keySet()) {
                cumulativeCount += characterCounts.get(character);
                if (randomInt < cumulativeCount) {
                    randomCharacter = character;
                    break;
                }
            }
            if (randomCharacter != null) {
                builder.append(randomCharacter);
            }
        }
        if (builder.length() > 0) {
            result = builder.toString();
        }

        return result;
    }


    /** Count consecutive characters in a string that could possibly be generated by this block.
     *
     * @param input the string to consider
     * @return number of consecutive characters that could be generated
     */
    public Integer getGatherCount(String input) {
        Integer numGathered = null;
        if (input != null) {
            numGathered = 0;
            if (input.length() > 0) {
                Integer subMaxLength = 0;
                Integer maxLength = this.getMaxLength();
                while ((numGathered < maxLength) &&
                        (input.length() > numGathered) &&
                        characterCounts.keySet().contains(input.charAt(numGathered))) {
                    numGathered += 1;
                    if (lengthCounts.keySet().contains(numGathered) && (lengthCounts.get(numGathered) > 0)) {
                        subMaxLength = numGathered;
                    }
                }
                // correct for case when final length is not in the length distribution
                // roll back to largest legal length
                if (subMaxLength < numGathered) {
                    numGathered = subMaxLength;
                }
            }
        }
        return numGathered;
    }


    /** Calculate the probability a given string being generated by this block.
     *
     * @param input string to consider
     * @return probability of generating this exact string
     */
    public Double calculateLikelihood(String input) {
        Double likelihood = null;
        if (input != null) {
            Integer length = input.length();
            // check if length is possible
            if (!lengthCounts.containsKey(length) || (lengthCounts.get(length) < 1)) {
                likelihood = 0.;
            } else {
                // calculate probability for this length
                likelihood = 1.*lengthCounts.get(length)/this.getTotalLengthCounts();

                //calculate probability for each character
                Integer characterTotalCount = this.getTotalCharacterCounts();
                for (Integer position = 0; position < length; position ++) {
                    Character current = input.charAt(position);
                    // check if this character is possible
                    if (!characterCounts.keySet().contains(current)) {
                        likelihood = 0.;
                        break;
                    }
                    // aggregate multiplicatively
                    likelihood *= 1.*characterCounts.get(current)/characterTotalCount;
                }
            }
        }
        return likelihood;
    }


    /** Serialize this block to a JSON string.
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
        json.key("defaultBlock").value(defaultBlock);
        json.key("blockCount").value(blockCount);
        json.key("characterCounts").object(); // Nested characterCounts object
        for (Map.Entry<Character, Integer> entry : characterCounts.entrySet()) {
            json.key(entry.getKey().toString()).value(entry.getValue());
        }
        json.endObject(); // End nested characterCounts
        json.key("lengthCounts").object(); // Nested lengthCounts object
        for (Map.Entry<Integer, Integer> entry : lengthCounts.entrySet()) {
            json.key(entry.getKey().toString()).value(entry.getValue());
        }
        json.endObject(); // End nested lengthCounts
        json.endObject(); // End outer
    }

    /** Deserialize a block from a JSON string
     *
     * @param jsonString the JSON string
     * @return a block object represented by the string
     * @throws JSONException
     */
    static public Block fromJSONString(String jsonString) throws JSONException {
        return fromJSON(new JSONObject(jsonString));
    }
    static Block fromJSON(JSONObject json) throws JSONException {
        Block output =  new Block();

        output.setDefaultBlock(json.getBoolean("defaultBlock"));
        output.addBlockCount(json.getInt("blockCount"));

        JSONObject characterCountsJSON = json.optJSONObject("characterCounts");
        if (characterCountsJSON == null) {
            throw new JSONException("Missing object for key \"characterCounts\".");
        }
        Iterator<String> keys = characterCountsJSON.keys();
        String key;
        while (keys.hasNext()) {
            key = keys.next();
            if (key != null) {
                if (key.length() > 1) {
                    throw new JSONException("Character value in \"characters\" has length > 1.");
                } else {
                    output.addCharacter(key.charAt(0), characterCountsJSON.getInt(key));
                }
            }
        }

        JSONObject lengthCountsJSON = json.optJSONObject("lengthCounts");
        if (lengthCountsJSON == null) {
            throw new JSONException("Missing object for key \"lengthCounts\".");
        }
        keys = lengthCountsJSON.keys();
        while (keys.hasNext()) {
            key = keys.next();
            if (key != null) {
                output.addLength(new Integer(key), lengthCountsJSON.getInt(key));
            }
        }

        return output;
    }

    @Override
    public String toString() {
        return "Block [characterCounts=" + characterCounts.toString()
                + ", lengthCounts=" + lengthCounts.toString()
                + ", blockCount=" + blockCount.toString()
                + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(characterCounts)
                                         .append(lengthCounts)
                                         .append(blockCount)
                                         .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Block)) return false;
        Block other = (Block) obj;
        return new EqualsBuilder().append(characterCounts, other.getCharacterCounts())
                                  .append(lengthCounts, other.getLengthCounts())
                                  .append(blockCount, other.getBlockCount())
                                  .isEquals();
    }
}
