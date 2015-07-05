/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger
 */

package org.nuxeo.ecm.imaging.recompute;

import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_FACET;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

@Name("imagingRecomputeActions")
@Scope(ScopeType.CONVERSATION)
public class ImagingRecomputeActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_NXQL_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Picture' AND picture:views/*/title IS NULL";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    protected String nxqlQuery = DEFAULT_NXQL_QUERY;

    public String getNxqlQuery() {
        return nxqlQuery;
    }

    public void setNxqlQuery(String nxqlQuery) {
        this.nxqlQuery = nxqlQuery;
    }

    public void recomputePictureViews() {
        recomputePictureViews(navigationContext.getCurrentDocument());
    }

    public void recomputePictureViews(DocumentModel doc) {
        if (doc.hasFacet(PICTURE_FACET)) {
            BlobHolder blobHolder = doc.getAdapter(BlobHolder.class);
            if (blobHolder.getBlob() != null) {
                blobHolder.setBlob(blobHolder.getBlob());
                Events.instance().raiseEvent(EventNames.BEFORE_DOCUMENT_CHANGED, doc);
                documentManager.saveDocument(doc);
                documentManager.save();
                navigationContext.invalidateCurrentDocument();
            }
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "label.imaging.recompute.views.done");
        }
    }

    public void launchPictureViewsRecomputation() {
        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        if (workManager == null) {
            throw new RuntimeException("No WorkManager available");
        }

        if (!StringUtils.isBlank(nxqlQuery)) {
            ImagingRecomputeWork work = new ImagingRecomputeWork(documentManager.getRepositoryName(), nxqlQuery);
            workManager.schedule(work, WorkManager.Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);

            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "label.imaging.recompute.work.launched");
        }

    }
}
