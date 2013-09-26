package com.rb.server;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that adds a new message to all registered devices.
 * <p>
 * This servlet is used just by the browser (i.e., not device).
 */
@SuppressWarnings("serial")
public class SendAllMessagesServlet extends BaseServlet {

  /**
   * Processes the request to add a new message.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    List<String> devices = Datastore.getDevices();
    String status;
    if (devices.isEmpty()) {
      status = "Message ignored as there is no device registered!";
    } else {
      Queue queue = QueueFactory.getQueue("gcm");
      // NOTE: check below is for demonstration purposes; a real application
      // could always send a multicast, even for just one recipient
      String message = getParameter(req, SendMessageServlet.PARAMETER_MESSAGE, "admin test");
      //String message = new String(encodedMessage.getBytes("UTF-8"), "UTF-8");
      String inRegistrationId = getParameter(req, SendMessageServlet.PARAMETER_INCOME_REGISTRATION_ID, HomeServlet.PARAMETER_ADMIN);
      
      if (devices.size() == 1) {
        // send a single message using plain post
        String device = devices.get(0);
        queue.add(withUrl("/sendMessage").param(
        		SendMessageServlet.PARAMETER_DEVICE, device)
						.param(SendMessageServlet.PARAMETER_MESSAGE, message)
						.param(SendMessageServlet.PARAMETER_INCOME_REGISTRATION_ID,
								inRegistrationId));
        status = "Single message queued for registration id " + device;
      } else {
        // send a multicast message using JSON
        String key = Datastore.createMulticast(devices);
        queue.add(withUrl("/sendMessage").param(
        		SendMessageServlet.PARAMETER_MULTICAST, key)
						.param(SendMessageServlet.PARAMETER_MESSAGE, message)
						.param(SendMessageServlet.PARAMETER_INCOME_REGISTRATION_ID,
								inRegistrationId));
        status = "Multicast message queued for " + devices.size() + " devices";
      }
    }
    req.setAttribute(HomeServlet.ATTRIBUTE_STATUS, status.toString());
    getServletContext().getRequestDispatcher("/home").forward(req, resp);
  }

}
