package com.intuit.datum_ipsum.implementations.hive;

import com.intuit.datum_ipsum.model.Sequence;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.io.Text;
import org.codehaus.jettison.json.JSONException;

public class Generator extends GenericUDF {
    private PrimitiveObjectInspector definitionOI;
    private PrimitiveObjectInspector seedOI;
    private PrimitiveObjectInspector saltOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] argOIs)
            throws UDFArgumentException {
        if (argOIs.length != 2 && argOIs.length != 3) {
            throw new UDFArgumentLengthException(
                    "Please specify either 2 or 3 arguments.");
        }

        ObjectInspector.Category category = argOIs[0].getCategory();
        if (category != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be a string, but "
                            + category.toString() + " was found.");
        }
        definitionOI = (PrimitiveObjectInspector) argOIs[0];
        PrimitiveObjectInspector.PrimitiveCategory primitiveCategory =
                definitionOI.getPrimitiveCategory();
        if (primitiveCategory !=
                PrimitiveObjectInspector.PrimitiveCategory.STRING) {
            throw new UDFArgumentTypeException(1,
                    "The first argument must be a string, but "
                            + primitiveCategory.toString() + " was found.");
        }

        category = argOIs[1].getCategory();
        if (category != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(2,
                    "The second argument must be a string, but "
                            + category.toString() + " was found.");
        }
        seedOI = (PrimitiveObjectInspector) argOIs[1];
        primitiveCategory = seedOI.getPrimitiveCategory();
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
                        "The third argument must be a string, but "
                                + category.toString() + " was found.");
            }
            saltOI = (PrimitiveObjectInspector) argOIs[2];
            primitiveCategory = saltOI.getPrimitiveCategory();
            if (primitiveCategory !=
                    PrimitiveObjectInspector.PrimitiveCategory.STRING) {
                throw new UDFArgumentTypeException(3,
                        "The third argument must be a string, but "
                                + primitiveCategory.toString() + " was found.");
            }
        }

        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        assert (arguments.length == 2 || arguments.length == 3);
        Text output = null;

        if (arguments[0].get() != null && arguments[1].get() != null) {
            String jsonString = PrimitiveObjectInspectorUtils.getString(arguments[0].get(), definitionOI);
            Sequence inputSequence;
            try {
                inputSequence = Sequence.fromJSONString(jsonString);
            } catch (JSONException je) {
                throw new HiveException("JSONException while deserializing Sequence.", je.getCause());
            }

            String seed = PrimitiveObjectInspectorUtils.getString(arguments[1].get(), seedOI);
            String salt = null;
            if (arguments.length == 3 && arguments[2].get() != null) {
                salt = PrimitiveObjectInspectorUtils.getString(arguments[2].get(), saltOI);
            }

            String result = inputSequence.generate(seed, salt);
            if (result != null) {
                output = new Text(result);
            }
        }
        
        return output;
    }

    @Override
    public String getDisplayString(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("generate(");
        if (args.length > 0) {
            sb.append(args[0]);
            for (int i = 1; i < args.length; i++) {
                sb.append(", ");
                sb.append(args[i]);
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
