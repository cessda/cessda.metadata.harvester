package eu.cessda.eqb.harvester;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oclc.oai.harvester2.verb.RecordHeader;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IOUtilitiesTests {
    @Test
    void shouldCreateOutputDirectory(@TempDir Path tempDir) throws DirectoryCreationFailedException {
        var dirToCreate = Path.of(String.valueOf(new Random().nextInt()));

        // Assert that the directory initially does not exist
        assertThat(tempDir.resolve(dirToCreate)).doesNotExist();

        // Create the directory
        var createdDirectory = IOUtilities.createDestinationDirectory(tempDir, dirToCreate);

        // Assert that the directory has been created
        assertThat(tempDir.resolve(dirToCreate)).exists();

        // Assert that the returned path is equal to the expected path
        assertThat(createdDirectory).isEqualTo(tempDir.resolve(dirToCreate));
    }

    @Test
    void shouldWriteSourceToXMLFile(@TempDir Path tempDir)
            throws ParserConfigurationException, IOException, TransformerException, SAXException {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var uuid = UUID.randomUUID();

        // Create XML document
        var testDocument = documentBuilder.newDocument();
        var rootElement = testDocument.createElement("testNode");
        testDocument.appendChild(rootElement);
        rootElement.appendChild(testDocument.createTextNode(uuid.toString()));

        // Write out the XML
        var tempFile = tempDir.resolve(uuid + ".xml");
        new IOUtilities().writeDomSource(new DOMSource(testDocument), tempFile);

        assertThat(tempFile).exists();

        // Parse the XML
        var parsedDocument = documentBuilder.parse(tempFile.toFile());

        // The parsed document should have node equality
        assertThat(parsedDocument.isEqualNode(testDocument)).isTrue();
    }

    @Test
    void shouldDeleteOrphanedRecords(@TempDir Path tempDir) throws IOException {
        // Generate random file names
        var fileNameIntArray = new Random().ints(5).toArray();

        // Select files to be kept
        var filesToKeep = Arrays.stream(fileNameIntArray).limit(3).toArray();

        // Create all the files
        for (var fileName : fileNameIntArray) {
            Files.createFile(tempDir.resolve(fileName + ".xml"));
        }

        // Delete orphaned records
        var mockRecordHeaders = Arrays.stream(filesToKeep)
                .mapToObj(i -> new RecordHeader(String.valueOf(i), LocalDate.now(), Collections.emptySet(), null))
                .toList();
        IOUtilities.deleteOrphanedRecords(new Repo(null, "TEST", null, null, false, null, null), mockRecordHeaders,
                tempDir);

        // Check that the directory is in the expected state
        for (var file : fileNameIntArray) {
            if (Arrays.stream(filesToKeep).anyMatch(fileToKeep -> fileToKeep == file)) {
                // Verify kept files are still present
                assertThat(tempDir).isDirectoryContaining(path -> path.getFileName().toString().equals(file + ".xml"));
            } else {
                // Verify orphaned records are deleted
                assertThat(tempDir)
                        .isDirectoryNotContaining(path -> path.getFileName().toString().equals(file + ".xml"));
            }
        }
    }
}
