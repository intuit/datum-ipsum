package com.intuit.datum_ipsum.model;

import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceTest {
    Double precision = 1e-9;

    Character[] characters1 = {'e', 'f', 's', 'a', 'd'};
    BlockDefinition definition1 = new BlockDefinition(new HashSet<Character>(Arrays.asList(characters1)));
    Character[] characters2 = {'^', '%', '&', '#', '@'};
    BlockDefinition definition2 = new BlockDefinition(new HashSet<Character>(Arrays.asList(characters2)));
    Character[] characters3 = {'2', '3'};
    BlockDefinition definition3 = new BlockDefinition(new HashSet<Character>(Arrays.asList(characters3)));
    Character[] characters4 = {'2', '3', 'f', 'a'};
    BlockDefinition definition4 = new BlockDefinition(new HashSet<Character>(Arrays.asList(characters4)));

    @Test
    public void initTest() {
        Sequence nullSequence = new Sequence();
        assertEquals((Integer) 0, nullSequence.getNullCount());
        assertEquals((Integer) 0, nullSequence.getTotalCount());
        assertEquals(0, nullSequence.getWheels().size());

        Sequence nullInput = new Sequence(null);
        assertEquals((Integer) 1, nullInput.getNullCount());
        assertEquals((Integer) 1, nullInput.getTotalCount());
        assertEquals(0, nullInput.getWheels().size());

        Sequence noDefinition = new Sequence("a");
        assertEquals((Integer) 0, noDefinition.getNullCount());
        assertEquals((Integer) 1, noDefinition.getTotalCount());
        assertEquals(1, noDefinition.getWheels().size());
    }

    @Test
    public void addTotalCountTest() {
        Sequence testSequence = new Sequence();
        assertEquals(0, (long) testSequence.getTotalCount());

        testSequence.addTotalCount(1);
        testSequence.addTotalCount(2);
        assertEquals(3, (long) testSequence.getTotalCount());
    }

    @Test
    public void addNullCountTest() {
        Sequence testSequence = new Sequence();
        assertEquals(0, (long) testSequence.getNullCount());

        testSequence.addNullCount(1);
        testSequence.addNullCount(2);
        assertEquals(3, (long) testSequence.getNullCount());
    }

    @Test
    public void addWheelTest() {
        Sequence testSequence = new Sequence();
        assertNotNull(testSequence.getWheels());
        assertEquals(0, testSequence.getWheels().size());

        Block testBlock1 = new Block();
        testBlock1.addCharacter('V', 3);
        testBlock1.addCharacter('v', 18);
        testBlock1.addCharacter('%', 12);
        testBlock1.addLength(2, 7);
        testBlock1.addLength(4, 1);
        testBlock1.addLength(5, 3);
        testBlock1.addBlockCount(11);
        Wheel testWheel1 = new Wheel(testBlock1);

        Block testBlock2 = new Block();
        testBlock2.addCharacter('V', 5);
        testBlock2.addCharacter('3', 16);
        testBlock2.addCharacter('/', 11);
        testBlock2.addLength(3, 3);
        testBlock2.addLength(8, 1);
        testBlock2.addLength(5, 3);
        testBlock2.addBlockCount(7);
        Wheel testWheel2 = new Wheel(testBlock2);

        testSequence.addWheel(testWheel1);
        testSequence.addWheel(testWheel2);
        assertNotNull(testSequence.getWheels());
        assertEquals(2, testSequence.getWheels().size());
    }

    @Test
    public void sequenceTest() {
        String input = "efsadf^%^&&dsa323fdd#@ad";
        Block expectedBlock = new Block();
        expectedBlock.addCharacter('e', 1);
        expectedBlock.addCharacter('f', 3);
        expectedBlock.addCharacter('s', 2);
        expectedBlock.addCharacter('a', 3);
        expectedBlock.addCharacter('d', 5);
        expectedBlock.addCharacter('^', 2);
        expectedBlock.addCharacter('%', 1);
        expectedBlock.addCharacter('&', 2);
        expectedBlock.addCharacter('3', 2);
        expectedBlock.addCharacter('2', 1);
        expectedBlock.addCharacter('#', 1);
        expectedBlock.addCharacter('@', 1);
        expectedBlock.addLength(24, 1);
        expectedBlock.addBlockCount(1);
        expectedBlock.setDefaultBlock(true);

        Wheel expectedWheel = new Wheel(expectedBlock);
        Sequence expectedSequence = new Sequence();
        expectedSequence.addTotalCount(1);
        expectedSequence.addWheel(expectedWheel);

        Sequence result = new Sequence(input);
        assertEquals(expectedSequence, result);
    }

    @Test
    public void sequenceTestWithDefinitions() {
        String input = "efsadf^%^&&dsa323fdd#@ad";

        List<BlockDefinition> definitions = new ArrayList();
        definitions.add(definition1);
        definitions.add(definition2);
        definitions.add(definition3);
        Sequence expectedSequence = new Sequence();
        expectedSequence.addTotalCount(1);

        Block expectedBlock1A = new Block(definition1);
        expectedBlock1A.addCharacter('e', 1);
        expectedBlock1A.addCharacter('f', 2);
        expectedBlock1A.addCharacter('s', 1);
        expectedBlock1A.addCharacter('a', 1);
        expectedBlock1A.addCharacter('d', 1);
        expectedBlock1A.addLength(6, 1);
        expectedBlock1A.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1A));

        Block expectedBlock2A = new Block(definition2);
        expectedBlock2A.addCharacter('^', 2);
        expectedBlock2A.addCharacter('%', 1);
        expectedBlock2A.addCharacter('&', 2);
        expectedBlock2A.addLength(5, 1);
        expectedBlock2A.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock2A));

        Block expectedBlock1B = new Block(definition1);
        expectedBlock1B.addCharacter('s', 1);
        expectedBlock1B.addCharacter('a', 1);
        expectedBlock1B.addCharacter('d', 1);
        expectedBlock1B.addLength(3, 1);
        expectedBlock1B.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1B));

        Block expectedBlock3 = new Block(definition3);
        expectedBlock3.addCharacter('3', 2);
        expectedBlock3.addCharacter('2', 1);
        expectedBlock3.addLength(3, 1);
        expectedBlock3.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock3));

        Block expectedBlock1C = new Block(definition1);
        expectedBlock1C.addCharacter('f', 1);
        expectedBlock1C.addCharacter('d', 2);
        expectedBlock1C.addLength(3, 1);
        expectedBlock1C.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1C));

        Block expectedBlock2B = new Block(definition2);
        expectedBlock2B.addCharacter('#', 1);
        expectedBlock2B.addCharacter('@', 1);
        expectedBlock2B.addLength(2, 1);
        expectedBlock2B.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock2B));

        Block expectedBlock1D = new Block(definition1);
        expectedBlock1D.addCharacter('a', 1);
        expectedBlock1D.addCharacter('d', 1);
        expectedBlock1D.addLength(2, 1);
        expectedBlock1D.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1D));

        Sequence result = new Sequence(input, definitions);
        assertEquals(expectedSequence, result);
    }

    @Test
    public void defintionPriorityTest() {
        String input = "efsadf^%^&&dsa323fdd#@ad";

        List<BlockDefinition> definitions = new ArrayList();
        definitions.add(definition1);
        definitions.add(definition2);
        definitions.add(definition4);
        Sequence expectedSequence = new Sequence();
        expectedSequence.addTotalCount(1);

        Block expectedBlock1A = new Block(definition1);
        expectedBlock1A.addCharacter('e', 1);
        expectedBlock1A.addCharacter('f', 2);
        expectedBlock1A.addCharacter('s', 1);
        expectedBlock1A.addCharacter('a', 1);
        expectedBlock1A.addCharacter('d', 1);
        expectedBlock1A.addLength(6, 1);
        expectedBlock1A.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1A));

        Block expectedBlock2A = new Block(definition2);
        expectedBlock2A.addCharacter('^', 2);
        expectedBlock2A.addCharacter('%', 1);
        expectedBlock2A.addCharacter('&', 2);
        expectedBlock2A.addLength(5, 1);
        expectedBlock2A.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock2A));

        Block expectedBlock1B = new Block(definition1);
        expectedBlock1B.addCharacter('s', 1);
        expectedBlock1B.addCharacter('a', 1);
        expectedBlock1B.addCharacter('d', 1);
        expectedBlock1B.addLength(3, 1);
        expectedBlock1B.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1B));

        Block expectedBlock3 = new Block(definition4);
        expectedBlock3.addCharacter('3', 2);
        expectedBlock3.addCharacter('2', 1);
        expectedBlock3.addCharacter('f', 1);
        expectedBlock3.addLength(4, 1);
        expectedBlock3.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock3));

        Block expectedBlock1C = new Block(definition1);
        expectedBlock1C.addCharacter('d', 2);
        expectedBlock1C.addLength(2, 1);
        expectedBlock1C.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1C));

        Block expectedBlock2B = new Block(definition2);
        expectedBlock2B.addCharacter('#', 1);
        expectedBlock2B.addCharacter('@', 1);
        expectedBlock2B.addLength(2, 1);
        expectedBlock2B.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock2B));

        Block expectedBlock1D = new Block(definition1);
        expectedBlock1D.addCharacter('a', 1);
        expectedBlock1D.addCharacter('d', 1);
        expectedBlock1D.addLength(2, 1);
        expectedBlock1D.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock1D));

        Sequence result = new Sequence(input, definitions);
        assertEquals(expectedSequence, result);
    }

    @Test
    public void emptyReduceTest() {
        Sequence emptySequence = new Sequence();

        Sequence expectedSequence = new Sequence();
        expectedSequence.addTotalCount(4);
        expectedSequence.addNullCount(1);
        Wheel expectedWheel2 = new Wheel();

        Block expectedBlock1 = new Block(definition1);
        expectedBlock1.addCharacter('e', 1);
        expectedBlock1.addCharacter('f', 2);
        expectedBlock1.addCharacter('s', 2);
        expectedBlock1.addCharacter('a', 2);
        expectedBlock1.addCharacter('d', 2);
        expectedBlock1.addLength(3, 3);
        expectedBlock1.addBlockCount(3);
        expectedSequence.addWheel(new Wheel(expectedBlock1));

        Block expectedBlock2A = new Block(definition2);
        expectedBlock2A.addCharacter('^', 2);
        expectedBlock2A.addCharacter('%', 1);
        expectedBlock2A.addCharacter('&', 2);
        expectedBlock2A.addLength(5, 1);
        expectedBlock2A.addBlockCount(1);
        expectedWheel2.addBlock(expectedBlock2A);

        Block expectedBlock2B = new Block(definition3);
        expectedBlock2B.addCharacter('3', 2);
        expectedBlock2B.addCharacter('2', 1);
        expectedBlock2B.addLength(3, 1);
        expectedBlock2B.addBlockCount(1);
        expectedWheel2.addBlock(expectedBlock2B);
        expectedSequence.addWheel(expectedWheel2);

        Block expectedBlock3 = new Block(definition1);
        expectedBlock3.addCharacter('a', 1);
        expectedBlock3.addLength(1, 1);
        expectedBlock3.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock3));

        emptySequence.reduce(expectedSequence);
        assertEquals(expectedSequence, emptySequence);
    }

    @Test
    public void reduceTest() {
        Sequence sequenceA = new Sequence();
        sequenceA.addTotalCount(2);
        Sequence sequenceB = new Sequence();
        sequenceB.addTotalCount(2);
        sequenceB.addNullCount(1);

        Block blockA1 = new Block(definition1);
        blockA1.addCharacter('e', 1);
        blockA1.addCharacter('f', 2);
        blockA1.addCharacter('s', 1);
        blockA1.addCharacter('a', 1);
        blockA1.addCharacter('d', 1);
        blockA1.addLength(3, 2);
        blockA1.addBlockCount(2);
        sequenceA.addWheel(new Wheel(blockA1));

        Block blockA2 = new Block(definition2);
        blockA2.addCharacter('^', 2);
        blockA2.addCharacter('%', 1);
        blockA2.addCharacter('&', 2);
        blockA2.addLength(5, 1);
        blockA2.addBlockCount(1);
        sequenceA.addWheel(new Wheel(blockA2));

        Block blockB1 = new Block(definition1);
        blockB1.addCharacter('s', 1);
        blockB1.addCharacter('a', 1);
        blockB1.addCharacter('d', 1);
        blockB1.addLength(3, 1);
        blockB1.addBlockCount(1);
        sequenceB.addWheel(new Wheel(blockB1));

        Block blockB2 = new Block(definition3);
        blockB2.addCharacter('3', 2);
        blockB2.addCharacter('2', 1);
        blockB2.addLength(3, 1);
        blockB2.addBlockCount(1);
        sequenceB.addWheel(new Wheel(blockB2));

        Block blockB3 = new Block(definition1);
        blockB3.addCharacter('a', 1);
        blockB3.addLength(1, 1);
        blockB3.addBlockCount(1);
        sequenceB.addWheel(new Wheel(blockB3));


        Sequence expectedSequence = new Sequence();
        expectedSequence.addTotalCount(4);
        expectedSequence.addNullCount(1);
        Wheel expectedWheel2 = new Wheel();

        Block expectedBlock1 = new Block(definition1);
        expectedBlock1.addCharacter('e', 1);
        expectedBlock1.addCharacter('f', 2);
        expectedBlock1.addCharacter('s', 2);
        expectedBlock1.addCharacter('a', 2);
        expectedBlock1.addCharacter('d', 2);
        expectedBlock1.addLength(3, 3);
        expectedBlock1.addBlockCount(3);
        expectedSequence.addWheel(new Wheel(expectedBlock1));

        Block expectedBlock2A = new Block(definition2);
        expectedBlock2A.addCharacter('^', 2);
        expectedBlock2A.addCharacter('%', 1);
        expectedBlock2A.addCharacter('&', 2);
        expectedBlock2A.addLength(5, 1);
        expectedBlock2A.addBlockCount(1);
        expectedWheel2.addBlock(expectedBlock2A);

        Block expectedBlock2B = new Block(definition3);
        expectedBlock2B.addCharacter('3', 2);
        expectedBlock2B.addCharacter('2', 1);
        expectedBlock2B.addLength(3, 1);
        expectedBlock2B.addBlockCount(1);
        expectedWheel2.addBlock(expectedBlock2B);
        expectedSequence.addWheel(expectedWheel2);

        Block expectedBlock3 = new Block(definition1);
        expectedBlock3.addCharacter('a', 1);
        expectedBlock3.addLength(1, 1);
        expectedBlock3.addBlockCount(1);
        expectedSequence.addWheel(new Wheel(expectedBlock3));

        sequenceA.reduce(sequenceB);
        assertEquals(expectedSequence, sequenceA);
    }

    @Test
    public void generateTest() {
        String input = "efsadf^%^&&dsa323fdd#@ad";
        Character[] allCharacters = ArrayUtils.addAll(characters1, ArrayUtils.addAll(characters2, characters3));
        BlockDefinition fullDefinition = new BlockDefinition(new HashSet<Character>(Arrays.asList(allCharacters)));
        Sequence characterized = new Sequence(input);

        String output1 = characterized.generate("test");
        assertEquals(24, output1.length());
        Character[] outputArray = ArrayUtils.toObject(output1.toCharArray());
        Set<Character> outputCharacters = new HashSet(Arrays.asList(outputArray));
        assertTrue(fullDefinition.getCharacters().containsAll(outputCharacters));

        assertEquals(characterized.generate("test", "salt"), characterized.generate("test", "salt"));
        assertNotEquals(output1, characterized.generate("test", "salt"));

        Sequence nullsOnly = new Sequence();
        nullsOnly.addTotalCount(5);
        nullsOnly.addNullCount(5);
        String nullOutput = nullsOnly.generate("test");
        assertNull(nullOutput);

        Sequence characterized2 = new Sequence(input);
        characterized.resetGenerator("test");
        characterized2.resetGenerator(3556498);
        assertEquals(characterized.generate(), characterized2.generate());
    }

    @Test
    public void serializeDeserializeTest() throws IOException, ClassNotFoundException {
        String input = "efsadf^%^&&dsa323fdd#@ad";
        Sequence characterized = new Sequence(input);

        Sequence characterized2 = Sequence.deserialize(characterized.serialize());
        assertEquals(characterized, characterized2);
    }

    @Test
    public void serializeDeserializeJSONTest() throws JSONException {
        String input = "e";
        Sequence characterized = new Sequence(input);

        Sequence characterized2 = Sequence.fromJSONString(characterized.toJSONString());
        assertEquals(characterized, characterized2);
    }

    @Test
    public void calculateLikelihoodTest() {
        Sequence testSequence = new Sequence();
        testSequence.addTotalCount(4);
        testSequence.addNullCount(1);
        Wheel testWheel2 = new Wheel();

        Block expectedBlock1 = new Block(definition1);
        expectedBlock1.addCharacter('e', 1);
        expectedBlock1.addCharacter('f', 2);
        expectedBlock1.addCharacter('s', 2);
        expectedBlock1.addCharacter('a', 2);
        expectedBlock1.addCharacter('d', 2);
        expectedBlock1.addLength(3, 3);
        expectedBlock1.addBlockCount(3);
        testSequence.addWheel(new Wheel(expectedBlock1));

        Block expectedBlock2A = new Block(definition2);
        expectedBlock2A.addCharacter('^', 2);
        expectedBlock2A.addCharacter('%', 1);
        expectedBlock2A.addCharacter('&', 2);
        expectedBlock2A.addLength(5, 1);
        expectedBlock2A.addBlockCount(1);
        testWheel2.addBlock(expectedBlock2A);

        Block expectedBlock2B = new Block(definition3);
        expectedBlock2B.addCharacter('3', 2);
        expectedBlock2B.addCharacter('2', 1);
        expectedBlock2B.addLength(3, 1);
        expectedBlock2B.addBlockCount(1);
        testWheel2.addBlock(expectedBlock2B);
        testSequence.addWheel(testWheel2);

        Block expectedBlock3 = new Block(definition1);
        expectedBlock3.addCharacter('a', 1);
        expectedBlock3.addLength(1, 1);
        expectedBlock3.addBlockCount(1);
        testSequence.addWheel(new Wheel(expectedBlock3));

        assertEquals(0.25, testSequence.calculateLikelihood(null), precision);
        assertEquals(0., testSequence.calculateLikelihood(""), precision);
        assertEquals(0., testSequence.calculateLikelihood("ff"), precision);
        assertEquals(0.003657978966620941, testSequence.calculateLikelihood("fff"), precision);
        assertEquals(0., testSequence.calculateLikelihood("ffff"), precision);
        assertEquals(0., testSequence.calculateLikelihood("fff3"), precision);
        assertEquals(0.0007225637464930252, testSequence.calculateLikelihood("fff333"), precision);
        assertEquals(1.2485901539399481e-05, testSequence.calculateLikelihood("fff&&&&&a"), precision);
        assertEquals(0., testSequence.calculateLikelihood("fff&&&&&aa"), precision);
        assertEquals(0., testSequence.calculateLikelihood("fff&&&&&ab"), precision);
    }
}
