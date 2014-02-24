package com.linkedin.uif.converter;

import com.google.common.collect.Maps;
import com.linkedin.uif.source.workunit.WorkUnit;

import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link Converter} that applies a given list of
 * {@link Converter}s in the given order.
 *
 * @author ynli
 */
public class MultiConverter<SI, DI> implements Converter<SI, Object, DI, Object> {

    // The list of converters to be applied
    private final List<Converter<SI, Object, DI, Object>> converters;
    // Remember the mapping between converter and schema it generates
    private final Map<Converter, Object> schemas = Maps.newHashMap();

    public MultiConverter(List<Converter<SI, Object, DI, Object>> converters) {
        this.converters = converters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertSchema(SI inputSchema, WorkUnit workUnit) {
        Object schema = inputSchema;
        for (Converter converter : this.converters) {
            // Apply the converter and remember the output schema of this converter
            schema = converter.convertSchema(schema, workUnit);
            this.schemas.put(converter, schema);
        }
        return schema;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convertRecord(Object outputSchema, DI inputRecord, WorkUnit workUnit) {
        if (schemas.size() != this.converters.size()) {
            throw new RuntimeException(
                    "convertRecord should be called only after convertSchema is called");
        }

        Object record = inputRecord;
        for (Converter converter : this.converters) {
            // Apply the converter using the output schema of this converter
            record = converter.convertRecord(
                    this.schemas.get(converter), record, workUnit);
        }
        return record;
    }
}