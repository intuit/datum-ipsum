package com.intuit.datum_ipsum.implementations.hive;

import com.intuit.datum_ipsum.model.Sequence;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.io.Text;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MultipleGenerator extends GenericUDTF {
    private PrimitiveObjectInspector numResultsOI;
    private PrimitiveObjectInspector definitionOI;
    private PrimitiveObjectInspector seedOI;
    private PrimitiveObjectInspector.PrimitiveCategory seedType;
    private Object[] forwardColumns = new Object[1];

    @Override
    public StructObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        if (argOIs.length != 2 && argOIs.length != 3) {
            throw new UDFArgumentLengthException(
                    "Please specify either 2 or 3 arguments.");
        }

        ObjectInspector.Category category = argOIs[0].getCategory();
        if (category != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be an integer, but "
                            + category.toString() + " was found.");
        }
        numResultsOI = (PrimitiveObjectInspector) argOIs[0];
        PrimitiveObjectInspector.PrimitiveCategory primitiveCategory =
                numResultsOI.getPrimitiveCategory();
        if (primitiveCategory !=
                PrimitiveObjectInspector.PrimitiveCategory.INT) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be an integer, but "
                            + primitiveCategory.toString() + " was found.");
        }

        category = argOIs[1].getCategory();
        if (category != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(2,
                    "The second argument must be a string, but "
                            + category.toString() + " was found.");
        }
        definitionOI = (PrimitiveObjectInspector) argOIs[1];
        primitiveCategory = definitionOI.getPrimitiveCategory();
        if (primitiveCategory !=
                PrimitiveObjectInspector.PrimitiveCategory.STRING) {
            throw new UDFArgumentTypeException(2,
                    "The second argument must be a string, but "
                            + primitiveCategory.toString() + " was found.");
        }

        if (argOIs.length == 3) {
            category = argOIs[2].getCategory();
            if (category != ObjectInspector.Category.PRIMITIVE) {
                throw new UDFArgumentTypeException(3,
                        "The third argument must be a string or integer, but "
                                + category.toString() + " was found.");
            }
            seedOI = (PrimitiveObjectInspector) argOIs[2];
            seedType = seedOI.getPrimitiveCategory();
            if (seedType != PrimitiveObjectInspector.PrimitiveCategory.STRING &&
                    seedType != PrimitiveObjectInspector.PrimitiveCategory.INT) {
                throw new UDFArgumentTypeException(3,
                        "The third argument must be a string or integer, but "
                                + primitiveCategory.toString() + " was found.");
            }
        }

        List<String> fieldNames = new ArrayList<String>();
        List<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();
        fieldNames.add("result");
        fieldOIs.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames,
                fieldOIs);
    }

    @Override
    public void process(Object[] arguments) throws HiveException {
        assert(arguments.length == 2 || arguments.length == 3);
        if (arguments[0] == null || arguments[1] == null) {
            forwardColumns[0] = null;
            forward(forwardColumns);
        } else {
            Integer numResults = PrimitiveObjectInspectorUtils.getInt(arguments[0], numResultsOI);
            String jsonString = PrimitiveObjectInspectorUtils.getString(arguments[1], definitionOI);
            Sequence inputSequence;
            try {
                inputSequence = Sequence.fromJSONString(jsonString);
            } catch (JSONException je) {
                throw new HiveException("JSONException while deserializing Sequence.", je.getCause());
            }

            if (arguments.length == 3 && arguments[2] != null) {
                assert (seedType == PrimitiveObjectInspector.PrimitiveCategory.STRING ||
                        seedType == PrimitiveObjectInspector.PrimitiveCategory.INT);
                if (seedType == PrimitiveObjectInspector.PrimitiveCategory.STRING) {
                    String seed = PrimitiveObjectInspectorUtils.getString(arguments[2], seedOI);
                    inputSequence.resetGenerator(seed);
                } else {
                    Integer seed = PrimitiveObjectInspectorUtils.getInt(arguments[2], seedOI);
                    inputSequence.resetGenerator(seed);
                }
            }

            String output;
            for(Integer i = 0; i < numResults; i ++) {
                output = inputSequence.generate();
                if (output != null) {
                    forwardColumns[0] = new Text(output);
                } else {
                    forwardColumns[0] = null;
                }
                forward(forwardColumns);
            }
        }
    }

    @Override
    public void close() throws HiveException {
    }
}