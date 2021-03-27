/*
 * Copyright (c) 2002-2021 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPageTest;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * Unit tests for {@link CookieManager}.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class CookieManager5Test extends WebServerTestCase {

    /**
     * Check the cookie expires gets overwritten.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void updateCookieExpires() throws Exception {
        final Date date = new Date(System.currentTimeMillis() + 500_000);
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put(SetCookieExpires10Servlet.URL, SetCookieExpires10Servlet.class);
        servlets.put(SetCookieExpires1000Servlet.URL, SetCookieExpires1000Servlet.class);
        startWebServer("./", null, servlets);

        final WebClient webClient = getWebClient();
        webClient.getPage("http://localhost:" + PORT + SetCookieExpires10Servlet.URL);
        assertEquals(1, webClient.getCookieManager().getCookies().size());
        Cookie cookie = webClient.getCookieManager().getCookies().iterator().next();
        assertFalse("" + cookie.getExpires(), cookie.getExpires().after(date));

        webClient.getPage("http://localhost:" + PORT + SetCookieExpires1000Servlet.URL);
        assertEquals(1, webClient.getCookieManager().getCookies().size());
        cookie = webClient.getCookieManager().getCookies().iterator().next();
        assertTrue("" + cookie.getExpires(), cookie.getExpires().after(date));
    }

    /**
     * Helper class for {@link #updateCookieExpires}.
     */
    public abstract static class SetCookieExpiresServlet extends HttpServlet {
        private static final String HTML_ = HtmlPageTest.STANDARDS_MODE_PREFIX_
                + "<html><head></head>\n"
                + "<body>\n"
                + "</body></html>";

        protected abstract long getTimeout();

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            resp.setStatus(200);
            final String expires = DateUtils.formatDate(new Date(System.currentTimeMillis() + getTimeout()));
            resp.setHeader("Set-Cookie", "first=1; expires=" + expires + ";");
            resp.getWriter().write(HTML_);
        }
    }

    /**
     * Helper class for {@link #updateCookieExpires}.
     */
    public static class SetCookieExpires10Servlet extends SetCookieExpiresServlet {
        static final String URL = "/test10";

        @Override
        protected long getTimeout() {
            return 10_000L;
        }
    }

    /**
     * Helper class for {@link #updateCookieExpires}.
     */
    public static class SetCookieExpires1000Servlet extends SetCookieExpiresServlet {
        static final String URL = "/test1000";

        @Override
        protected long getTimeout() {
            return 1_000_000L;
        }
    }
}
