package com.intuit.datum_ipsum.implementations.hive;

import com.intuit.datum_ipsum.model.BlockDefinition;
import com.intuit.datum_ipsum.model.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.Text;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterizeResolver extends AbstractGenericUDAFResolver {
    static final Log LOG = LogFactory.getLog(CharacterizeResolver.class.getName());

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        if (parameters.length != 1 && parameters.length != 2) {
            throw new UDFArgumentTypeException(parameters.length - 1,
                    "Please specify either 1 or 2 arguments.");
        }
        if (parameters[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be a primitive type, but "
                            + parameters[0].getTypeName() + " was found.");
        }
        if ( ((PrimitiveTypeInfo) parameters[0]).getPrimitiveCategory()
                != PrimitiveObjectInspector.PrimitiveCategory.STRING) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be a string, but "
                            + parameters[0].getTypeName() + " was found.");
        }

        if (parameters.length == 2) {
            if (parameters[1].getCategory() != ObjectInspector.Category.LIST) {
                throw new UDFArgumentTypeException(2,
                        "The second argument must be an array(string), but "
                                + parameters[1].getTypeName() + " was found.");
            }
            ListTypeInfo lti = (ListTypeInfo) parameters[1];
            ObjectInspector.Category category = lti.getListElementTypeInfo().getCategory();
            if (category != ObjectInspector.Category.PRIMITIVE) {
                throw new UDFArgumentTypeException(2,
                        "The second argument must be an array(string), but "
                                + "array(" + category.toString() + ") was found.");
            }
            PrimitiveObjectInspector.PrimitiveCategory pc =
                    ((PrimitiveTypeInfo) lti.getListElementTypeInfo()).getPrimitiveCategory();
            if (pc != PrimitiveObjectInspector.PrimitiveCategory.STRING) {
                throw new UDFArgumentTypeException(2,
                        "The second argument must be an array(string), but "
                                + "array(" + pc.toString() + ") was found.");
            }
        }

        return new CharacterizeEvaluator();
    }

    public static class CharacterizeEvaluator extends GenericUDAFEvaluator {
        // For PARTIAL1 and COMPLETE: ObjectInspectors for original data
        private PrimitiveObjectInspector inputOI;
        private StandardListObjectInspector definitionsOI;
        private PrimitiveObjectInspector charactersOI;

        // For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations
        private PrimitiveObjectInspector aggregateOI;

        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] argOIs)
                throws HiveException {
            super.init(m, argOIs);

            // init input object inspectors
            if (m == Mode.PARTIAL1 || m == Mode.COMPLETE) {
                assert (argOIs.length == 1 || argOIs.length == 2);
                assert (argOIs[0].getCategory() == ObjectInspector.Category.PRIMITIVE);
                inputOI = (PrimitiveObjectInspector) argOIs[0];
                assert (inputOI.getPrimitiveCategory() ==
                        PrimitiveObjectInspector.PrimitiveCategory.STRING);

                if (argOIs.length == 2) {
                    assert (argOIs[1].getCategory() == ObjectInspector.Category.LIST);
                    definitionsOI = (StandardListObjectInspector) argOIs[1];

                    ObjectInspector elementsOI = definitionsOI.getListElementObjectInspector();
                    assert (elementsOI.getCategory() == ObjectInspector.Category.PRIMITIVE);

                    charactersOI = (PrimitiveObjectInspector) elementsOI;
                    assert (charactersOI.getPrimitiveCategory() ==
                            PrimitiveObjectInspector.PrimitiveCategory.STRING);
                }
            } else {
                assert (argOIs[0].getCategory() == ObjectInspector.Category.PRIMITIVE);
                aggregateOI = (PrimitiveObjectInspector) argOIs[0];
                assert (aggregateOI.getPrimitiveCategory() ==
                        PrimitiveObjectInspector.PrimitiveCategory.STRING);
            }

            return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            StdAgg myagg = (StdAgg) agg;
            String output;
            try {
                output = myagg.aggregateSequence.toJSONString();
            }
            catch (JSONException je) {
                throw new HiveException("JSONException while serializing Sequence.", je.getCause());
            }
            Text result = new Text(output);
            return result;
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            StdAgg myagg = (StdAgg) agg;
            String output;
            try {
                output = myagg.aggregateSequence.toJSONString();
            }
            catch (JSONException je) {
                throw new HiveException("JSONException while serializing Sequence.", je.getCause());
            }
            Text result = new Text(output);
            return result;
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial)
                throws HiveException {
            if(partial != null) {
                String partialJSON = PrimitiveObjectInspectorUtils.getString(partial, aggregateOI);
                Sequence partialSequence = null;
                try {
                    partialSequence = Sequence.fromJSONString(partialJSON);
                } catch (JSONException je) {
                    throw new HiveException("JSONException while deserializing Sequence.", je.getCause());
                }
                StdAgg myagg = (StdAgg) agg;
                myagg.aggregateSequence.reduce(partialSequence);
            }
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] arguments)
                throws HiveException {
            assert (arguments.length == 1 || arguments.length == 2);

            StdAgg myagg = (StdAgg) agg;

            List<BlockDefinition> definitions = new ArrayList();
            if(arguments.length == 2 && arguments[1] != null) {
                Set<Character> characters;
                List inputStrings = (List) definitionsOI.getList(arguments[1]);
                for (Integer arrayIndex = 0; arrayIndex < inputStrings.size(); arrayIndex ++) {
                    characters = new HashSet();
                    String definition = (String) charactersOI.getPrimitiveJavaObject(inputStrings.get(arrayIndex));
                    for (Integer stringIndex = 0; stringIndex < definition.length(); stringIndex ++) {
                        characters.add(definition.charAt(stringIndex));
                    }
                    definitions.add(new BlockDefinition(characters));
                }
            }

            // Process the current data point
            String input = null;
            if(arguments[0] != null) {
                input = PrimitiveObjectInspectorUtils.getString(arguments[0], inputOI);
            }
            Sequence newSequence = new Sequence(input, definitions);
            myagg.aggregateSequence.reduce(newSequence);
        }

        // Aggregation buffer definition and manipulation methods
        static class StdAgg implements AggregationBuffer {
            Sequence aggregateSequence;
        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() throws HiveException {
            StdAgg result = new StdAgg();
            reset(result);
            return result;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            StdAgg myagg = (StdAgg) agg;
            myagg.aggregateSequence = new Sequence();
        }
    }
}
