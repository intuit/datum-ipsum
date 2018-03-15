# Datum Ipsum

Datum Ipsum is a tool for generating fake strings using a set of statistical properties characterizing the strings. These statistical properties can be determined from a set of input strings or can be user-specified. The primary statistical distributions of interest are (word) length and character frequency.

Datum Ipsum is designed to support execution via the map-reduce paradigm, e.g. in Hive.

## Contents
1. [Motivation](#motivation)
2. [Concepts](#concepts)
    1. [Block](#block)
    2. [Wheel](#wheel)
    3. [Sequence](#sequence)
    4. [Generating Strings](#generating-strings)
    5. [Control of Random Seeds and Salts](#control-of-random-seeds-and-salts)
    6. [Characterizing Sequences](#characterizing-sequences)
    7. [Block Definition](#block-definition)
    8. [Likelihood Calculation](#likelihood-calculation)
3. [Usage and Examples](#usage-and-examples)
    1. [Registering the UDFs](#registering-the-udfs)
    2. [Loading Example Data](#loading-example-data)
    3. [Usage Examples](#usage-examples)
4. [Build Process](#build-process)
5. [License](#license)

## Motivation
Here are a few situations in which you might find Datum Ipsum useful:

* You want to test a potentially insecure environment (e.g. pre-production, a cloud compute account) using example data that accurately represents your real data.
* You want to check a large set of text data (e.g. phone numbers or names) to determine if the data follows the desired format (and what discrepancies exist).
* You want to compare two sets of strings (e.g. user names) and to calculate, for a given string, which set it most likely came from.

## Concepts
Datum Ipsum generates strings based on three statistical distributions:

1. Frequency of characters.
2. Length of (sub)strings.
3. Frequency of nulls.

The distribution of nulls is applied to the entire string (which is either null or not). The character and length distributions are applied to substrings, which are called blocks.

### Block
A block is like a word in a sentence. It is defined by a length distribution -- describing the number of characters, and a character distribution -- describing the frequency of characters. Each distribution is represented by a set of options with corresponding counts.

This is a length distribution (in JSON representation) with 25% probability of length 2, 25% probability of length 4, and 50% probability of length 5.

    "lengthCounts" : {"2": 1, "4": 1, "5": 2}

This character distribution contains English vowels with frequencies based on the common mnemonic "aeiou and sometimes y". All other characters have probability 0.

    "characterCounts" : {"a": 2, "e": 2, "i": 2, "o": 2, "u": 2, "y": 1}

We'll discuss below how these distributons can be generated from data. They can also be user-specified.

### Wheel
Continuing the word analogy, if a block characterizes the length and character distributions of a single word then a wheel contains the different words (or blocks) that might be present at a given position. Each wheel is a collection of blocks along with counts for each block (technically, the counts are stored as part of the block object). Similar to the length and character distributions, a wheel represents a probability distribution over blocks.

### Sequence
The final construct in our analogy is the sequence, corresponding to a sentence of words. This is the top level object that you'll be working with most directly. A sequence is comprised of a list of wheels. A sequence also tracks counts of the total number of strings (used as a denominator for the sequence- and wheel-level probabilty distributions) and the number of nulls.

### Generating Strings
One of the core functionalities of Datum Ipsum is to generate random strings from a defined sequence (which is comprised of wheels and blocks). The generation process follows the following logic:

1. Randomly determine if the string is null, based on the probability given by the null count divided by the total count.
2. If the string is not null, randomly choose a block from the first wheel. The block probabilities are given by the count for each block divided by the total count.
3. Randomly select a number of characters for the block, based on the length distrbution.
4. For each character, randomly draw from the block's character distribution.
5. This completes the first wheel. Move to the next wheel in the sequence and repeat steps 2 - 4.  During step 2 (choosing a block), it may be that no block is chosen because the total of the block counts is less than the total counts.  In this case the wheel returns an empty string and the generation of the sequence terminates.
6. The string is complete when all wheels in the sequence have been evaluated (or have been terminated, as described above). The output string is the concatenation of all wheel contributions, in order.

We have discussed blocks, wheels, and sequences with an analogy to sentences. This particular implementation is achieved by alternating blocks with letters (e.g. a-z) with blocks comprised only of a single space. However, other patterns are also possible. For example, sample email addresses can be generated by three alphanumeric blocks, separated by symbol blocks (each with a single symbol: "@" and ".", respectively). Below, we discuss more about how to define blocks and construct them based on a set of input data.

### Control of Random Seeds and Salts
Datum Ipsum provides control over the randomization used in string generation. A sequence object contains a random number generator which can be (re)initialized with a specified seed. This allows multiple random output strings to be generated from the same sequence with repeatability across runs (if desired).

Alternatively, the random number generator can be reseeded for each generated string output, based on an input string. This is useful to generate fake data that preserves relationships in a set of input data. For example, if you have a database of US state names (e.g. as part of a customer address) each state will comprise a certain portion of the data. In this case, each original string (acutal state) is used as the seed for generating a corresponding random string (fake 'state'). Any two identical real strings will map to the same (randomly generated) output string, and hence each (fake) 'state' will maintain the same portion of representation in the data. If desired, the input seed string can also be salted. To continue the example above, you might have a "home address state" field and a "billing address state" field that you want to handle separately. Using the field name as a salt will lead to consistency in the random output within each field, but not across fields.

### Characterizing Sequences
You can create your own sequence by building it from wheels and blocks as described above. This can be achieved by manipulating these objects in Java or by reconstructing a sequence from a serialized JSON representation. However, Datum Ipsum also provides powerful functionality to characterize a sequence based on a set of input data.

Starting with a single input string, a sequence is constructed with a single wheel containing a single block. The block characteristics (length and character distributions) are derived from the input string. This process is performed on each input string (map step) and the resulting sequences are combined together (reduce step) to produce a final output sequence. Sequences are combined by aggregating counts at each level:

1. Total and null counts at the sequence level.
2. Block counts, for matching blocks between wheels with the same ordered position.
3. Length and character counts within matching blocks.

In this simple example, each sequence only contains a single wheel. Blocks are matched based on their block definitions (more on this below). Because the block definitions were not defined in this example, they are derived from the input strings.

### Block Definition
When characterizing a sequence you can also specify an ordered list of block definitions. A block definition is merely a set of characters that go into that block. If you wanted to characterize a sequence based on email addresses you might use these block definitions:

1. alphanumeric characters (plus whatever other symbols are valid in the username or domain portion)
2. "@"
3. "."

Or you might just define blocks 2 and 3 and let Datum Ipsum create other blocks as necessary (this is especially useful if you suspect that your data is contaminated by uncommon or invalid characters).

When characterizing with block definitions, the following occurs:

1. For each character in the input string, check if that character is part of the block definitons (in order).
2. If a match is found, start using the block. Otherwise, create a default block that will accumulate characters not specified as part of the block definitions.
3. Once started, a (non-generic) block will greedily accumulate characters from the input string until encountering one that doesn't match the block definitions. Default blocks will only accumulate characters that are not part of the specified block definitions.
4. Once a block ends it is placed in the next available wheel (starting with the first wheel) and a new block started. In this manner, each input string creates an ordered list of blocks which accumulate into an ordered list of wheels as the sequence is aggregated across multiple input strings (following the steps above).

Block definitions are a powerful tool to control the structure of the final sequence. You can use block definitions to specify separation characters (e.g. whitespace) or character sets (e.g. alphanumeric) that form patterns in your data. Block definitions can be mutually exclusive or may share characters. But you don't have to include all possible characters -- Datum Ipsum will always collect any unspecified characters it comes across. This is especially useful because real data often has unexpected characters that you wouldn't think to include if generating test data by hand.

### Likelihood Calculation
Because Datum Ipsum is based on probability distributions, it can also be used to answer questions about those distributions. Specifcally, you can calculate the probability that a specific string would be generated from a given sequence (out of all the possible strings that could be generated by that sequence). This probability could be compared across multiple strings for a given sequence to understand the distribution of potential outputs. Or, it could be compared across sequences (for a single output string) to calculate which sequence was more likely to produce that string.

## Usage and Examples
This section shows how to use the Datum Ipsum UDFs in Hive.

### Registering the UDFs
The functions in the jar file must be registered with Hive before use.

If using the Beeline CLI, the jar has to be on HDFS:

    hadoop fs -put datum-ipsum-1.1.jar {path}/datum-ipsum-1.1.jar

After that, register the UDFs within Beeline:
```
create function characterize as 'com.intuit.datum_ipsum.implementations.hive.CharacterizeResolver' using jar 'hdfs:{path}/datum-ipsum-1.1.jar';
create function generate as 'com.intuit.datum_ipsum.implementations.hive.Generator' using jar 'hdfs:{path}/datum-ipsum-1.1.jar';
create function generatemultiple as 'com.intuit.datum_ipsum.implementations.hive.MultipleGenerator' using jar 'hdfs:{path}/datum-ipsum-1.1.jar';
create function likelihood as 'com.intuit.datum_ipsum.implementations.hive.LikelihoodCalculator' using jar 'hdfs:{path}/datum-ipsum-1.1.jar';
create function combine as 'com.intuit.datum_ipsum.implementations.hive.SequenceCombiner' using jar 'hdfs:{path}/datum-ipsum-1.1.jar';
```

Or, if you're using the old Hive CLI:
```
add jar datum-ipsum-1.1.jar;
list jars; -- optional, to confirm the previous step
create temporary function characterize as 'com.intuit.datum_ipsum.implementations.hive.CharacterizeResolver';
create temporary function generate as 'com.intuit.datum_ipsum.implementations.hive.Generator';
create temporary function generatemultiple as 'com.intuit.datum_ipsum.implementations.hive.MultipleGenerator';
create temporary function likelihood as 'com.intuit.datum_ipsum.implementations.hive.LikelihoodCalculator';
create temporary function combine as 'com.intuit.datum_ipsum.implementations.hive.SequenceCombiner';
```

### Loading Example Data
**Warning: the load scripts will overwrite existing tables of the same name!** For this reason, we sugest you create a new database for exploring Datum Ipsum.

In order to load data, copy the `examples` directory to an edge node on your Hadoop cluster (just the local file system, not HDFS). Then run the `load_all.sh` script, specifying the database name and map-reduce queue, to load two small tables: `simple` and `names`. The syntax is:
```
load_all.sh [database_name] [map_reduce_queue]
```
These tables are used in the examples below.

Note: this script uses the Hive CLI to load the tables. Although it's deprecated, the Hive CLI is simpler than using Beeline, since the csv files can be read from the local filesystem rather than having to be loaded into HDFS. Feel free to use Beeline if you're so inclined (and contribute your code back to this project!).

### Usage Examples

Make sure to use the database from above, where you loaded the sample data.

#### Simple characterize step
```
select characterize(example) from simple where row <= 1;
select characterize(example) from simple where row <= 2;
select characterize(example) from simple where row <= 3;
```

Example Output:
```
{"totalCount":3,"nullCount":1,"wheels":[{"blocks":[{"defaultBlock":true,"blockCount":2,"characterCounts":{"d":1,"a":2,"t":1,"u":2,"m":3," ":1,"i":1,"p":1,"s":2,"w":1,"e":2,"o":1},"lengthCounts":{"11":1,"7":1}}]}]}
```

#### Generate random data (no seed, string seed, integer seed)
```
create table simple_characterized as select characterize(example) as characterization from simple;
select generatemultiple(10, characterization) from simple_characterized;
select generatemultiple(10, characterization, '0') from simple_characterized;
select generatemultiple(10, characterization, 0) from simple_characterized;
```

Example Output:
```
result
NULL
poteauo
ueuasda
edoppupwaep
oaitiei
NULL
eeeotmo
emmedau
udsime
mtmtessases
```

#### More realistic characterize and generate example (without and with salt)
```
select characterize(name) from names;
create table names_characterized as select characterize(name) as characterization from names;
select generate(characterization, name), name, type from names_characterized full join names;
select generate(characterization, name, type), name, type from names_characterized full join names;
```

Example Output:
```
amr srihndfe	eddie bauer	company
tpla  a fra	eddie bauer	personal
hle nmhlted 	ralph lauren	company
r naua ldbi	ralph lauren	personal
ldi teei lie	james todd fast	personal
rtroadhbre u	intuit	   	company
```

#### Same data, separating out spaces
```
select characterize(name, array(' ')) from names;
create table names_characterized2 as
select characterize(name, array(' ')) as characterization from names;
select generate(characterization, name), name, type from names_characterized2 full join names;
select generate(characterization, name, type), name, type from names_characterized2 full join names;
```

Example Output:
```
aedem		eddie bauer	company
iseaj rnrrur	eddie bauer	personal
sdadp erdad	ralph lauren	company
hddta aedb	ralph lauren	personal
llhai brubea	james todd fast	personal
erele tborrn	intuit	   	company
```

#### Calculate likelihood example
```
select likelihood("test", characterization),
       likelihood("aaaaaaa", characterization)
from simple_characterized;
```

Example Output:
```
0.0	1.0453757906438444E-7
```

## Build Process
Datum Ipsum uses Maven. To build the jar, run `mvn paackage` from the top-level directory (with the pom file). The current 1.1 jar is also included in the `jars` directory, so you can try out Datum Ipsum without having to rebuild the jar.

## License
Copyright 2018 Intuit, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.