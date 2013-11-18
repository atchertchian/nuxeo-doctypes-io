package org.nuxeo.core.types.io;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.types.ComplexType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Schema;

@Provider
@Produces("text/json")
public class TypesDefWriter implements MessageBodyWriter<TypesDef> {

    @Override
    public void writeTo(TypesDef typesDef, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {

            JsonFactory factory = new JsonFactory();

            JsonGenerator jg = factory.createJsonGenerator(entityStream, JsonEncoding.UTF8);
            jg.useDefaultPrettyPrinter();

            // start root
            jg.writeStartObject();

            // write types
            jg.writeObjectFieldStart("doctypes");
            for (DocumentType doctype : typesDef.getDocTypes()) {
                writeDocType(jg, doctype);
            }
            jg.writeEndObject();

            // write schemas
            jg.writeObjectFieldStart("schemas");
            for (Schema schema : typesDef.getSchemas()) {
                writeSchema(jg, schema);
            }
            jg.writeEndObject();


            // end root
            jg.writeEndObject();

            // flush
            jg.flush();
            jg.close();
            entityStream.flush();
        } catch (Exception e) {
            throw new IOException("Failed to return types as JSON", e);
        }
    }

    protected void writeSchema(JsonGenerator jg, Schema schema) throws Exception {
        jg.writeObjectFieldStart(schema.getName());
        jg.writeStringField("@prefix", schema.getNamespace().prefix);
        for (Field field : schema.getFields()) {
            writeField(jg, field);
        }
        jg.writeEndObject();
    }

    protected void writeDocType(JsonGenerator jg, DocumentType docType) throws Exception {

        jg.writeObjectFieldStart(docType.getName());

        if (docType.getSuperType()!=null) {
            jg.writeStringField("parent", docType.getSuperType().getName());
        }

        jg.writeArrayFieldStart("facets");
        for (String facet : docType.getFacets()) {
            jg.writeString(facet);
        }
        jg.writeEndArray();

        jg.writeArrayFieldStart("schemas");
        for (String schema : docType.getSchemaNames()) {
            jg.writeString(schema);
        }
        jg.writeEndArray();
        jg.writeEndObject();
    }

    protected void writeField(JsonGenerator jg, Field field) throws Exception {
        if (!field.getType().isComplexType()) {
            if (field.getType().isListType()) {
                ListType lt = (ListType) field.getType();
                if (lt.getFieldType().isComplexType()) {
                    if (lt.getFieldType().getName().equals("content")) {
                        jg.writeStringField(field.getName().getLocalName(), "blob[]");

                    } else {
                        jg.writeObjectFieldStart(field.getName().getLocalName());
                        buildComplexFields(jg,lt.getField());
                        jg.writeStringField("type", "complex[]");
                        jg.writeEndObject();
                    }
                } else {

                    jg.writeStringField(field.getName().getLocalName(), lt.getFieldType().getName() + "[]");
                }
            } else {
                    jg.writeStringField(field.getName().getLocalName(), field.getType().getName());
            }
        } else {
            if (field.getType().getName().equals("content")) {
                jg.writeStringField(field.getName().getLocalName(), "blob");
            } else {

                jg.writeObjectFieldStart(field.getName().getLocalName());
                buildComplexFields(jg,field);
                jg.writeStringField("type", "complex");
                jg.writeEndObject();
            }
        }

    }

    protected void buildComplexFields(JsonGenerator jg, Field field)
            throws Exception {
        ComplexType cplXType = (ComplexType) field.getType();
        jg.writeObjectFieldStart("fields");
        for (Field subField : cplXType.getFields()) {
            writeField(jg, subField);
        }
        jg.writeEndObject();
    }

    @Override
    public long getSize(TypesDef arg0, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type type, Annotation[] arg2,
            MediaType arg3) {
        return TypesDef.class == arg0;
    }


}
