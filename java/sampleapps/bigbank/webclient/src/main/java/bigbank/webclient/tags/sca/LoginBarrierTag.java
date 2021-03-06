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
package bigbank.webclient.tags.sca;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;

import bigbank.webclient.services.profile.ProfileService;

public class LoginBarrierTag extends TagSupport {

    public LoginBarrierTag() {
        super();
    }

    private String mProfile;

    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String pProfile) {
        mProfile = pProfile;
    }

    private String mUrl;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String pUrl) {
        mUrl = pUrl;
    }

    int doPage = EVAL_PAGE;


    public int doStartTag() throws JspException {
        if (mProfile == null || mProfile.length() < 1) {
            throw new JspException("Invalid profile location specified");
        }

        ModuleContext moduleContext = CurrentModuleContext.getContext();

        ProfileService profile = (ProfileService) moduleContext.locateService(mProfile);

        if (profile == null) {
            throw new JspException("Profile [" + mProfile + "] not found in current module context");
        }

        if (profile.isLoggedIn()) {
            return EVAL_BODY_INCLUDE;
        } else {
            try {
                doPage = SKIP_PAGE;
                pageContext.forward(mUrl);
                ((HttpServletResponse) (pageContext.getResponse())).sendRedirect("login.html");
                return SKIP_BODY;
            } catch (ServletException e) {
                throw new JspException("Unable to forward to [" + mUrl + "]");
            } catch (IOException e) {
                throw new JspException("Unable to forward to [" + mUrl + "]");
            }
        }
    }

    public int doEndTag() throws JspException {
        return doPage;
    }

    public void release() {
        super.release();
    }

}
