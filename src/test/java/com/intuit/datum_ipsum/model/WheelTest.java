package com.intuit.datum_ipsum.model;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WheelTest {
    Double precision = 1e-9;

    Block testBlock1A = new Block();
    Block testBlock1B = new Block();
    Block compareBlock1 = new Block();
    Block testBlock2 = new Block();
    Block compareBlock2 = new Block();

    @Before
    public void createBlocks() {
        testBlock1A.addCharacter('V', 3);
        testBlock1A.addCharacter('v', 18);
        testBlock1A.addCharacter('%', 12);
        testBlock1A.addLength(2, 7);
        testBlock1A.addLength(4, 1);
        testBlock1A.addLength(5, 3);
        testBlock1A.addBlockCount(11);

        testBlock1B.addCharacter('V', 7);
        testBlock1B.addCharacter('v', 9);
        testBlock1B.addCharacter('%', 18);
        testBlock1B.addLength(3, 7);
        testBlock1B.addLength(1, 1);
        testBlock1B.addLength(4, 3);
        testBlock1B.addBlockCount(11);

        compareBlock1.addCharacter('V', 10);
        compareBlock1.addCharacter('v', 27);
        compareBlock1.addCharacter('%', 30);
        compareBlock1.addLength(2, 7);
        compareBlock1.addLength(4, 1);
        compareBlock1.addLength(5, 3);
        compareBlock1.addLength(3, 7);
        compareBlock1.addLength(1, 1);
        compareBlock1.addLength(4, 3);
        compareBlock1.addBlockCount(22);

        testBlock2.addCharacter('V', 5);
        testBlock2.addCharacter('3', 16);
        testBlock2.addCharacter('/', 11);
        testBlock2.addLength(3, 3);
        testBlock2.addLength(8, 1);
        testBlock2.addLength(5, 3);
        testBlock2.addBlockCount(7);

        compareBlock2.addCharacter('V', 5);
        compareBlock2.addCharacter('3', 16);
        compareBlock2.addCharacter('/', 11);
        compareBlock2.addLength(3, 3);
        compareBlock2.addLength(8, 1);
        compareBlock2.addLength(5, 3);
        compareBlock2.addBlockCount(7);
    }

    @Test
    public void reduceTest() {
        Wheel testWheel1 = new Wheel();
        testWheel1.addBlock(testBlock1A);

        Wheel testWheel2 = new Wheel();
        testWheel2.addBlock(testBlock1B);
        testWheel2.addBlock(testBlock2);

        BlockDefinition definition1 = testBlock1A.getDefinition();
        BlockDefinition definition2 = testBlock2.getDefinition();

        testWheel1.reduce(testWheel2);

        Map<BlockDefinition, Block> blocks = testWheel1.getBlocks();
        assertNotNull(blocks);
        assertEquals(2, blocks.size());
        assertEquals(compareBlock1, blocks.get(definition1));
        assertEquals(compareBlock2, blocks.get(definition2));
    }

    @Test
    public void generateTest() {
        Wheel testWheel = new Wheel();
        testWheel.addBlock(testBlock1A);
        testWheel.addBlock(testBlock2);

        Set<Character> characterSet = new HashSet();
        Set<Integer> lengthSet = new HashSet();
        for (Block block : testWheel.getBlocks().values()) {
            characterSet.addAll(block.getCharacters());
            lengthSet.addAll(block.getLengthCounts().keySet());
        }

        String output = testWheel.generate(new Random());
        assertTrue(lengthSet.contains(output.length()));
        Character[] outputArray = ArrayUtils.toObject(output.toCharArray());
        Set<Character> outputCharacters = new HashSet(Arrays.asList(outputArray));
        assertTrue(characterSet.containsAll(outputCharacters));


        Wheel testEmpty = new Wheel();
        Character[] characters = {'e', 'f', 's', 'a', 'd'};
        Block zeroBlock = new Block(new HashSet<Character>(Arrays.asList(characters)));
        testEmpty.addBlock(zeroBlock);

        output = testEmpty.generate(new Random(), 10);
        assertEquals("", output);
    }

    @Test
    public void calculateLikelihoodTest() {
        Wheel testWheel = new Wheel();
        testWheel.addBlock(testBlock1A);
        testWheel.addBlock(testBlock2);
        List<Character> testString = null;

        testString = convertString("");
        assertEquals(0., testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString(""), testString);

        testString = convertString("V");
        assertEquals(0., testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString("V"), testString);

        testString = convertString("Vt");
        assertEquals(0., testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString("Vt"), testString);

        testString = convertString("VV");
        assertEquals(0.0032139577594123047, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString(""), testString);

        testString = convertString("VVt");
        assertEquals(0.0032139577594123047, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString("t"), testString);

        testString = convertString("VVV");
        assertEquals(0.0006357828776041665, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString(""), testString);

        testString = convertString("VVV");
        assertEquals(0.0006357828776041665, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString(""), testString);

        testString = convertString("VVVVV");
        assertEquals(1.655691178202323e-05, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString(""), testString);

        testString = convertString("VVVVVt");
        assertEquals(1.655691178202323e-05, testWheel.calculateLikelihood(testString), precision);
        assertEquals(convertString("t"), testString);
    }


    private List<Character> convertString(String input) {
        List<Character> output = new ArrayList();
        for (Character c : ArrayUtils.toObject(input.toCharArray())) {
            output.add(c);
        }
        return output;
    }
}
