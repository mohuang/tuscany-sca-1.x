/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package bigbank.webclient.tags.account;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class StockLogTag extends TagSupport {
    
    public StockLogTag() {
        super();
    }

    private String mId;

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    private Iterator mIterator;

    public int doStartTag() throws JspException {
         
        List entries = (List) pageContext.getAttribute("StockLogEntries");
        if(null == entries) return SKIP_BODY;
        mIterator = entries.iterator();
        if (mIterator.hasNext()) {
            pageContext.setAttribute(mId, mIterator.next());
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    public int doAfterBody() {
        if (mIterator.hasNext()) {
            pageContext.setAttribute(mId, mIterator.next());
            return EVAL_BODY_AGAIN;
        } else {
            pageContext.setAttribute("StockLogEntries", null);
            return SKIP_BODY;
        }
    }

    public void release() {
        pageContext.setAttribute("StockLogEntries", null);
        super.release();
        mId = null;
        mIterator = null;
    }
}
