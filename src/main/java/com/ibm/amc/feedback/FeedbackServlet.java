/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.ibm.amc.feedback;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.amc.Constants;
import com.ibm.amc.ras.Logger47;

/**
 * Servlet implementation class FeedbackServlet
 */
@WebServlet(name = "FeedbackServlet", urlPatterns = { "/feedback" }, asyncSupported = true)
public class FeedbackServlet extends HttpServlet
{
	// @CLASS-COPYRIGHT@

	static Logger47 logger = Logger47.get(FeedbackServlet.class.getCanonicalName(), Constants.CWZBA_BUNDLE_NAME);

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final String method = "doGet";
		logger.entry(method, request, response);

		ServletContext servletContext = request.getServletContext();

		if (request.getUserPrincipal() == null)
		{
			response.setStatus(401);
			return;
		}

		FeedbackHandler handler = (FeedbackHandler) servletContext.getAttribute("feedbackHandler");
		handler.handleRequest(request, response);

		logger.exit(method);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGet(req, resp);
	}

}
