package cessda.eqb.harvester;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListRecordsTest {

    public static boolean debug = false;

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test01");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd");
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test02");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V2_0;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd" + "'", str0, "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test03");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_RECORDS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd");
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test04");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_IDENTIFY;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test05");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords5 = new org.oclc.oai.harvester2.verb.ListRecords("http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd?verb=ListRecords&from=http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd&until=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd&set=&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_GET_RECORD;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd");
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_SETS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd");
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords2 = new org.oclc.oai.harvester2.verb.ListRecords("http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd", "");
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd?verb=ListRecords&resumptionToken=");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords5 = new org.oclc.oai.harvester2.verb.ListRecords("", "", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd", "");
            org.junit.Assert.fail("Expected exception of type java.net.MalformedURLException; message: no protocol: ?verb=ListRecords&from=&until=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd&set=http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd&metadataPrefix=");
        } catch (java.net.MalformedURLException e) {
        // Expected exception.
        }
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        org.oclc.oai.harvester2.verb.ListRecords listRecords0 = new org.oclc.oai.harvester2.verb.ListRecords();
        java.lang.Class<?> wildcardClass1 = listRecords0.getClass();
        org.junit.Assert.assertNotNull(wildcardClass1);
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd");
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        org.oclc.oai.harvester2.verb.ListRecords listRecords0 = new org.oclc.oai.harvester2.verb.ListRecords();
        org.w3c.dom.NodeList nodeList1 = listRecords0.getErrors();
        java.lang.String str2 = listRecords0.toString();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str4 = listRecords0.getSingleString("http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd");
            org.junit.Assert.fail("Expected exception of type org.apache.xpath.domapi.XPathStylesheetDOM3Exception; message: Das Pra?fix muss in einen Namensbereich aufgelo?st werden: http");
        } catch (org.apache.xpath.domapi.XPathStylesheetDOM3Exception e) {
        // Expected exception.
        }
        org.junit.Assert.assertNull(nodeList1);
        org.junit.Assert.assertEquals("'" + str2 + "' != '" + "" + "'", str2, "");
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        org.oclc.oai.harvester2.verb.ListRecords listRecords0 = new org.oclc.oai.harvester2.verb.ListRecords();
        java.lang.String str1 = listRecords0.getSchemaLocation();
        java.lang.Class<?> wildcardClass2 = listRecords0.getClass();
        org.junit.Assert.assertNull(str1);
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords5 = new org.oclc.oai.harvester2.verb.ListRecords("http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd", "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd?verb=ListRecords&from=http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd&until=http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd&set=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        org.oclc.oai.harvester2.verb.ListRecords listRecords0 = new org.oclc.oai.harvester2.verb.ListRecords();
        java.lang.String str1 = listRecords0.getRequestURL().toString();
        org.junit.Assert.assertNull(str1);
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords2 = new org.oclc.oai.harvester2.verb.ListRecords("http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "");
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd?verb=ListRecords&resumptionToken=");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListRecords listRecords5 = new org.oclc.oai.harvester2.verb.ListRecords("http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "hi!", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd?verb=ListRecords&from=hi!&until=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd&set=http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        org.oclc.oai.harvester2.verb.ListRecords listRecords0 = new org.oclc.oai.harvester2.verb.ListRecords();
        org.w3c.dom.NodeList nodeList1 = listRecords0.getErrors();
        java.lang.String str2 = listRecords0.toString();
        java.lang.String str3 = listRecords0.getRequestURL().toString();
        org.junit.Assert.assertNull(nodeList1);
        org.junit.Assert.assertEquals("'" + str2 + "' != '" + "" + "'", str2, "");
        org.junit.Assert.assertNull(str3);
    }
}

