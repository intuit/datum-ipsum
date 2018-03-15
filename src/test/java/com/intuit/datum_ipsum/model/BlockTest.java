package com.intuit.datum_ipsum.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.ArrayUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BlockTest {
    Double precision = 1e-9;

    @Test
    public void characterizedBlockTest() {
        Character[] characters = {'0', '$', 'K', 'm'};
        Set<Character> definition = new HashSet(Arrays.asList(characters));
        BlockDefinition definitionBlock = new BlockDefinition(definition);

        //test constructor using Collection
        Block testBlock = new Block(definition);
        testConstructor(testBlock, definition);

        //test constructor using BlockDefinition
        testBlock = new Block(definitionBlock);
        testConstructor(testBlock, definition);

    }

    @Test
    public void addCharacterTest() {
        Block testBlock = new Block();
        Map<Character, Integer> characterCounts = testBlock.getCharacterCounts();
        assertNotNull(characterCounts);
        assertEquals(0, characterCounts.size());

        testBlock.addCharacter('W', 1);
        testBlock.addCharacter('w', 1);
        testBlock.addCharacter('W', 2);

        characterCounts = testBlock.getCharacterCounts();
        assertNotNull(characterCounts);
        assertEquals(2, characterCounts.size());
        assertEquals(3, (long) characterCounts.get('W'));
        assertEquals(1, (long) characterCounts.get('w'));
    }

    @Test
    public void addLengthTest() {
        Block testBlock = new Block();
        Map<Integer, Integer> lengthCounts = testBlock.getLengthCounts();
        assertNotNull(lengthCounts);
        assertEquals(0, lengthCounts.size());

        testBlock.addLength(5, 1);
        testBlock.addLength(4, 1);
        testBlock.addLength(5, 3);

        lengthCounts = testBlock.getLengthCounts();
        assertNotNull(lengthCounts);
        assertEquals(2, lengthCounts.size());
        assertEquals(4, (long) lengthCounts.get(5));
        assertEquals(1, (long) lengthCounts.get(4));
    }

    @Test
    public void addBlockCountTest() {
        Block testBlock = new Block();
        assertEquals(0, (long) testBlock.getBlockCount());

        testBlock.addBlockCount(3);
        assertEquals(3, (long) testBlock.getBlockCount());
    }

    @Test
    public void reduceTest() {
        Block testBlock1 = new Block();
        testBlock1.addCharacter('V', 3);
        testBlock1.addCharacter('v', 18);
        testBlock1.addCharacter('%', 12);
        testBlock1.addLength(2, 7);
        testBlock1.addLength(4, 1);
        testBlock1.addLength(5, 3);
        testBlock1.addBlockCount(11);

        Block testBlock2 = new Block();
        testBlock2.addCharacter('V', 5);
        testBlock2.addCharacter('3', 16);
        testBlock2.addCharacter('/', 11);
        testBlock2.addLength(3, 3);
        testBlock2.addLength(8, 1);
        testBlock2.addLength(5, 3);
        testBlock2.addBlockCount(7);

        Block resultBlock = new Block();
        resultBlock.addCharacter('V', 8);
        resultBlock.addCharacter('v', 18);
        resultBlock.addCharacter('%', 12);
        resultBlock.addCharacter('3', 16);
        resultBlock.addCharacter('/', 11);
        resultBlock.addLength(2, 7);
        resultBlock.addLength(4, 1);
        resultBlock.addLength(5, 6);
        resultBlock.addLength(3, 3);
        resultBlock.addLength(8, 1);
        resultBlock.addBlockCount(18);

        testBlock1.reduce(testBlock2);
        assertEquals(resultBlock, testBlock1);
    }

    @Test
    public void generateTest() {
        Block testBlock = new Block();
        testBlock.addCharacter('V', 8);
        testBlock.addCharacter('v', 18);
        testBlock.addCharacter('%', 12);
        testBlock.addCharacter('3', 16);
        testBlock.addCharacter('/', 11);
        testBlock.addLength(2, 7);
        testBlock.addLength(4, 1);
        testBlock.addLength(5, 6);
        testBlock.addLength(3, 3);
        testBlock.addLength(8, 1);
        testBlock.addBlockCount(18);

        Set<Character> characterSet = testBlock.getCharacterCounts().keySet();
        Set<Integer> lengthSet = testBlock.getLengthCounts().keySet();

        String output = testBlock.generate(new Random());
        assertTrue(lengthSet.contains(output.length()));
        Character[] outputArray = ArrayUtils.toObject(output.toCharArray());
        Set<Character> outputCharacters = new HashSet(Arrays.asList(outputArray));
        assertTrue(characterSet.containsAll(outputCharacters));
    }

    @Test
    public void getGatherCountTest() {
        Block testBlock = new Block();
        testBlock.addCharacter('V', 8);
        testBlock.addCharacter('v', 18);
        testBlock.addCharacter('%', 12);
        testBlock.addCharacter('3', 16);
        testBlock.addCharacter('/', 11);
        testBlock.addLength(2, 7);
        testBlock.addLength(4, 1);
        testBlock.addLength(5, 6);
        testBlock.addLength(3, 3);
        testBlock.addLength(8, 1);
        testBlock.addBlockCount(18);

        assertNull(testBlock.getGatherCount(null));
        assertEquals(0, (long) testBlock.getGatherCount(""));
        assertEquals(0, (long) testBlock.getGatherCount("V"));
        assertEquals(0, (long) testBlock.getGatherCount("tVVV"));
        assertEquals(2, (long) testBlock.getGatherCount("V/"));
        assertEquals(2, (long) testBlock.getGatherCount("V/t"));
        assertEquals(3, (long) testBlock.getGatherCount("V/3"));
        assertEquals(5, (long) testBlock.getGatherCount("vvvvvvv"));
        assertEquals(8, (long) testBlock.getGatherCount("vvvvvvvv"));
        assertEquals(8, (long) testBlock.getGatherCount("vvvvvvvvv"));
    }

    @Test
    public void calcLikelihoodTest() {
        Block testBlock = new Block();
        testBlock.addCharacter('V', 8);
        testBlock.addCharacter('v', 18);
        testBlock.addCharacter('%', 12);
        testBlock.addCharacter('3', 16);
        testBlock.addCharacter('/', 11);
        testBlock.addLength(2, 7);
        testBlock.addLength(4, 1);
        testBlock.addLength(5, 6);
        testBlock.addLength(3, 3);
        testBlock.addLength(8, 1);
        testBlock.addBlockCount(18);

        assertNull(testBlock.calculateLikelihood(null));
        assertEquals(0., testBlock.calculateLikelihood(""), precision);
        assertEquals(0., testBlock.calculateLikelihood("V"), precision);
        assertEquals(0., testBlock.calculateLikelihood("Vt"), precision);
        assertEquals(0., testBlock.calculateLikelihood("VVt"), precision);
        assertEquals(0.00589086127547666, testBlock.calculateLikelihood("VV"), precision);
        assertEquals(0.0006991351843422851, testBlock.calculateLikelihood("VvV"), precision);
    }

    private void testConstructor(Block testBlock, Set<Character> definition) {
        assertEquals(definition, testBlock.getCharacters());
        Map<Character, Integer> characterCounts = testBlock.getCharacterCounts();
        assertNotNull(characterCounts);
        assertEquals(definition, characterCounts.keySet());
        for (Character character : characterCounts.keySet()) {
            assertEquals(0, (long) characterCounts.get(character));
        }
        Map<Integer, Integer> lengthCounts = testBlock.getLengthCounts();
        assertNotNull(lengthCounts);
        assertEquals(0, lengthCounts.size());
        assertEquals(0, (long) testBlock.getBlockCount());
    }
}
