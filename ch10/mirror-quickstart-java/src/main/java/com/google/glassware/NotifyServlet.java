/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Attachment;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;
import com.google.common.collect.Lists;

/**
 * Handles the notifications sent back from subscriptions
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class NotifyServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(NotifyServlet.class.getSimpleName());

	private static final String[] CAT_UTTERANCES = {
		"<em class='green'>Purr...</em>",
		"<em class='red'>Hisss... scratch...</em>",
		"<em class='yellow'>Meow...</em>"
	};
	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Respond with OK and status 200 in a timely fashion to prevent redelivery
		response.setContentType("text/html");
		Writer writer = response.getWriter();
		writer.append("OK");
		writer.close();

		// Get the notification object from the request body (into a string so we
		// can log it)
		BufferedReader notificationReader =
				new BufferedReader(new InputStreamReader(request.getInputStream()));
		String notificationString = "";

		// Count the lines as a very basic way to prevent Denial of Service attacks
		int lines = 0;
		while (notificationReader.ready()) {
			notificationString += notificationReader.readLine();
			lines++;

			// No notification would ever be this long. Something is very wrong.
			if (lines > 1000) {
				throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
			}
		}

		LOG.info("got raw notification " + notificationString);

		JsonFactory jsonFactory = new JacksonFactory();

		// If logging the payload is not as important, use
		// jacksonFactory.fromInputStream instead.
		Notification notification = jsonFactory.fromString(notificationString, Notification.class);

		LOG.info("Got a notification with ID: " + notification.getItemId());

		// Figure out the impacted user and get their credentials for API calls
		String userId = notification.getUserToken();
		Credential credential = AuthUtil.getCredential(userId);
		Mirror mirrorClient = MirrorClient.getMirror(credential);


		if (notification.getCollection().equals("locations")) {
			LOG.info("Notification of updated location");
			Mirror glass = MirrorClient.getMirror(credential);
			// item id is usually 'latest'
			Location location = glass.locations().get(notification.getItemId()).execute();

			LOG.info("New location is " + location.getLatitude() + ", " + location.getLongitude());
			MirrorClient.insertTimelineItem(
					credential,
					new TimelineItem()
					.setText("Java Quick Start says you are now at " + location.getLatitude()
							+ " by " + location.getLongitude())
							.setNotification(new NotificationConfig().setLevel("DEFAULT")).setLocation(location)
							.setMenuItems(Lists.newArrayList(new MenuItem().setAction("NAVIGATE"))));

			// This is a location notification. Ping the device with a timeline item
			// telling them where they are.
		} else if (notification.getCollection().equals("timeline")) {
			// Get the impacted timeline item
			TimelineItem timelineItem = mirrorClient.timeline().get(notification.getItemId()).execute();
			LOG.info("Notification impacted timeline item with ID: " + timelineItem.getId());

			// If it was a share, and contains a photo, update the photo's caption to
			// acknowledge that we got it.
			if (notification.getUserActions() != null && notification.getUserActions().contains(new UserAction().setType("SHARE"))
					&& timelineItem.getAttachments() != null && timelineItem.getAttachments().size() > 0) {
				LOG.info("It was a share of a photo. Updating the caption on the photo.");
				LOG.info("getAttachments size="+timelineItem.getAttachments().size() + 
						", 1st item url:"+timelineItem.getAttachments().get(0).getContentUrl() + " size:"+
						timelineItem.getAttachments().get(0).getContentType() + ":"+
						timelineItem.getAttachments().get(0).getIsProcessingContent());

				InputStream inputStream = downloadAttachment(mirrorClient, notification.getItemId(), timelineItem.getAttachments().get(0));
				try { 
					URL url = new URL("http://www.morkout.com/iapps/social/glassupload.php?shareapp=mirror");
					String boundary = "*****";
					String lineEnd = "\r\n";
					String twoHyphens = "--";
					int bytesRead, bytesAvailable, bufferSize;
					byte[] buffer;
					int maxBufferSize = 1 * 1024 * 1024;

					// Open a HTTP  connection to  the URL
					HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
					conn.setDoInput(true); // Allow Inputs
					conn.setDoOutput(true); // Allow Outputs
					conn.setUseCaches(false); // Don't use a Cached Copy
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("ENCTYPE", "multipart/form-data");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
					conn.setRequestProperty("Filedata", "jjmirror.jpg"); 

					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

					dos.writeBytes(twoHyphens + boundary + lineEnd); 
					dos.writeBytes("Content-Disposition: form-data; name=Filedata;filename=jjmirror.jpg"+ lineEnd);

					dos.writeBytes(lineEnd);

					// create a buffer of  maximum size
					bytesAvailable = inputStream.available(); 
					LOG.info(">>>>bytesAvailable="+bytesAvailable);

					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					// read file and write it into form...
					bytesRead = inputStream.read(buffer, 0, bufferSize);  

					LOG.info(">>>>bytesRead="+bytesRead);

					while (bytesRead > 0) {

						dos.write(buffer, 0, bufferSize);
						bytesAvailable = inputStream.available();
						LOG.info(">>>> inside while: bytesAvailable="+bytesAvailable);
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = inputStream.read(buffer, 0, bufferSize);   
						LOG.info(">>>> inside while: bytesRead="+bytesRead);

					}

					LOG.info("bytesRead="+bytesRead);

					// send multipart form data necesssary after file data...
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					// Responses from the server (code and message)
					int serverResponseCode = conn.getResponseCode();
					String serverResponseMessage = conn.getResponseMessage();

					LOG.info("uploadFile HTTP Response is : "
							+ serverResponseMessage + ": " + serverResponseCode);

					if(serverResponseCode == 200){
						LOG.info("File Upload Completed.");
					}

					InputStream is = conn.getInputStream();
					int ch;

					StringBuffer b = new StringBuffer();
					while ((ch = is.read()) != -1) {
						b.append((char) ch);
					}

					LOG.info("uploaded file at http://www.morkout.com/iapps/social/uploads/" + b.toString());     			
					is.close();

					dos.flush();
					dos.close();

				} catch (Exception e) {
					LOG.warning(e.getMessage());
				}


				String caption = timelineItem.getText();
				if (caption == null) {
					caption = "";
				}
				LOG.info("caption="+caption);

				// Create a new item with just the values that we want to patch.
				TimelineItem itemPatch = new TimelineItem();
				itemPatch.setText("J.J. Mirror got your photo! " + caption);

				// Patch the item. Notice that since we retrieved the entire item above
				// in order to access the caption, we could have just changed the text
				// in place and used the update method, but we wanted to illustrate the
				// patch method here.
				mirrorClient.timeline().patch(notification.getItemId(), itemPatch).execute();
			} else if (notification.getUserActions().contains(new UserAction().setType("LAUNCH"))) {
				LOG.info("It was a note taken with the 'take a note' or 'post an update' voice command. Processing it.");

				// Grab the spoken text from the timeline card and update the card with
				// an HTML response (deleting the text as well).
				String noteText = timelineItem.getText();
				String utterance = CAT_UTTERANCES[new Random().nextInt(CAT_UTTERANCES.length)];

				timelineItem.setText(null);
				timelineItem.setHtml(makeHtmlForCard("<p class='text-auto-size'>"
						+ "Oh, did you say " + noteText + "? " + utterance + "</p>"));
				timelineItem.setMenuItems(Lists.newArrayList(
						new MenuItem().setAction("DELETE")));

				mirrorClient.timeline().update(timelineItem.getId(), timelineItem).execute();
				
				
				// search for NBA roster info
				for (String[] team : MainServlet.teams) {
					//LOG.info("team[2]="+team[2].toLowerCase() +", noteText="+noteText.toLowerCase());
					if (team[2].toLowerCase().equals(noteText.toLowerCase())) {
						String html = "<article><section><div class='layout-figure'><div class='align-center yellow'><p class='text-auto-size'>" + team[2] + "</p><p class='green'>Age: " + team[4] + "</p></div><div class='text-normal'>";
						html += "<p>No. " + team[1] + ", " + team[3] + "</p>";
						html += "<p>" + team[5] + ", " + team[6] + "lb</p>";
						html += "<p>" + team[7] + "</p>";
						html += "<p class='red'>" + team[8] + "</p>";
						html += "</div></div></section></article>";

						LOG.info("taka a note: html="+html);
						TimelineItem item = new TimelineItem();
						item.setHtml(html);
						item.setSpeakableText(team[2] + ", Age " + team[4] + ", " + team[5] + ", " + team[6] + ", " + team[7] + ", Salary: " + team[8]);
						List<MenuItem> menuItemList = new ArrayList<MenuItem>();
						menuItemList.add(new MenuItem().setAction("SHARE"));
						menuItemList.add(new MenuItem().setAction("READ_ALOUD"));
						menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
						
						// hybrid way - doesn't show?
//						List<MenuValue> menuValues = new ArrayList<MenuValue>();			
//						menuValues.add(new MenuValue().setDisplayName("HelloGlass"));
//						menuItemList.add(new MenuItem().setValues(menuValues).setAction("OPEN_URI").setPayload("helloglass"));						
//						
//						// shows
//						menuValues = new ArrayList<MenuValue>();
//						menuValues.add(new MenuValue().setDisplayName("Hello World"));
//						menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));
//						
//						// shows
//						menuItemList.add(new MenuItem().setAction("OPEN_URI").setPayload(
//								"http://www.morkout.com"));
						
						
						item.setMenuItems(menuItemList);								
						
						MirrorClient.insertTimelineItem(credential, item);
						break;
					}
				}
				
			} else if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("drill"))) {
				TimelineItem item = new TimelineItem();
				item.setTitle("Drill In Requested"); // doesn't work
				item.setHtml("<article><section><p>First page</p></section></article><article><section><p>Second page</p></section></article><article><section><p>Third page</p></section></article>");
				MirrorClient.insertTimelineItem(credential, item);
			} else {
				boolean notificationProcessed = false;
				// get all mascots
				Set<String> mascots = new LinkedHashSet<String>();
				for (String[] team : MainServlet.teams) {
					String words[] = team[0].split(" ");
					String mascot = words[words.length - 1].toLowerCase();
					mascots.add(mascot);								
				}
				for (String mascot : mascots) {
					if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload(mascot))) {
						notificationProcessed = true;
						TimelineItem item = new TimelineItem();

						for (String[] team : MainServlet.teams) {
							String words[] = team[0].split(" ");
							if (mascot.equals(words[words.length - 1].toLowerCase())) {
								String html = "<article><section><div class='layout-figure'><div class='align-center yellow'><p class='text-auto-size'>" + team[2] + "</p><p class='green'>Age: " + team[4] + "</p></div><div class='text-normal'>";
								html += "<p>No. " + team[1] + ", " + team[3] + "</p>";
								html += "<p>" + team[5] + ", " + team[6] + "lb</p>";
								html += "<p>" + team[7] + "</p>";
								html += "<p class='red'>" + team[8] + "</p>";
								html += "</div></div></section></article>";

								LOG.info("html="+html);
								item.setHtml(html);
								item.setBundleId(mascot);
								item.setSpeakableText(team[2] + ", Age " + team[4] + ", " + team[5] + ", " + team[6] + ", " + team[7] + ", Salary: " + team[8]);
								List<MenuItem> menuItemList = new ArrayList<MenuItem>();
								menuItemList.add(new MenuItem().setAction("SHARE"));
								menuItemList.add(new MenuItem().setAction("READ_ALOUD"));
								menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
								item.setMenuItems(menuItemList);								
								
								MirrorClient.insertTimelineItem(credential, item);
							}
						}
						
						break;
					}
				}
				if (!notificationProcessed) {
					LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
				}
			}
		}
	}

	/**
	 * Wraps some HTML content in article/section tags and adds a footer
	 * identifying the card as originating from the Java Quick Start.
	 *
	 * @param content the HTML content to wrap
	 * @return the wrapped HTML content
	 */
	private static String makeHtmlForCard(String content) {
		return "<article class='auto-paginate'>" + content
				+ "<footer><p>Java Quick Start</p></footer></article>";
	}

	/**
	 * Download a timeline items's attachment.
	 * 
	 * @param service Authorized Mirror service.
	 * @param itemId ID of the timeline item to download the attachment for.
	 * @param attachment Attachment to download content for.
	 * @return The attachment content on success, {@code null} otherwise.
	 */
	public static InputStream downloadAttachment(Mirror service, String itemId, Attachment attachment) {
		try {
			HttpResponse resp =
					service.getRequestFactory().buildGetRequest(new GenericUrl(attachment.getContentUrl()))
					.execute();
			return resp.getContent();
		} catch (IOException e) {
			// An error occurred.
			LOG.warning(e.getMessage());
			return null;
		}
	}

}
