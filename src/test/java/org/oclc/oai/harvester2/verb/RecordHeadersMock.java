package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2026 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


/**
 * mock data for Record headers.
 *
 * @author moses AT doraventures DOT com
 */
public final class RecordHeadersMock
{

    //language=xml
    public static final String GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                    <responseDate>2018-01-10T16:31:14Z</responseDate>
                    <request verb="ListIdentifiers" metadataPrefix="ddi" from="2016-06-01">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>
                    <ListIdentifiers>
                        <header>
                            <identifier>850229</identifier>
                            <datestamp>2017-11-20T10:37:18Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <header>
                            <identifier>850232</identifier>
                            <datestamp>2017-11-20T10:37:18Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <header>
                            <identifier>850235</identifier>
                            <datestamp>2017-11-20T10:37:18Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <resumptionToken completeListSize="3" cursor="0"></resumptionToken>
                    </ListIdentifiers>
                </OAI-PMH>""";

    //language=xml
    public static final String GET_LIST_IDENTIFIERS_XML_RESUMPTION_TOKEN_NOT_MOCKED_FOR_INVALID = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                    <responseDate>2018-01-10T16:31:14Z</responseDate>
                    <request verb="ListIdentifiers" metadataPrefix="ddi" from="2016-06-01">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>
                    <ListIdentifiers>
                        <header>
                            <identifier>850235</identifier>
                            <datestamp>2017-11-20T10:37:18Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <resumptionToken completeListSize="3" cursor="0">3/6/7/ddi/null/2017-01-01/null</resumptionToken>
                    </ListIdentifiers>
                </OAI-PMH>""";
    //language=xml
    public static final String GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                    <responseDate>2018-01-12T10:11:30Z</responseDate>
                    <request verb="ListIdentifiers" resumptionToken="6500/7000/7606/ddi/null/2017-01-01/null">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>
                    <ListIdentifiers>
                        <header>
                            <identifier>7753</identifier>
                            <datestamp>2018-01-11T07:43:20Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <header>
                            <identifier>8300</identifier>
                            <datestamp>2018-01-11T07:43:20Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <header>
                            <identifier>8301</identifier>
                            <datestamp>2018-01-11T07:43:20Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <resumptionToken completeListSize="7" cursor="3">3/6/7/ddi/null/2017-01-01/null</resumptionToken>
                    </ListIdentifiers>
                </OAI-PMH>""";
    //language=xml
    public static final String GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                    <responseDate>2018-01-12T10:05:01Z</responseDate>
                    <request verb="ListIdentifiers" resumptionToken="7000/7500/7606/ddi/null/2017-01-01/null">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>
                    <ListIdentifiers> \s
                        <header>
                            <identifier>998</identifier>
                            <datestamp>2018-01-11T07:43:39Z</datestamp>
                            <setSpec>DataCollections</setSpec>
                        </header>
                        <resumptionToken completeListSize="7" cursor="6"/>
                    </ListIdentifiers>
                </OAI-PMH>""";
    //language=xml
    public static final String GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR = """
                <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                  <responseDate>2018-03-01T14:29:31Z</responseDate>
                  <request>http://services.fsd.uta.fi/v0/oai</request>
                  <error code="cannotDisseminateFormat"/>
                </OAI-PMH>""";
    //language=xml
    public static final String GET_LIST_SETS_XML = """
                <?xml version="1.0" encoding="utf-16" standalone="no"?>
                <?xml-stylesheet type='text/xsl' href='/oai/oai-pmh.xsl'?>
                <oai:OAI-PMH xmlns:oai="http://www.openarchives.org/OAI/2.0/">
                  <!-- Non-compliant date format, should be ignored -->
                  <oai:responseDate>9/9/2025 12:13:58 PM</oai:responseDate>
                  <oai:request verb="ListSets">/oai/request</oai:request>
                  <oai:ListSets>
                    <oai:set>
                      <oai:setSpec>f2b9352a-d976-4eac-8ee1-0c76da7cfca4</oai:setSpec>
                      <oai:setName>Projects</oai:setName>
                    </oai:set>
                    <oai:set>
                      <oai:setSpec>4bd6eef6-99df-40e6-9b11-5b8f64e5cb23</oai:setSpec>
                      <oai:setName>Series</oai:setName>
                    </oai:set>
                    <oai:set>
                      <oai:setSpec>30ea0200-7121-4f01-8d21-a931a182b86d</oai:setSpec>
                      <oai:setName>Studies</oai:setName>
                    </oai:set>
                    <oai:resumptionToken completeListSize="7" cursor="3">token</oai:resumptionToken>
                  </oai:ListSets>
                </oai:OAI-PMH>""";
    //language=xml
    public static final String GET_LIST_SETS_XML_LAST_LIST = """
                <?xml version="1.0" encoding="utf-16" standalone="no"?>
                <?xml-stylesheet type='text/xsl' href='/oai/oai-pmh.xsl'?>
                <oai:OAI-PMH xmlns:oai="http://www.openarchives.org/OAI/2.0/">
                    <!-- Non-compliant date format, should be ignored -->
                    <oai:responseDate>9/9/2025 12:13:58 PM</oai:responseDate>
                    <oai:request verb="ListSets">/oai/request</oai:request>
                    <oai:ListSets>
                        <oai:set>
                            <oai:setSpec>679a61f5-4246-4c89-b482-924dec09af98</oai:setSpec>
                            <oai:setName>Metadata Packages</oai:setName>
                        </oai:set>
                        <oai:set>
                            <oai:setSpec>f196cc07-9c99-4725-ad55-5b34f479cf7d</oai:setSpec>
                            <oai:setName>Instruments</oai:setName>
                        </oai:set>
                        <oai:set>
                            <oai:setSpec>a1bb19bd-a24a-4443-8728-a6ad80eb42b8</oai:setSpec>
                            <oai:setName>Questions</oai:setName>
                        </oai:set>
                        <oai:set>
                            <oai:setSpec>8b108ef8-b642-4484-9c49-f88e4bf7cf1d</oai:setSpec>
                            <oai:setName>Code Lists</oai:setName>
                        </oai:set>
                        <oai:set>
                            <oai:setSpec>a51e85bb-6259-4488-8df2-f08cb43485f8</oai:setSpec>
                            <oai:setName>Data Files</oai:setName>
                        </oai:set>
                        <oai:set>
                            <oai:setSpec>683889c6-f74b-4d5e-92ed-908c0a42bb2d</oai:setSpec>
                            <oai:setName>Variables</oai:setName>
                        </oai:set>
                        <oai:resumptionToken completeListSize="9" cursor="6"/>
                    </oai:ListSets>
                </oai:OAI-PMH>""";
}
