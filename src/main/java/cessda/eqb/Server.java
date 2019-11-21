package cessda.eqb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PreDestroy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oclc.oai.harvester2.verb.GetRecord;
import org.oclc.oai.harvester2.verb.ListIdentifiers;
import org.oclc.oai.harvester2.verb.ListSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jodd.mail.Email;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;

@EnableScheduling
@SpringBootApplication
@ManagedResource
public class Server extends SpringBootServletInitializer {

	final static Logger log = LoggerFactory.getLogger(Server.class.getName());

	static boolean fullIsRunning = false;

	static boolean incrementalIsRunning = false;

	@Async
	@ManagedOperation(description = "Run harvesting on several repo starting from 'harvester.from.single'. Separate more than one repo with comma. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. The position corresponds to the number given in the list of repos in the configuration view, starting from 0. See environments tab and search for 'harvester.repos'")
	public String bundleHarvesting(String commaSeparatedIntegerPositionInRepoList) {

		String res = "";
		int[] numbers = Arrays.asList(commaSeparatedIntegerPositionInRepoList.split(",")).stream().map(String::trim).mapToInt(Integer::parseInt).toArray();
		for (int i : numbers) {
			res += "Repo " + i + " : " + singleHarvesting(i) + "                           \n";
		}
		return res + "    Bundle harvesting finished from " + harvesterConfiguration.getFrom().getSingle();
	}

	@Async
	@ManagedOperation(description = "Run harvesting on one single repo starting from 'harvester.from.single'. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. The position corresponds to the number given in the list of repos in the configuration view, starting from 0. See environments tab and search for 'harvester.repos'")
	public String singleHarvesting(Integer positionInRepoList) {

		if (incrementalIsRunning) {
			return "Not started. An incremental harvesting progress is already running";
		}
		incrementalIsRunning = true;
		hlog.info("Single harvesting starting from " + harvesterConfiguration.getFrom().getSingle());
		runSingleHarvest(harvesterConfiguration.getFrom().getSingle(), positionInRepoList);
		hlog.info("Single harvesting finished from " + harvesterConfiguration.getFrom().getSingle());

		incrementalIsRunning = false;
		return "Single harvesting for " + positionInRepoList + "th repository started. See log section for details";
	}

	/**
	 * runs right after service startup takes place
	 * 
	 * @return status
	 */
	@Async
	@ManagedOperation(description = "Run initial harvesting. Set from date with key harvester.cron.initial. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. Don't forget to reset the environment and update application.yml for persistent configuration")
	@Scheduled(initialDelay = 10000L, fixedDelay = 315360000000l)
	public String initialHarvesting() {

		log.info(harvesterConfiguration.getMetadataFormat());
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -2);
		String newInitial = rsmFlDtFrmt.format(cal.getTime());
		// set initial value dynamically
		if (harvesterConfiguration.getFrom().getInitial() == null) {
			harvesterConfiguration.getFrom().setInitial(newInitial);
		}
		log.info(harvesterConfiguration.getFrom().getInitial());

		if (incrementalIsRunning) {
			return "Not started. An incremental harvesting progress is already running";
		}
		incrementalIsRunning = true;
		hlog.info("Full harvest schedule: " + harvesterConfiguration.getCron().getFull());
		hlog.info("Incremental harvest schedule: " + harvesterConfiguration.getCron().getIncremental());

		hlog.info("Initial harvesting starting from " + harvesterConfiguration.getFrom().getInitial());
		hlog.info("Incremental harvesting will start with cron schedule " + harvesterConfiguration.getCron().getIncremental() + " from " + harvesterConfiguration.getFrom().getIncremental());
		runHarvest(harvesterConfiguration.getFrom().getInitial());
		hlog.info("Initial harvesting finished from " + harvesterConfiguration.getFrom().getInitial());

		incrementalIsRunning = false;
		return "Initial harvesting finished from " + harvesterConfiguration.getFrom().getInitial();
	}

	/**
	 * runs once in a year, no incremental harvesting takes place
	 * 
	 * @return status
	 */
	@Async
	@ManagedOperation(description = "Run full harvesting. Set from date with key harvester.cron.full")
	@Scheduled(cron = "${harvester.cron.full:0 30 1 15 * ?}")
	public String fullHarvesting() {

		if (fullIsRunning) {
			return "Not started. A full harvesting progress is already running";
		}
		hlog.info("Full harvesting started from " + harvesterConfiguration.getFrom().getFull());
		fullIsRunning = true;
		runHarvest(harvesterConfiguration.getFrom().getFull());
		hlog.info("Full harvesting finished");
		fullIsRunning = false;
		return "Full harvesting finished from " + harvesterConfiguration.getFrom().getFull();
	}

	/**
	 * runs always.
	 * 
	 * @return status
	 */
	@Async
	@ManagedOperation(description = "Run incremental harvesting. Set from date with key harvester.cron.incremental")
	@Scheduled(cron = "${harvester.cron.incremental:0 0 4 * * *}")
	public String incrementalHarvesting() {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -2);
		String newIncFrom = rsmFlDtFrmt.format(cal.getTime());
		String msg = "";
		if (!fullIsRunning) {
			if (!incrementalIsRunning) {
				incrementalIsRunning = true;
				hlog.info("Incremental harvesting started from " + harvesterConfiguration.getFrom().getIncremental());
				runHarvest(harvesterConfiguration.getFrom().getIncremental());
				hlog.info("Incremental harvesting finished");

				msg = "Incremental harvesting finished from " + harvesterConfiguration.getFrom().getIncremental();

				harvesterConfiguration.getFrom().setIncremental(newIncFrom);
				hlog.info("Next incremental harvest will start from " + newIncFrom);
			} else {
				String o = "Incremental harvesting already running.";
				hlog.info(o);
				return o;

			}

		} else {
			hlog.info("No incremental harvesting, as full harvesting is in progress.");
			return "No incremental harvesting, as full harvesting is in progress.";
		}

		incrementalIsRunning = false;
		return msg;
	}

	@Async
	public void runSingleHarvest(String fromDate, Integer position) {

		hlog.info("Harvesting started from " + fromDate + " for repo " + position);
		try {
			encoding();
			type = "dc";
			mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat() : "oai_dc";
			to = rsmFlDtFrmt.format(new Date());

			String baseUrl = harvesterConfiguration.getRepoBaseUrls().get(position);
			hlog.info("Single harvesting " + baseUrl + " from " + fromDate);
			if (baseUrl.trim() != "") {
				if (baseUrl.indexOf("#") != -1) {
					baseUrl = baseUrl.substring(0, baseUrl.indexOf("#"));
				}
				log.trace(baseUrl + " " + fromDate);
				for (String set : getSpecs(baseUrl)) {
					hlog.info("Start to get " + type + " records for " + baseUrl + " / " + set + " from " + fromDate);
					fetchDCRecords(oaiBase(baseUrl), set, fromDate);
				}
			}

			File[] directories = new File(harvesterConfiguration.getDir()).listFiles(File::isDirectory);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			log.error(e.getMessage());

			incrementalIsRunning = false;
		}
	}

	public void runHarvest(String fromDate) {

		hlog.info("Harvesting started from " + fromDate);
		try {
			encoding();
			type = "dc";
			mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat() : "oai_dc";
			to = rsmFlDtFrmt.format(new Date());

			for (String baseUrl : harvesterConfiguration.getRepoBaseUrls()) {
				if (baseUrl.trim() != "") {
					if (baseUrl.indexOf("#") != -1) {
						baseUrl = baseUrl.substring(0, baseUrl.indexOf("#"));
					}
					log.trace(baseUrl + " " + fromDate);
					for (String set : getSpecs(baseUrl)) {
						hlog.info("Start to get " + type + " records for " + baseUrl + " / " + set + " from " + fromDate);
						fetchDCRecords(oaiBase(baseUrl), set, fromDate);
					}
				}
			}

			File[] directories = new File(harvesterConfiguration.getDir()).listFiles(File::isDirectory);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	void fetchDCRecords(String repoBase, String setspec, String fromDate) {

		log.info(harvesterConfiguration.getDir());
		log.info(repoBase + "  " + setspec + "   " + fromDate);
		File f = new File(harvesterConfiguration.getDir());
		if (!f.exists()) {
			f = new File(harvesterConfiguration.getDir());
		}
		try {
			ArrayList<String> currentlyRetrievedSet = null;
			do {
				log.info("Fetching " + type + " records for repo " + repoBase + " and  pmh set " + setspec + ". be patient, this can take hours.");
				currentlyRetrievedSet = null;
				do {
					currentlyRetrievedSet = getIdentifiersForSet(repoBase, setspec, largeHarvestInteruptedToken, new ArrayList<String>(), Optional.of(mdFormat), fromDate);
					writeToLocalFileSystem(currentlyRetrievedSet, repoBase, setspec, f.getAbsolutePath());
					if (currentlyRetrievedSet.size() == 0) {
						break;
					}
					log.info("\tSET\t" + setspec + "\tsize:\t" + currentlyRetrievedSet.size() + "\tURL\t" + repoBase);
				} while (largeHarvestInteruptedToken != null && itemsInCurrentSet != 50);
			} while (currentlyRetrievedSet.size() != 0 && currentlyRetrievedSet.size() % 50 == 0 && itemsInCurrentSet != 50 && itemsInCurrentSet % currentlyRetrievedSet.size() != 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Server.class, args);

		log.info("Harvester running. ");
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Server.class);
	}

	static final Logger hlog = LoggerFactory.getLogger(HarvesterReport.class);

	static String largeHarvestInteruptedToken = null;

	static Long itemsInCurrentSet = 0l;

	protected static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	protected static String fromEmail = "system.wts@gesis.org";

	protected static int largeHarvestLimit = 50;

	protected static DateFormat oaiDtFrmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	protected static SimpleDateFormat rsmFlDtFrmt = new SimpleDateFormat("yyyy-MM-dd");

	protected static String to = "";

	@Autowired
	ApplicationContext applicationContext;

	protected static TimeZone tZone = TimeZone.getTimeZone("UTC");

	@Value("${spring.mail.host}")
	String mailhost = "smtp.gmail.com";

	protected ArrayList<String> getIdentifiersForSet(String url, String set, String resumptionToken, ArrayList<String> list, Optional<String> overwrite, String fromDate) {

		log.info("Harvesting started for " + oaiBase(url) + "?verb=ListRecords" + "&set=" + set + "&metadataPrefix=" + mdFormat + "&from=" + fromDate + "&resumptionToken=" + resumptionToken, "");
		ArrayList<String> records = list;
		log.debug("URL: " + url + " list size : " + list.size() + " restoken " + resumptionToken);
		log.trace("limit : " + largeHarvestLimit + " recordssize: " + records.size());
		if (records.size() >= largeHarvestLimit) {
			largeHarvestInteruptedToken = resumptionToken;
			log.info("reached limit of " + largeHarvestLimit + ". Processing " + records.size() + " records and then resume with " + largeHarvestInteruptedToken);
			return records;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			log.trace("recurse:\turl " + url + " set " + set + " token " + resumptionToken + "  ");
			DocumentBuilder builder = factory.newDocumentBuilder();
			ListIdentifiers li;
			log.trace(resumptionToken);
			if (resumptionToken != null) {
				log.trace(url);
				li = new ListIdentifiers(oaiBase(url), resumptionToken);
			} else {
				if (set.compareTo(url) == 0 || set.startsWith("http")) {
					set = null;
				}
				log.debug("From " + fromDate + "  until " + to + "  " + oaiBase(url) + "  " + set + "  " + mdFormat);
				li = new ListIdentifiers(oaiBase(url), fromDate, to, set, overwrite.orElse(mdFormat), harvesterConfiguration.getTimeout());
				log.debug(oaiBase(url));
			}
			log.trace(li.getRequestURL());
			InputSource is = new InputSource(new StringReader(li.toString()));
			Document identfiers = builder.parse(is);
			NodeList resumptionTokenReq = identfiers.getElementsByTagName("resumptionToken");
			// add to list of records to fetch
			NodeList identifiersIDs = identfiers.getElementsByTagName("identifier");
			if (identifiersIDs != null) {
				for (int j = 0; j < identifiersIDs.getLength(); j++) {
					Node n = identifiersIDs.item(j);
					if (n.getTextContent() != null) {
						records.add(n.getTextContent());
					} else {
						log.warn("Node " + n + " is null");
					}
				}
			} else {
				log.warn("Identifiers in this block are null for resumption token " + resumptionToken);
			}
			// need to recurse?
			if (resumptionTokenReq.getLength() > 0 && resumptionTokenReq.item(0).getTextContent() != "") {
				String rTok = resumptionTokenReq.item(0).getTextContent();
				log.info("\tSet\t" + set + "\tToken\t" + rTok + "\tSize \t " + records.size() + "\tURL\t" + url);
				records = getIdentifiersForSet(url, set, rTok, records, null, fromDate);
				// need to interrupt recursion?

			} else {
				largeHarvestInteruptedToken = null;
			}
			if (resumptionTokenReq.getLength() != 0 && resumptionTokenReq.item(0).hasAttributes() && resumptionTokenReq.item(0).getAttributes().getNamedItem("completeListSize") != null) {

				itemsInCurrentSet = Long.parseLong(resumptionTokenReq.item(0).getAttributes().getNamedItem("completeListSize").getTextContent());
				log.info("Items in current set: " + itemsInCurrentSet);

			} else {
				log.info(" - ");
			}
			log.trace(itemsInCurrentSet + "");
		} catch (SocketTimeoutException ste) {
			log.error("OAI response timed out  " + harvesterConfiguration.getTimeout());

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			String m = "\n";
			for (StackTraceElement ste : e.getStackTrace()) {
				if (ste.getFileName() != null) {
					m += ste.getFileName() + "  " + ste.getMethodName() + "   " + ste.getLineNumber() + "\n";
				}
			}
			this.notifyOnError(
					"Harvesting failed for " + oaiBase(url) + "?verb=ListRecords" + "&set=" + set + "&metadataPrefix=" + mdFormat + "&from=" + fromDate + "&resumptionToken=" + resumptionToken,
					e.getMessage() + "\n" + m);
		}
		resumptionToken = null;
		log.trace("Records to fetch : " + records.size());

		return records;
	}

	protected void notifyOnError(String subject, String msg) {

		hlog.error(subject + "\n" + String.format("%.40s", msg));
		if (harvesterConfiguration.getRecipient().compareTo("") == 0) {
			log.warn("no recipient for notifications given in config.");
			return;
		}
		try {
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();

			Email noti = new Email();
			noti.from(fromEmail);
			// log.debug(harvesterConfiguration.recipient.split(",") + "");
			log.debug(fromEmail);
			log.debug(harvesterConfiguration.getRecipient().split(",") + "");
			noti.to(harvesterConfiguration.getRecipient().split(","));
			noti.setSubject(localMachine.getHostName() + " : " + subject);

			msg += "\n" + InetAddress.getLoopbackAddress().getHostAddress() + "\n" + InetAddress.getLoopbackAddress().getHostName();
			noti.addText(msg);
			SmtpServer smtpServer = SmtpServer.create(this.mailhost);
			log.warn("smtp port " + smtpServer.getPort() + "");
			SendMailSession session = smtpServer.createSession();
			session.open();
			log.error(session.sendMail(noti));
			log.debug("mail sent to " + harvesterConfiguration.getRecipient());
			session.close();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	protected void writeToLocalFileSystem(ArrayList<String> records, String oaiUrl, String specId, String path) {

		log.info(oaiUrl + "\t" + specId + "\t" + path);
		String indexName = shortened("", oaiUrl) + "-" + specId;
		Path dest = Paths.get(path, indexName.replace(":", "-").replace("\\", "-").replaceAll("/", "-"));
		try {
			Files.createDirectories(dest);
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}
		log.info(dest.toFile().getAbsolutePath() + "  " + dest.toFile().exists() + "");

		records.stream().map(s -> s.trim()).forEach(currentRecord -> {
			String fname = "";
			try {

				fname = "";
				GetRecord pmhRecord = null;
				fname = (indexName + "__" + currentRecord + "_" + harvesterConfiguration.getDialectDefinitionName() + ".xml").replace(":", "-").replace("\\", "-").replaceAll("/", "-");

				try {
					log.trace(oaiUrl);
					try {
						pmhRecord = new GetRecord(oaiUrl, currentRecord, mdFormat, harvesterConfiguration.getTimeout());
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						log.error(oaiUrl + " " + currentRecord + " " + e.getMessage());
					}
					Path fdest = Paths.get(path, indexName.replace(":", "-").replace("\\", "-").replace("/", "-"), fname);
					File f = new File(fdest.toString());
					if (pmhRecord.getDocument().getElementsByTagName("metadata").item(0) != null) {
						NodeList nl = pmhRecord.getDocument().getElementsByTagName("metadata").item(0).getChildNodes();
						for (int i = 0; i < nl.getLength(); i++) {
							Node child = nl.item(i);
							if (child instanceof Element) {
								Source input = new DOMSource(child);
								Transformer transformer = TransformerFactory.newInstance().newTransformer();
								Result output = new StreamResult(f);
								log.trace("Stored : " + f.getAbsolutePath());
								transformer.transform(input, output);
								break;

							}
						}

					} else {
						log.error(pmhRecord.getDocument().getElementsByTagName("error").item(0).getTextContent());
					}

				} catch (ClosedByInterruptException cbie) {
					log.trace("Storing metadata interupted. Restart requested while processing " + fname);
				} catch (NullPointerException | IOException | InvalidPathException e) {
					log.error(e.getMessage());
					e.printStackTrace();
					log.error(pmhRecord.getDocument().toString());
				}
			} catch (SAXParseException e) {

				log.error(fname + " : " + e.getMessage());

			} catch (DOMException | ParserConfigurationException | SAXException | TransformerException e) {

				log.error(fname);
				log.error(e.getMessage());
				e.printStackTrace();

			} catch (Exception z) {
				z.printStackTrace();
				log.error(z.getMessage());
			}
		});

	}

	protected static String oaiBase(String u) {

		if (u.endsWith("/")) {
			return u;
		}
		if (u.indexOf("?") != -1) {
			u = u.substring(0, u.indexOf("?"));
		}
		return u;
	}

	protected static String oaiSet(String u) {

		if (u.indexOf("set=") != -1) {
			u = u.substring(u.indexOf("set="));
			if (u.contains("&")) {
				u = u.substring(0, u.indexOf("&"));
			}
			return u;
		}
		return null;
	}

	protected static String shortened(String prefix, String oaiUrl) {

		try {
			if (prefix.startsWith("!")) {
				return new URI(prefix.substring(1)).toString();
			}
			log.debug(oaiUrl);
			return prefix + new URI(oaiUrl).getHost().toString().replace(".", "_").replace(":", "-").toLowerCase();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
	}

	protected static String mdFormat = "oai_ddi";

	protected static String type = "dc";

	public static String repoBaseUrl = "http://svko-dara-test.gesis.org:8080/oaip/oai";

	@Autowired
	protected HarvesterConfiguration harvesterConfiguration;

	Properties existingIndexes = null;

	@Deprecated
	List<String> getRepoBaseUrls(String file) {

		ArrayList<String> reposToUnfold = new ArrayList<String>();
		File fil = new File(file);
		if (!fil.exists()) {
			reposToUnfold.add("http://www.da-ra.de/oaip/oai?verb=ListIdentifiers&metadataPrefix=oai_dc&set=16");
			reposToUnfold.add("http://www.da-ra.de/oaip/oai?verb=ListIdentifiers&metadataPrefix=oai_dc&set=18");
			return reposToUnfold;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fil))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#") && line.trim() != "" && line.trim() != null) {
					line = line.trim();
					if (line.contains("&set=")) {
						reposToUnfold.add(line);
					} else {
						if (line.indexOf("?") != -1) {
							reposToUnfold.add(line.substring(0, line.indexOf("?")));
						} else {
							reposToUnfold.add(line);
						}
					}
				}

			}
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
		for (String string : reposToUnfold) {
			log.debug(string);
		}
		log.trace(reposToUnfold + "");
		return reposToUnfold;

	}

	List<String> getSpecs(String url) {

		ArrayList<String> unfoldedSets = new ArrayList<String>();
		// skip if set is explicitly referenced
		if (url.contains("set=") && unfoldedSets.add(url.substring(url.indexOf("set=") + 4)) || url.length() == 0) {
			return unfoldedSets;
		}
		try {
			ListSets ls = null;
			try {
				do {
					log.warn("" + url);
					ls = new ListSets(url.trim(), harvesterConfiguration.getTimeout());
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					InputSource is = new InputSource(new StringReader(ls.toString()));

					Document document = builder.parse(is);

					NodeList nl = document.getElementsByTagName("setSpec");

					for (int i = 0; i <= nl.getLength() - 1; i++) {
						String setSpec = nl.item(i).getTextContent();
						unfoldedSets.add(setSpec);
						log.info("Set: " + setSpec);
					}
					if (ls.toString().contains("error")) {
						log.error("Invalid request" + ls);

					}
					if (ls.getResumptionToken() != "") {
						log.info(ls.getResumptionToken());
						url = url + "?verb=ListSets&resumptionToken=" + ls.getResumptionToken();
						log.info("" + url);
					}
				} while (unfoldedSets.size() % 50 == 0 && ls.getResumptionToken() != "" && url.trim() != "" && url.trim() != null);
				log.info("");
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				log.info("less than 50 specs or end of spec list reached" + e.getMessage());
			} catch (SocketTimeoutException ste) {
				log.error("Request to oai endpoint timed out " + harvesterConfiguration.getTimeout());
			}

		} catch (IOException e) {
			e.printStackTrace();
			log.error("");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			log.error("");
		} catch (SAXParseException e) {
			log.error("");
			unfoldedSets.add(url);
			return unfoldedSets;
		} catch (SAXException e) {
			e.printStackTrace();
			log.error("");
		} catch (TransformerException e) {
			e.printStackTrace();
			log.error("");
		}
		log.info("No. of sets: " + unfoldedSets.size());
		return unfoldedSets;
	}

	void mem() {

		Runtime runtime = Runtime.getRuntime();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		long max = runtime.maxMemory();
		long used = total - free;

		log.trace(Math.round(max / 1e6) + " MB available before Cycle");
		log.trace(Math.round(total / 1e6) + " MB allocated before Cycle");
		log.trace(Math.round(free / 1e6) + " MB free before Cycle");
		log.trace(Math.round(used / 1e6) + " MB used before Cycle");
	}

	protected void encoding() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		System.setProperty("file.encoding", "UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null, null);
		log.trace(System.getProperty("user.country"));
		log.trace(System.getProperty("user.language"));
		Locale.setDefault(new Locale("en", "GB"));
		System.setProperty("user.country", "GB");
		System.setProperty("user.language", "en");
		log.trace(System.getProperty("user.country"));
		log.trace(System.getProperty("user.language"));
	}

	@PreDestroy
	void printConfig() {

		hlog.info(harvesterConfiguration.toString());
	}
}
