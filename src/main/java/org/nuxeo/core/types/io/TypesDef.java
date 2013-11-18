package org.nuxeo.core.types.io;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.types.Schema;

public class TypesDef {

    protected final Schema[] schemas;

    protected final DocumentType[] docTypes;

    protected boolean allSchemas;

    public TypesDef(Schema[] schemas, DocumentType[] docTypes, boolean allSchemas) {
        this.schemas = schemas;
        this.docTypes = docTypes;
        this.allSchemas=allSchemas;
    }

    public List<Schema> getUsedSchemas() {
        List<Schema> result = new ArrayList<Schema>();
        for (DocumentType type : docTypes) {
            for (Schema schema : type.getSchemas()) {
                if (!result.contains(schema)) {
                    result.add(schema);
                }
            }
        }
        return result;
    }

    public Schema[] getSchemas() {
        if (allSchemas) {
            return schemas;
        } else {
            List<Schema> schemas = getUsedSchemas();
            return schemas.toArray(new Schema[schemas.size()]);
        }
    }

    public DocumentType[] getDocTypes() {
        return docTypes;
    }

}
