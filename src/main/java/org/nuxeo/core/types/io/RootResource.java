package org.nuxeo.core.types.io;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 *
 */
@WebObject(type = "types")
@Path("/types")
public class RootResource extends ModuleRoot {

    @GET
    public TypesDef index(@QueryParam("allSchemas") Boolean allschemas) {
        if (allschemas==null) {
            allschemas = false;
        }
        SchemaManager sm = Framework.getLocalService(SchemaManager.class);
        return new TypesDef(sm.getSchemas(),sm.getDocumentTypes(), allschemas);
    }

}
