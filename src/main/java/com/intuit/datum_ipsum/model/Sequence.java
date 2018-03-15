package com.intuit.datum_ipsum.model;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONStringer;
import org.codehaus.jettison.json.JSONWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Top level object used to represent a set of strings.
 *
 * A sequence is comprised of an ordered list of wheels and also tracks various counts of the strings.
 * A sequence also contains a random number generator, used for generating new strings from the sequence.
 * */
public class Sequence implements Serializable {
    private Integer totalCount = 0; // total number of strings characterized
    private Integer nullCount = 0; // number of null strings
    private List<Wheel> wheels = new ArrayList();
    private Random globalGenerator = null; // random number generator used to generate new strings

    /** @return total number of strings characterized by this sequence */
    public Integer getTotalCount() {
        return totalCount;
    }
    /** @param count number of additional strings to add */
    public void addTotalCount(Integer count) {
        totalCount += count;
    }
    /** @return number of null strings characterized by this sequence */
    public Integer getNullCount() {
        return nullCount;
    }
    /** @param count number of additional null strings to add */
    public void addNullCount(Integer count) {
        nullCount += count;
    }
    /** @return wheels comprising the sequence */
    public List<Wheel> getWheels() {
        return wheels;
    }
    /** @param nextWheel wheel to append */
    public void addWheel(Wheel nextWheel) {
        wheels.add(nextWheel);
    }

    /** Resets the random number generator used by this sequence.
     *
     * @param seed seed for the generator
     */
    public void resetGenerator(Integer seed) {
        globalGenerator = new Random(seed);
    }
    /** Resets the random number generator used by this sequence.
     *
     * @param input seed for the generator
     */
    public void resetGenerator(String input) {
        Integer seed = input.hashCode();
        globalGenerator = new Random(seed);
    }
    /** Resets the random number generator used by this sequence with a random seed. */
    public void resetGenerator() {
        globalGenerator = new Random();
    }


    /** Create an empty sequence. */
    public Sequence() {
        super();
    }

    /** Create a sequence from a single string.
     *
     * @param input string to characterize
     * @param definitions block definitions used to segment string into blocks
     */
    public Sequence(String input, List<BlockDefinition> definitions) {
        super();
        totalCount = 1;
        if (input == null) {
            nullCount = 1;
        } else {
            Integer numberCharacters = input.length();
            Block currentBlock = null; // start with no block defined
            Set<Character> allCharacters = new HashSet(); // set of all characters used in block definitions
            for (BlockDefinition definition : definitions) {
                allCharacters.addAll(definition.getCharacters());
            }
            Integer currentLength = 0; // track length of the current block
            // loop through the string to identify blocks
            for (Integer position = 0; position < numberCharacters; position ++) {
                Character currentCharacter = input.charAt(position);
                currentLength ++; // increment length of the current block
                if (currentBlock == null) {
                    // start a new block
                    // find the first block definition with a matching character
                    for (BlockDefinition definition : definitions) {
                        if (definition.getCharacters().contains(currentCharacter)) {
                            currentBlock = new Block(definition); // generate a new block from this definition
                            currentBlock.addCharacter(currentCharacter, 1); // increment count for this character
                            break;
                        }
                    }
                    if (currentBlock == null) {
                        // no matching block definition: use a default block (acts as a catchall)
                        currentBlock = new Block();
                        currentBlock.setDefaultBlock(true);
                        currentBlock.addCharacter(currentCharacter, 1); // increment count for this character
                    }
                } else {
                    // check if this character can be added to current block, or a new block should be started
                    if (!currentBlock.isDefaultBlock() && currentBlock.getDefinition().contains(currentCharacter)) {
                        // the current block is not a default block and the definition contains this character
                        currentBlock.addCharacter(currentCharacter, 1);
                    } else if (currentBlock.isDefaultBlock() && !allCharacters.contains(currentCharacter)) {
                        // the current block is a default block but no other block definitions contain this character
                        currentBlock.addCharacter(currentCharacter, 1);
                    } else {
                        // otherwise, a new block must be started, so close the current wheel
                        currentBlock.addBlockCount(1);
                        // adjust length and increment the length distribution
                        currentBlock.addLength(currentLength - 1, 1);
                        this.addWheel(new Wheel(currentBlock));

                        // reset to a new block with length 0
                        currentBlock = null;
                        currentLength = 0;

                        // decrement character position to process this character again
                        position --;
                    }
                }

            }
            // close final wheel
            if (currentBlock != null) {
                currentBlock.addBlockCount(1);
                currentBlock.addLength(currentLength, 1);
                this.addWheel(new Wheel(currentBlock));
            }
        }
    }

    /** Create a sequence from a single string and no block definitions.
     *  Will group all characters into a single catchall default block.
     *
     * @param input string to characterize
     */
    public Sequence(String input) {
        this(input, new ArrayList<BlockDefinition>());
    }


    /** Combine another sequence into this one by aggregating counts at each level (sequence, wheel, block).
     *
     * @param other the other sequence to combine
     */
    public void reduce(Sequence other) {
        if (other != null) {
            // add top level counts
            this.addTotalCount(other.getTotalCount());
            this.addNullCount(other.getNullCount());

            Integer thisNumberWheels = wheels.size();
            List<Wheel> otherWheels = other.getWheels();
            Integer otherNumberWheels = otherWheels.size();

            Integer position;
            // combine wheels by position
            for (position = 0; position < Math.min(thisNumberWheels, otherNumberWheels); position ++) {
                wheels.get(position).reduce(otherWheels.get(position));
            }
            if (otherNumberWheels > thisNumberWheels) {
                for(; position < otherNumberWheels; position ++) {
                    this.addWheel(otherWheels.get(position));
                }
            }
        }
    }


    /** Generate a random string based on this sequence, using the current random generator.
     *
     * @return the random string
     */
    public String generate() {
        if (globalGenerator == null) {
            this.resetGenerator();
        }
        return generate(globalGenerator);
    }

    /** Generate a random string based on this sequence, using the specified random generator.
     *
     * @param randomGenerator random number generator to use
     * @return the random string
     */
    public String generate(Random randomGenerator) {
        String result = null;

        Integer randomInt = randomGenerator.nextInt(totalCount);
        // randomize if result is non-null
        if (randomInt >= nullCount) {
            result = "";
            // randomly generate for each wheel
            for (Wheel wheel : wheels) {
                String toAppend = wheel.generate(randomGenerator, totalCount - nullCount);
                result += toAppend;
                if (toAppend.equals("")) break;
            }
        }

        return result;
    }

    /** Generate a random string based on this sequence, using a new random generator created with the specified seed.
     *
     * @param input seed for the random generator
     * @return the random string
     */
    public String generate(String input) {
        return generate(input, "");
    }

    /** Generate a random string based on this sequence, using a new random generator created with the specified seed.
     *  As a convenience, a salt may also be specified to modify (append to) the seed.
     *
     * @param input base seed for the random generator
     * @param salt salt to append to the seed
     * @return the random string
     */
    public String generate(String input, String salt) {
        String base = input;
        if (salt != null) {
            base += salt;
        }
        Integer seed = base.hashCode();
        Random randomGenerator = new Random(seed);

        return generate(randomGenerator);
    }


    /** Calculate the probability of a given string being generated by this sequence.
     *
     * @param input the string to consider
     * @return probability of generating this exact string
     */
    public Double calculateLikelihood(String input) {
        Double likelihood = null;
        if (input == null) {
            // calculate probability of generating a null result
            likelihood = 1.*nullCount/totalCount;
        } else {
            likelihood = 1.;
            if ((input.length() > 0) && (wheels.size() == 0)) {
                // cannot generate a non-empty string if no wheels exist
                likelihood = 0.;
            } else {
                List<Character> currentInput = new ArrayList<Character>();
                for (Character character : ArrayUtils.toObject(input.toCharArray())) {
                    currentInput.add(character);
                }
                // calculate probability for each wheel; aggregate multiplicatively
                for (Wheel wheel : wheels) {
                    if (currentInput.size() == 0) {
                        // no remaining characters: calculating probability of terminating here
                        likelihood *= (1 - 1.*wheel.getTotalBlockCounts()/(totalCount - nullCount));
                        break;
                    }
                    // probability that the wheel does not terminate
                    likelihood *= 1.*wheel.getTotalBlockCounts()/(totalCount - nullCount);
                    // calculate probability for this wheel, using as many consecutive characters as possible
                    // trim (from the front) the input string at each step
                    likelihood *= wheel.calculateLikelihood(currentInput);
                }
                if (currentInput.size() > 0) {
                    // any remaining characters cannot be generated, so probability is zero
                    likelihood = 0.;
                }
            }
        }
        return likelihood;
    }


    /** Serialize this sequence to a JSON string.
     *
     * @return the JSON string
     * @throws JSONException
     */
    public String toJSONString() throws JSONException {
        JSONWriter stringer = new JSONStringer();
        appendToJSON(stringer);
        return stringer.toString();
    }
    void appendToJSON(JSONWriter json) throws JSONException{
        json.object(); // outer object
        json.key("totalCount").value(totalCount);
        json.key("nullCount").value(nullCount);
        json.key("wheels").array();
        for (Wheel wheel: wheels) {
            wheel.appendToJSON(json);
        }
        json.endArray();
        json.endObject(); // end outer
    }

    /** Deserialize a sequence from a JSON string
     *
     * @param jsonString the JSON string
     * @return a sequence object represented by the string
     * @throws JSONException
     */
    static public Sequence fromJSONString(String jsonString) throws JSONException {
        return fromJSON(new JSONObject(jsonString));
    }
    static Sequence fromJSON(JSONObject json) throws JSONException {
        Sequence output = new Sequence();

        output.addTotalCount(json.getInt("totalCount"));
        output.addNullCount(json.getInt("nullCount"));

        JSONArray wheelsJSON = json.optJSONArray("wheels");
        if (wheelsJSON == null) {
            throw new JSONException("Missing array for key \"wheels\".");
        }
        Wheel wheel;
        for (Integer index = 0; index < wheelsJSON.length(); index ++) {
            wheel = Wheel.fromJSON(wheelsJSON.getJSONObject(index));
            output.addWheel(wheel);
        }
        return output;
    }

    /** Serialize this sequence to a byte array.
     *
     * @return the byte array
     * @throws IOException
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(baos);
        oout.writeObject(this);
        oout.close();
        return baos.toByteArray();
    }

    /** Deserialize a sequence from a byte array.
     *
     * @param byteArray the byte array
     * @return a sequence object represented by the byte array
     * @throws IOException
     * @throws ClassNotFoundException
     */
    static public Sequence deserialize (byte[] byteArray) throws IOException, ClassNotFoundException {
        Sequence deserialized = null;
        if (byteArray != null) {
            ObjectInputStream objectIn = new ObjectInputStream(
                    new ByteArrayInputStream(byteArray));
            deserialized = (Sequence) objectIn.readObject();
        }
        return deserialized;
    }

    @Override
    public String toString() {
        return "Sequence [totalCount=" + totalCount.toString()
                + ", nullCount=" + nullCount.toString()
                + ", wheels:" + wheels.toString()
                + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(totalCount).append(nullCount).append(wheels).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Sequence)) return false;
        Sequence other = (Sequence) obj;
        return new EqualsBuilder().append(totalCount, other.getTotalCount())
                                  .append(nullCount, other.getNullCount())
                                  .append(wheels, other.getWheels()).isEquals();
    }
}
