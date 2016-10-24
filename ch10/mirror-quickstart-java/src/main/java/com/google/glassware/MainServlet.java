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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.mirror.model.Command;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.TimelineListResponse;
import com.google.common.collect.Lists;

/**
 * Handles POST requests from index.jsp
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class MainServlet extends HttpServlet {

	/**
	 * Private class to process batch request results.
	 * <p/>
	 * For more information, see
	 * https://code.google.com/p/google-api-java-client/wiki/Batch.
	 */
	private final class BatchCallback extends JsonBatchCallback<TimelineItem> {
		private int success = 0;
		private int failure = 0;

		@Override
		public void onSuccess(TimelineItem item, HttpHeaders headers) throws IOException {
			++success;
		}

		@Override
		public void onFailure(GoogleJsonError error, HttpHeaders headers) throws IOException {
			++failure;
			LOG.info("Failed to insert item: " + error.getMessage());
		}
	}

	private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());
	public static final String CONTACT_ID = "com.google.glassware.contact.java-quick-start";
	public static final String CONTACT_NAME = "J.J. Mirror";

	private static final String PAGINATED_HTML =
			"<article class='auto-paginate'>"
					+ "<h2 class='blue text-large'>Did you know...?</h2>"
					+ "<p>Cats are <em class='yellow'>solar-powered.</em> The time they spend napping in "
					+ "direct sunlight is necessary to regenerate their internal batteries. Cats that do not "
					+ "receive sufficient charge may exhibit the following symptoms: lethargy, "
					+ "irritability, and disdainful glares. Cats will reactivate on their own automatically "
					+ "after a complete charge cycle; it is recommended that they be left undisturbed during "
					+ "this process to maximize your enjoyment of your cat.</p><br/><p>"
					+ "For more cat maintenance tips, tap to view the website!</p>"
					+ "</article>";

	public static final String[][] teams =  {
		{"Dallas Mavericks", "45", "DeJuan Blair", "C", "25", "6-7", "270", "Pittsburgh", "$884,293"},
		{"Dallas Mavericks", "8", "Jose Calderon", "PG", "32", "6-3", "211", "", "$6,791,570"},
		{"Dallas Mavericks", "25", "Vince Carter", "SG", "37", "6-6", "220", "North Carolina", "$3,180,000"},
		{"Dallas Mavericks", "9", "Jae Crowder", "SF", "23", "6-6", "235", "Marquette", "$788,872"},
		{"Dallas Mavericks", "1", "Samuel Dalembert", "C", "33", "6-11", "250", "Seton Hall", "$3,700,748"},
		{"Dallas Mavericks", "21", "Wayne Ellington", "SG", "26", "6-4", "200", "North Carolina", "$2,652,000"},
		{"Dallas Mavericks", "11", "Monta Ellis", "SG", "28", "6-3", "185", "", "$8,000,000"},
		{"Dallas Mavericks", "20", "Devin Harris", "PG", "31", "6-3", "192", "Wisconsin", "$884,293"},
		{"Dallas Mavericks", "5", "Bernard James", "C", "29", "6-10", "240", "Florida State", "$788,872"},
		{"Dallas Mavericks", "3", "Shane Larkin", "PG", "21", "5-11", "176", "Miami (FL)", "$1,536,960"},
		{"Dallas Mavericks", "7", "Ricky Ledo", "SG", "21", "6-7", "195", "Providence", "$544,000"},
		{"Dallas Mavericks", "0", "Shawn Marion", "SF", "36", "6-7", "228", "UNLV", "$9,316,796"},
		{"Dallas Mavericks", "33", "Gal Mekel", "PG", "26", "6-3", "191", "Wichita State", "$490,180"},
		{"Dallas Mavericks", "41", "Dirk Nowitzki", "PF", "35", "7-0", "245", "", "$22,721,381"},
		{"Dallas Mavericks", "34", "Brandan Wright", "PF", "26", "6-10", "210", "North Carolina", "$5,000,000"},
		{"Golden State Warriors", "57", "Hilton Armstrong", "PF", "29", "6-11", "235", "Connecticut", "$660,619"},
		{"Golden State Warriors", "40", "Harrison Barnes", "SF", "21", "6-8", "210", "North Carolina", "$2,923,920"},
		{"Golden State Warriors", "25", "Steve Blake", "PG", "34", "6-3", "172", "Maryland", "$4,000,000"},
		{"Golden State Warriors", "12", "Andrew Bogut", "C", "29", "7-0", "260", "Utah", "$14,000,000"},
		{"Golden State Warriors", "55", "Jordan Crawford", "SG", "25", "6-4", "195", "Xavier", "$2,162,419"},
		{"Golden State Warriors", "30", "Stephen Curry", "PG", "26", "6-3", "185", "Davidson", "$9,887,642"},
		{"Golden State Warriors", "31", "Festus Ezeli", "C", "24", "6-11", "255", "Vanderbilt", "$1,066,920"},
		{"Golden State Warriors", "23", "Draymond Green", "SF", "24", "6-7", "230", "Michigan State", "$875,500"},
		{"Golden State Warriors", "9", "Andre Iguodala", "SF", "30", "6-6", "207", "Arizona", "$12,868,632"},
		{"Golden State Warriors", "1", "Ognjen Kuzmic", "C", "23", "7-0", "251", "", "$490,180"},
		{"Golden State Warriors", "10", "David Lee", "PF", "31", "6-9", "240", "Florida", "$13,878,000"},
		{"Golden State Warriors", "8", "Nemanja Nedovic", "PG", "22", "6-3", "192", "", "$1,056,720"},
		{"Golden State Warriors", "7", "Jermaine O'Neal", "C", "35", "6-11", "255", "", "$2,000,000"},
		{"Golden State Warriors", "5", "Marreese Speights", "PF", "26", "6-10", "255", "Florida", "$3,500,000"},
		{"Golden State Warriors", "11", "Klay Thompson", "SG", "24", "6-7", "205", "Washington State", "$2,317,920"},
		{"Houston Rockets", "3", "Omer Asik", "C", "27", "7-0", "255", "", "$8,374,646"},
		{"Houston Rockets", "2", "Patrick Beverley", "PG", "25", "6-1", "185", "Arkansas", "$788,872"},
		{"Houston Rockets", "1", "Isaiah Canaan", "PG", "22", "6-0", "188", "Murray State", "$570,515"},
		{"Houston Rockets", "18", "Omri Casspi", "SF", "25", "6-9", "225", "", "$947,907"},
		{"Houston Rockets", "33", "Robert Covington", "SF", "23", "6-9", "215", "Tennessee State", "$490,180"},
		{"Houston Rockets", "30", "Troy Daniels", "SG", "22", "6-4", "204", "Virginia Commonwealth", "$158,588"},
		{"Houston Rockets", "32", "Francisco Garcia", "SG", "33", "6-7", "195", "Louisville", "$1,265,977"},
		{"Houston Rockets", "5", "Jordan Hamilton", "SF", "23", "6-7", "220", "Texas", "$1,169,880"},
		{"Houston Rockets", "13", "James Harden", "SG", "24", "6-5", "220", "Arizona State", "$13,701,250"},
		{"Houston Rockets", "12", "Dwight Howard", "C", "28", "6-11", "265", "", "$20,513,178"},
		{"Houston Rockets", "6", "Terrence Jones", "PF", "22", "6-9", "252", "Kentucky", "$1,551,840"},
		{"Houston Rockets", "7", "Jeremy Lin", "PG", "25", "6-3", "200", "Harvard", "$8,374,646"},
		{"Houston Rockets", "20", "Donatas Motiejunas", "PF", "23", "7-0", "222", "", "$1,422,720"},
		{"Houston Rockets", "25", "Chandler Parsons", "SF", "25", "6-9", "227", "Florida", "$926,500"},
		{"Houston Rockets", "21", "Josh Powell", "PF", "31", "6-9", "240", "North Carolina State", "$854,389"},
		{"Los Angeles Clippers", "22", "Matt Barnes", "SF", "34", "6-7", "226", "UCLA", "$3,250,000"},
		{"Los Angeles Clippers", "25", "Reggie Bullock", "SG", "23", "6-7", "205", "North Carolina", "$1,149,000"},
		{"Los Angeles Clippers", "2", "Darren Collison", "PG", "26", "6-0", "175", "UCLA", "$1,900,000"},
		{"Los Angeles Clippers", "11", "Jamal Crawford", "SG", "34", "6-5", "200", "Michigan", "$5,225,000"},
		{"Los Angeles Clippers", "0", "Glen Davis", "PF", "28", "6-9", "289", "LSU", "$338,594"},
		{"Los Angeles Clippers", "9", "Jared Dudley", "SF", "28", "6-7", "225", "Boston College", "$4,250,000"},
		{"Los Angeles Clippers", "33", "Danny Granger", "SF", "31", "6-9", "228", "New Mexico", "$249,683"},
		{"Los Angeles Clippers", "34", "Willie Green", "SG", "32", "6-3", "201", "Detroit", "$1,399,507"},
		{"Los Angeles Clippers", "32", "Blake Griffin", "PF", "25", "6-10", "251", "Oklahoma", "$16,441,500"},
		{"Los Angeles Clippers", "15", "Ryan Hollins", "C", "29", "7-0", "240", "UCLA", "$884,293"},
		{"Los Angeles Clippers", "6", "DeAndre Jordan", "C", "25", "6-11", "265", "Texas A&M", "$10,986,550"},
		{"Los Angeles Clippers", "3", "Chris Paul", "PG", "29", "6-0", "175", "Wake Forest", "$18,668,431"},
		{"Los Angeles Clippers", "4", "J.J. Redick", "SG", "29", "6-4", "190", "Duke", "$6,500,000"},
		{"Los Angeles Clippers", "8", "Hedo Turkoglu", "SF", "35", "6-10", "220", "", "$473,357"},
		{"Memphis Grizzlies", "9", "Tony Allen", "SG", "32", "6-4", "213", "Oklahoma State", "$4,494,383"},
		{"Memphis Grizzlies", "12", "Nick Calathes", "SG", "25", "6-6", "213", "Florida", "$490,180"},
		{"Memphis Grizzlies", "11", "Mike Conley", "PG", "26", "6-1", "185", "Ohio State", "$8,600,001"},
		{"Memphis Grizzlies", "32", "Ed Davis", "PF", "24", "6-10", "225", "North Carolina", "$3,153,860"},
		{"Memphis Grizzlies", "22", "Jamaal Franklin", "SG", "22", "6-5", "191", "San Diego State", "$535,000"},
		{"Memphis Grizzlies", "33", "Marc Gasol", "C", "29", "7-1", "265", "", "$14,860,523"},
		{"Memphis Grizzlies", "3", "James Johnson", "PF", "27", "6-9", "248", "Wake Forest", "$634,610"},
		{"Memphis Grizzlies", "41", "Kosta Koufos", "C", "25", "7-0", "265", "Ohio State", "$3,000,000"},
		{"Memphis Grizzlies", "5", "Courtney Lee", "SG", "28", "6-5", "200", "Western Kentucky", "$5,225,000"},
		{"Memphis Grizzlies", "30", "Jon Leuer", "PF", "24", "6-10", "228", "Wisconsin", "$900,000"},
		{"Memphis Grizzlies", "13", "Mike Miller", "SF", "34", "6-8", "218", "Florida", "$884,293"},
		{"Memphis Grizzlies", "20", "Quincy Pondexter", "SF", "26", "6-6", "225", "Washington", "$2,225,479"},
		{"Memphis Grizzlies", "21", "Tayshaun Prince", "SF", "34", "6-9", "215", "Kentucky", "$7,235,955"},
		{"Memphis Grizzlies", "50", "Zach Randolph", "PF", "32", "6-9", "260", "Michigan State", "$18,238,333"},
		{"Memphis Grizzlies", "19", "Beno Udrih", "PG", "31", "6-3", "210", "", "$884,293"},
		{"Oklahoma City Thunder", "12", "Steven Adams", "C", "20", "7-0", "255", "Pittsburgh", "$2,090,880"},
		{"Oklahoma City Thunder", "2", "Caron Butler", "SF", "34", "6-7", "228", "Connecticut", "$244,489"},
		{"Oklahoma City Thunder", "4", "Nick Collison", "PF", "33", "6-10", "255", "Kansas", "$2,585,668"},
		{"Oklahoma City Thunder", "35", "Kevin Durant", "SF", "25", "6-9", "240", "Texas", "$17,832,627"},
		{"Oklahoma City Thunder", "6", "Derek Fisher", "PG", "39", "6-1", "210", "Arkansas-Little Rock", "$884,293"},
		{"Oklahoma City Thunder", "9", "Serge Ibaka", "PF", "24", "6-10", "245", "", "$12,350,000"},
		{"Oklahoma City Thunder", "15", "Reggie Jackson", "PG", "24", "6-3", "208", "Boston College", "$1,260,360"},
		{"Oklahoma City Thunder", "7", "Grant Jerrett", "PF", "20", "6-10", "232", "Arizona"},
		{"Oklahoma City Thunder", "3", "Perry Jones", "SF", "22", "6-11", "235", "Baylor", "$1,082,520"},
		{"Oklahoma City Thunder", "11", "Jeremy Lamb", "SG", "21", "6-5", "185", "Connecticut", "$2,111,160"},
		{"Oklahoma City Thunder", "5", "Kendrick Perkins", "C", "29", "6-10", "270", "", "$8,727,437"},
		{"Oklahoma City Thunder", "21", "Andre Roberson", "SG", "22", "6-7", "210", "Colorado", "$740,560"},
		{"Oklahoma City Thunder", "25", "Thabo Sefolosha", "SG", "30", "6-7", "222", "", "$3,900,000"},
		{"Oklahoma City Thunder", "34", "Hasheem Thabeet", "C", "27", "7-3", "263", "Connecticut", "$1,200,000"},
		{"Oklahoma City Thunder", "0", "Russell Westbrook", "PG", "25", "6-3", "200", "UCLA", "$14,693,906"},
		{"Portland Trail Blazers", "12", "LaMarcus Aldridge", "PF", "28", "6-11", "240", "Texas", "$14,878,000"},
		{"Portland Trail Blazers", "5", "Will Barton", "SG", "23", "6-6", "175", "Memphis", "$788,872"},
		{"Portland Trail Blazers", "88", "Nicolas Batum", "SF", "25", "6-8", "200", "", "$11,295,250"},
		{"Portland Trail Blazers", "18", "Victor Claver", "PF", "25", "6-9", "224", "", "$1,330,000"},
		{"Portland Trail Blazers", "23", "Allen Crabbe", "SG", "22", "6-6", "210", "California", "$825,000"},
		{"Portland Trail Blazers", "19", "Joel Freeland", "C", "27", "6-10", "225", "", "$2,897,976"},
		{"Portland Trail Blazers", "11", "Meyers Leonard", "C", "22", "7-1", "245", "Illinois", "$2,222,160"},
		{"Portland Trail Blazers", "0", "Damian Lillard", "PG", "23", "6-3", "195", "Weber State", "$3,202,920"},
		{"Portland Trail Blazers", "42", "Robin Lopez", "C", "26", "7-0", "255", "Stanford", "$5,904,261"},
		{"Portland Trail Blazers", "2", "Wesley Matthews", "SG", "27", "6-5", "220", "Marquette", "$6,875,480"},
		{"Portland Trail Blazers", "3", "C.J. McCollum", "SG", "22", "6-4", "200", "Lehigh", "$2,316,720"},
		{"Portland Trail Blazers", "41", "Thomas Robinson", "PF", "23", "6-10", "237", "Kansas", "$3,526,440"},
		{"Portland Trail Blazers", "17", "Earl Watson", "PG", "34", "6-1", "199", "UCLA", "$884,293"},
		{"Portland Trail Blazers", "25", "Mo Williams", "PG", "31", "6-1", "195", "Alabama", "$2,652,000"},
		{"Portland Trail Blazers", "1", "Dorell Wright", "SF", "28", "6-9", "205", "", "$3,000,000"},
		{"San Antonio Spurs", "11", "Jeff Ayres", "C", "27", "6-9", "250", "Arizona State", "$1,750,000"},
		{"San Antonio Spurs", "16", "Aron Baynes", "PF", "27", "6-10", "260", "Washington State", "$788,872"},
		{"San Antonio Spurs", "3", "Marco Belinelli", "SG", "28", "6-5", "210", "", "$2,750,000"},
		{"San Antonio Spurs", "15", "Matt Bonner", "PF", "34", "6-10", "235", "Florida", "$3,945,000"},
		{"San Antonio Spurs", "23", "Austin Daye", "SF", "25", "6-11", "200", "Gonzaga", "$947,907"},
		{"San Antonio Spurs", "33", "Boris Diaw", "PF", "32", "6-8", "250", "", "$4,702,500"},
		{"San Antonio Spurs", "21", "Tim Duncan", "PF", "38", "6-11", "250", "Wake Forest", "$10,361,446"},
		{"San Antonio Spurs", "20", "Manu Ginobili", "SG", "36", "6-6", "205", "", "$7,500,000"},
		{"San Antonio Spurs", "4", "Danny Green", "SG", "26", "6-6", "215", "North Carolina", "$3,762,500"},
		{"San Antonio Spurs", "7", "Damion James", "SF", "26", "6-7", "225", "Texas", "$68,902"},
		{"San Antonio Spurs", "5", "Cory Joseph", "PG", "22", "6-3", "190", "Texas", "$1,120,920"},
		{"San Antonio Spurs", "2", "Kawhi Leonard", "SF", "22", "6-7", "230", "San Diego State", "$1,887,840"},
		{"San Antonio Spurs", "8", "Patty Mills", "PG", "25", "6-0", "185", "Saint Mary's", "$1,133,950"},
		{"San Antonio Spurs", "9", "Tony Parker", "PG", "31", "6-2", "185", "", "$12,500,000"},
		{"San Antonio Spurs", "22", "Tiago Splitter", "C", "29", "6-11", "245", "", "$10,000,000"},

		{"Atlanta Hawks", "6", "Pero Antic", "C", "31", "6-11", "260", "", "$1,200,000"},
		{"Atlanta Hawks", "14", "Gustavo Ayon", "PF", "29", "6-10", "250", "", "$1,500,000"},
		{"Atlanta Hawks", "42", "Elton Brand", "PF", "35", "6-9", "254", "Duke", "$4,000,000"},
		{"Atlanta Hawks", "5", "DeMarre Carroll", "SF", "27", "6-8", "212", "Missouri", "$2,557,545"},
		{"Atlanta Hawks", "15", "Al Horford", "C", "27", "6-10", "250", "Florida", "$12,000,000"},
		{"Atlanta Hawks", "12", "John Jenkins", "SG", "23", "6-4", "215", "Vanderbilt", "$1,258,800"},
		{"Atlanta Hawks", "26", "Kyle Korver", "SG", "33", "6-7", "212", "Creighton", "$6,760,563"},
		{"Atlanta Hawks", "8", "Shelvin Mack", "PG", "24", "6-3", "207", "Butler", "$884,293"},
		{"Atlanta Hawks", "20", "Cartier Martin", "SF", "29", "6-7", "220", "Kansas State", "$104,034"},
		{"Atlanta Hawks", "4", "Paul Millsap", "PF", "29", "6-8", "253", "Louisiana Tech", "$9,500,000"},
		{"Atlanta Hawks", "31", "Mike Muscala", "C", "22", "6-11", "239", "Bucknell", "$138,404"},
		{"Atlanta Hawks", "17", "Dennis Schroder", "PG", "20", "6-1", "168", "", "$1,348,200"},
		{"Atlanta Hawks", "32", "Mike Scott", "PF", "25", "6-8", "237", "Virginia", "$788,872"},
		{"Atlanta Hawks", "0", "Jeff Teague", "PG", "25", "6-2", "181", "Wake Forest", "$8,000,000"},
		{"Atlanta Hawks", "3", "Louis Williams", "SG", "27", "6-1", "175", "", "$5,225,000"},	
		{"Brooklyn Nets", "6", "Alan Anderson", "SF", "31", "6-6", "220", "Michigan State", "$947,907"},
		{"Brooklyn Nets", "0", "Andray Blatche", "C", "27", "6-11", "260", "", "$1,375,604"},
		{"Brooklyn Nets", "98", "Jason Collins", "C", "35", "7-0", "255", "Stanford", "$275,691"},
		{"Brooklyn Nets", "2", "Kevin Garnett", "C", "37", "6-11", "253", "", "$12,433,735"},
		{"Brooklyn Nets", "13", "Jorge Gutierrez", "PG", "25", "6-3", "195", "California", "$54,785"},
		{"Brooklyn Nets", "7", "Joe Johnson", "SG", "32", "6-7", "240", "Arkansas", "$21,466,718"},
		{"Brooklyn Nets", "47", "Andrei Kirilenko", "SF", "33", "6-9", "235", "", "$3,183,000"},
		{"Brooklyn Nets", "14", "Shaun Livingston", "PG", "28", "6-7", "175", "", "$884,293"},
		{"Brooklyn Nets", "11", "Brook Lopez", "C", "26", "7-0", "275", "Stanford", "$14,693,906"},
		{"Brooklyn Nets", "34", "Paul Pierce", "SF", "36", "6-7", "235", "Kansas", "$15,333,334"},
		{"Brooklyn Nets", "1", "Mason Plumlee", "PF", "24", "6-11", "235", "Duke", "$1,298,640"},
		{"Brooklyn Nets", "12", "Marquis Teague", "PG", "21", "6-2", "190", "Kentucky", "$1,074,720"},
		{"Brooklyn Nets", "33", "Mirza Teletovic", "PF", "28", "6-9", "242", "", "$3,229,050"},
		{"Brooklyn Nets", "10", "Marcus Thornton", "SG", "26", "6-4", "205", "LSU", "$8,050,000"},
		{"Brooklyn Nets", "8", "Deron Williams", "PG", "29", "6-3", "209", "Illinois", "$18,466,130"},
		{"Charlotte Bobcats", "0", "Bismack Biyombo", "C", "21", "6-9", "245", "", "$3,049,920"},
		{"Charlotte Bobcats", "55", "Chris Douglas-Roberts", "SG", "27", "6-7", "210", "Memphis", "$660,619"},
		{"Charlotte Bobcats", "33", "Brendan Haywood", "C", "34", "7-0", "263", "North Carolina", "$2,050,000"},
		{"Charlotte Bobcats", "9", "Gerald Henderson", "SG", "26", "6-5", "215", "Duke", "$6,000,000"},
		{"Charlotte Bobcats", "25", "Al Jefferson", "C", "29", "6-10", "289", "", "$13,500,000"},
		{"Charlotte Bobcats", "14", "Michael Kidd-Gilchrist", "SF", "20", "6-7", "232", "Kentucky", "$4,809,840"},
		{"Charlotte Bobcats", "11", "Josh McRoberts", "PF", "27", "6-10", "240", "Duke", "$2,652,000"},
		{"Charlotte Bobcats", "12", "Gary Neal", "SG", "29", "6-4", "210", "Towson", "$3,250,000"},
		{"Charlotte Bobcats", "5", "Jannero Pargo", "PG", "34", "6-1", "185", "Arkansas", "$884,293"},
		{"Charlotte Bobcats", "13", "Luke Ridnour", "PG", "33", "6-2", "175", "Oregon", "$4,420,000"},
		{"Charlotte Bobcats", "44", "Jeff Taylor", "SF", "24", "6-7", "225", "Vanderbilt", "$788,872"},
		{"Charlotte Bobcats", "43", "Anthony Tolliver", "PF", "28", "6-8", "240", "Creighton", "$884,293"},
		{"Charlotte Bobcats", "15", "Kemba Walker", "PG", "24", "6-1", "184", "Connecticut", "$2,568,360"},
		{"Charlotte Bobcats", "8", "D.J. White", "PF", "27", "6-9", "250", "Indiana", "$104,028"},
		{"Charlotte Bobcats", "40", "Cody Zeller", "C", "21", "7-0", "240", "Indiana", "$3,857,040"},		
		{"Chicago Bulls", "17", "Lou Amundson", "PF", "31", "6-9", "225", "UNLV", "$811,469"},
		{"Chicago Bulls", "14", "D.J. Augustin", "PG", "26", "6-0", "183", "Texas", "$650,215"},
		{"Chicago Bulls", "5", "Carlos Boozer", "PF", "32", "6-9", "266", "Duke", "$15,300,000"},
		{"Chicago Bulls", "11", "Ronnie Brewer", "SF", "29", "6-7", "235", "Arkansas", "$1,186,459"},
		{"Chicago Bulls", "21", "Jimmy Butler", "SG", "24", "6-7", "220", "Marquette", "$1,112,880"},
		{"Chicago Bulls", "34", "Mike Dunleavy", "SF", "33", "6-9", "230", "Duke", "$3,183,000"},
		{"Chicago Bulls", "32", "Jimmer Fredette", "PG", "25", "6-2", "195", "Brigham Young", "$239,279"},
		{"Chicago Bulls", "22", "Taj Gibson", "PF", "28", "6-9", "225", "USC", "$7,550,000"},
		{"Chicago Bulls", "12", "Kirk Hinrich", "SG", "33", "6-4", "190", "Kansas", "$4,059,000"},
		{"Chicago Bulls", "8", "Mike James", "PG", "38", "6-2", "188", "Duquesne", "$52,017"},
		{"Chicago Bulls", "48", "Nazr Mohammed", "C", "36", "6-10", "250", "Kentucky", "$884,293"},
		{"Chicago Bulls", "13", "Joakim Noah", "C", "29", "6-11", "232", "Florida", "$11,100,000"},
		{"Chicago Bulls", "1", "Derrick Rose", "PG", "25", "6-3", "190", "Memphis", "$17,632,688"},
		{"Chicago Bulls", "9", "Greg Smith", "PF", "23", "6-10", "250", "Fresno State", "$884,293"},
		{"Chicago Bulls", "20", "Tony Snell", "SG", "22", "6-7", "200", "New Mexico", "$1,409,040"},
		{"Indiana Pacers", "5", "Lavoy Allen", "PF", "25", "6-9", "255", "Temple", "$3,060,000"},
		{"Indiana Pacers", "8", "Rasual Butler", "SG", "34", "6-7", "215", "La Salle", "$884,293"},
		{"Indiana Pacers", "17", "Andrew Bynum", "C", "26", "7-0", "285", "", "$1,000,000"},
		{"Indiana Pacers", "22", "Chris Copeland", "SF", "30", "6-8", "235", "Colorado", "$3,000,000"},
		{"Indiana Pacers", "24", "Paul George", "SF", "24", "6-9", "220", "Fresno State", "$3,282,003"},
		{"Indiana Pacers", "55", "Roy Hibbert", "C", "27", "7-2", "290", "Georgetown", "$14,283,844"},
		{"Indiana Pacers", "3", "George Hill", "PG", "28", "6-3", "188", "IUPUI", "$8,000,000"},
		{"Indiana Pacers", "9", "Solomon Hill", "SF", "23", "6-7", "225", "Arizona", "$1,246,680"},
		{"Indiana Pacers", "28", "Ian Mahinmi", "C", "27", "6-11", "250", "", "$4,000,000"},
		{"Indiana Pacers", "4", "Luis Scola", "PF", "34", "6-9", "240", "", "$4,508,504"},
		{"Indiana Pacers", "15", "Donald Sloan", "PG", "26", "6-3", "205", "Texas A&M", "$884,293"},
		{"Indiana Pacers", "1", "Lance Stephenson", "SG", "23", "6-5", "230", "Cincinnati", "$1,005,000"},
		{"Indiana Pacers", "12", "Evan Turner", "SF", "25", "6-7", "220", "Ohio State", "$6,679,867"},
		{"Indiana Pacers", "32", "C.J. Watson", "PG", "30", "6-2", "175", "Tennessee", "$2,016,000"},
		{"Indiana Pacers", "21", "David West", "PF", "33", "6-9", "250", "Xavier", "$12,000,000"},
		{"Miami Heat", "34", "Ray Allen", "SG", "38", "6-5", "205", "Connecticut", "$3,229,050"},
		{"Miami Heat", "11", "Chris Andersen", "PF", "35", "6-10", "245", "Blinn College", "$1,399,507"},
		{"Miami Heat", "31", "Shane Battier", "SF", "35", "6-8", "220", "Duke", "$3,270,000"},
		{"Miami Heat", "8", "Michael Beasley", "SF", "25", "6-10", "235", "Kansas State", "$884,293"},
		{"Miami Heat", "1", "Chris Bosh", "C", "30", "6-11", "235", "Georgia Tech", "$19,067,500"},
		{"Miami Heat", "15", "Mario Chalmers", "PG", "27", "6-2", "190", "Kansas", "$4,000,000"},
		{"Miami Heat", "30", "Norris Cole", "PG", "25", "6-2", "175", "Cleveland State", "$1,129,200"},
		{"Miami Heat", "0", "Toney Douglas", "PG", "28", "6-2", "195", "Florida State", "$1,600,000"},
		{"Miami Heat", "7", "Justin Hamilton", "C", "24", "7-0", "255", "LSU", "$98,036"},
		{"Miami Heat", "40", "Udonis Haslem", "PF", "33", "6-8", "235", "Florida", "$4,340,000"},
		{"Miami Heat", "6", "LeBron James", "SF", "29", "6-8", "250", "", "$19,067,500"},
		{"Miami Heat", "22", "James Jones", "SF", "33", "6-8", "215", "Miami (FL)", "$1,500,000"},
		{"Miami Heat", "9", "Rashard Lewis", "PF", "34", "6-10", "235", "", "$1,399,507"},
		{"Miami Heat", "20", "Greg Oden", "C", "26", "7-0", "273", "Ohio State", "$884,293"},
		{"Miami Heat", "3", "Dwyane Wade", "SG", "32", "6-4", "220", "Marquette", "$18,673,000"},
		{"Toronto Raptors", "13", "Dwight Buycks", "PG", "25", "6-3", "190", "Marquette", "$700,000"},
		{"Toronto Raptors", "3", "Nando de Colo", "PG", "26", "6-5", "195", "", "$1,463,000"},
		{"Toronto Raptors", "10", "DeMar DeRozan", "SG", "24", "6-7", "216", "USC", "$9,500,000"},
		{"Toronto Raptors", "2", "Landry Fields", "SF", "25", "6-7", "215", "Stanford", "$6,250,000"},
		{"Toronto Raptors", "50", "Tyler Hansbrough", "PF", "28", "6-9", "250", "North Carolina", "$3,183,000"},
		{"Toronto Raptors", "44", "Chuck Hayes", "PF", "30", "6-6", "250", "Kentucky", "$5,722,500"},
		{"Toronto Raptors", "15", "Amir Johnson", "PF", "27", "6-9", "210", "", "$6,500,000"},
		{"Toronto Raptors", "7", "Kyle Lowry", "PG", "28", "6-0", "205", "Villanova", "$6,210,000"},
		{"Toronto Raptors", "16", "Steve Novak", "SF", "30", "6-10", "235", "Marquette", "$3,750,000"},
		{"Toronto Raptors", "54", "Patrick Patterson", "PF", "25", "6-9", "235", "Kentucky", "$3,105,302"},
		{"Toronto Raptors", "31", "Terrence Ross", "SF", "23", "6-6", "195", "Washington", "$2,678,640"},
		{"Toronto Raptors", "25", "John Salmons", "SF", "34", "6-6", "207", "Miami (FL)", "$7,583,000"},
		{"Toronto Raptors", "77", "Julyan Stone", "SG", "25", "6-6", "200", "UTEP", "$884,293"},
		{"Toronto Raptors", "17", "Jonas Valanciunas", "C", "22", "6-11", "231", "", "$3,526,440"},
		{"Toronto Raptors", "21", "Greivis Vasquez", "PG", "27", "6-6", "211", "Maryland", "$2,150,188"},
		{"Washington Wizards", "1", "Trevor Ariza", "SF", "28", "6-8", "220", "UCLA", "$7,727,280"},
		{"Washington Wizards", "3", "Bradley Beal", "SG", "20", "6-5", "207", "Florida", "$4,319,280"},
		{"Washington Wizards", "35", "Trevor Booker", "PF", "26", "6-8", "235", "Clemson", "$2,350,820"},
		{"Washington Wizards", "90", "Drew Gooden", "PF", "32", "6-10", "250", "Kansas", "$52,017"},
		{"Washington Wizards", "4", "Marcin Gortat", "C", "30", "6-11", "240", "", "$7,727,280"},
		{"Washington Wizards", "7", "Al Harrington", "PF", "34", "6-9", "245", "", "$884,293"},
		{"Washington Wizards", "42", "Nene Hilario", "PF", "31", "6-11", "250", "", "$13,000,000"},
		{"Washington Wizards", "24", "Andre Miller", "PG", "38", "6-2", "200", "Utah", "$5,000,000"},
		{"Washington Wizards", "22", "Otto Porter Jr.", "SF", "20", "6-8", "198", "Georgetown", "$4,278,000"},
		{"Washington Wizards", "14", "Glen Rice Jr.", "SG", "23", "6-6", "206", "Georgia Tech", "$490,180"},
		{"Washington Wizards", "13", "Kevin Seraphin", "C", "24", "6-10", "278", "", "$2,761,114"},
		{"Washington Wizards", "31", "Chris Singleton", "SF", "24", "6-9", "228", "Florida State", "$1,618,680"},
		{"Washington Wizards", "17", "Garrett Temple", "SG", "28", "6-6", "195", "LSU", "$884,293"},
		{"Washington Wizards", "2", "John Wall", "PG", "23", "6-4", "195", "Kentucky", "$7,459,925"},
		{"Washington Wizards", "9", "Martell Webster", "SF", "27", "6-7", "230", "", "$5,150,000"},
	};

	/**
	 * Do stuff when buttons on index.jsp are clicked
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

		String userId = AuthUtil.getUserId(req);
		Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
		String message = "";

		if (req.getParameter("operation").equals("insertSubscription")) {

			// subscribe (only works deployed to production)
			try {
				MirrorClient.insertSubscription(credential, WebUtil.buildUrl(req, "/notify"), userId,
						req.getParameter("collection"));
				message = "Application is now subscribed to updates.";
			} catch (GoogleJsonResponseException e) {
				LOG.warning("Could not subscribe " + WebUtil.buildUrl(req, "/notify") + " because "
						+ e.getDetails().toPrettyString());
				message = "Failed to subscribe. Check your log for details";
			}

		} else if (req.getParameter("operation").equals("deleteSubscription")) {

			// subscribe (only works deployed to production)
			MirrorClient.deleteSubscription(credential, req.getParameter("subscriptionId"));

			message = "Application has been unsubscribed.";

		} else if (req.getParameter("operation").equals("insertItem")) {
			LOG.fine("Inserting Timeline Item");
			TimelineItem timelineItem = new TimelineItem();

			if (req.getParameter("message") != null) {
				timelineItem.setText(req.getParameter("message"));
			}

			// Triggers an audible tone when the timeline item is received
			timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			//timelineItem.setCreator() 
			// TODO: VOICE_CALL - Initiate a phone call using the timeline item's creator.phoneNumber attribute as recipient.

			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			menuItemList.add(new MenuItem().setAction("REPLY"));
			menuItemList.add(new MenuItem().setAction("SHARE"));
			menuItemList.add(new MenuItem().setAction("READ_ALOUD"));
			menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
			menuItemList.add(new MenuItem().setAction("PLAY_VIDEO").setPayload("http://morkout.com/mirrortest.mp4"));

			List<MenuValue> menuValues = new ArrayList<MenuValue>();			
			menuValues.add(new MenuValue().setDisplayName("Graphics").setState("DEFAULT"));
			//menuItemList.add(new MenuItem().setValues(menuValues).setAction("OPEN_URI").setPayload("com.morkout.smartcamerabasic.scheme://open/hello/glass"));						
			menuItemList.add(new MenuItem().setValues(menuValues).setAction("OPEN_URI").setPayload("immersion_scheme://xxxx"));						

			menuValues = new ArrayList<MenuValue>();
			menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/drill.png")).setDisplayName("Drill In"));
			menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));
			
			
			menuItemList.add(new MenuItem().setAction("OPEN_URI").setPayload("http://www.google.com"));
			
			menuItemList.add(new MenuItem().setAction("DELETE"));

			

			// this works, but it may cause the "SHARE" not to work? - NO, because I didn't select a picture to share!!!! so in NotifyServlet
			// the attachment is null
			// com.google.glassware.NotifyServlet doPost: got raw notification { "collection": "timeline", "itemId": 
			// "fa1cef65-31dd-4005-a45b-ed2c0d7d1291", "operation": "INSERT", "userToken": "107295550450648165922", 
			// "userActions": [  {   "type": "SHARE"  } ]}
			// com.google.glassware.NotifyServlet doPost: I don't know what to do with this notification, so I'm ignoring it.

			//			menuItemList.add(new MenuItem().setAction("PLAY_VIDEO").setPayload("http://morkout.com/mirrortest.mp4"));			

			timelineItem.setMenuItems(menuItemList);


			if (req.getParameter("imageUrl") != null) {
				// Attach an image, if we have one
				URL url = new URL(req.getParameter("imageUrl"));
				String contentType = req.getParameter("contentType");
				MirrorClient.insertTimelineItem(credential, timelineItem, contentType, url.openStream());
			} else {
				MirrorClient.insertTimelineItem(credential, timelineItem);
			}

			//			if (req.getParameter("videoUrl") != null) {
			//				// Attach an video, if we have one
			//				URL url = new URL(req.getParameter("videoUrl"));
			//				String contentType = req.getParameter("contentType");
			//				MirrorClient.insertTimelineItem(credential, timelineItem, contentType, url.openStream());
			//			} else {
			//				MirrorClient.insertTimelineItem(credential, timelineItem);
			//			}			

			message = "A timeline item has been inserted.";

			List<TimelineItem> result = new ArrayList<TimelineItem>();
			TimelineListResponse timelineItems = MirrorClient.listItems(credential, 100);
			LOG.info("timelineItems size="+timelineItems.getItems().size());
			if (timelineItems.getItems() != null && timelineItems.getItems().size() > 0) {
				result.addAll(timelineItems.getItems());
				for (int i=0; i<timelineItems.getItems().size(); i++) {
					LOG.info("timelineItem: "+timelineItems.getItems().get(i).toString());					
				}
			}

		} else if (req.getParameter("operation").equals("insertPaginatedItem")) {
			LOG.fine("Inserting Timeline Item");
			TimelineItem timelineItem = new TimelineItem();
			timelineItem.setHtml(PAGINATED_HTML);

			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			menuItemList.add(new MenuItem().setAction("OPEN_URI").setPayload(
					"https://www.google.com/search?q=cat+maintenance+tips"));
			timelineItem.setMenuItems(menuItemList);

			// Triggers an audible tone when the timeline item is received
			timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			MirrorClient.insertTimelineItem(credential, timelineItem);

			message = "A timeline item has been inserted.";

		} else if (req.getParameter("operation").equals("insertItemWithAction")) {
			LOG.fine("Inserting Timeline Item");
			TimelineItem timelineItem = new TimelineItem();
			timelineItem.setText("Tell me what you had for lunch :)");

			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			// Built in actions
			menuItemList.add(new MenuItem().setAction("REPLY"));
			menuItemList.add(new MenuItem().setAction("READ_ALOUD"));

			// And custom actions
			List<MenuValue> menuValues = new ArrayList<MenuValue>();
			menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/drill.png"))
					.setDisplayName("Drill In"));
			menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));

			timelineItem.setMenuItems(menuItemList);
			timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			MirrorClient.insertTimelineItem(credential, timelineItem);

			message = "A timeline item with actions has been inserted.";

		} else if (req.getParameter("operation").equals("insertContact")) {
			if (req.getParameter("iconUrl") == null || req.getParameter("name") == null) {
				message = "Must specify iconUrl and name to insert contact";
			} else {
				// Insert a contact
				LOG.fine("Inserting contact Item");
				Contact contact = new Contact();
				contact.setId(req.getParameter("id"));
				contact.setDisplayName(req.getParameter("name"));
				contact.setImageUrls(Lists.newArrayList(req.getParameter("iconUrl")));
				contact.setAcceptCommands(Lists.newArrayList(new Command().setType("TAKE_A_NOTE"), 
						new Command().setType("POST_AN_UPDATE")));
				MirrorClient.insertContact(credential, contact);

				message = "Inserted contact: " + req.getParameter("name");
			}

		} else if (req.getParameter("operation").equals("deleteContact")) {

			// Insert a contact
			LOG.fine("Deleting contact Item");
			MirrorClient.deleteContact(credential, req.getParameter("id"));

			message = "Contact has been deleted.";

		} else if (req.getParameter("operation").equals("insertItemAllUsers")) {
			if (req.getServerName().contains("glass-java-starter-demo.appspot.com")) {
				message = "This function is disabled on the demo instance.";
			}

			// Insert a contact
			List<String> users = AuthUtil.getAllUserIds();
			LOG.info("found " + users.size() + " users");
			if (users.size() > 10) {
				// We wouldn't want you to run out of quota on your first day!
				message =
						"Total user count is " + users.size() + ". Aborting broadcast " + "to save your quota.";
			} else {
				TimelineItem allUsersItem = new TimelineItem();
				allUsersItem.setText("Hello Everyone!");

				BatchRequest batch = MirrorClient.getMirror(null).batch();
				BatchCallback callback = new BatchCallback();

				// TODO: add a picture of a cat
				for (String user : users) {
					Credential userCredential = AuthUtil.getCredential(user);
					MirrorClient.getMirror(userCredential).timeline().insert(allUsersItem)
					.queue(batch, callback);
				}

				batch.execute();
				message =
						"Successfully sent cards to " + callback.success + " users (" + callback.failure
						+ " failed).";
			}


		} else if (req.getParameter("operation").equals("deleteTimelineItem")) {

			// Delete a timeline item
			LOG.fine("Deleting Timeline Item");
			MirrorClient.deleteTimelineItem(credential, req.getParameter("itemId"));

			message = "Timeline Item has been deleted.";

		} else if (req.getParameter("operation").equals("getNBAPlayerInfo")) {
			TimelineItem timelineItem = new TimelineItem();
			timelineItem.setText("NBA Team Rosters");
			List<MenuItem> menuItemList = new ArrayList<MenuItem>();			

			Set<String> names = new LinkedHashSet<String>();
			for (String[] team : teams)
				names.add(team[0]);			
			for (String name : names) {
				// get the last word of name 
				String words[] = name.split(" ");
				String mascot = words[words.length - 1].toLowerCase();
				List<MenuValue> menuValues = new ArrayList<MenuValue>();			
				//				menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/" + mascot + ".gif")).
				//						setDisplayName(name));
				menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/ic_run_50.png"))
						.setDisplayName(name));

				menuItemList.add(new MenuItem().setValues(menuValues).setId(mascot).setAction("CUSTOM"));

			}

			timelineItem.setMenuItems(menuItemList);
			timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			MirrorClient.insertTimelineItem(credential, timelineItem);
		} else {
			String operation = req.getParameter("operation");
			LOG.warning("Unknown operation specified " + operation);
			message = "I don't know how to do that";
		}
		WebUtil.setFlash(req, message);
		res.sendRedirect(WebUtil.buildUrl(req, "/"));
	}
}
