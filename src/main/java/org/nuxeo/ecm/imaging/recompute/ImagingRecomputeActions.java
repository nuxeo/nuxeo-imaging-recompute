/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thomas Roger
 */

package org.nuxeo.ecm.imaging.recompute;

import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.VIDEO_FACET;
import static org.nuxeo.ecm.platform.thumbnail.ThumbnailConstants.THUMBNAIL_FACET;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
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
    public static final String VIDEO_NXQL_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Video' AND vid:info/format IS NULL";
    public static final String THUMB_NXQL_QUERY = "SELECT * FROM Document WHERE ecm:mixinType = 'Thumbnail' AND thumb:thumbnail/data IS NULL";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    protected String nxqlQuery = DEFAULT_NXQL_QUERY;
    protected String videoQuery = VIDEO_NXQL_QUERY;
    protected String thumbQuery = THUMB_NXQL_QUERY;

    public String getNxqlQuery() {
        return nxqlQuery;
    }

    public void setNxqlQuery(String nxqlQuery) {
        this.nxqlQuery = nxqlQuery;
    }

    public String getVideoQuery() {
        return videoQuery;
    }

    public void setVideoQuery(String nxqlQuery) {
        this.videoQuery = videoQuery;
    }

    public String getThumbQuery() {
        return thumbQuery;
    }

    public void setThumbQuery(String nxqlQuery) {
        this.thumbQuery = thumbQuery;
    }

    public void recomputePictureViews() {
        recomputePictureViews(navigationContext.getCurrentDocument());
    }

    public void recomputePictureViews(DocumentModel doc) {
        if (doc.hasFacet(PICTURE_FACET) || doc.hasFacet(VIDEO_FACET) || doc.hasFacet(THUMBNAIL_FACET)) {
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
