package com.intuit.datum_ipsum.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockDefinitionTest {

    @Test
    public void blockTest() {
        Character[] characters = {'g', 'H', '8', '^'};
        Set<Character> definition = new HashSet(Arrays.asList(characters));
        BlockDefinition testBlock = new BlockDefinition(definition);
        assertEquals(definition, testBlock.getCharacters());
    }

    @Test
    public void equalsTest() {
        Character[] characters = {'g', 'H', '8', '^'};
        Set<Character> definition = new HashSet(Arrays.asList(characters));
        BlockDefinition testDefinition1 = new BlockDefinition(definition);
        BlockDefinition testDefinition2 = new BlockDefinition(definition);
        assertEquals(testDefinition1, testDefinition2);
    }
}
