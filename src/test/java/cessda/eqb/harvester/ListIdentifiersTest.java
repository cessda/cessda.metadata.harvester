package cessda.eqb.harvester;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIdentifiersTest {

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
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers6 = new org.oclc.oai.harvester2.verb.ListIdentifiers("http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd", (java.lang.Integer) 1);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd?verb=ListIdentifiers&from=http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd&until=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd&set=&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        java.lang.Object obj0 = new java.lang.Object();
        java.lang.Class<?> wildcardClass1 = obj0.getClass();
        org.junit.Assert.assertNotNull(wildcardClass1);
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers3 = new org.oclc.oai.harvester2.verb.ListIdentifiers("http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "", (int) 'a');
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd?verb=ListIdentifiers&resumptionToken=");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers6 = new org.oclc.oai.harvester2.verb.ListIdentifiers("", "hi!", "", "", "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd", (java.lang.Integer) 10);
            org.junit.Assert.fail("Expected exception of type java.net.MalformedURLException; message: no protocol: ?verb=ListIdentifiers&from=hi!&until=&set=&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd");
        } catch (java.net.MalformedURLException e) {
        // Expected exception.
        }
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_GET_RECORD;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd");
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test10");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_SETS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd");
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        java.lang.String str0 = org.oclc.oai.harvester2.verb.HarvesterVerb.SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS;
        org.junit.Assert.assertEquals("'" + str0 + "' != '" + "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd" + "'", str0, "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd");
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.String str1 = listIdentifiers0.getSchemaLocation();
        org.w3c.dom.Node node2 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str4 = listIdentifiers0.getSingleString(node2, "");
            org.junit.Assert.fail("Expected exception of type javax.xml.transform.TransformerException; message: Leerer Ausdruck!");
        } catch (javax.xml.transform.TransformerException e) {
        // Expected exception.
        }
        org.junit.Assert.assertNull(str1);
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.Class<?> wildcardClass1 = listIdentifiers0.getClass();
        org.junit.Assert.assertNotNull(wildcardClass1);
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test14");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        org.w3c.dom.Node node1 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str3 = listIdentifiers0.getSingleString(node1, "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
            org.junit.Assert.fail("Expected exception of type org.apache.xpath.domapi.XPathStylesheetDOM3Exception; message: Das Pra?fix muss in einen Namensbereich aufgelo?st werden: http");
        } catch (org.apache.xpath.domapi.XPathStylesheetDOM3Exception e) {
        // Expected exception.
        }
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.String str1 = listIdentifiers0.toString();
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "" + "'", str1, "");
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test16");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers3 = new org.oclc.oai.harvester2.verb.ListIdentifiers("", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd", (int) '4');
            org.junit.Assert.fail("Expected exception of type java.net.MalformedURLException; message: no protocol: ?verb=ListIdentifiers&resumptionToken=http%3A%2F%2Fwww.openarchives.org%2FOAI%2F1.1%2FOAI_Identify+http%3A%2F%2Fwww.openarchives.org%2FOAI%2F1.1%2FOAI_Identify.xsd");
        } catch (java.net.MalformedURLException e) {
        // Expected exception.
        }
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test17");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        // The following exception was thrown during execution in test generation
        try {
            org.w3c.dom.NodeList nodeList2 = listIdentifiers0.getNodeList("http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd");
            org.junit.Assert.fail("Expected exception of type org.apache.xpath.domapi.XPathStylesheetDOM3Exception; message: Das Pra?fix muss in einen Namensbereich aufgelo?st werden: http");
        } catch (org.apache.xpath.domapi.XPathStylesheetDOM3Exception e) {
        // Expected exception.
        }
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test18");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.String str1 = listIdentifiers0.getSchemaLocation();
        org.w3c.dom.Document document2 = listIdentifiers0.getDocument();
        org.junit.Assert.assertNull(str1);
        org.junit.Assert.assertNull(document2);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        org.w3c.dom.Document document1 = listIdentifiers0.getDocument();
        org.junit.Assert.assertNull(document1);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        // The following exception was thrown during execution in test generation
        try {
            org.w3c.dom.NodeList nodeList2 = listIdentifiers0.getNodeList("hi!");
            org.junit.Assert.fail("Expected exception of type javax.xml.transform.TransformerException; message: Zusa?tzliche nicht zula?ssige Token: '!'");
        } catch (javax.xml.transform.TransformerException e) {
        // Expected exception.
        }
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test21");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers6 = new org.oclc.oai.harvester2.verb.ListIdentifiers("http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd", "", "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd", "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd", "", (java.lang.Integer) 0);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd?verb=ListIdentifiers&from=&until=http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd&set=http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd&metadataPrefix=");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.String str1 = listIdentifiers0.getSchemaLocation();
        java.lang.String str2 = listIdentifiers0.getSchemaLocation();
        org.w3c.dom.Document document3 = listIdentifiers0.getDocument();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str4 = listIdentifiers0.getResumptionToken();
            org.junit.Assert.fail("Expected exception of type java.lang.NoSuchFieldException; message: null");
        } catch (java.lang.NoSuchFieldException e) {
        // Expected exception.
        }
        org.junit.Assert.assertNull(str1);
        org.junit.Assert.assertNull(str2);
        org.junit.Assert.assertNull(document3);
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers6 = new org.oclc.oai.harvester2.verb.ListIdentifiers("http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd", "hi!", "", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd", (java.lang.Integer) 10);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd?verb=ListIdentifiers&from=hi!&until=&set=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        java.lang.String str1 = listIdentifiers0.getSchemaLocation();
        java.lang.String str2 = listIdentifiers0.getSchemaLocation();
        org.w3c.dom.Document document3 = listIdentifiers0.getDocument();
        java.lang.String str4 = listIdentifiers0.getSchemaLocation();
        org.junit.Assert.assertNull(str1);
        org.junit.Assert.assertNull(str2);
        org.junit.Assert.assertNull(document3);
        org.junit.Assert.assertNull(str4);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test25");
        org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers0 = new org.oclc.oai.harvester2.verb.ListIdentifiers();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str1 = listIdentifiers0.getResumptionToken();
            org.junit.Assert.fail("Expected exception of type java.lang.NoSuchFieldException; message: null");
        } catch (java.lang.NoSuchFieldException e) {
        // Expected exception.
        }
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test26");
        // The following exception was thrown during execution in test generation
        try {
            org.oclc.oai.harvester2.verb.ListIdentifiers listIdentifiers6 = new org.oclc.oai.harvester2.verb.ListIdentifiers("http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", "", "", "hi!", "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd", (java.lang.Integer) 10);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Server returned HTTP response code: 400 for URL: http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd?verb=ListIdentifiers&from=&until=&set=hi!&metadataPrefix=http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd");
        } catch (java.io.IOException e) {
        // Expected exception.
        }
    }
}

