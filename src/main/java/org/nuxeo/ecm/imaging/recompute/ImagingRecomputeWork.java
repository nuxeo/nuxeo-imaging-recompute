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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class ImagingRecomputeWork extends AbstractWork {

    protected final static int BATCH_SIZE = 10;

    protected String repositoryName;

    protected String nxqlQuery;

    public ImagingRecomputeWork(String repositoryName, String nxqlQuery) {
        this.repositoryName = repositoryName;
        this.nxqlQuery = nxqlQuery;
    }

    @Override
    public String getTitle() {
        return "Picture Views Recomputation";
    }

    @Override
    public void work() {
        setProgress(Progress.PROGRESS_INDETERMINATE);

        initSession();
        DocumentModelList docs = session.query(nxqlQuery);
        long docsUpdated = 0;

        setStatus("Generating views");
        for (DocumentModel doc : docs) {
            if (doc.hasFacet(PICTURE_FACET)) {
                BlobHolder blobHolder = doc.getAdapter(BlobHolder.class);
                if (blobHolder.getBlob() != null) {
                    blobHolder.setBlob(blobHolder.getBlob());
                    session.saveDocument(doc);
                    docsUpdated++;
                    if (docsUpdated % BATCH_SIZE == 0) {
                        commitOrRollbackTransaction();
                        startTransaction();
                    }
                }
            }
        }
        setStatus("Done");
    }

}
